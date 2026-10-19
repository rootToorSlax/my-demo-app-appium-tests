package mda.locators

import io.appium.java_client.AppiumBy
import org.openqa.selenium.By

object MenuLocators {
    val open: By = AppiumBy.accessibilityId("View menu")

    // Log In is the last row, below the fold. Scroll the menu to its content description.
    val login: By = AppiumBy.androidUIAutomator(
        "new UiScrollable(new UiSelector().resourceId(\"com.saucelabs.mydemoapp.android:id/menuRV\"))" +
            ".scrollIntoView(new UiSelector().description(\"Login Menu Item\"))"
    )

    // This row has no content description. The visible text is the locator.
    val webView: By = AppiumBy.androidUIAutomator("""new UiSelector().text("WebView")""")
}
