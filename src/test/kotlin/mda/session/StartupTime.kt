package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config

object StartupTime {
    fun coldStartMs(driver: AndroidDriver): Long {
        val component = "${Config.appPackage}/${Config.appActivity}"
        Adb.shell(driver, "am force-stop ${Config.appPackage}")
        val output = Adb.shellCapture(
            driver,
            "am start -W -S -n $component"
        )
        val match = Regex("TotalTime:\\s*(\\d+)").find(output)
            ?: error("Could not read TotalTime from am start output:\n$output")
        return match.groupValues[1].toLong()
    }
}
