package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config

/** App process kill/restart — always re-apply mute when configured (TC-14 and similar). */
object AppLifecycle {
    fun terminateApp(driver: AndroidDriver) {
        driver.terminateApp(Config.appPackage)
        AudioGuard.enforce(driver)
    }

    fun activateApp(driver: AndroidDriver) {
        driver.activateApp(Config.appPackage)
        AudioGuard.enforce(driver)
    }
}
