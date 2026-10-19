package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.config.Config
import mda.session.SystemOverlays
import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

open class Screen(protected val driver: AndroidDriver) {
    protected fun visible(locator: By, timeoutSeconds: Long = Config.uiWaitSeconds): WebElement {
        val found = wait(timeoutSeconds).until(
            ExpectedCondition { _ ->
                SystemOverlays.dismissBlockingIfEnabled(driver)
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

    protected fun gone(locator: By, timeoutSeconds: Long = Config.uiWaitSeconds) {
        wait(timeoutSeconds).until(
            ExpectedCondition { _ ->
                SystemOverlays.dismissBlockingIfEnabled(driver)
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

    protected fun showing(locator: By, timeoutSeconds: Long = Config.uiWaitSeconds): Boolean {
        return try {
            visible(locator, timeoutSeconds)
            true
        } catch (error: TimeoutException) {
            false
        }
    }

    protected fun <T> until(timeoutSeconds: Long = Config.uiWaitSeconds, condition: () -> T?): T {
        val value = wait(timeoutSeconds).until(
            ExpectedCondition { _ ->
                SystemOverlays.dismissBlockingIfEnabled(driver)
                condition()
            }
        )
        return value ?: throw TimeoutException("Condition was not met within ${timeoutSeconds}s")
    }

    private fun wait(timeoutSeconds: Long): FluentWait<WebDriver> {
        return WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .pollingEvery(Duration.ofMillis(250))
    }
}
