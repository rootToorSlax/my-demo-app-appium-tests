@echo off
cd /d "%~dp0.."
echo Stopping Gradle...
call gradlew.bat --stop >nul 2>&1
call "%~dp0android-env.cmd" >nul 2>&1
if not errorlevel 1 (
  echo Stopping emulators...
  for /f "skip=1 tokens=1" %%s in ('adb devices ^| findstr /r "emulator-"') do adb -s %%s emu kill >nul 2>&1
)
echo Done.
exit /b 0
