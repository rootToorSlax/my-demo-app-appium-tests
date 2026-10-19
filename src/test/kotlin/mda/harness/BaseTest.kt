package mda.harness

import io.appium.java_client.android.AndroidDriver
import mda.config.Config
import mda.session.ApkVersion
import mda.session.AppiumServer
import mda.session.SessionChecks
import mda.session.SessionGuard
import mda.session.SessionOptions
import mda.session.CrashOracle
import mda.session.AudioGuard
import mda.session.EmulatorLifecycle
import mda.session.DeviceWait
import mda.session.SessionCleanup
import mda.session.SessionHealth
import mda.session.SystemOverlays
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.TimeUnit
import org.openqa.selenium.SessionNotCreatedException
import org.openqa.selenium.WebDriverException

@ExtendWith(FailureEvidenceExtension::class, InstrumentationRetryExtension::class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
abstract class BaseTest {
    protected lateinit var driver: AndroidDriver

    internal fun recoverSessionForRetry(context: ExtensionContext) {
        quitSession()
        openFreshSession(context.displayName)
    }

    @BeforeEach
    fun openFreshSession(testInfo: TestInfo) {
        openFreshSession(testInfo.displayName)
    }

    private fun openFreshSession(displayName: String) {
        val label = displayName
        println("\n--- Session start: $label ---")
        SessionGuard.stopIfLocked()
        val serial = SessionCleanup.resolvedSerial()
        SessionCleanup.prepareForNewSession(serial)
        AudioGuard.enforce(serial)
        if (Config.clearLogcatEachTest) {
            SessionCleanup.clearLogcat(serial)
        }
        var lastError: Exception? = null
        repeat(3) { attempt ->
            DeviceWait.ensureDeviceForSuite(if (attempt == 0) 120 else 45)
            val options = SessionOptions.build()
            try {
                driver = AndroidDriver(AppiumServer.url(), options)
                DriverSlot.driver = driver
                SessionHealth.assertUiAutomator2Responsive(driver)
                SystemOverlays.stabilize(driver)
                EmulatorLifecycle.muteSystemSounds(driver)
                SessionChecks.assertFresh(driver)
                AudioGuard.enforce(driver)
                println("--- Session ready ($displayName) ---")
                return
            } catch (error: Exception) {
                if (!isRecoverableSessionError(error)) {
                    throw error
                }
                lastError = error
                SessionCleanup.prepareForNewSession(SessionCleanup.resolvedSerial())
                Thread.sleep(1_000L * (attempt + 1))
            }
        }
        throw lastError ?: IllegalStateException("Could not start an Appium session")
    }

    @AfterEach
    fun quitSession() {
        val serial = if (this::driver.isInitialized) {
            runCatching { SessionCleanup.deviceSerial(driver) }.getOrNull()
        } else {
            null
        }
        DriverSlot.driver = null
        if (this::driver.isInitialized) {
            try {
                CrashOracle.assertClean(driver)
            } catch (_: Exception) {
            }
            try {
                driver.quit()
            } catch (_: Exception) {
            }
        }
        SessionCleanup.afterSession(serial)
        println("--- Session end ---")
    }

    private fun isRecoverableSessionError(error: Exception): Boolean {
        if (error is SessionNotCreatedException) {
            return true
        }
        if (SessionHealth.isInstrumentationDead(error)) {
            return true
        }
        if (error is WebDriverException) {
            val message = error.message?.lowercase() ?: ""
            return message.contains("session is either terminated")
        }
        return false
    }

    companion object {
        init {
            TestRunTeardown.registerShutdownHook()
        }

        @JvmStatic
        @BeforeAll
        fun prepareSuite() {
            println("Harness profile: ${Config.harnessProfile} (strict extras: oracle=${Config.crashOracleEnabled}, logcat=${Config.clearLogcatEachTest}, overlayPoll=${Config.dismissOverlaysOnWaitPoll})")
            ApkVersion.assertMatchesPin()
            DeviceWait.ensureDeviceForSuite(120)
            AppiumServer.url()
            val serial = SessionCleanup.resolvedSerial()
            AudioGuard.enforce(serial)
            EmulatorLifecycle.applyFastHarnessTweaks(serial)
            EvidenceRun.prepare()
        }

        @JvmStatic
        @AfterAll
        fun finishSuite() {
            EvidenceRun.markCompleteAndPrune()
        }
    }
}
