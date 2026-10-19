package mda.tests

import mda.copy.Copy
import mda.harness.BaseTest
import mda.session.AudioGuard
import mda.pages.CatalogPage
import mda.pages.LoginPage
import mda.session.DeviceOrientation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

class LoginTest : BaseTest() {
    @Test
    @Tag("smoke")
    @DisplayName("TC-01 tapping the listed account opens the catalog")
    fun `tapping the listed account opens the catalog`() {
        val catalog = CatalogPage(driver)
        LoginPage(driver).submitListedAccount()
        assertTrue(catalog.isShowing())
    }

    data class LoginValidationCase(
        val label: String,
        val act: (LoginPage) -> Unit,
        val error: (LoginPage) -> String,
        val expected: String,
        val extra: (LoginPage) -> Unit = {},
    ) {
        override fun toString(): String = label
    }

    companion object {
        @JvmStatic
        fun validationCases(): Stream<LoginValidationCase> = Stream.of(
            LoginValidationCase(
                "TC-02 empty username",
                { it.submitEmptyUsername() },
                { it.usernameError() },
                Copy.login.usernameRequired,
                { login -> assertTrue(login.isShowing()) },
            ),
            LoginValidationCase(
                "TC-03 empty password",
                { it.submitEmptyPassword() },
                { it.passwordError() },
                Copy.login.passwordRequired,
                { login -> assertFalse(login.usernameErrorShown()) },
            ),
            LoginValidationCase(
                "TC-04 locked user",
                { it.submitAlice() },
                { it.passwordError() },
                Copy.login.lockedOut,
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("validationCases")
    @DisplayName("TC-02/03/04 login validation shows the expected message")
    fun `login validation shows the expected message`(case: LoginValidationCase) {
        val login = LoginPage(driver)
        case.act(login)
        assertEquals(case.expected, case.error(login))
        case.extra(login)
    }

    @Test
    @DisplayName("TC-12 a half filled login survives rotation and keeps the username")
    fun `a half filled login survives rotation and keeps the username`() {
        val login = LoginPage(driver)
        login.fillListedAccountWithoutSubmitting()
        val before = login.usernameFieldValue()
        DeviceOrientation.landscapeThenPortrait(driver)
        assertTrue(login.isShowing())
        assertEquals(before, login.usernameFieldValue())
    }

    @Test
    @DisplayName("TC-13 a half filled login survives a short background and keeps the username")
    fun `a half filled login survives a short background and keeps the username`() {
        val login = LoginPage(driver)
        login.fillListedAccountWithoutSubmitting()
        val before = login.usernameFieldValue()
        driver.runAppInBackground(Duration.ofSeconds(2))
        AudioGuard.enforce(driver)
        assertTrue(login.isShowing())
        assertEquals(before, login.usernameFieldValue())
    }
}
