# My Demo App — Android tests

Black-box UI checks for Sauce Labs **My Demo App** (Android), using **Kotlin**, **JUnit 5**, and **Appium 3** with **UiAutomator2**.

**Reviewer path:** read `docs/test-scenarios.md`, then `src/test/kotlin/mda/tests`. A green GitHub Actions run is enough to judge the automation; use [Quick start](#quick-start) only if you want to run locally.

The **Git repository URL** is sent in the submission email; this README assumes you already cloned that repo into a working directory.

## Quick start

1. Complete [First-time toolchain](#first-time-toolchain) once per machine (JDK, Android SDK, AVD, Appium).
2. [Download the APK](#app-binary) into `apps/` (not in git).
3. Start the emulator (or skip if Appium should boot the AVD — see step 5 in toolchain verify).
4. From the **project root** (where `gradlew` lives):
   - Windows: `gradlew.bat test`
   - Linux / macOS: `./gradlew test`

   When `appium.server.url` is empty, the suite starts Appium on `127.0.0.1:4723` before the first session.

**Success:** Gradle prints `BUILD SUCCESSFUL` and **13** tests pass. First run may take several minutes (Gradle, emulator boot, Appium driver install on device).

## First-time toolchain

Use this section for a **clean machine** or when an automated agent installs dependencies. Order matters.

### 1. JDK 17

Install **Eclipse Temurin 17** (or any JDK 17). Point `JAVA_HOME` at the JDK root and put `%JAVA_HOME%\bin` (Windows) or `$JAVA_HOME/bin` (Unix) on `PATH`.

Check:

```bash
java -version
```

Expect a `17.x` line. Gradle uses the toolchain from the project; without JDK 17 the build fails early.

### 2. Android SDK (API 34)

**Option A — Android Studio (simplest for humans):** install [Android Studio](https://developer.android.com/studio), open SDK Manager, install **Android 14 (API 34)** platform, **Android SDK Platform-Tools**, **Android Emulator**, and a **Google APIs** system image for API 34. Create a virtual device named **`Pixel_7_API_34`** (Pixel-class phone is fine).

**Option B — command line (agents / CI-style):** install [command-line tools only](https://developer.android.com/studio#command-tools), then:

```bash
# Set ANDROID_HOME before sdkmanager (examples):
#   Windows:  %LOCALAPPDATA%\Android\Sdk
#   Linux:    $HOME/Android/Sdk
#   macOS:    $HOME/Library/Android/sdk

sdkmanager --licenses    # accept all (interactive once)
sdkmanager "platform-tools" "emulator" "platforms;android-34" "system-images;android-34;google_apis;x86_64"
avdmanager create avd -n Pixel_7_API_34 -k "system-images;android-34;google_apis;x86_64" -d pixel_7
```

On Apple Silicon use `google_apis;arm64-v8a` instead of `x86_64` in both lines.

### 3. Environment variables

Set for the shell that runs tests (and for any IDE):

| Variable | Value |
|----------|--------|
| `ANDROID_HOME` | SDK root (same as `ANDROID_SDK_ROOT`) |
| `PATH` | add `$ANDROID_HOME/platform-tools`, `$ANDROID_HOME/emulator`, and `$ANDROID_HOME/cmdline-tools/latest/bin` |

Check:

```bash
adb version
emulator -list-avds    # must list Pixel_7_API_34 (or change avd in config.properties)
```

**Virtualization:** the emulator needs hardware acceleration (Windows: Hyper-V / WHPX; Linux: KVM). If the emulator window is unusable, set AVD **Graphics** to **Software** or start with `emulator -avd Pixel_7_API_34 -gpu swiftshader_indirect`.

### 4. Node.js and Appium 3

Install **Node.js 18+** (LTS). Then:

```bash
npm install -g appium@3
appium driver install uiautomator2
appium -v
appium driver list --installed   # should include uiautomator2
```

### 5. Before `gradlew test`

From the cloned project root:

1. Download the [pinned APK](#app-binary) to `apps/mda-2.3.0-27.apk`.
2. Start the emulator (recommended):
   ```bash
   emulator -avd Pixel_7_API_34
   ```
3. Wait until boot finishes, then:
   ```bash
   adb devices
   ```
   You should see one line ending in `device`. Export that serial as `UDID` (optional but avoids launching a second AVD):
   ```bash
   export UDID=emulator-5554    # example; use your serial
   ```
4. Run tests ([Run](#run)).

### Toolchain checklist (for scripts / agents)

| Step | Command / condition |
|------|---------------------|
| JDK | `java -version` → 17 |
| SDK | `ANDROID_HOME` set, `adb version` works |
| AVD | `emulator -list-avds` contains `Pixel_7_API_34` |
| Device | `adb devices` shows `device` (not `offline`) |
| Appium | `appium -v` ≥ 3, uiautomator2 driver installed |
| APK | file `apps/mda-2.3.0-27.apk` exists |
| Tests | `gradlew test` / `./gradlew test` → 13 passed |

If `io.appium.settings` fails to upgrade on the emulator, run once: `adb uninstall io.appium.settings`, then re-run tests.

## What is covered

**13 tests**, one behaviour each:

- Login: listed account, empty username, empty password, locked user, half-filled form after background, half-filled form after orientation or display-size stress.
- Cart: badge after add, badge after quantity up, empty copy after remove.
- Catalog: sort modal opens; price descending yields a non-increasing price list on screen.
- WebView: non-https message; live https URL (host checked from the test JVM, then the in-app screen).

Scenario list: `docs/test-scenarios.md`.

## What is left out

- iOS, Espresso, biometrics, QR, geo, checkout (manual in docs).
- Test retries and fixed `Thread.sleep` in tests (explicit waits only).

## How the code is organised

One direction of dependency:

```
tests  →  pages  →  locators (+ copy for expected strings)
              ↓
         session / harness / config
```

| Package | Role |
|---------|------|
| `mda.tests` | Arrange, act, assert — no locators here. |
| `mda.pages` | Screen actions; waits go through `Screen.visible`, `gone`, or `until`. |
| `mda.locators` | `By` definitions only. |
| `mda.copy` | Expected UI strings. |
| `mda.session` | Appium server, capabilities, APK pin, overlay dismissal, device quirks. |
| `mda.harness` | JUnit lifecycle, failure screenshots/logcat. |

**Rule for new work:** add behaviour in a page method, assertions in a test, selectors in locators. Emulator-only work stays in `mda.session` (not scattered in tests).

## Locator policy

- **Accessibility id** (`content-desc`) first — stable for user-visible controls.
- **Resource id** (`Ids.of("…")` → `AppiumBy.id(package:id/…)`) when there is no useful content description, or when several rows share the same pattern (e.g. product tiles, prices).
- **No XPath** in this project.
- Expected copy lives in `mda.copy.Copy`; tests assert `Copy…`, they do not duplicate UI strings.

## Waits and overlays

- No hard sleeps in tests. `WebDriverWait` lives in `Screen`.
- `SystemOverlays.dismissBlocking` clears permission dialogs and the emulator **“System UI isn’t responding”** sheet (clicks **Wait**). It runs on every wait poll and briefly after each new session (`stabilize`).
- `DeviceOrientation.landscapeThenPortrait` tries real rotation; if the app locks portrait, it toggles display size via `adb` (documented quirk for this APK on the emulator).

## Session

- `UiAutomator2Options`, `noReset=false`, fresh session per test.
- `@BeforeEach` retries session creation up to three times when the local Appium process is slow to answer.
- `SessionChecks` asserts a new `sessionId` and expected capabilities; `SessionGuard` stops the suite if isolation breaks (so later reds are not misleading).

## App binary

Pinned release **2.3.0**, file `apps/mda-2.3.0-27.apk`. `@BeforeAll` reads `versionName` on the device and fails if it is not `2.3.0`.

```bash
mkdir -p apps
curl -fsSL -o apps/mda-2.3.0-27.apk \
  https://github.com/saucelabs/my-demo-app-android/releases/download/2.3.0/mda-2.3.0-27.apk
```

On Windows (PowerShell): `New-Item -ItemType Directory -Force apps` then the same `curl` line, or download the URL manually into `apps\`.

## Configuration

Defaults live in `src/test/resources/config.properties`.

| Key | Purpose |
|-----|---------|
| `app.path` | APK path relative to project root |
| `apk.version.name` | Expected `versionName` (pin) |
| `app.package` / `app.activity` | Launch targets |
| `device.name` / `avd` | Emulator name when no `udid` |
| `udid` | Device serial; empty = use `avd` capability |
| `appium.server.url` | Empty = start local Appium; set to reuse an existing server |
| `new.command.timeout.seconds` | Appium new-command timeout |
| `language` / `locale` | Session locale (`en` / `US`) |
| `device.logs.on.failure` | Capture logcat on failed tests |
| `screenshots.runs.to.keep` | How many finished evidence runs to keep |

**Environment overrides:** any key can be set as an env var with the same name, or with dots replaced by underscores (e.g. `udid` → `UDID`, `appium.server.url` → `APPIUM_SERVER_URL`, `device.logs.on.failure` → `DEVICE_LOGS_ON_FAILURE`). Env wins over the file.

## Run

| OS | Command |
|----|---------|
| Windows | `gradlew.bat test` |
| Linux / macOS | `./gradlew test` |

CI exports `UDID` from `adb devices` and runs `./gradlew test` against that emulator.

## Failure evidence

On failure: screenshot and optional logcat under `build/failure-evidence/<runId>/`. Last three finished runs kept.

## CI

`.github/workflows/android-tests.yml` — Ubuntu, JDK 17, Appium 3, API 34 emulator, `./gradlew test`, artifacts on failure.
