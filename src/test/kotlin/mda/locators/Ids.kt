package mda.locators

import io.appium.java_client.AppiumBy
import org.openqa.selenium.By

object Ids {
    private const val prefix = "com.saucelabs.mydemoapp.android:id/"

    fun of(name: String): By = AppiumBy.id(prefix + name)
}
