package mda.tests

import mda.copy.Copy
import mda.harness.BaseTest
import mda.session.DeviceOrientation
import mda.pages.CatalogPage
import mda.pages.LoginPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class LoginTest : BaseTest() {
    @Test
    fun `tapping the listed account opens the catalog`() {
        val catalog = CatalogPage(driver)
        LoginPage(driver).submitListedAccount()
        assertTrue(catalog.isShowing())
    }

    @Test
    fun `an empty username shows that a username is required and stays on login`() {
        val login = LoginPage(driver)
        login.submitEmptyUsername()
        assertEquals(Copy.login.usernameRequired, login.usernameError())
        assertTrue(login.isShowing())
    }

    @Test
    fun `an empty password shows that a password is required and does not show the username error`() {
        val login = LoginPage(driver)
        login.submitEmptyPassword()
        assertEquals(Copy.login.passwordRequired, login.passwordError())
        assertFalse(login.usernameErrorShown())
    }

    @Test
    fun `alice shows the locked out message`() {
        val login = LoginPage(driver)
        login.submitAlice()
        assertEquals(Copy.login.lockedOut, login.passwordError())
    }

    @Test
    fun `a half filled login survives rotation and keeps the username`() {
        val login = LoginPage(driver)
        login.fillListedAccountWithoutSubmitting()
        val before = login.usernameFieldValue()
        DeviceOrientation.landscapeThenPortrait(driver)
        assertTrue(login.isShowing())
        assertEquals(before, login.usernameFieldValue())
    }

    @Test
    fun `a half filled login survives a short background and keeps the username`() {
        val login = LoginPage(driver)
        login.fillListedAccountWithoutSubmitting()
        val before = login.usernameFieldValue()
        driver.runAppInBackground(Duration.ofSeconds(2))
        assertTrue(login.isShowing())
        assertEquals(before, login.usernameFieldValue())
    }
}
