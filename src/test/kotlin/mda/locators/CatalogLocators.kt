package mda.locators

import io.appium.java_client.AppiumBy
import org.openqa.selenium.By

object CatalogLocators {
    val sortButton: By = AppiumBy.accessibilityId(
        "Shows current sorting order and displays available sorting options"
    )
    val sortModal: By = Ids.of("sortTV")
    val priceDescending: By = AppiumBy.accessibilityId("Descending order by price")
    val productList: By = AppiumBy.accessibilityId("Displays all products of catalog")
    val productTile: By = Ids.of("productIV")
    val price: By = Ids.of("priceTV")
}
