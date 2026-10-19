@echo off
setlocal EnableDelayedExpansion
REM Mirrors EmulatorLifecycle.muteDeviceAudio — only skipped when mute.android.system=false in config or env.
call "%~dp0android-env.cmd" >nul 2>&1
if errorlevel 1 exit /b 1

set "MUTE=1"
if /I "%MUTE_ANDROID_SYSTEM%"=="false" set "MUTE=0"
for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0..\src\test\resources\config.properties") do (
  if /I "%%a"=="mute.android.system" if /I "%%b"=="false" set "MUTE=0"
)
if not "%MUTE%"=="0" goto do_mute
echo Audio mute disabled via config or MUTE_ANDROID_SYSTEM=false.
exit /b 0

:do_mute
set "SERIAL="
for /f "skip=1 tokens=1,2" %%a in ('adb devices 2^>nul') do (
  if "%%b"=="device" set "SERIAL=%%a"
)
if not defined SERIAL (
  echo apply-mute: no device - skip.
  exit /b 0
)

echo Applying emulator audio mute on %SERIAL% ...
for /L %%s in (0,1,10) do adb -s %SERIAL% shell cmd media volume --stream %%s --set 0 >nul 2>&1
adb -s %SERIAL% shell cmd media volume --set 0 >nul 2>&1
adb -s %SERIAL% shell settings put system sound_effects_enabled 0 >nul 2>&1
adb -s %SERIAL% shell settings put system haptic_feedback_enabled 0 >nul 2>&1
adb -s %SERIAL% shell settings put system vibrate_when_ringing 0 >nul 2>&1
adb -s %SERIAL% shell settings put system notification_sound "" >nul 2>&1
adb -s %SERIAL% shell settings put system ringtone "" >nul 2>&1
adb -s %SERIAL% shell settings put system alarm_alert "" >nul 2>&1
adb -s %SERIAL% shell settings put secure notification_sound "" >nul 2>&1
adb -s %SERIAL% shell settings put global zen_mode 3 >nul 2>&1
adb -s %SERIAL% shell settings put global apply_ramping_ringer 0 >nul 2>&1
echo Audio mute applied.
exit /b 0
