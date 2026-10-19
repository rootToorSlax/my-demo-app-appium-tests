# Run commands (cheat sheet)

Narrative install guide for reviewers: **README → [Local installation manual (reviewers)](../README.md#local-installation-manual-reviewers)**.

Prerequisites: APK in `apps/`, Appium 3 + uiautomator2, API 34 AVD `Pixel_7_API_34`.

## Windows (recommended local)

**PowerShell** (repo root): always use **`.\`** — e.g. **`.\run-full.cmd`**, **`.\gradlew.bat smokeTest`**.  
**cmd.exe**: `run-full.cmd` or `gradlew.bat` works without `.\`.

All `run-*.cmd` scripts: emulator window → boot wait → Gradle (`scripts\run-bootstrap.cmd` + task).

| Command | What it runs |
|---------|----------------|
| `.\run-smoke.cmd` | Boot emulator, wait, **`gradlew smokeTest --rerun-tasks`** (~2 min, 4 tests) |
| `.\run-full.cmd` | Same boot/wait, **`gradlew test --rerun-tasks`** + **`harness.profile=fast`** in `config.properties` (~5–7 min, 17 tests) |
| `.\run-gate.cmd` | Same boot/wait, **`HARNESS_PROFILE=strict`** + full `test --rerun-tasks` (CI / GATE extras) |
| `.\stop.cmd` | `gradlew --stop` + `adb emu kill` |
| `scripts\android-env.cmd` | Set `ANDROID_HOME` + `PATH` for manual `adb` |

Local scripts set **`KILL_AFTER_TESTRUN=false`** (maps to `kill.after.testrun` in `config.properties`) so the emulator stays up.

## Gradle (from repo root)

| Command | Tests |
|---------|--------|
| `gradlew.bat test` | **All** tests — **emulator must be running** (`adb devices`) or use `run-full.cmd` |
| `gradlew.bat test --rerun-tasks` | Full suite, ignore “UP-TO-DATE” cache |
| `gradlew.bat smokeTest` | Tag **`smoke`** only (TC-01, TC-05, TC-08, TC-10) |
| `gradlew.bat a11yTest` | Tag **`a11y`** only (TC-A11Y ×2) |
| `gradlew.bat test --tests mda.tests.CartTest` | One class |
| `gradlew.bat test --tests "*TC-14*"` | By display name pattern (JUnit) |
| `gradlew.bat --stop` | Stop Gradle daemons |

Linux/macOS: use `./gradlew` instead of `gradlew.bat`.

## Environment overrides (`config.properties` keys)

Set in CMD before `gradlew` (dots → underscores, uppercase):

| Variable | Example | Effect |
|----------|---------|--------|
| `UDID` | `emulator-5554` | Attach to running device (no second AVD) |
| `KILL_AFTER_TESTRUN` | `false` | Do not `adb emu kill` after JVM exit |
| `MUTE_ANDROID_SYSTEM` | `true` / `false` | Mute streams via adb each session (default **true**) |
| `ANDROID_HOME` | SDK path | Required for adb/aapt |
| `APPIUM_SERVER_URL` | `http://127.0.0.1:4723` | Reuse external Appium |
| `HARNESS_PROFILE` | `fast` (default) / `strict` | Bundles extras below; CI sets **strict** |
| `CRASH_ORACLE` | fast: off, strict: on | Per-test logcat crash oracle |
| `SESSION_CLEAR_LOGCAT` | fast: off, strict: on | `adb logcat -c` before each test |
| `OVERLAY_DISMISS_ON_WAIT_POLL` | fast: off, strict: on | ANR/permission sweep on every wait (slow) |
| `APPIUM_SKIP_LOGCAT_CAPTURE` | fast: on, strict: off | UiAutomator2 logcat capture |
| `HARNESS_DISABLE_ANIMATIONS` | fast: on, strict: off | `animator_duration_scale=0` on emulator |
| `MUTE_ANDROID_SYSTEM` | default **true** | Guest + notification audio off (`apply-mute.cmd` + each session). **Only** `false` enables sound. |
| `OVERLAY_STABILIZE_ROUNDS` | `1` | Overlay dismiss loops at session start |
| `UI_WAIT_SECONDS` | `10` | Default Selenium wait per locator |
| `NEW_COMMAND_TIMEOUT_SECONDS` | `60` | Appium command timeout |
| `CATALOG_PRODUCT_WAIT_SECONDS` | `10` | Max wait for product tiles |
| `CART_EMPTY_WAIT_SECONDS` | `12` | Empty cart copy after remove (F-01) |

## JUnit tags in this repo

| Tag | Count | Gradle task |
|-----|-------|-------------|
| (none) | 17 total in `test` | `test` |
| `smoke` | 4 | `smokeTest` |
| `a11y` | 2 | `a11yTest` |
| `perf` | 1 | `test --tests StartupTest` |

CI: all triggers run full **`test`** (17 tests, `HARNESS_PROFILE=strict`). Local quick gate: `smokeTest`.
