package mda.config

import java.util.Properties

object Config {
    private val properties: Properties = Properties().apply {
        val stream = Config::class.java.classLoader.getResourceAsStream("config.properties")
            ?: error("config.properties is not on the classpath")
        stream.use { load(it) }
    }

    /** `fast` = local iteration (minimal adb/log/oracle). `strict` = CI / pre-release gate. */
    val harnessProfile: String
        get() = optional("harness.profile", "fast").lowercase()

    val isStrictHarness: Boolean
        get() = harnessProfile == "strict"

    val appPath: String
        get() = required("app.path")

    val apkVersionName: String
        get() = required("apk.version.name")

    val appPackage: String
        get() = required("app.package")

    val appActivity: String
        get() = required("app.activity")

    val deviceName: String
        get() = required("device.name")

    val avd: String
        get() = required("avd")

    val udid: String
        get() = optional("udid", "")

    val newCommandTimeoutSeconds: Long
        get() = required("new.command.timeout.seconds").toLong()

    val uiWaitSeconds: Long
        get() = optional("ui.wait.seconds", "10").toLong()

    val uiWaitLongSeconds: Long
        get() = optional("ui.wait.seconds.long", "15").toLong()

    val catalogProductWaitSeconds: Long
        get() = optional("catalog.product.wait.seconds", "10").toLong()

    val cartEmptyWaitSeconds: Long
        get() = optional("cart.empty.wait.seconds", "12").toLong()

    val adbShellTimeoutSeconds: Long
        get() = optional("adb.shell.timeout.seconds", "25").toLong()

    val language: String
        get() = required("language")

    val locale: String
        get() = required("locale")

    val appiumServerUrl: String
        get() = optional("appium.server.url", "")

    val appiumLogLevel: String
        get() = optional("appium.log.level", if (isStrictHarness) "warn" else "error")

    val screenshotsRunsToKeep: Int
        get() = optional("screenshots.runs.to.keep", "3").toInt()

    val deviceLogsOnFailure: Boolean
        get() = profileBoolean("device.logs.on.failure", fast = false, strict = true)

    val crashOracleEnabled: Boolean
        get() = profileBoolean("crash.oracle", fast = false, strict = true)

    val clearLogcatEachTest: Boolean
        get() = profileBoolean("session.clear.logcat", fast = false, strict = true)

    val dismissOverlaysOnWaitPoll: Boolean
        get() = profileBoolean("overlay.dismiss.on.wait.poll", fast = false, strict = true)

    val disableEmulatorAnimations: Boolean
        get() = profileBoolean("harness.disable.animations", fast = true, strict = false)

    val startupBudgetMs: Long
        get() = optional("startup.budget.ms", "3000").toLong()

    val overlayStabilizeRounds: Int
        get() = lookup("overlay.stabilize.rounds")?.toIntOrNull()
            ?: if (isStrictHarness) 1 else 0

    val killAfterTestrun: Boolean
        get() = optional("kill.after.testrun", "true").equals("true", ignoreCase = true)

    /**
     * Default **true**. Guest audio is forced off on every session reset and app restart via [mda.session.AudioGuard].
     * Reviewer/local sound: set `mute.android.system=false` or env `MUTE_ANDROID_SYSTEM=false` only.
     */
    val muteAndroidSystem: Boolean
        get() = optional("mute.android.system", "true").equals("true", ignoreCase = true)

    val skipAppiumLogcatCapture: Boolean
        get() = profileBoolean("appium.skip.logcat.capture", fast = true, strict = false)

    private fun profileBoolean(key: String, fast: Boolean, strict: Boolean): Boolean {
        val explicit = lookup(key)
        if (explicit != null) {
            return explicit.equals("true", ignoreCase = true)
        }
        return if (isStrictHarness) strict else fast
    }

    private fun required(key: String): String {
        return lookup(key) ?: error("Missing config key $key")
    }

    private fun optional(key: String, defaultValue: String): String {
        return lookup(key) ?: defaultValue
    }

    private fun lookup(key: String): String? {
        val direct = System.getenv(key)
        if (direct != null) {
            return direct
        }
        val forShell = System.getenv(key.replace('.', '_').uppercase())
        if (forShell != null) {
            return forShell
        }
        return properties.getProperty(key)
    }
}
