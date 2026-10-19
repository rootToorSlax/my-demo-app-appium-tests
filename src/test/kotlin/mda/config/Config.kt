package mda.config

import java.util.Properties

object Config {
    private val properties: Properties = Properties().apply {
        val stream = Config::class.java.classLoader.getResourceAsStream("config.properties")
            ?: error("config.properties is not on the classpath")
        stream.use { load(it) }
    }

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

    val language: String
        get() = required("language")

    val locale: String
        get() = required("locale")

    val appiumServerUrl: String
        get() = optional("appium.server.url", "")

    val screenshotsRunsToKeep: Int
        get() = optional("screenshots.runs.to.keep", "3").toInt()

    val deviceLogsOnFailure: Boolean
        get() = optional("device.logs.on.failure", "true").equals("true", ignoreCase = true)

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
