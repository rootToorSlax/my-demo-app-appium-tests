package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config

/**
 * Single gate for emulator audio. When [Config.muteAndroidSystem] is true (default),
 * every call path that can unmute or play sound must go through [enforce].
 */
object AudioGuard {
    fun enforce(serial: String?) {
        if (!Config.muteAndroidSystem) {
            return
        }
        EmulatorLifecycle.muteDeviceAudio(serial)
    }

    fun enforce(driver: AndroidDriver) {
        enforce(SessionCleanup.deviceSerial(driver) ?: SessionCleanup.resolvedSerial())
    }
}
