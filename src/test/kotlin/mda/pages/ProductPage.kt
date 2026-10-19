package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.locators.ProductLocators

class ProductPage(driver: AndroidDriver) : Screen(driver) {
    fun addToCart() {
        visible(ProductLocators.addToCart).click()
    }
}
