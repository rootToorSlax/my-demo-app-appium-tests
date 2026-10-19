package mda.locators

import io.appium.java_client.AppiumBy
import org.openqa.selenium.By

object CartLocators {
    val open: By = AppiumBy.accessibilityId("View cart")
    val badge: By = Ids.of("cartTV")
    val increase: By = AppiumBy.accessibilityId("Increase item quantity")
    val remove: By = AppiumBy.accessibilityId("Removes product from cart")
    val emptyTitle: By = Ids.of("noItemTitleTV")
}
