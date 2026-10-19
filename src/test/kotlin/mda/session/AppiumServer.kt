package mda.session

import io.appium.java_client.service.local.AppiumDriverLocalService
import io.appium.java_client.service.local.AppiumServiceBuilder
import io.appium.java_client.service.local.flags.GeneralServerFlag
import mda.config.Config
import java.io.File
import java.net.URI
import java.net.URL

object AppiumServer {
    private const val port = 4723
    private var service: AppiumDriverLocalService? = null
    private var hookRegistered: Boolean = false

    fun url(): URL {
        val configured = Config.appiumServerUrl
        if (configured.isNotBlank()) {
            return URI(configured).toURL()
        }
        val running = service
        if (running != null && running.isRunning) {
            return running.url
        }
        val started = start()
        service = started
        return started.url
    }

    private fun start(): AppiumDriverLocalService {
        val built = AppiumServiceBuilder()
            .withAppiumJS(appiumMain())
            .withIPAddress("127.0.0.1")
            .usingPort(port)
            .withArgument(GeneralServerFlag.LOG_LEVEL, "warn")
            .withArgument(
                GeneralServerFlag.ALLOW_INSECURE,
                "uiautomator2:chromedriver_autodownload"
            )
            .withEnvironment(environmentForChild())
            .build()
        built.start()
        if (!hookRegistered) {
            Runtime.getRuntime().addShutdownHook(Thread {
                if (built.isRunning) {
                    built.stop()
                }
            })
            hookRegistered = true
        }
        return built
    }

    private fun environmentForChild(): Map<String, String> {
        val env = HashMap<String, String>(System.getenv())
        val sdk = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        if (!sdk.isNullOrBlank()) {
            env["ANDROID_HOME"] = sdk
            env["ANDROID_SDK_ROOT"] = sdk
        }
        return env
    }

    private fun appiumMain(): File {
        val fromEnv = System.getenv("APPIUM_MAIN")
        val candidates = mutableListOf<File>()
        if (!fromEnv.isNullOrBlank()) {
            candidates.add(File(fromEnv))
        }
        val appData = System.getenv("APPDATA")
        if (!appData.isNullOrBlank()) {
            candidates.add(File(appData, "npm/node_modules/appium/build/lib/main.js"))
        }
        val local = System.getenv("LOCALAPPDATA")
        if (!local.isNullOrBlank()) {
            candidates.add(
                File(local, "Volta/tools/image/packages/appium/node_modules/appium/build/lib/main.js")
            )
        }
        val npmRoot = npmRootGlobal()
        if (npmRoot != null) {
            candidates.add(File(npmRoot, "appium/build/lib/main.js"))
        }
        val found = candidates.firstOrNull { it.isFile }
        if (found != null) {
            return found
        }
        error("Appium main.js was not found. Checked: ${candidates.joinToString { it.absolutePath }}")
    }

    private fun npmRootGlobal(): String? {
        val npm = if (System.getProperty("os.name").lowercase().contains("win")) "npm.cmd" else "npm"
        return try {
            val process = ProcessBuilder(npm, "root", "-g")
                .redirectErrorStream(true)
                .start()
            val text = process.inputStream.bufferedReader().readText().trim()
            val code = process.waitFor()
            if (code != 0) {
                null
            } else {
                text.lineSequence().lastOrNull()?.trim()
            }
        } catch (ignored: Exception) {
            null
        }
    }
}
