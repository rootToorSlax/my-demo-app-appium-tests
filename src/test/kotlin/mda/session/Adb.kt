package mda.session

import io.appium.java_client.android.AndroidDriver
import mda.config.Config
import java.io.File
import java.util.concurrent.TimeUnit

object Adb {
    fun shellCapture(driver: AndroidDriver, command: String): String {
        val udid = driver.capabilities.getCapability("appium:deviceUDID")?.toString()
        return shellCaptureOn(udid, command)
    }

    fun shellCaptureOn(udid: String?, command: String): String {
        val adb = adbExecutable()
        val args = mutableListOf(adb.absolutePath)
        if (!udid.isNullOrBlank()) {
            args += listOf("-s", udid)
        }
        args += listOf("shell", *command.split(" ").toTypedArray())
        val process = ProcessBuilder(args).redirectErrorStream(true).start()
        val adbTimeout = Config.adbShellTimeoutSeconds
        val finished = process.waitFor(adbTimeout, TimeUnit.SECONDS)
        val output = process.inputStream.readBytes().decodeToString()
        if (!finished) {
            process.destroyForcibly()
            error("adb shell timed out after ${adbTimeout}s: $command")
        }
        val exit = process.exitValue()
        if (exit != 0) {
            error("adb shell failed ($exit): $output")
        }
        return output
    }

    fun shell(driver: AndroidDriver, command: String) {
        shellCapture(driver, command)
    }

    fun connectedDeviceSerials(): List<String> {
        val adb = adbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "devices").redirectErrorStream(true).start()
        val adbTimeout = Config.adbShellTimeoutSeconds
        val finished = process.waitFor(adbTimeout, TimeUnit.SECONDS)
        val output = process.inputStream.bufferedReader().readText()
        if (!finished) {
            process.destroyForcibly()
            error("adb devices timed out after ${adbTimeout}s")
        }
        return output.lineSequence()
            .drop(1)
            .mapNotNull { line ->
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size >= 2 && parts[1] == "device") {
                    parts[0]
                } else {
                    null
                }
            }
            .toList()
    }

    fun emulatorSerials(): List<String> {
        return connectedDeviceSerials().filter { it.startsWith("emulator-") }
    }

    fun emuKill(serial: String) {
        val adb = adbExecutable()
        val process = ProcessBuilder(adb.absolutePath, "-s", serial, "emu", "kill")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()
        if (exit != 0) {
            error("adb emu kill failed ($exit) on $serial: $output")
        }
    }

    private fun adbExecutable(): File {
        val sdk = System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: error("ANDROID_HOME is not set")
        val adbName = if (System.getProperty("os.name").lowercase().contains("win")) "adb.exe" else "adb"
        return File(sdk, "platform-tools/$adbName")
    }
}
