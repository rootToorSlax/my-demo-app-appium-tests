package mda.session

import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.InvalidElementStateException
import org.openqa.selenium.ScreenOrientation
object DeviceOrientation {
    /**
     * MDA locks portrait on login; [AndroidDriver.rotate] often fails with
     * [InvalidElementStateException] and can **block** until Appium command timeout.
     * On emulators we only toggle display size via adb (see F-02).
     */
    fun landscapeThenPortrait(driver: AndroidDriver) {
        val serial = SessionCleanup.deviceSerial(driver)
        if (serial?.startsWith("emulator-") == true) {
            toggleDisplaySize(driver, serial)
            return
        }
        try {
            driver.rotate(ScreenOrientation.LANDSCAPE)
            driver.rotate(ScreenOrientation.PORTRAIT)
        } catch (_: InvalidElementStateException) {
            toggleDisplaySize(driver, serial)
        }
    }

    private fun toggleDisplaySize(driver: AndroidDriver, serial: String?) {
        if (serial != null) {
            Adb.shellCaptureOn(serial, "wm size 2400x1080")
            Thread.sleep(400)
            Adb.shellCaptureOn(serial, "wm size 1080x2400")
        } else {
            Adb.shell(driver, "wm size 2400x1080")
            Thread.sleep(400)
            Adb.shell(driver, "wm size 1080x2400")
        }
    }
}
