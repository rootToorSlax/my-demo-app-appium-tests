import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.io.File

plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.appium:java-client:11.0.0")
    testImplementation("org.seleniumhq.selenium:selenium-support:4.50.0")
    testImplementation("org.seleniumhq.selenium:selenium-remote-driver:4.50.0")
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 1
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }

    val sdk = System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: File(System.getProperty("user.home"), "AppData/Local/Android/Sdk").absolutePath
    val pathEntries = mutableListOf<String>()
    pathEntries.add(File(sdk, "platform-tools").absolutePath)
    pathEntries.add(File(sdk, "emulator").absolutePath)
    pathEntries.add(File(sdk, "cmdline-tools/latest/bin").absolutePath)
    val buildTools = File(sdk, "build-tools")
    if (buildTools.isDirectory) {
        buildTools.listFiles()?.maxByOrNull { it.name }?.let { pathEntries.add(it.absolutePath) }
    }
    val appData = System.getenv("APPDATA")
    if (!appData.isNullOrBlank()) {
        pathEntries.add(File(appData, "npm").absolutePath)
    }
    val localAppData = System.getenv("LOCALAPPDATA")
    if (!localAppData.isNullOrBlank()) {
        pathEntries.add(File(localAppData, "Volta/bin").absolutePath)
    }
    pathEntries.add("C:\\Program Files\\nodejs")
    val inherited = System.getenv("PATH") ?: ""
    environment("ANDROID_HOME", sdk)
    environment("ANDROID_SDK_ROOT", sdk)
    environment("PATH", (pathEntries + inherited).joinToString(File.pathSeparator))
}
