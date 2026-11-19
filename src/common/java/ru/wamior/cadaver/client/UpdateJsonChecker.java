package ru.wamior.cadaver.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.wamior.cadaver.Cadaver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal reader of the Forge-style update.json for loaders without a built-in update checker (Fabric).
 * Same rules as Forge's VersionChecker: {@code <mc>-recommended} newer than us → outdated, otherwise
 * {@code <mc>-latest} newer than us → outdated (beta). Runs once, off-thread.
 */
public final class UpdateJsonChecker {
    public record Result(String current, String target, String url) {
    }

    private static volatile Result result;
    private static boolean started;

    public static synchronized void start(String url, String minecraftVersion, String currentVersion) {
        if (started) return;
        started = true;
        Thread thread = new Thread(() -> check(url, minecraftVersion, currentVersion), "Cadaver update check");
        thread.setDaemon(true);
        thread.start();
    }

    /** Non-null once the check found a newer version. */
    public static Result result() {
        return result;
    }

    private static void check(String url, String mc, String current) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Cadaver/" + current).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return;

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject promos = json.has("promos") ? json.getAsJsonObject("promos") : new JsonObject();
            String recommended = string(promos.get(mc + "-recommended"));
            String latest = string(promos.get(mc + "-latest"));
            String target = null;
            if (recommended != null && compare(recommended, current) > 0) target = recommended;
            else if (latest != null && compare(latest, current) > 0) target = latest;
            if (target != null) result = new Result(current, target, string(json.get("homepage")));
        } catch (Exception e) {
            Cadaver.LOGGER.debug("Update check failed", e);
        }
    }

    private static String string(JsonElement e) {
        return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
    }

    /** Compares dotted versions numerically ("1.10.0" > "1.9.2"); a trailing "-beta" sorts lower. */
    static int compare(String a, String b) {
        String[] pa = a.split("[.+-]");
        String[] pb = b.split("[.+-]");
        for (int i = 0; i < Math.max(pa.length, pb.length); i++) {
            String x = i < pa.length ? pa[i] : "0";
            String y = i < pb.length ? pb[i] : "0";
            boolean nx = x.chars().allMatch(Character::isDigit) && !x.isEmpty();
            boolean ny = y.chars().allMatch(Character::isDigit) && !y.isEmpty();
            int c;
            if (nx && ny) c = Long.compare(Long.parseLong(x), Long.parseLong(y));
            else if (nx) c = 1;   // "1.0.0" > "1.0.0-beta"
            else if (ny) c = -1;
            else c = x.compareTo(y);
            if (c != 0) return c;
        }
        return 0;
    }

    private UpdateJsonChecker() {
    }
}
