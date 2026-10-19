# Scenarios

The app is Sauce Labs My Demo App 2.3.0, package `com.saucelabs.mydemoapp.android`. The launcher activity is the splash screen. The activity that stays is main. The account printed on the login screen is `bod@example.com`, and the password is printed beside it. Tapping that row fills the form. Any non-empty password except the locked account is accepted, so a "wrong password" case would not describe this app.

## Automated

1. Tap the listed account and log in. The catalog opens.
2. Submit with an empty username. The screen shows "Username is required" and stays on login.
3. Submit with a username and an empty password. The screen shows "Enter Password" and does not show the username error.
4. Tap `alice@example.com (locked out)` and log in. The screen shows "Sorry this user has been locked out."
5. Open a product and add it once. The cart badge is 1.
6. Add a product, open the cart, and increase the quantity. The badge goes up by one.
7. Add a product, open the cart, and remove the item. The cart shows "No Items".
8. Open sort. The modal is on screen (`sortTV`, "Sort by:").
9. Choose price descending. Prices read from the catalog are non-increasing top to bottom (equal neighbours are allowed when variants share a price).
10. Open WebView and submit `http://localhost`. The screen shows "Please provide a correct https url." The screen's own checker accepts some plain `http` hosts, so the sample that actually produces this message is localhost.
11. GET `https://example.com` from the test process and expect a 2xx status. Then submit that URL in the app. The app's error panel stays hidden. If a `WEBVIEW` context exists, the page title is not blank. A missing context is not a failure.
12. Fill the listed account on login without submitting, send the app to the background briefly, return. The username field still holds the same value.
13. Fill the listed account without submitting, then apply orientation or display-size stress (the app locks portrait, so rotation may fall back to `adb wm size` on the emulator). The username field still holds the same value.

## Manual

Checkout stays manual: add an item, proceed to checkout, fill shipping, payment, and review, then place the order. It is a long form, and it is not part of the automated set.

Biometrics, the QR scanner, and geo location stay manual as well. They depend on a sensor this emulator session does not fake, and the suite does not grant those permissions.

## Finding

Recorded after a run on the emulator. See the note below once the checkout path has been walked.
