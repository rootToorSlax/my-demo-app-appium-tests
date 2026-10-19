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
        options.setApp(apk.absolutePath)
        options.setAppPackage(Config.appPackage)
        options.setAppActivity(Config.appActivity)
        // The launcher in the manifest is the splash activity. It does not stay on screen.
        options.setAppWaitActivity("com.saucelabs.mydemoapp.android.view.activities.MainActivity")
        options.setDeviceName(Config.deviceName)
        if (Config.udid.isNotBlank()) {
            options.setUdid(Config.udid)
        } else {
            options.setAvd(Config.avd)
        }
        options.setNewCommandTimeout(Duration.ofSeconds(Config.newCommandTimeoutSeconds))
        options.setNoReset(false)
        options.setLanguage(Config.language)
        options.setLocale(Config.locale)
        options.setAutoGrantPermissions(false)
        return options
    }
}
