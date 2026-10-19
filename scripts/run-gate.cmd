@echo off
setlocal
cd /d "%~dp0.."
title MDA GATE strict harness
set "RC=0"

call "%~dp0run-bootstrap.cmd"
if errorlevel 1 goto on_boot_fail

set "HARNESS_PROFILE=strict"
echo UDID=%UDID%  HARNESS_PROFILE=strict
echo Full suite with crash oracle, logcat hygiene, overlay polls CI-equivalent.
echo.

call "%~dp0apply-mute.cmd"
call gradlew.bat test --rerun-tasks
set "RC=%errorlevel%"
if not "%RC%"=="0" goto on_tests_fail
echo.
echo GATE test PASSED.
goto test_done

:on_boot_fail
echo Boot wait failed.
pause
exit /b 1

:on_tests_fail
echo GATE test failed.
pause
goto test_done

:test_done
endlocal
exit /b %RC%
