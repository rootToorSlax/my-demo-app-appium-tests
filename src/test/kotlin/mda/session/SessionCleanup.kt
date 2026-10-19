package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config

object SessionCleanup {
    private val automationPackages = listOf(
        "io.appium.uiautomator2.server",
        "io.appium.uiautomator2.server.test",
    )

    /** Only returns a serial that adb currently lists as `device` (ignores stale env UDID). */
    fun resolvedSerial(): String? {
        val online = Adb.connectedDeviceSerials()
        val configured = Config.udid.takeIf { it.isNotBlank() }
        if (configured != null && online.contains(configured)) {
            return configured
        }
        return online.firstOrNull()
    }

    fun deviceSerial(driver: AndroidDriver): String? {
        return driver.capabilities.getCapability("appium:deviceUDID")?.toString()
    }

    fun resetAutomationOnDevice(serial: String?) {
        for (pkg in automationPackages) {
            runCatching { Adb.shellCaptureOn(serial, "am force-stop $pkg") }
        }
    }

    /** Before a new Appium session — avoids "instrumentation process is not running" after a heavy test. */
    fun prepareForNewSession(serial: String?) {
        val target = serial ?: resolvedSerial()
        resetAutomationOnDevice(target)
        runCatching { Adb.shellCaptureOn(target, "am force-stop ${Config.appPackage}") }
        resetAutomationOnDevice(target)
        AudioGuard.enforce(target)
    }

    fun afterSession(serial: String?) {
        val target = serial ?: resolvedSerial()
        runCatching { Adb.shellCaptureOn(target, "am force-stop ${Config.appPackage}") }
        resetAutomationOnDevice(target)
        AudioGuard.enforce(target)
    }

    fun clearLogcat(serial: String?) {
        runCatching { Adb.shellCaptureOn(serial, "logcat -c") }
    }
}
