package mda.locators

import io.appium.java_client.AppiumBy
import org.openqa.selenium.By

object LoginLocators {
    // The listed accounts share one content description, so the row is the resource id.
    val listedAccount: By = Ids.of("username1TV")
    val alice: By = Ids.of("username2TV")
    val username: By = Ids.of("nameET")
    val password: By = Ids.of("passwordET")
    val loginButton: By = AppiumBy.accessibilityId("Tap to login with given credentials")
    val usernameError: By = Ids.of("nameErrorTV")
    val passwordError: By = Ids.of("passwordErrorTV")
    val title: By = Ids.of("loginTV")
}
