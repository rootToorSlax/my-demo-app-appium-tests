@echo off
call "%~dp0android-env.cmd"
if errorlevel 1 exit /b 1

if "%AVD%"=="" set "AVD=Pixel_7_API_34"

set "NO_AUDIO=-no-audio"
if /I "%MUTE_ANDROID_SYSTEM%"=="false" set "NO_AUDIO="
for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0..\src\test\resources\config.properties") do (
  if /I "%%a"=="mute.android.system" if /I "%%b"=="false" set "NO_AUDIO="
)

for /f "skip=1 tokens=1,2" %%a in ('adb devices 2^>nul') do (
  if "%%b"=="device" (
    echo Emulator already online: %%a
    call "%~dp0apply-mute.cmd"
    exit /b 0
  )
)

echo Opening emulator window (verbose logs, %NO_AUDIO% from config)...
start "MDA Emulator - %AVD%" "%ANDROID_HOME%\emulator\emulator.exe" -avd %AVD% %NO_AUDIO% -verbose -show-kernel
echo Emulator process started — tests run from the run-full / run-smoke window after boot.
exit /b 0
