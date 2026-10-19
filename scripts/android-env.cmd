@echo off
REM Use in cmd before manual adb/emulator commands (Gradle test task sets this automatically).
if "%ANDROID_HOME%"=="" if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
if "%ANDROID_HOME%"=="" (
  echo Set ANDROID_HOME to your Android SDK, then run this script again.
  exit /b 1
)
set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
set "PATH=%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%"
echo ANDROID_HOME=%ANDROID_HOME%
adb devices
