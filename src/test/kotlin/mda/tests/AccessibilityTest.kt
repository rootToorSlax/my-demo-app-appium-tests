package mda.tests

import mda.harness.A11yChecks
import mda.harness.BaseTest
import mda.locators.CartLocators
import mda.locators.CatalogLocators
import mda.locators.LoginLocators
import mda.locators.MenuLocators
import mda.pages.CatalogPage
import mda.pages.LoginPage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AccessibilityTest : BaseTest() {
    private val catalogDecorativeStars = setOf(
        "com.saucelabs.mydemoapp.android:id/start1IV",
        "com.saucelabs.mydemoapp.android:id/start2IV",
        "com.saucelabs.mydemoapp.android:id/start3IV",
        "com.saucelabs.mydemoapp.android:id/start4IV",
        "com.saucelabs.mydemoapp.android:id/start5IV",
    )

    @Test
    @Order(1)
    @Tag("a11y")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    @DisplayName("TC-A11Y login clickable controls expose content description or text")
    fun `login clickable controls expose content description or text`() {
        LoginPage(driver).fillListedAccountWithoutSubmitting()
        A11yChecks.assertControlsLabeled(
            driver,
            listOf(
                MenuLocators.open,
                LoginLocators.listedAccount,
                LoginLocators.alice,
                LoginLocators.username,
                LoginLocators.password,
                LoginLocators.loginButton,
            ),
        )
    }

    @Test
    @Order(2)
    @Tag("a11y")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    @DisplayName("TC-A11Y catalog clickable controls expose content description or text")
    fun `catalog clickable controls expose content description or text`() {
        LoginPage(driver).submitListedAccount()
        CatalogPage(driver).isShowing()
        A11yChecks.assertControlsLabeled(
            driver,
            listOf(
                MenuLocators.open,
                CatalogLocators.sortButton,
                CartLocators.open,
                CatalogLocators.productList,
            ),
            allowUnlabeledResourceIds = catalogDecorativeStars,
        )
    }
}
