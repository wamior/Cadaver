@echo off
rem Release a new Cadaver version: release.bat [version] [changelog] [-DryRun]
rem Without arguments it asks for the version and the changelog. See README.md, "Releasing a new version".
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\release.ps1" %*
set CODE=%ERRORLEVEL%
echo.
pause
exit /b %CODE%
