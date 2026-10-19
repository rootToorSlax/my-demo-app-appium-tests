@echo off
setlocal EnableDelayedExpansion
REM Requires android-env.cmd (adb on PATH). Uses ping instead of timeout (works when stdin is redirected).
set /a WAIT_SEC=0
set /a MAX_SEC=240

adb wait-for-device >nul 2>&1

:poll
set "BOOT="
for /f %%b in ('adb shell getprop sys.boot_completed 2^>nul') do set "BOOT=%%b"
if "!BOOT!"=="1" goto resolve_udid
ping -n 3 127.0.0.1 >nul
set /a WAIT_SEC+=2
if !WAIT_SEC! geq %MAX_SEC% (
  echo ERROR: Emulator did not finish boot within %MAX_SEC%s.
  exit /b 1
)
goto poll

:resolve_udid
set "UDID="
for /f "skip=1 tokens=1,2" %%a in ('adb devices') do (
  if "%%b"=="device" set "UDID=%%a"
)
if not defined UDID (
  echo ERROR: adb devices has no device in ready state.
  exit /b 1
)
echo Emulator ready: !UDID!
set "SCRIPTS=%~dp0"
echo !UDID!> "%SCRIPTS%.last-udid"
call "%~dp0apply-mute.cmd"
endlocal
exit /b 0
