# Выпуск новой версии Cadaver: сборка всех jar, update.json, коммит, тег, GitHub Release.
# Запуск: release.bat [версия] [список изменений]   (без аргументов спросит сам)
#         release.bat 1.2.0 "Исправлен ..." -DryRun   (всё соберёт и покажет, но ничего не отправит)
param(
    [string]$Version,
    [string]$Changelog,
    [switch]$DryRun,
    [switch]$Publish
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
$Utf8 = New-Object System.Text.UTF8Encoding $false
$Repo = 'wamior/Cadaver'

function Fail([string]$msg) {
    Write-Host ""
    Write-Host "ОШИБКА: $msg" -ForegroundColor Red
    exit 1
}

function Step([string]$msg) {
    Write-Host ""
    Write-Host "==> $msg" -ForegroundColor Cyan
}

function Run([string]$exe, [string[]]$arguments) {
    & $exe @arguments
    if ($LASTEXITCODE -ne 0) { Fail "$exe $($arguments -join ' ') завершился с кодом $LASTEXITCODE" }
}

function Compare-Version([string]$a, [string]$b) {
    $x = $a.Split('.') | ForEach-Object { [int]$_ }
    $y = $b.Split('.') | ForEach-Object { [int]$_ }
    for ($i = 0; $i -lt [Math]::Max($x.Count, $y.Count); $i++) {
        $p = 0; if ($i -lt $x.Count) { $p = $x[$i] }
        $q = 0; if ($i -lt $y.Count) { $q = $y[$i] }
        if ($p -ne $q) { return [Math]::Sign($p - $q) }
    }
    return 0
}

function Json-String([string]$s) {
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.Append('"')
    foreach ($c in $s.ToCharArray()) {
        switch ($c) {
            '"' { [void]$sb.Append('\"') }
            '\' { [void]$sb.Append('\\') }
            "`n" { [void]$sb.Append('\n') }
            "`r" { }
            "`t" { [void]$sb.Append('\t') }
            default { [void]$sb.Append($c) }
        }
    }
    [void]$sb.Append('"')
    return $sb.ToString()
}

# ------------------------------------------------------------------ проверки

Step "Проверка окружения"
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { Fail "git не найден" }
if ($Publish) {
    if (-not (Get-Command python -ErrorAction SilentlyContinue)) { Fail "для публикации нужен Python 3.11+" }
    if (-not $DryRun -and (-not $env:CURSEFORGE_TOKEN -or -not $env:MODRINTH_TOKEN)) {
        Fail "задайте CURSEFORGE_TOKEN и MODRINTH_TOKEN в окружении (см. README.md)"
    }
}
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { Fail "GitHub CLI (gh) не найден: https://cli.github.com, потом 'gh auth login'" }
& gh auth status *> $null
if ($LASTEXITCODE -ne 0) { Fail "gh не авторизован: выполните 'gh auth login'" }

function Check([bool]$ok, [string]$msg) {
    if ($ok) { return }
    if ($DryRun) { Write-Host "Предупреждение (пробный запуск продолжается): $msg" -ForegroundColor Yellow }
    else { Fail $msg }
}

$branch = (& git rev-parse --abbrev-ref HEAD).Trim()
Check ($branch -eq 'main') "релиз делается из ветки main (сейчас: $branch)"
$dirty = & git status --porcelain
Check (-not $dirty) "есть незакоммиченные изменения, сначала закоммитьте их:`n$($dirty -join "`n")"
Run git @('fetch', '--quiet', 'origin')
$behind = (& git rev-list --count 'HEAD..origin/main').Trim()
Check ($behind -eq '0') "локальная ветка отстаёт от GitHub main на $behind коммит(ов): выполните 'git pull'"

$propsPath = Join-Path $Root 'gradle.properties'
$props = [System.IO.File]::ReadAllText($propsPath)
if ($props -notmatch '(?m)^mod_version=(\S+)') { Fail "mod_version не найден в gradle.properties" }
$current = $Matches[1]

# ------------------------------------------------------------------ версия и изменения

if (-not $Version) {
    $parts = $current.Split('.')
    $suggest = "$($parts[0]).$($parts[1]).$([int]$parts[2] + 1)"
    $Version = Read-Host "Новая версия (сейчас $current, Enter = $suggest)"
    if (-not $Version) { $Version = $suggest }
}
$Version = $Version.Trim().TrimStart('v')
if ($Version -notmatch '^\d+\.\d+\.\d+$') { Fail "версия должна быть вида 1.2.3, а не '$Version'" }
if ((Compare-Version $Version $current) -le 0) { Fail "версия $Version должна быть больше текущей $current" }
$tag = "v$Version"
& git rev-parse -q --verify "refs/tags/$tag" *> $null
if ($LASTEXITCODE -eq 0) { Fail "тег $tag уже существует" }

if (-not $Changelog) {
    $Changelog = Read-Host "Что нового (одной строкой, Enter = 'Исправления и улучшения')"
    if (-not $Changelog) { $Changelog = 'Исправления и улучшения' }
}

# ------------------------------------------------------------------ сборка

Step "mod_version: $current -> $Version"
$newProps = [regex]::Replace($props, '(?m)^mod_version=\S+', "mod_version=$Version")
[System.IO.File]::WriteAllText($propsPath, $newProps, $Utf8)

Step "Сборка всех версий (gradlew buildAll), это займёт несколько минут"
& (Join-Path $Root 'gradlew.bat') buildAll --console=plain
if ($LASTEXITCODE -ne 0) {
    [System.IO.File]::WriteAllText($propsPath, $props, $Utf8)
    Fail "сборка не прошла, mod_version возвращён к $current"
}
$jarDir = Join-Path $Root "build\libs\$Version"
# One directory per Minecraft build version. Ignore legacy flat output from older builds.
$jars = @(Get-ChildItem -LiteralPath $jarDir -Directory | ForEach-Object {
    Get-ChildItem -LiteralPath $_.FullName -File -Filter "cadaver-$Version+*.jar"
} | Sort-Object Name)
if ($jars.Count -eq 0) { Fail "в $jarDir нет jar-файлов" }

# ------------------------------------------------------------------ update.json

Step "Обновление update.json"
$targets = & (Join-Path $Root 'gradlew.bat') -q printTargets
if ($LASTEXITCODE -ne 0) { Fail "gradlew printTargets не сработал" }
$mcVersions = @()
foreach ($row in $targets) {
    $cols = $row.Trim() -split '\s+'
    if ($cols.Count -ge 4) { $mcVersions += $cols[3].Split(',') }
}
$mcVersions = @($mcVersions | Where-Object { $_ } | Sort-Object -Unique)
if ($mcVersions.Count -eq 0) { Fail "не удалось получить список версий Minecraft" }

$updatePath = Join-Path $Root 'update.json'
$updateOriginal = $null
if (Test-Path $updatePath) { $updateOriginal = [System.IO.File]::ReadAllText($updatePath) }
$promos = [ordered]@{}
$changes = @{}
if (Test-Path $updatePath) {
    $old = [System.IO.File]::ReadAllText($updatePath) | ConvertFrom-Json
    if ($old.promos) { foreach ($p in $old.promos.PSObject.Properties) { $promos[$p.Name] = $p.Value } }
    foreach ($p in $old.PSObject.Properties) {
        if ($p.Name -in @('homepage', 'promos')) { continue }
        $entries = [ordered]@{}
        foreach ($e in $p.Value.PSObject.Properties) { $entries[$e.Name] = $e.Value }
        $changes[$p.Name] = $entries
    }
}
foreach ($mc in $mcVersions) {
    $promos["$mc-latest"] = $Version
    $promos["$mc-recommended"] = $Version
    if (-not $changes.ContainsKey($mc)) { $changes[$mc] = [ordered]@{} }
    $entries = [ordered]@{}
    $entries[$Version] = $Changelog
    foreach ($k in $changes[$mc].Keys) { if ($k -ne $Version) { $entries[$k] = $changes[$mc][$k] } }
    $changes[$mc] = $entries
}

function Version-Key([string]$name) {
    $v = $name -replace '-(latest|recommended)$', ''
    $key = ($v.Split('.') | ForEach-Object { '{0:D5}' -f [int]$_ }) -join '.'
    return "$key|$name"
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add('{')
$lines.Add("  `"homepage`": $(Json-String "https://github.com/$Repo/releases/latest"),")
$lines.Add('  "promos": {')
$promoKeys = @($promos.Keys | Sort-Object { Version-Key $_ })
for ($i = 0; $i -lt $promoKeys.Count; $i++) {
    $comma = if ($i -lt $promoKeys.Count - 1) { ',' } else { '' }
    $lines.Add("    $(Json-String $promoKeys[$i]): $(Json-String $promos[$promoKeys[$i]])$comma")
}
$lines.Add('  },')
$mcKeys = @($changes.Keys | Sort-Object { Version-Key $_ })
for ($i = 0; $i -lt $mcKeys.Count; $i++) {
    $lines.Add("  $(Json-String $mcKeys[$i]): {")
    $vers = @($changes[$mcKeys[$i]].Keys)
    for ($j = 0; $j -lt $vers.Count; $j++) {
        $comma = if ($j -lt $vers.Count - 1) { ',' } else { '' }
        $lines.Add("    $(Json-String $vers[$j]): $(Json-String ([string]$changes[$mcKeys[$i]][$vers[$j]]))$comma")
    }
    $comma = if ($i -lt $mcKeys.Count - 1) { ',' } else { '' }
    $lines.Add("  }$comma")
}
$lines.Add('}')
$json = ($lines -join "`n") + "`n"
$null = $json | ConvertFrom-Json   # проверка, что получился корректный JSON
[System.IO.File]::WriteAllText($updatePath, $json, $Utf8)
Write-Host "Версии Minecraft в update.json: $($mcVersions -join ', ')"

# ------------------------------------------------------------------ заметки к релизу

$table = New-Object System.Collections.Generic.List[string]
$table.Add('| Файл | Загрузчик | Minecraft |')
$table.Add('|---|---|---|')
foreach ($row in $targets) {
    $cols = $row.Trim() -split '\s+'
    if ($cols.Count -lt 4) { continue }
    $loaderName = switch ($cols[1]) { 'neoforge' { 'NeoForge' } 'forge' { 'Forge' } 'fabric' { 'Fabric' } default { $cols[1] } }
    $table.Add("| ``cadaver-$Version+$($cols[2])-$($cols[1]).jar`` | $loaderName | $($cols[3].Replace(',', ', ')) |")
}
$notes = @"
$Changelog

## Какой файл скачать
Выберите jar под свой загрузчик и версию Minecraft. Мод нужен и на сервере, и на клиенте. На Fabric также нужен Fabric API.

$($table -join "`n")

Curios (NeoForge / Forge) и Trinkets (Fabric) поддерживаются, но не обязательны.
"@
$notesPath = Join-Path $Root "build\release-notes-$Version.md"
[System.IO.File]::WriteAllText($notesPath, $notes, $Utf8)

if ($DryRun) {
    Step "Пробный запуск: ничего не отправлено"
    Write-Host "Собрано jar: $($jars.Count) в $jarDir"
    Write-Host "Заметки к релизу: $notesPath"
    $preview = Join-Path $Root "build\update-preview-$Version.json"
    Copy-Item $updatePath $preview -Force
    Write-Host "Каким стал бы update.json: $preview"
    Write-Host "gradle.properties и update.json возвращены как были."
    [System.IO.File]::WriteAllText($propsPath, $props, $Utf8)
    if ($null -ne $updateOriginal) { [System.IO.File]::WriteAllText($updatePath, $updateOriginal, $Utf8) }
    else { Remove-Item -LiteralPath $updatePath -ErrorAction SilentlyContinue }
    if ($Publish) {
        Run python @((Join-Path $PSScriptRoot 'publish.py'), '--version', $Version, '--changelog-file', $notesPath, '--dry-run')
    }
    exit 0
}

# ------------------------------------------------------------------ публикация

if ($Publish) {
    Step "Проверка проектов и тегов CurseForge / Modrinth до отправки на GitHub"
    & python (Join-Path $PSScriptRoot 'publish.py') --version $Version --changelog-file $notesPath --check
    if ($LASTEXITCODE -ne 0) {
        [System.IO.File]::WriteAllText($propsPath, $props, $Utf8)
        if ($null -ne $updateOriginal) { [System.IO.File]::WriteAllText($updatePath, $updateOriginal, $Utf8) }
        else { Remove-Item -LiteralPath $updatePath -ErrorAction SilentlyContinue }
        Fail "проверка площадок не прошла; версия и update.json восстановлены, GitHub не изменён"
    }
}

Step "Коммит, тег $tag и отправка на GitHub"
Run git @('add', 'gradle.properties', 'update.json')
Run git @('commit', '-q', '-m', "Release $tag")
Run git @('tag', '-a', $tag, '-m', "Cadaver $Version")
Run git @('push', '--quiet', 'origin', 'main')
Run git @('push', '--quiet', 'origin', $tag)

Step "Создание GitHub Release $tag ($($jars.Count) jar)"
$ghArgs = @('release', 'create', $tag, '--repo', $Repo, '--title', "Cadaver $Version", '--notes-file', $notesPath, '--latest') + ($jars | ForEach-Object { $_.FullName })
Run gh $ghArgs

if ($Publish) {
    Step "Загрузка тех же JAR на CurseForge и Modrinth"
    & python (Join-Path $PSScriptRoot 'publish.py') --version $Version --changelog-file $notesPath --upload
    if ($LASTEXITCODE -ne 0) {
        Fail "GitHub Release уже создан. Для продолжения: publish.bat --version $Version --changelog-file build/release-notes-$Version.md --upload"
    }
}

Write-Host ""
Write-Host "Готово: https://github.com/$Repo/releases/tag/$tag" -ForegroundColor Green
Write-Host "Игроки со старой версией увидят сообщение об обновлении при следующем входе в игру."
if (-not $Publish) { Write-Host "Для загрузки на CurseForge и Modrinth: publish.bat --version $Version --upload" }
