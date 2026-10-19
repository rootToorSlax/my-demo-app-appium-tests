package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.locators.MenuLocators
import mda.locators.WebLocators

class WebPage(driver: AndroidDriver) : Screen(driver) {
    fun liveHttpsUrl(): String = "https://example.com"

    fun open() {
        visible(MenuLocators.open).click()
        visible(MenuLocators.webView).click()
        visible(WebLocators.urlField)
    }

    // The screen's checker accepts some http hosts, including example.com.
    // localhost is not https, and that checker rejects it, so the https message shows.
    fun submitNonHttps() {
        typeAndGo("http://localhost")
    }

    fun submitLiveHttps() {
        typeAndGo(liveHttpsUrl())
    }

    fun httpsError(): String = visible(WebLocators.httpsError).text

    fun errorPanelShown(): Boolean {
        gone(WebLocators.loading, 40)
        return driver.findElements(WebLocators.errorPanel).any { element ->
            try {
                element.isDisplayed
            } catch (ignored: Exception) {
                false
            }
        }
    }

    fun titleFromWebContext(): String? {
        return try {
            val web = driver.contextHandles.firstOrNull { handle -> handle.contains("WEBVIEW") }
                ?: return null
            val original = driver.context
            try {
                driver.context(web)
                driver.title
            } finally {
                driver.context(original ?: "NATIVE_APP")
            }
        } catch (ignored: Exception) {
            null
        }
    }

    private fun typeAndGo(url: String) {
        val field = visible(WebLocators.urlField)
        field.clear()
        field.sendKeys(url)
        visible(WebLocators.go).click()
    }
}
