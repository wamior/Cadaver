"""Offline publishing regression tests. Run: python -m unittest discover -s scripts."""

import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile

import publish as p


class FakeAPI:
    def __init__(self, responses=None, fail_post=False):
        self.responses = responses or {}
        self.fail_post = fail_post
        self.calls = []

    def request(self, path, body=None, content_type=None):
        self.calls.append((path, body, content_type))
        if body:
            if self.fail_post:
                raise p.PublishError("Lost response")
            return {"id": "file-123"}
        return self.responses[path]


class PublisherTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.target("fabric-1.21.1", "fabric", "1.21.1", "1.21,1.21.1")
        self.target("neoforge-1.21.1", "neoforge", "1.21.1", curios=True)
        self.target("neoforge-1.21.3", "neoforge", "1.21.3")
        (self.root / "gradle.properties").write_text(
            "mod_version=1.0.0\ntargets=fabric-1.21.1,neoforge-1.21.1,neoforge-1.21.3\n")
        (self.root / "publish.json").write_text(json.dumps({
            "curseforge": {"project_id": 1816809}, "modrinth": {"project_id": "cadavers"}}))
        self.items = p.artifacts(self.root, "1.0.0")
        self.state_path = self.root / ".publish" / "state.json"

    def target(self, name, loader, mc, mc_versions=None, curios=False, version="1.0.0"):
        cfg = self.root / "targets" / name / "gradle.properties"
        cfg.parent.mkdir(parents=True, exist_ok=True)
        text = f"loader={loader}\nminecraft_version={mc}\njava_release=21\n"
        if mc_versions:
            text += f"mc_versions={mc_versions}\n"
        if curios:
            text += "curios_version=9.5.1+1.21.1\n"
        cfg.write_text(text)
        jar = self.root / "build" / "libs" / "1.0.0" / mc / f"cadaver-1.0.0+{mc}-{loader}.jar"
        jar.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(jar, "w") as z:
            if loader == "fabric":
                z.writestr("fabric.mod.json", json.dumps({"id": "cadaver", "version": version}))
            else:
                z.writestr("META-INF/neoforge.mods.toml", f'[[mods]]\nmodId="cadaver"\nversion="{version}"\n')

    def test_target_ranges_and_dependencies(self):
        fabric, curios, noop = self.items
        self.assertEqual(fabric["game_versions"], ["1.21", "1.21.1"])
        self.assertEqual(curios["dependencies"], [("curios", "optional")])
        self.assertEqual(noop["dependencies"], [])
        cf = p.metadata("curseforge", fabric, 1816809, "Fixes", "release")
        self.assertIn("Java 21", cf["gameVersionNames"])
        self.assertEqual(cf["relations"]["projects"][0], {"slug": "fabric-api", "type": "requiredDependency"})
        mr = p.metadata("modrinth", fabric, "project", "Fixes", "release")
        self.assertEqual(mr["dependencies"][1]["dependency_type"], "optional")
        self.assertEqual(mr["version_number"], "1.0.0+1.21.1-fabric")

    def test_missing_or_stale_jar_rejected(self):
        self.target("fabric-1.21.1", "fabric", "1.21.1", version="0.9.0")
        with self.assertRaisesRegex(p.PublishError, "embedded mod version"):
            p.artifacts(self.root, "1.0.0")
        (self.root / self.items[0]["file"]).unlink()
        with self.assertRaisesRegex(p.PublishError, "Missing"):
            p.artifacts(self.root, "1.0.0")

    def test_offline_preview_never_contacts_api_or_writes_tokens(self):
        with patch.object(p, "ROOT", self.root), patch.object(p.API, "request", side_effect=AssertionError("Network used")), patch.dict("os.environ", {"CURSEFORGE_TOKEN": "secret-cf", "MODRINTH_TOKEN": "secret-mr"}):
            p.main(["--dry-run"])
        plan = (self.root / "build" / "publish-plan-1.0.0.json").read_text()
        self.assertNotIn("secret-", plan)
        self.assertEqual(len(json.loads(plan)["artifacts"]), 3)
        self.assertFalse(self.state_path.exists())

    def test_missing_tag_prevents_any_post(self):
        api = FakeAPI({"/game/versions": [{"id": 1, "name": "1.21"}]})
        with self.assertRaisesRegex(p.PublishError, "missing or ambiguous"):
            p.prepare("curseforge", {"curseforge": {"project_id": 1816809}}, self.items, "Fixes", "release", api)
        self.assertTrue(all(body is None for _, body, _ in api.calls))

    def test_modrinth_slug_resolved_with_authenticated_api(self):
        api = FakeAPI({
            "/project/cadavers": {"id": "canonical-id"},
            "/tag/game_version": [{"version": "1.21"}, {"version": "1.21.1"}],
            "/tag/loader": [{"name": "fabric"}],
            "/project/canonical-id/version": [],
        })
        project, jobs, remote = p.prepare("modrinth", {"modrinth": {"project_id": "cadavers"}}, self.items[:1], "Fixes", "release", api)
        self.assertEqual(project, "canonical-id")
        self.assertEqual(jobs[0]["project_id"], "canonical-id")
        self.assertEqual(remote, [])

    def test_repeated_platform_argument_does_not_duplicate_upload_jobs(self):
        with patch.object(p, "ROOT", self.root):
            p.main(["--platforms", "modrinth", "modrinth"])
        plan = json.loads((self.root / "build" / "publish-plan-1.0.0.json").read_text())
        self.assertEqual(list(plan["platforms"]), ["modrinth"])

    def test_curseforge_resolves_exact_catalog_ids(self):
        names = ["1.21", "1.21.1", "Fabric", "Java 21", "Client", "Server"]
        api = FakeAPI({"/game/versions": [{"id": i, "name": n} for i, n in enumerate(names, 100)]})
        project, jobs, remote = p.prepare("curseforge", {"curseforge": {"project_id": 1816809}}, self.items[:1], "Fixes", "release", api)
        self.assertEqual(project, "1816809")
        self.assertEqual(jobs[0]["gameVersions"], list(range(100, 106)))
        self.assertNotIn("gameVersionNames", jobs[0])

    def test_multipart_preserves_jar_bytes_and_utf8_changelog(self):
        binary = b"PK\x00\xff\r\n"
        body, content_type = p.multipart("data", {"changelog": "Исправления"}, "cadaver.jar", binary)
        self.assertIn(binary, body)
        self.assertIn("Исправления".encode(), body)
        self.assertIn(b'name="data"', body)
        boundary = content_type.split("boundary=")[1]
        self.assertTrue(body.endswith(f"--{boundary}--\r\n".encode()))

    def test_resume_skips_successful_uploads(self):
        api, state = FakeAPI(), {}
        prepared = [("curseforge", api, "1816809", [{"displayName": "test"}], [])]
        p.upload(self.root, prepared, self.items[:1], state, self.state_path)
        self.assertEqual(len(api.calls), 1)
        self.assertEqual(next(iter(state.values()))["id"], "file-123")
        p.upload(self.root, prepared, self.items[:1], json.loads(self.state_path.read_text()), self.state_path)
        self.assertEqual(len(api.calls), 1)

    def test_lost_response_does_not_blindly_retry(self):
        api, state = FakeAPI(fail_post=True), {}
        prepared = [("curseforge", api, "1816809", [{}], [])]
        with self.assertRaisesRegex(p.PublishError, "Lost response"):
            p.upload(self.root, prepared, self.items[:1], state, self.state_path)
        with self.assertRaisesRegex(p.PublishError, "Uncertain previous"):
            p.upload(self.root, prepared, self.items[:1], state, self.state_path)
        self.assertEqual(len(api.calls), 1)
        self.assertEqual(next(iter(state.values()))["status"], "uncertain")

    def test_modrinth_reconciles_lost_response_from_remote_hash(self):
        item = self.items[0]
        remote = [{"id": "remote-456", "version_number": item["version_number"],
                   "files": [{"hashes": {"sha512": item["sha512"]}}]}]
        api = FakeAPI()
        state = {p.state_key("modrinth", "id", item): {"sha256": item["sha256"], "status": "uncertain"}}
        p.upload(self.root, [("modrinth", api, "id", [{}], remote)], [item], state, self.state_path)
        self.assertEqual(api.calls, [])
        self.assertEqual(next(iter(state.values()))["id"], "remote-456")

    def test_conflicting_remote_version_stops_before_post(self):
        remote = [{"id": "conflict", "version_number": self.items[0]["version_number"], "files": []}]
        api = FakeAPI()
        with self.assertRaisesRegex(p.PublishError, "different bytes"):
            p.upload(self.root, [("modrinth", api, "id", [{}], remote)], self.items[:1], {}, self.state_path)
        self.assertEqual(api.calls, [])

    def test_modified_artifact_rejected_after_preflight(self):
        item = self.items[0]
        (self.root / item["file"]).write_bytes(b"changed while publishing")
        api = FakeAPI()
        with self.assertRaisesRegex(p.PublishError, "changed after validation"):
            p.upload(self.root, [("curseforge", api, "1816809", [{}], [])], [item], {}, self.state_path)
        self.assertEqual(api.calls, [])


if __name__ == "__main__":
    unittest.main()
