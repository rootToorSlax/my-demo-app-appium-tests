package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config

/** Sauce docs mention `mydemoapp://` URIs; APK 2.3.0 does not register them (adb VIEW fails). */
object DeepLinks {
    fun open(driver: AndroidDriver, url: String) {
        driver.executeScript(
            "mobile: deepLink",
            mapOf(
                "url" to url,
                "package" to Config.appPackage,
            )
        )
    }

    fun productDetails(driver: AndroidDriver, productId: Int = 1) {
        open(driver, "mydemoapp://product-details/$productId")
    }

    fun cart(driver: AndroidDriver) {
        open(driver, "mydemoapp://cart")
    }
}
