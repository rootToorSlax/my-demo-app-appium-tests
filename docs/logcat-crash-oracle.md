# Logcat crash oracle

## What logcat is

Android writes a rolling **system log** (tags, levels, messages). App crashes and ANRs appear here even when the UI still looks fine for a moment.

## Why UI asserts are not enough

A test can pass on visible text while the app process **crashed and restarted** in the background. Users see “App keeps stopping”; automation might not notice without reading logs.

## What we look for

| Signal | Meaning |
|--------|---------|
| `FATAL EXCEPTION` | Uncaught exception on a JVM thread (`AndroidRuntime`). |
| `Process … has died` | ActivityManager reports the app process ended. |
| `ANR in <package>` | Main thread did not respond within the ANR window. |

Lines are filtered to our package `com.saucelabs.mydemoapp.android` before matching.

## How Appium exposes it

UiAutomator2 collects **logcat from session start**. After each test, `CrashOracle` reads `driver.manage().logs().get("logcat")` and fails the test if any pattern matches.

Toggle: `crash.oracle=true` in `config.properties` (env `CRASH_ORACLE`).

## Relation to “API checks”

This app has no REST API for cart/login. The oracle is the **second source of truth** beside the UI: “did the process stay healthy?”

## Production notes

Filter by PID, attach log excerpt to reports, and treat emulator GL spam as environment noise (not app crashes).
