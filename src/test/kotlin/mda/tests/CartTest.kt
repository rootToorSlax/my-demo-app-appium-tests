package mda.tests

import mda.copy.Copy
import mda.session.AppLifecycle
import mda.harness.BaseTest
import mda.pages.CartPage
import mda.pages.CatalogPage
import mda.pages.LoginPage
import mda.pages.ProductPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CartTest : BaseTest() {
    @Test
    @Tag("smoke")
    @DisplayName("TC-05 adding one item sets the cart badge to 1")
    fun `adding one item sets the cart badge to 1`() {
        LoginPage(driver).submitListedAccount()
        val catalog = CatalogPage(driver)
        catalog.openFirstProduct()
        ProductPage(driver).addToCart()
        assertEquals(1, CartPage(driver).badge())
    }

    @Test
    @DisplayName("TC-06 increasing the quantity raises the cart badge by one")
    fun `increasing the quantity raises the cart badge by one`() {
        val catalog = CatalogPage(driver)
        loginWithItemInCart(catalog)
        val cart = CartPage(driver)
        val before = cart.badge()
        catalog.openCart()
        cart.increaseQuantity()
        val after = cart.badgeAbove(before)
        assertEquals(before + 1, after)
    }

    @Test
    @DisplayName("TC-07 removing the item shows the empty cart copy")
    fun `removing the item shows the empty cart copy`() {
        val catalog = CatalogPage(driver)
        loginWithItemInCart(catalog)
        catalog.openCart()
        val cart = CartPage(driver)
        cart.remove()
        assertEquals(Copy.cart.empty, cart.emptyCopy())
    }

    @Test
    @DisplayName("TC-14 cart does not keep the badge after app kill and relaunch")
    fun `cart does not keep the badge after app kill and relaunch`() {
        LoginPage(driver).submitListedAccount()
        val catalog = CatalogPage(driver)
        catalog.openFirstProduct()
        ProductPage(driver).addToCart()
        assertEquals(1, CartPage(driver).badge())
        AppLifecycle.terminateApp(driver)
        AppLifecycle.activateApp(driver)
        LoginPage(driver).submitListedAccount()
        assertEquals(0, CartPage(driver).badgeCountOrZero())
    }

    private fun loginWithItemInCart(catalog: CatalogPage) {
        LoginPage(driver).submitListedAccount()
        catalog.openFirstProduct()
        ProductPage(driver).addToCart()
    }
}
