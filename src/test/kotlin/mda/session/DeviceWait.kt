package mda.session

import mda.config.Config

object DeviceWait {
    /**
     * Blocks until [Config.udid] (if set) or any device is online and booted.
     * Prevents Appium 500 when `UDID=emulator-5554` is in the environment but the AVD is off.
     */
    fun ensureDeviceForSuite(maxWaitSeconds: Long = 120) {
        val deadline = System.currentTimeMillis() + maxWaitSeconds * 1_000
        val configured = Config.udid.takeIf { it.isNotBlank() }
        while (System.currentTimeMillis() < deadline) {
            Adb.reconnectOffline()
            val online = Adb.connectedDeviceSerials()
            val serial = when {
                configured != null && online.contains(configured) -> configured
                configured == null && online.isNotEmpty() -> online.first()
                else -> null
            }
            if (serial != null && isBooted(serial)) {
                return
            }
            Thread.sleep(2_000)
        }
        throw IllegalStateException(buildHint(maxWaitSeconds, configured))
    }

    private fun isBooted(serial: String): Boolean {
        return runCatching {
            Adb.shellCaptureOn(serial, "getprop sys.boot_completed").trim() == "1"
        }.getOrDefault(false)
    }

    private fun buildHint(maxWaitSeconds: Long, configured: String?): String {
        val base = "No ready Android device within ${maxWaitSeconds}s."
        return if (configured != null) {
            "$base UDID=$configured is set but adb has no matching device. " +
                "Start the emulator (run-full.cmd) or clear stale UDID: set UDID="
        } else {
            "$base Start Pixel_7_API_34 (run-full.cmd) or export UDID after adb devices."
        }
    }
}
