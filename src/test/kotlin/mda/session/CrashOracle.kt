package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config

object CrashOracle {
    private val patterns = listOf(
        Regex("FATAL EXCEPTION"),
        Regex("Process ${Regex.escape(Config.appPackage)}.*has died"),
        Regex("ANR in ${Regex.escape(Config.appPackage)}"),
    )

    fun assertClean(driver: AndroidDriver) {
        if (!Config.crashOracleEnabled) {
            return
        }
        val hits = findHits(driver)
        if (hits.isNotEmpty()) {
            throw AssertionError(
                "The app logcat shows a crash or ANR during this test:\n" +
                    hits.take(8).joinToString("\n")
            )
        }
    }

    fun findHits(driver: AndroidDriver): List<String> {
        val packageName = Config.appPackage
        val serial = SessionCleanup.deviceSerial(driver)
        val lines = readRecentLogcat(serial)
        return lines
            .filter { line -> line.contains(packageName) }
            .filter { line -> patterns.any { pattern -> pattern.containsMatchIn(line) } }
    }

    /** Bounded adb tail; Appium logcat over HTTP can stall for minutes on a long-lived emulator. */
    private fun readRecentLogcat(serial: String?): List<String> {
        return runCatching {
            Adb.shellCaptureOn(serial, "logcat -d -t 150")
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()
        }.getOrElse { emptyList() }
    }
}
