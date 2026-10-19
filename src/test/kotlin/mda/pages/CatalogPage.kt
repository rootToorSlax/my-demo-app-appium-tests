package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.config.Config
import mda.locators.CartLocators
import mda.locators.CatalogLocators
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import java.math.BigDecimal

class CatalogPage(driver: AndroidDriver) : Screen(driver) {
    fun isShowing(): Boolean = showing(CatalogLocators.productList)

    /** Fails fast when the catalog API did not populate tiles (empty grid on emulator). */
    fun waitUntilHasProducts(minCount: Int = 1, timeoutSeconds: Long = Config.catalogProductWaitSeconds) {
        until(timeoutSeconds) {
            val count = driver.findElements(CatalogLocators.productTile).count { element ->
                try {
                    element.isDisplayed
                } catch (_: Exception) {
                    false
                }
            }
            if (count >= minCount) true else null
        }
    }

    fun openFirstProduct() {
        visible(CatalogLocators.productList)
        waitUntilHasProducts()
        val first = until(Config.uiWaitLongSeconds) { topLeftProductTile() }
        first.click()
    }

    fun openCart() {
        visible(CartLocators.open).click()
    }

    fun openSort() {
        visible(CatalogLocators.sortButton).click()
    }

    fun sortModalVisible(): Boolean = showing(CatalogLocators.sortModal, 8)

    fun sortByPriceDescending() {
        openSort()
        visible(CatalogLocators.priceDescending).click()
        gone(CatalogLocators.sortModal, Config.uiWaitSeconds)
    }

    fun pricesOnScreen(): List<BigDecimal> {
        waitUntilHasProducts(2)
        until(Config.uiWaitSeconds) { if (readPrices().size >= 2) true else null }
        return readPrices()
    }

    private fun topLeftProductTile(): WebElement? {
        return driver.findElements(CatalogLocators.productTile)
            .filter { element ->
                try {
                    element.isDisplayed
                } catch (ignored: StaleElementReferenceException) {
                    false
                }
            }
            .minWithOrNull(compareBy({ it.rect.y }, { it.rect.x }))
    }

    private fun readPrices(): List<BigDecimal> {
        val placed = mutableListOf<PlacedPrice>()
        for (element in driver.findElements(CatalogLocators.price)) {
            try {
                if (!element.isDisplayed) {
                    continue
                }
                val text = element.text
                if (!text.contains("$")) {
                    continue
                }
                val rect = element.rect
                placed.add(PlacedPrice(rect.y, rect.x, parsePrice(text)))
            } catch (ignored: StaleElementReferenceException) {
                // The grid was redrawn between the find and the read.
            }
        }
        return placed.sortedWith(compareBy({ it.y }, { it.x })).map { it.amount }
    }

    private fun parsePrice(text: String): BigDecimal {
        val number = Regex("""[0-9]+(?:\.[0-9]+)?""").find(text)?.value
            ?: error("Could not read a price from '$text'")
        return BigDecimal(number)
    }

    private class PlacedPrice(val y: Int, val x: Int, val amount: BigDecimal)
}
