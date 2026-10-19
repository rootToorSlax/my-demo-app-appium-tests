package mda.session

import mda.config.Config
import org.junit.jupiter.api.Assertions.fail
import java.io.File

object ApkVersion {
    fun assertMatchesPin() {
        val apk = File(Config.appPath)
        if (!apk.isFile) {
            fail<Unit>("APK not found at ${apk.absolutePath}. Download the pinned release into apps/.")
        }
        val actual = readVersionName(apk)
        val expected = Config.apkVersionName
        if (actual != expected) {
            fail<Unit>("APK versionName is $actual, the pin is $expected. Refusing to run.")
        }
    }

    private fun readVersionName(apk: File): String {
        val aapt = findAapt()
        val process = ProcessBuilder(aapt.absolutePath, "dump", "badging", apk.absolutePath)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val code = process.waitFor()
        if (code != 0) {
            error("aapt could not read the APK: $output")
        }
        val match = Regex("versionName='([^']+)'").find(output)
            ?: error("versionName was not in the aapt output")
        return match.groupValues[1]
    }

    private fun findAapt(): File {
        val sdk = System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: error("ANDROID_HOME is not set, so the APK version cannot be checked")
        val buildTools = File(sdk, "build-tools")
        val folders = buildTools.listFiles()?.filter { it.isDirectory }?.sortedByDescending { it.name }
            ?: emptyList()
        for (folder in folders) {
            val windows = File(folder, "aapt.exe")
            if (windows.isFile) {
                return windows
            }
            val unix = File(folder, "aapt")
            if (unix.isFile) {
                return unix
            }
        }
        error("aapt was not found under ${buildTools.absolutePath}")
    }
}
