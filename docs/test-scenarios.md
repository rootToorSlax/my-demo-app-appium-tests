# Scenarios

The app is Sauce Labs My Demo App 2.3.0, package `com.saucelabs.mydemoapp.android`. The account printed on the login screen is `bod@example.com`. Tapping that row fills the form.

Gradle reports **TC-xx** ids from `@DisplayName` on each test.

## Automated

| TC | Scenario | Test class |
|----|----------|------------|
| TC-01 | Tap the listed account and log in. The catalog opens. | `LoginTest` (smoke) |
| TC-02 | Empty username → "Username is required", stay on login. | `LoginTest` (parameterized) |
| TC-03 | Empty password → "Enter Password", no username error. | `LoginTest` (parameterized) |
| TC-04 | Locked user Alice → locked-out message. | `LoginTest` (parameterized) |
| TC-05 | Add one product via catalog UI → cart badge 1. | `CartTest` (smoke) |
| TC-06 | Add via catalog, open cart, increase quantity → badge +1. | `CartTest` |
| TC-07 | Add via catalog, open cart, remove → empty cart copy. | `CartTest` |
| TC-08 | Open sort → modal visible. | `CatalogTest` (smoke) |
| TC-09 | Price descending → non-increasing prices on screen. | `CatalogTest` |
| TC-10 | WebView `http://localhost` → https required copy. | `WebViewTest` (smoke) |
| TC-11 | Live https (host check + in-app) → no error panel. | `WebViewTest` |
| TC-12 | Half-filled login survives background. | `LoginTest` |
| TC-13 | Half-filled login survives rotation / display-size stress. | `LoginTest` |
| TC-14 | Cart badge **clears** after kill + relaunch (see F-07). | `CartTest` |
| TC-PERF | Cold start median within budget (emulator). | `StartupTest` |
| TC-A11Y | Clickable controls have label on login and catalog. | `AccessibilityTest` |

## Manual

Checkout happy path and biometrics / QR / geo remain manual (see `scope-decisions.md`).

## Findings

App and environment notes: `findings.md`.
