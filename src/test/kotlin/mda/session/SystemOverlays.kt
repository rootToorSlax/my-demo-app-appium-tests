package mda.session

import io.appium.java_client.AppiumBy
import io.appium.java_client.android.AndroidDriver
import mda.config.Config
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

/**
 * Emulator and OS popups that sit above the app and block automation.
 * Swept on session start and on every wait poll in [mda.pages.Screen].
 */
object SystemOverlays {
    private val permissionAllow = listOf(
        "com.android.permissioncontroller:id/permission_allow_button",
        "com.android.permissioncontroller:id/permission_allow_foreground_only_button",
        "com.android.permissioncontroller:id/permission_allow_one_time_button",
    )

    private val systemAnrTitle = listOf(
        """new UiSelector().textContains("System UI")""",
        """new UiSelector().textContains("isn't responding")""",
        """new UiSelector().textContains("not responding")""",
        """new UiSelector().textContains("uživatelské rozhraní systému")""",
        """new UiSelector().textContains("nereaguje")""",
    )

    private val systemAnrWait = listOf(
        """new UiSelector().text("Wait")""",
        """new UiSelector().text("Počkat")""",
        """new UiSelector().description("Wait")""",
    )

    fun stabilize(driver: AndroidDriver, rounds: Int = Config.overlayStabilizeRounds) {
        repeat(rounds) {
            dismissBlocking(driver)
            Thread.sleep(100)
        }
    }

    fun dismissBlockingIfEnabled(driver: AndroidDriver) {
        if (!Config.dismissOverlaysOnWaitPoll) {
            return
        }
        dismissBlocking(driver)
    }

    fun dismissBlocking(driver: AndroidDriver) {
        dismissSystemAnr(driver)
        dismissPermissions(driver)
    }

    private fun dismissSystemAnr(driver: AndroidDriver) {
        if (!systemAnrVisible(driver)) {
            return
        }
        for (selector in systemAnrWait) {
            val wait = firstDisplayed(driver, AppiumBy.androidUIAutomator(selector))
            if (wait != null) {
                wait.click()
                Thread.sleep(150)
                return
            }
        }
        try {
            driver.navigate().back()
        } catch (_: Exception) {
        }
    }

    private fun systemAnrVisible(driver: AndroidDriver): Boolean {
        return systemAnrTitle.any { selector ->
            driver.findElements(AppiumBy.androidUIAutomator(selector)).any { element ->
                isDisplayed(element)
            }
        }
    }

    private fun dismissPermissions(driver: AndroidDriver) {
        for (id in permissionAllow) {
            firstDisplayed(driver, AppiumBy.id(id))?.click()
        }
    }

    private fun firstDisplayed(driver: AndroidDriver, locator: By): WebElement? {
        return driver.findElements(locator).firstOrNull { element -> isDisplayed(element) }
    }

    private fun isDisplayed(element: WebElement): Boolean {
        return try {
            element.isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}
