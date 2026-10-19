@echo off
REM Shared: android-env, emulator, boot wait, UDID file, local run env. Sets SCRIPTS and UDID.
set "SCRIPTS=%~dp0"
call "%SCRIPTS%android-env.cmd"
if errorlevel 1 exit /b 1
call "%SCRIPTS%start-emulator-console.cmd"
if errorlevel 1 exit /b 1
call "%SCRIPTS%wait-for-boot.cmd"
if errorlevel 1 exit /b 1
set /p UDID=<"%SCRIPTS%.last-udid"
set "KILL_AFTER_TESTRUN=false"
exit /b 0
