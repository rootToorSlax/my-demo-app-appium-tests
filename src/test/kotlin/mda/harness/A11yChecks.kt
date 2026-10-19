package mda.harness

import io.appium.java_client.android.AndroidDriver
import org.junit.jupiter.api.Assertions.assertFalse
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

/** Targeted label checks — avoids a full-device `clickable(true)` scan (very slow on catalog). */
object A11yChecks {
    fun assertControlsLabeled(
        driver: AndroidDriver,
        locators: List<By>,
        allowUnlabeledResourceIds: Set<String> = emptySet(),
    ) {
        val gaps = mutableListOf<String>()
        for (locator in locators) {
            for (element in driver.findElements(locator)) {
                if (!isDisplayed(element)) {
                    continue
                }
                val resourceId = element.getAttribute("resource-id") ?: ""
                if (resourceId in allowUnlabeledResourceIds) {
                    continue
                }
                val desc = element.getAttribute("content-desc")?.trim() ?: ""
                val text = element.text?.trim() ?: ""
                if (desc.isEmpty() && text.isEmpty()) {
                    gaps.add(resourceId.ifBlank { locator.toString() })
                }
            }
        }
        assertFalse(
            gaps.isNotEmpty(),
            "Clickable controls without label or text: ${gaps.take(8)}",
        )
    }

    private fun isDisplayed(element: WebElement): Boolean {
        return try {
            element.isDisplayed
        } catch (_: Exception) {
            false
        }
    }
}
