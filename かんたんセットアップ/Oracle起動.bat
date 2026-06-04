@echo off
rem Bootstrap only. The real work (Japanese UI + logic) lives in the .ps1
rem because cmd.exe cannot parse UTF-8 batch lines that contain Japanese.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0bin\oracle-start.ps1"
exit /b %errorlevel%
