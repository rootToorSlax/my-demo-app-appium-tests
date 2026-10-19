# Scope decisions (what we deliberately did not build)

| Topic | Decision | Why | Instead |
|-------|----------|-----|---------|
| Test retries | No automatic retry on failure | Hides real flakes; interview suite stays honest. | Session create retry only (Appium warm-up). |
| Allure / reporting server | Not added | Extra infra; JUnit XML + CI summary + failure evidence are enough. | `mikepenz/action-junit-report`, screenshots, logcat. |
| Visual regression | Not added | Wrong tool for functional homework; brittle on emulator GPU. | Functional UI asserts only. |
| Cloud device farm | Not added | Out of scope; local + GitHub Actions emulator. | Single AVD in CI. |
| Parallel test forks | `maxParallelForks = 1` | One device; cart/session isolation is per test anyway. | Sequential suite. |
| Device matrix (API 30 + 34) | CI on API 34 only | Doubles CI time; same UiAutomator2 path. | Pin API 34 to match README. |
| Screen recording on failure | Not added | Large artifacts; screenshot + logcat suffice. | `build/failure-evidence/`. |
| iOS / XCUITest | Out of scope | Assignment Android path; no Mac in setup. | UiAutomator2 only. |
| Espresso | Not used | Release APK without test instrumentation. | Black-box Appium. |
| API checks in every test | Not used | App has **no backend**; cart/login are local UI. | `HostCheck` for live https before WebView; **CrashOracle** on logcat as second oracle. |
| XPath locators | Not used | Harder to read and maintain. | Accessibility id, then resource id (`Ids`). |
| Hard `Thread.sleep` in tests | Avoided | Masks races. | `WebDriverWait` in `Screen`. |
| `autoGrantPermissions=true` | Left false | Would hide permission UX we do not fake (biometrics, camera, geo). | Manual scenarios in docs. |
| `noReset=true` | Left false | Would leak cart state between tests. | Fresh session per test + `SessionGuard`. |
| Harness `fast` vs `strict` | **fast** default locally; **strict** in CI | Per-test logcat clear + crash oracle + overlay poll each wait made runs 15–18 min while UI looked frozen. | `harness.profile` / `HARNESS_PROFILE`; GATE via `run-gate.cmd`. Test isolation unchanged. |
| Checkout E2E | Deferred (optional `e2e` tag later) | Long form; time-boxed homework. | Documented manual path; can add `CheckoutTest` on `feat/extras` if time allows. |
| Deep link setup (`mydemoapp://`) | Not used in tests | APK **2.3.0** does not register VIEW handlers (verified via `adb`). | UI navigation; `DeepLinks.kt` for future builds. |
| WebView offline (TC-15) | Not automated | Emulator connectivity toggles do not reproduce the in-app error panel reliably (see F-10). | Manual: turn off Wi‑Fi on device, open WebView, load https URL. |
