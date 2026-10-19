package mda.tests

import mda.copy.Copy
import mda.harness.BaseTest
import mda.pages.CartPage
import mda.pages.CatalogPage
import mda.pages.ProductPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CartTest : BaseTest() {
    @Test
    fun `adding one item sets the cart badge to 1`() {
        val catalog = CatalogPage(driver)
        catalog.openFirstProduct()
        ProductPage(driver).addToCart()
        assertEquals(1, CartPage(driver).badge())
    }

    @Test
    fun `increasing the quantity raises the cart badge by one`() {
        val catalog = CatalogPage(driver)
        val cart = CartPage(driver)
        catalog.openFirstProduct()
        ProductPage(driver).addToCart()
        val before = cart.badge()
        catalog.openCart()
        cart.increaseQuantity()
        val after = cart.badgeAbove(before)
        assertEquals(before + 1, after)
    }

    @Test
    fun `removing the item shows the empty cart copy`() {
        val catalog = CatalogPage(driver)
        val cart = CartPage(driver)
        catalog.openFirstProduct()
        ProductPage(driver).addToCart()
        catalog.openCart()
        cart.remove()
        assertEquals(Copy.cart.empty, cart.emptyCopy())
    }
}
