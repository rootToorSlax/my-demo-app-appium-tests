package mda.tests

import mda.harness.BaseTest
import mda.pages.CatalogPage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CatalogTest : BaseTest() {
    @Test
    @Tag("smoke")
    @DisplayName("TC-08 opening sort shows the sort modal")
    fun `opening sort shows the sort modal`() {
        val catalog = CatalogPage(driver)
        catalog.openSort()
        assertTrue(catalog.sortModalVisible())
    }

    @Test
    @DisplayName("TC-09 price descending orders prices from highest to lowest")
    fun `price descending orders prices from highest to lowest`() {
        val catalog = CatalogPage(driver)
        val before = catalog.pricesOnScreen()
        assertTrue(before.isNotEmpty())
        val topBefore = before.first()
        catalog.sortByPriceDescending()
        val after = catalog.pricesOnScreen()
        assertTrue(after.size >= 2)
        assertTrue(after.zipWithNext().all { (higher, lower) -> higher >= lower })
        assertTrue(after.first() >= topBefore)
    }
}
