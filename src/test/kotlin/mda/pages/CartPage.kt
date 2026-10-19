package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.config.Config
import mda.locators.CartLocators
class CartPage(driver: AndroidDriver) : Screen(driver) {
    fun badge(): Int {
        return visible(CartLocators.badge).text.trim().toInt()
    }

    fun badgeCountOrZero(): Int {
        val shown = driver.findElements(CartLocators.badge).firstOrNull { element ->
            try {
                element.isDisplayed
            } catch (_: Exception) {
                false
            }
        }
        return shown?.text?.trim()?.toIntOrNull() ?: 0
    }

    fun badgeAbove(previous: Int): Int {
        return until(Config.uiWaitSeconds) {
            val shown = driver.findElements(CartLocators.badge).firstOrNull { element ->
                try {
                    element.isDisplayed
                } catch (ignored: Exception) {
                    false
                }
            }
            val parsed = shown?.text?.trim()?.toIntOrNull()
            if (parsed != null && parsed > previous) parsed else null
        }
    }

    fun increaseQuantity() {
        visible(CartLocators.increase).click()
    }

    // The app blocks its UI thread for about five seconds when an item is removed.
    fun remove() {
        visible(CartLocators.remove).click()
    }

    fun emptyCopy(): String = visible(CartLocators.emptyTitle, Config.cartEmptyWaitSeconds).text
}
