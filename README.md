# My Demo App — Android tests

Black-box UI checks for Sauce Labs **My Demo App** (Android), using **Kotlin**, **JUnit 5**, and **Appium 3** with **UiAutomator2**.

**Reviewer path:** a green **GitHub Actions** run is enough to judge the automation. For a **local** run, follow [Local installation manual (reviewers)](#local-installation-manual-reviewers) (Windows: `run-smoke.cmd` in **cmd**, ~2 min). Then `docs/test-scenarios.md` and `src/test/kotlin/mda/tests`. Command cheat sheet: `docs/run-commands.md`.

The **Git repository URL** is sent in the submission email; this README assumes you already cloned that repo into a working directory.

## Stack & local setup (reviewer or AI)

Pinned toolchain for this repo (do **not** mix with an old Appium 2 install on the same machine unless you reinstall the driver):

| Piece | Version / note |
|-------|----------------|
| **Appium server** | **3.x** (`npm install -g appium@3`) |
| **UiAutomator2 driver** | **8.x** (installed with Appium 3) |
| **java-client** | **11.0.0** (`build.gradle.kts`) |
| **Selenium** | 4.50.x (transitive) |
| **JDK** | 17 (Gradle toolchain) |
| **Android** | API **34** emulator, AVD name **`Pixel_7_API_34`** |
| **APK** | Sauce My Demo App **2.3.0** → `apps/mda-2.3.0-27.apk` (not in git) |

**One-time Appium install (human or agent):**

```bash
npm install -g appium@3
appium driver uninstall uiautomator2    # only if upgrading from Appium 2
appium driver install uiautomator2
appium -v
appium driver list --installed          # must list uiautomator2
```

**Already wired in the project (no manual flags when using `gradlew test`):**

- Local Appium is started with `--allow-insecure uiautomator2:chromedriver_autodownload` (`AppiumServer.kt`) so WebView tests can fetch a matching Chromedriver on Appium 3.
- Capabilities include `appium:chromedriverAutodownload=true` (`SessionOptions.kt`).
- CI uses the same stack: `npm install -g appium@3` and `appium driver install uiautomator2` in `.github/workflows/android-tests.yml`.

**Not used:** Espresso driver, XCUITest, Appium 2 / java-client 9–10, or a separate Chromedriver install.

**Agent checklist:** `JAVA_HOME` + `ANDROID_HOME` set → APK in `apps/` → emulator `device` in `adb devices` → [run scripts](#local-installation-manual-reviewers) or `gradlew` from repo root.

## Local installation manual (reviewers)

Step-by-step for **Windows** (same ideas on Linux/macOS with `./gradlew` and `scripts/android-env.cmd` patterns). Do this once per machine, then use the **run scripts** so you do not fight `UDID`, `adb`, or a silent emulator.

### 1. Prerequisites (one time)

Complete [First-time toolchain](#first-time-toolchain): **JDK 17**, **Android SDK API 34**, AVD **`Pixel_7_API_34`**, **Node.js**, **Appium 3** + **uiautomator2** driver, and the [pinned APK](#app-binary) at `apps/mda-2.3.0-27.apk`.

Verify:

```bat
java -version
adb version
emulator -list-avds
appium -v
```

`ANDROID_HOME` must point at the SDK (Windows default often `%LOCALAPPDATA%\Android\Sdk`). If `adb` is “not recognized” in cmd, run `call scripts\android-env.cmd` or add `%ANDROID_HOME%\platform-tools` to `PATH`.

### 2. Recommended: run scripts (no manual `UDID`)

From the **repo root**. Use **Command Prompt** or **PowerShell** with the **`.\` prefix** (PowerShell does not run `run-full.cmd` without it):

```bat
cd C:\path\to\my-demo-app-appium-tests
.\run-smoke.cmd
.\run-full.cmd
```

In **cmd.exe** you can also type `run-smoke.cmd` without `.\`. Do **not** type `gradlew test` alone — use **`.\gradlew.bat test`** (PowerShell) or **`gradlew.bat test`** (cmd).

| Script | Use when |
|--------|----------|
| **`run-smoke.cmd`** | Quick check — **4 smoke tests**, ~2 min |
| **`run-full.cmd`** | **Emulator + wait +** full **`gradlew test --rerun-tasks`** (17 tests), harness **fast** (~5–7 min target). Not emulator-only — keep the **MDA full regression** cmd window open. |
| **`run-gate.cmd`** | Pre-release / CI-like — **`HARNESS_PROFILE=strict`** + full test |
| **`stop.cmd`** | Stop Gradle daemons and kill emulators |

Each script:

1. Sets `ANDROID_HOME` / `PATH` (`scripts\android-env.cmd`)
2. Starts the emulator in a **second window** with **`-no-audio`** when `mute.android.system=true` (default)
3. Waits until `adb devices` shows **`device`** and boot completed
4. Writes `UDID` for that run (e.g. `emulator-5554`) — you do **not** need to type `set UDID=…` yourself
5. Runs `apply-mute.cmd` then Gradle

**Do not** run a second `gradlew test` in parallel while a script is still running.

### 3. Manual `gradlew` (only if you prefer)

Use this only when the emulator is **already** running and visible in `adb devices`:

```bat
call scripts\android-env.cmd
adb devices
```

You must see exactly one line like `emulator-5554    device`.

Then either:

- **Let the suite pick the device** (leave `udid` empty in `config.properties` and **do not** set `UDID` in the environment), or
- Set **`UDID` to that exact serial** — but only while that device is online:

```bat
set UDID=emulator-5554
gradlew.bat test --rerun-tasks
```

**Common mistake:** `set UDID=emulator-5554` left over from an old session while the emulator is **off**. Appium then returns *Could not find a connected Android device*. Fix:

```bat
set UDID=
```

or close the cmd window, or use **`run-full.cmd`** again. The suite only trusts `UDID` when `adb` lists that serial as `device` (`DeviceWait`).

**`gradlew test` alone does not start the emulator.** Use `run-full.cmd` or start the AVD yourself first.

### 4. Gradle tasks and tags

| Command | What runs |
|---------|-----------|
| `gradlew.bat test` / `test --rerun-tasks` | All **17** tests |
| `gradlew.bat smokeTest` | Tag **`smoke`** (4 tests) |
| `gradlew.bat a11yTest` | Tag **`a11y`** (2 tests) |
| `gradlew.bat test --tests mda.tests.CartTest` | One class |
| `gradlew.bat --stop` | Stop Gradle daemons |

Full list: `docs/run-commands.md`.

### 5. Harness profile (speed vs CI extras)

Default in `config.properties`: **`harness.profile=fast`** — good for local iteration (no per-test logcat clear / crash oracle / overlay sweep on every wait).

| Profile | Set via | Typical use |
|---------|---------|-------------|
| **fast** | default in `config.properties` | `run-full.cmd`, daily dev |
| **strict** | `set HARNESS_PROFILE=strict` or **`run-gate.cmd`** | Same extras as **GitHub Actions** (`crash.oracle`, logcat hygiene, overlay polls) |

At suite start, the log prints: `Harness profile: fast (strict extras: oracle=…, …)`.

### 6. Audio (silent by default)

Single switch: **`mute.android.system=true`** in `config.properties` (default). Scripts, emulator **`-no-audio`**, and **`AudioGuard`** in tests keep guest audio off across session resets and app restarts.

**Reviewer wants sound:** set `mute.android.system=false` or `set MUTE_ANDROID_SYSTEM=false` before run (and start emulator without `-no-audio` if you launch it manually).

### 7. After a failed or stuck run

```bat
stop.cmd
```

Then `run-smoke.cmd` or `run-full.cmd` once. If instrumentation errors persist: `adb uninstall io.appium.settings` once, then rerun.

### 8. What “success” looks like

- Smoke: `BUILD SUCCESSFUL`, **4/4** tests
- Full: `BUILD SUCCESSFUL`, **17/17** tests, summary `TEST SUMMARY: 17/17 passed`
- Lines `--- Session start: TC-… ---` / `--- Session ready ---` between tests

CI on push already runs **`smokeTest`** (PR) or **`test`** (main) with `HARNESS_PROFILE=strict` — no local setup required to review the submission.

## Quick start

1. Complete [First-time toolchain](#first-time-toolchain) once per machine (JDK, Android SDK, AVD, Appium).
2. [Download the APK](#app-binary) into `apps/` (not in git).
3. Start the emulator (or skip if Appium should boot the AVD — see step 5 in toolchain verify).
4. From the **project root** (where `gradlew` lives):

   **Windows (recommended — emulator console + wait for device + `UDID`):**
   ```bat
   run-smoke.cmd
   ```
   Opens the emulator in a **second cmd window** with verbose logs, waits until `adb` sees `device`, sets `UDID`, then runs **`smokeTest`** (~2 min). Full regression: `run-full.cmd` (~5–7 min). Stop Gradle/emulator: `stop.cmd`.

   **Manual:** emulator already up in `adb devices`, then `gradlew.bat test --rerun-tasks` — see [Local installation manual](#local-installation-manual-reviewers).

   When `appium.server.url` is empty, the suite starts Appium on `127.0.0.1:4723` before the first session.

**Success:** `BUILD SUCCESSFUL`. First run may take several minutes (Gradle, emulator boot, UiAutomator2 install on device).

**How long is normal?** **17** Appium sessions (one per test). With **`harness.profile=fast`** and a running emulator, full suite is often **~5–7 minutes**. Strict / extras or a stuck wait can be longer. One `gradlew` at a time; use `stop.cmd` if needed.

## Test suites

| Gradle task | JUnit tags | When |
|-------------|------------|------|
| `gradlew test` | (all) | Local full run; CI on **push** to `master` / `main`, nightly, manual dispatch |
| `gradlew smokeTest` | `smoke` | Fast gate — four checks; CI on **pull_request** |
| `gradlew a11yTest` | `a11y` | Accessibility label checks (2 tests) |

TC ids and mapping: `docs/test-scenarios.md`. App findings: `docs/findings.md`. Deliberate omissions: `docs/scope-decisions.md`. Logcat crash oracle: `docs/logcat-crash-oracle.md`.

## Senior extras (optional depth)

Beyond the original regression paths, the suite adds reviewer-friendly depth. Most behaviour is **on by default** and **tunable** in `config.properties` (or env overrides — see [Configuration](#configuration)).

| Extra | What it does | Tags / task | Config / toggle |
|-------|----------------|-------------|-----------------|
| TC-xx traceability | `@DisplayName("TC-…")` on every test; Gradle HTML report shows ids | all tests | — |
| Findings & scope | Documented app/env quirks and deliberate omissions | — | `docs/findings.md`, `docs/scope-decisions.md` |
| Parameterized login | TC-02/03/04 in one `@ParameterizedTest` | `LoginTest` | — |
| Smoke vs full | Fast PR gate vs full regression | `smoke` on four tests; `gradlew smokeTest` | CI: PR → smoke, push/main → `test` |
| JUnit report in CI | Test summary on the GitHub job | workflow | — |
| Logcat crash oracle | After each test, fail if logcat shows FATAL/ANR | all tests | `crash.oracle` — **on** in `strict` harness, **off** in `fast` (default local) |
| Cold-start budget | Three `am start -W` samples; median vs soft cap | `perf`, `StartupTest` | `startup.budget.ms` (default **3000**) |
| Kill / relaunch | TC-14: cart badge after process death (documented behaviour) | `CartTest` | — |
| Accessibility audit | Clickable controls on login + catalog must have label or text (known gaps allowlisted) | `a11y` | — |
| Harness fast / strict | Local speed vs CI-depth checks | all tests | `harness.profile` (**fast** default); CI sets **`HARNESS_PROFILE=strict`** |
| Session warm-up | Overlay sweep after each new session | all tests | `overlay.stabilize.rounds` (0 in fast, 1 in strict) |
| Emulator hygiene | Silent AVD + optional `adb emu kill` after JVM exit | harness | `mute.android.system` (**true** default); `kill.after.testrun` (**true**; **`false`** while debugging) |
| Deep links | `DeepLinks.kt` + `mobile: deepLink` helper | not used in asserts | APK **2.3.0** does not register URIs — see F-09 |
| Offline WebView | — | not automated | F-10; TC-10/11 cover validation and happy path |

**GATE before release:** `run-gate.cmd` (or `HARNESS_PROFILE=strict` + `gradlew test --rerun-tasks`) three times green, then `run-smoke.cmd` once.

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
appium driver uninstall uiautomator2    # only when upgrading from Appium 2
appium driver install uiautomator2
appium -v
appium driver list --installed   # should include uiautomator2
```

(Same commands as [Stack & local setup](#stack--local-setup-reviewer-or-ai).)

### 5. Run tests

From the cloned project root, after the APK is in `apps/`:

- **Windows (recommended):** [Local installation manual](#local-installation-manual-reviewers) — `run-smoke.cmd` or `run-full.cmd`.
- **Manual:** start `emulator -avd Pixel_7_API_34 -no-audio`, wait for `adb devices` → `device`, then `gradlew test --rerun-tasks`. Only set `UDID` to that serial while the emulator is online; see manual for stale-`UDID` pitfalls.

### Toolchain checklist (for scripts / agents)

| Step | Command / condition |
|------|---------------------|
| JDK | `java -version` → 17 |
| SDK | `ANDROID_HOME` set, `adb version` works |
| AVD | `emulator -list-avds` contains `Pixel_7_API_34` |
| Device | `adb devices` shows `device` (not `offline`) |
| Appium | `appium -v` ≥ 3, uiautomator2 driver installed |
| APK | file `apps/mda-2.3.0-27.apk` exists |
| Tests | `gradlew test` → **17** tests passed; `gradlew smokeTest` → **4** |

If `io.appium.settings` fails to upgrade on the emulator, run once: `adb uninstall io.appium.settings`, then re-run tests.

### Troubleshooting a red run

| Symptom | What it usually means | Fix |
|---------|------------------------|-----|
| `gradlw` not found | Typo | Use **`gradlew.bat`** (Windows) or `./gradlew` |
| `socket hang up` / `instrumentation process is not running` | Two **Appium sessions** fought for one emulator (often **two `gradlew` at once**, or a stale UiAutomator2 server) | Stop other test runs; `gradlew --stop`; close extra Appium windows on port **4723**; `adb emu kill` then boot one AVD; run **`gradlew.bat test`** again (suite resets UiAutomator2 between tests) |
| `chromedriverAutodownload` capability warning | Harmless log noise on some driver builds | Ignore; WebView still uses server `--allow-insecure uiautomator2:chromedriver_autodownload` |
| SLF4J “No providers” | Gradle test JVM has no SLF4J binding | Ignore (tests do not rely on SLF4J output) |
| `already in use by another session` | Overlapping sessions on `emulator-5554` | Same as socket hang up — **one Gradle test process** only; local Appium uses **`--session-override`** |
| Gradle stuck at **90% EXECUTING** / many `IDLE` lines | Test still running, or stuck `adb`/Appium | `stop.cmd`; use **`harness.profile=fast`** locally; one Gradle process |
| `Could not find a connected Android device` | Stale **`UDID`** or emulator off | `set UDID=` or use **`.\run-full.cmd`**; confirm `adb devices` |
| `run-full.cmd` / `gradlew` not recognized (PowerShell) | Current directory not on PATH | **`.\run-full.cmd`**, **`.\gradlew.bat test`** from repo root |
| `. was unexpected at this time.` after `Emulator ready` | Old batch bug in `apply-mute.cmd` | Pull latest; rerun **`.\run-full.cmd`** |
| `adb` not recognized (Windows) | SDK not on `PATH` | `call scripts\android-env.cmd` |

**Rule of thumb:** one emulator, one test run. Prefer **`run-smoke.cmd`** / **`run-full.cmd`** over bare `gradlew` unless the AVD is already up.

## What is covered

**Regression suite** (see `docs/test-scenarios.md` for TC-xx ids), including:

- Login: listed account, parameterized validation (empty user/password, locked), background and rotation stress.
- Cart: UI add (smoke), quantity/remove via catalog + cart screen, kill-and-relaunch badge (TC-14).
- Catalog: sort modal (smoke), price descending order on screen.
- WebView: https required (smoke), live https (offline error panel not automated — see `findings.md` F-10).
- Extras: logcat crash oracle, cold-start budget (`perf`), accessibility labels (`a11y`).

Scenario list: `docs/test-scenarios.md`.

## What is left out

- iOS, Espresso, biometrics, QR, geo, checkout (manual in docs).
- Test retries and fixed `Thread.sleep` in tests (explicit waits only).

**Not required for this assignment:** repo extras such as `AGENTS.md`, `SKILL.md` / skills folders, `CONTRIBUTORS`, or other modern agent/team metadata. They could be added later for day-to-day tooling; reviewers only need the test suite, docs above, and a green CI run.

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
- `SystemOverlays` clears permission dialogs and the **“System UI isn’t responding”** sheet. In **`strict`** harness it also runs on every wait poll; in **`fast`** (default local) only during session `stabilize`.
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
| `udid` | Device serial in file; usually leave **empty** — use env `UDID` only when `adb` shows that device |
| `harness.profile` | **`fast`** (default local) or **`strict`** (CI / `run-gate.cmd`) |
| `appium.server.url` | Empty = start local Appium; set to reuse an existing server |
| `new.command.timeout.seconds` | Appium new-command timeout |
| `ui.wait.seconds` / `catalog.product.wait.seconds` | UI wait tuning (see `docs/run-commands.md`) |
| `language` / `locale` | Session locale (`en` / `US`) |
| `device.logs.on.failure` | Capture logcat on failed tests |
| `screenshots.runs.to.keep` | How many finished evidence runs to keep |
| `crash.oracle` | In **strict** harness: fail on logcat FATAL/ANR; off in **fast** unless overridden |
| `startup.budget.ms` | Soft cold-start median budget for `StartupTest` |
| `overlay.stabilize.rounds` | Overlay sweep after session start (profile default) |
| `kill.after.testrun` | On Gradle test JVM exit, `adb emu kill` for emulators when `udid` is empty (default **true**; `false` for local debug) |
| `mute.android.system` | **Default true** — silent AVD via `AudioGuard` + scripts (`-no-audio`, `apply-mute.cmd`). Reviewer sound: **`false`** only |

**Environment overrides:** any key can be set as an env var with the same name, or with dots replaced by underscores (e.g. `udid` → `UDID`, `appium.server.url` → `APPIUM_SERVER_URL`, `device.logs.on.failure` → `DEVICE_LOGS_ON_FAILURE`). Env wins over the file.

## Run

See [Local installation manual (reviewers)](#local-installation-manual-reviewers) and `docs/run-commands.md`.

| OS | Typical command |
|----|-----------------|
| Windows | `run-full.cmd` or `gradlew.bat test --rerun-tasks` (emulator already in `adb devices`) |
| Linux / macOS | `./gradlew test --rerun-tasks` after `export UDID=…` matching `adb devices` |

CI boots the emulator, sets `UDID` from `adb devices`, `HARNESS_PROFILE=strict`, then `./gradlew test` or `smokeTest`.

## Failure evidence

On failure: screenshot and optional logcat under `build/failure-evidence/<runId>/`. Last three finished runs kept.

## CI

`.github/workflows/android-tests.yml` — Ubuntu, JDK 17, Appium 3, API 34 emulator; **pull_request** → `smokeTest`, **push** / nightly / manual → full `test`; JUnit summary via `mikepenz/action-junit-report`; failure evidence artifact when present.
