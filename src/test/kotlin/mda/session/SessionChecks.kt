package mda.session

import io.appium.java_client.android.AndroidDriver

object SessionChecks {
    fun assertFresh(driver: AndroidDriver) {
        val sessionId = driver.sessionId?.toString()
        if (sessionId.isNullOrBlank()) {
            SessionGuard.failNotNew("sessionId was null.")
        }
        if (sessionId == SessionGuard.previousSessionId) {
            SessionGuard.failNotNew("sessionId $sessionId was reused.")
        }
        val noReset = capability(driver, "noReset")
        val automationName = capability(driver, "automationName")?.toString()
        // The options class sends UiAutomator2. The session echoes UIAutomator2.
        val automationOk = automationName != null &&
            automationName.equals("UiAutomator2", ignoreCase = true)
        if (!isFalse(noReset) || !automationOk) {
            SessionGuard.failNotNew("noReset was $noReset, automationName was $automationName.")
        }
        SessionGuard.previousSessionId = sessionId
    }

    private fun capability(driver: AndroidDriver, name: String): Any? {
        val map = driver.capabilities.asMap()
        val direct = map[name] ?: map["appium:$name"]
        if (direct != null) {
            return direct
        }
        val nested = map["appium:options"]
        if (nested is Map<*, *>) {
            val found = nested[name] ?: nested["appium:$name"]
            if (found != null) {
                return found
            }
        }
        return map.entries.firstOrNull { entry -> entry.key.endsWith(name) }?.value
    }

    private fun isFalse(value: Any?): Boolean {
        return value == false || value?.toString() == "false"
    }
}
