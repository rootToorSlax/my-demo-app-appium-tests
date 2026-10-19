@echo off
setlocal
cd /d "%~dp0.."
title MDA smoke tests
set "RC=0"

echo [1-2/3] Emulator and boot wait...
call "%~dp0run-bootstrap.cmd"
if errorlevel 1 goto on_boot_fail

echo.
echo UDID=%UDID%
echo [3/3] gradlew.bat smokeTest --rerun-tasks - 4 tests, about 2 min
echo.

call "%~dp0apply-mute.cmd"
call gradlew.bat smokeTest --rerun-tasks
set "RC=%errorlevel%"
if not "%RC%"=="0" goto on_tests_fail
echo.
echo smokeTest PASSED.
goto test_done

:on_boot_fail
echo Boot wait failed - smokeTest was NOT started.
pause
exit /b 1

:on_tests_fail
echo.
echo smokeTest failed - see output above.
pause
goto test_done

:test_done
endlocal
exit /b %RC%
