@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0.."
title MDA full regression
set "RC=0"

echo.
echo === run-full: emulator, boot wait, gradlew test - 17 tests ===
echo Keep THIS window open until Gradle finishes - about 5-7 min.
echo Note: start-emulator-console alone does NOT run tests.
echo.

echo [1-2/3] Emulator and boot wait...
call "%~dp0run-bootstrap.cmd"
if errorlevel 1 goto on_boot_fail

echo.
echo UDID=%UDID%
echo [3/3] Starting Gradle: gradlew.bat test --rerun-tasks
echo Quick check: run-smoke.cmd - about 2 min.
echo.

call "%~dp0apply-mute.cmd"
call gradlew.bat test --rerun-tasks
set "RC=%errorlevel%"
if not "%RC%"=="0" goto on_tests_fail
echo.
echo test PASSED - 17 tests.
goto test_done

:on_boot_fail
echo.
echo Boot wait failed - tests were NOT started. Check adb devices, then rerun run-full.
pause
exit /b 1

:on_tests_fail
echo.
echo Tests failed - see Gradle output above.
pause
goto test_done

:test_done
endlocal
exit /b %RC%
