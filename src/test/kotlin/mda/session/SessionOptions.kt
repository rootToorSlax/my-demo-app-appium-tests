package mda.session

import mda.config.Config
import io.appium.java_client.android.options.UiAutomator2Options
import java.io.File
import java.time.Duration

object SessionOptions {
    fun build(): UiAutomator2Options {
        val options = UiAutomator2Options()
        options.setPlatformName("Android")
        options.setAutomationName("UiAutomator2")
        options.setCapability("appium:chromedriverAutodownload", true)
        val apk = File(Config.appPath)
        options.setAppPackage(Config.appPackage)
        val serial = SessionCleanup.resolvedSerial()
        if (serial == null || !appInstalledOnDevice(serial)) {
            options.setApp(apk.absolutePath)
        }
        options.setAppActivity(Config.appActivity)
        // The launcher in the manifest is the splash activity. It does not stay on screen.
        options.setAppWaitActivity("com.saucelabs.mydemoapp.android.view.activities.MainActivity")
        options.setDeviceName(Config.deviceName)
        if (serial != null) {
            options.setUdid(serial)
        } else {
            options.setAvd(Config.avd)
        }
        options.setNewCommandTimeout(Duration.ofSeconds(Config.newCommandTimeoutSeconds))
        options.setCapability("appium:uiautomator2ServerLaunchTimeout", 120_000)
        options.setCapability("appium:uiautomator2ServerInstallTimeout", 120_000)
        options.setCapability("appium:adbExecTimeout", 60_000)
        options.setCapability("appium:androidDeviceReadyTimeout", 60_000)
        options.setNoReset(false)
        options.setLanguage(Config.language)
        options.setLocale(Config.locale)
        options.setAutoGrantPermissions(false)
        if (Config.skipAppiumLogcatCapture) {
            options.setCapability("appium:skipLogcatCapture", true)
        }
        return options
    }

    private fun appInstalledOnDevice(serial: String): Boolean {
        return runCatching {
            Adb.shellCaptureOn(serial, "pm path ${Config.appPackage}").contains("package:")
        }.getOrDefault(false)
    }
}
