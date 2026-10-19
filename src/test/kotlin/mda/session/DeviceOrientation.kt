package mda.session

import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.InvalidElementStateException
import org.openqa.selenium.ScreenOrientation
import java.io.File

object DeviceOrientation {
    /**
     * MDA locks portrait on login; [AndroidDriver.rotate] often fails with
     * [InvalidElementStateException]. We still stress layout by rotating when allowed,
     * otherwise by toggling display size on the emulator via adb.
     */
    fun landscapeThenPortrait(driver: AndroidDriver) {
        try {
            driver.rotate(ScreenOrientation.LANDSCAPE)
            driver.rotate(ScreenOrientation.PORTRAIT)
        } catch (_: InvalidElementStateException) {
            adbShell(driver, "wm size 2400x1080")
            Thread.sleep(400)
            adbShell(driver, "wm size 1080x2400")
        }
    }

    private fun adbShell(driver: AndroidDriver, command: String) {
        val sdk = System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: error("ANDROID_HOME is not set")
        val adbName = if (System.getProperty("os.name").lowercase().contains("win")) "adb.exe" else "adb"
        val adb = File(sdk, "platform-tools/$adbName")
        val udid = driver.capabilities.getCapability("appium:deviceUDID")?.toString()
        val args = mutableListOf(adb.absolutePath)
        if (!udid.isNullOrBlank()) {
            args += listOf("-s", udid)
        }
        args += listOf("shell", *command.split(" ").toTypedArray())
        val process = ProcessBuilder(args).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()
        if (exit != 0) {
            error("adb shell failed ($exit): $output")
        }
    }
}
