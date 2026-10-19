package mda.harness

import io.appium.java_client.android.AndroidDriver
import mda.session.ApkVersion
import mda.session.AppiumServer
import mda.session.SessionChecks
import mda.session.SessionGuard
import mda.session.SessionOptions
import mda.session.SystemOverlays
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.SessionNotCreatedException

@ExtendWith(FailureEvidenceExtension::class)
abstract class BaseTest {
    protected lateinit var driver: AndroidDriver

    @BeforeEach
    fun openFreshSession() {
        SessionGuard.stopIfLocked()
        val options = SessionOptions.build()
        var lastError: Exception? = null
        repeat(3) { attempt ->
            try {
                driver = AndroidDriver(AppiumServer.url(), options)
                DriverSlot.driver = driver
                SystemOverlays.stabilize(driver)
                SessionChecks.assertFresh(driver)
                return
            } catch (error: SessionNotCreatedException) {
                lastError = error
                Thread.sleep(2_000L * (attempt + 1))
            }
        }
        throw lastError ?: IllegalStateException("Could not start an Appium session")
    }

    @AfterEach
    fun quitSession() {
        DriverSlot.driver = null
        if (this::driver.isInitialized) {
            try {
                driver.quit()
            } catch (ignored: Exception) {
            }
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun prepareSuite() {
            ApkVersion.assertMatchesPin()
            AppiumServer.url()
            EvidenceRun.prepare()
        }

        @JvmStatic
        @AfterAll
        fun finishSuite() {
            EvidenceRun.markCompleteAndPrune()
        }
    }
}
