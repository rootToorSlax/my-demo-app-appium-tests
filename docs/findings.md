# Findings (app and environment)

| ID | Area | Observation | Impact | How the suite handles it |
|----|------|-------------|--------|-------------------------|
| F-01 | Cart | After **Remove**, the UI thread blocks for several seconds before the empty state appears. | Slow cart test; risk of flaky timeout if waits are too short. | `CartPage.emptyCopy()` waits up to 25s explicitly. |
| F-02 | Login | Login activity locks **portrait**; `rotate()` often throws `InvalidElementStateException`. | Rotation stress test cannot use real landscape on this APK. | `DeviceOrientation` falls back to `adb wm size` on the emulator. |
| F-03 | Catalog | First tiles after **price descending** can share the same price (colour variants). | Asserting strict `price[0] > price[1]` fails while the app is correct. | Assert **non-increasing** prices on screen. |
| F-04 | WebView | `WEBVIEW` context / chromedriver may be missing on some runs. | Title assert would be flaky. | Title check is optional when context is absent. |
| F-05 | Emulator | **System UI isn't responding** sheet can appear (environment, not app). | Blocks locators until dismissed. | `SystemOverlays` taps **Wait** on every wait poll. |
| F-11 | Environment | Two `gradlew` runs (or stale UiAutomator2) on one emulator → `socket hang up` / parallel session warnings. | Cascading reds in `LoginTest` parameterized cases. | One suite at a time; `SessionCleanup` + Appium `--session-override`; attach via `udid` when AVD already booted. |
| F-06 | Speed | Full suite ~**5m54s–6m11s** wall on API 34 emulator (**17** tests, fresh session each; GATE 2026-10-19: 05:58 / 05:54 / 06:11). **~6 min needs `UDID` + one `gradlew` + no hung waits**; 10–15 min usually means slow session start or a test sitting on a timeout. | Shorter UI timeouts only speed up **failures**, not green paths. | Use `run-full.cmd` or `set UDID=emulator-5554`; smoke ~2 min. |
| F-09 | Deep links | `mydemoapp://product-details/*` and `mydemoapp://cart` do not resolve on **2.3.0** (`am start -a VIEW` fails). | T10 speed shortcut unavailable. | Cart tests use catalog UI; `DeepLinks.kt` kept as documented stub. |
| F-10 | WebView offline | `setConnectivity`, `svc wifi/data`, and `cmd connectivity airplane-mode` on API 34 emulator do **not** surface `errorCL` after loading `https://example.com`. | TC-15 would be flaky or fail open. | **No automated offline WebView test**; TC-10/11 still cover validation and happy path. |
| F-07 | Cart persistence | After `terminateApp` + `activateApp`, the **cart badge resets to 0** (session state is not kept across process death). | Real e-commerce might persist cart; demo app does not. | **TC-14** asserts badge 0 after relaunch; documented here. |
| F-08 | Catalog a11y | Product **star rating** icons (`start1IV`…`start5IV`) are clickable but have no `content-desc` or text. | WCAG gap in the demo app. | `AccessibilityTest` allowlists them; finding recorded for reviewers. |
| F-12 | A11y / Appium | `UiSelector().clickable(true)` **without** `packageName` can scan the whole device tree and **stall** for many minutes. | Looks like Gradle frozen at 90% on 2nd `AccessibilityTest`. | Scope to `Config.appPackage`; catalog test waits for `productIV` tiles. |
| F-13 | Catalog UI | Emulator can show **Products + footer** with **no tiles** until the catalog API responds (or if offline). | Empty screen while tests run; cart/catalog tests time out. | `CatalogPage.waitUntilHasProducts()`; check emulator browser/Wi‑Fi if it persists manually. |

Recorded on Sauce Labs My Demo App **2.3.0**, Pixel_7_API_34.
