package mda.locators

import io.appium.java_client.AppiumBy
import org.openqa.selenium.By

object WebLocators {
    val urlField: By = Ids.of("urlET")
    val go: By = AppiumBy.accessibilityId("Tap to view content of given url")
    val httpsError: By = Ids.of("urlErrorTV")
    val errorPanel: By = Ids.of("errorCL")
    val loading: By = Ids.of("loadingIV")
}
