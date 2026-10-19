package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.locators.CartLocators
import mda.locators.CatalogLocators
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import java.math.BigDecimal

class CatalogPage(driver: AndroidDriver) : Screen(driver) {
    fun isShowing(): Boolean = showing(CatalogLocators.productList)

    fun openFirstProduct() {
        visible(CatalogLocators.productList)
        val first = until(20) { topLeftProductTile() }
        first.click()
    }

    fun openCart() {
        visible(CartLocators.open).click()
    }

    fun openSort() {
        visible(CatalogLocators.sortButton).click()
    }

    fun sortModalVisible(): Boolean = showing(CatalogLocators.sortModal, 10)

    fun sortByPriceDescending() {
        openSort()
        visible(CatalogLocators.priceDescending).click()
        gone(CatalogLocators.sortModal, 15)
    }

    fun pricesOnScreen(): List<BigDecimal> {
        until(15) { if (readPrices().size >= 2) true else null }
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
