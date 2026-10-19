package mda.session

import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.WebDriverException

object SessionHealth {
    /** Fails fast when UiAutomator2 instrumentation is already dead (before the test body runs). */
    fun assertUiAutomator2Responsive(driver: AndroidDriver) {
        try {
            driver.currentPackage
        } catch (error: WebDriverException) {
            runCatching { driver.quit() }
            throw error
        }
    }

    fun isInstrumentationDead(error: Throwable): Boolean {
        val message = error.message?.lowercase() ?: ""
        return message.contains("instrumentation process is not running") ||
            message.contains("could not proxy command") ||
            message.contains("socket hang up")
    }
}
