package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.session.SystemOverlays
import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

open class Screen(protected val driver: AndroidDriver) {
    protected fun visible(locator: By, timeoutSeconds: Long = 20): WebElement {
        val wait = WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
        val found = wait.until(
            ExpectedCondition { _ ->
                SystemOverlays.dismissBlocking(driver)
                driver.findElements(locator).firstOrNull { element ->
                    try {
                        element.isDisplayed
                    } catch (ignored: Exception) {
                        false
                    }
                }
            }
        )
        return found ?: throw TimeoutException("Element not visible: $locator")
    }

    protected fun gone(locator: By, timeoutSeconds: Long = 20) {
        val wait = WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
        wait.until(
            ExpectedCondition { _ ->
                SystemOverlays.dismissBlocking(driver)
                driver.findElements(locator).none { element ->
                    try {
                        element.isDisplayed
                    } catch (ignored: Exception) {
                        false
                    }
                }
            }
        )
    }

    protected fun showing(locator: By, timeoutSeconds: Long = 20): Boolean {
        return try {
            visible(locator, timeoutSeconds)
            true
        } catch (error: TimeoutException) {
            false
        }
    }

    /** Explicit waits outside [visible] still sweep emulator and OS overlays each poll. */
    protected fun <T> until(timeoutSeconds: Long = 20, condition: () -> T?): T {
        val wait = WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
        val value = wait.until(
            ExpectedCondition { _ ->
                SystemOverlays.dismissBlocking(driver)
                condition()
            }
        )
        return value ?: throw TimeoutException("Condition was not met within ${timeoutSeconds}s")
    }
}
