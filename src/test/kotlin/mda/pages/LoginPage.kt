package mda.pages

import io.appium.java_client.android.AndroidDriver
import mda.locators.LoginLocators
import mda.locators.MenuLocators

class LoginPage(driver: AndroidDriver) : Screen(driver) {
    fun isShowing(): Boolean = showing(LoginLocators.title)

    fun fillListedAccountWithoutSubmitting() {
        open()
        visible(LoginLocators.listedAccount).click()
    }

    fun usernameFieldValue(): String = visible(LoginLocators.username).text.trim()

    fun submitListedAccount() {
        fillListedAccountWithoutSubmitting()
        visible(LoginLocators.loginButton).click()
    }

    fun submitEmptyUsername() {
        open()
        visible(LoginLocators.username).clear()
        visible(LoginLocators.password).clear()
        visible(LoginLocators.loginButton).click()
    }

    fun submitEmptyPassword() {
        open()
        val account = visible(LoginLocators.listedAccount).text
        val username = visible(LoginLocators.username)
        username.clear()
        username.sendKeys(account)
        visible(LoginLocators.password).clear()
        visible(LoginLocators.loginButton).click()
    }

    fun submitAlice() {
        open()
        visible(LoginLocators.alice).click()
        visible(LoginLocators.loginButton).click()
    }

    fun usernameError(): String = visible(LoginLocators.usernameError).text

    fun passwordError(): String = visible(LoginLocators.passwordError).text

    fun usernameErrorShown(): Boolean {
        return driver.findElements(LoginLocators.usernameError).any { element ->
            try {
                element.isDisplayed
            } catch (ignored: Exception) {
                false
            }
        }
    }

    private fun open() {
        visible(MenuLocators.open).click()
        visible(MenuLocators.login).click()
        visible(LoginLocators.title)
    }
}
