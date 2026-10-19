import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.io.File
import java.time.Duration

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
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
    testImplementation("io.appium:java-client:11.0.0")
    testImplementation("org.seleniumhq.selenium:selenium-support:4.50.0")
    testImplementation("org.seleniumhq.selenium:selenium-remote-driver:4.50.0")
}

fun Test.configureAndroidEnv() {
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
    val udid = System.getenv("UDID")?.takeIf { it.isNotBlank() }
        ?: System.getenv("udid")?.takeIf { it.isNotBlank() }
    if (udid != null) {
        environment("UDID", udid)
        environment("udid", udid)
    }
}

private object Ansi {
    private const val RESET = "\u001B[0m"

    fun wrap(code: Int, text: String): String = "\u001B[${code}m$text$RESET"

    fun greenBright(text: String): String = wrap(92, text)

    fun redBright(text: String): String = wrap(91, text)

    fun yellowBright(text: String): String = wrap(93, text)

    fun purpleLight(text: String): String = wrap(95, text)

    fun highlightTcIds(text: String): String {
        return text.replace(Regex("""TC-[A-Z0-9]+""")) { match -> purpleLight(match.value) }
    }
}

fun Test.configureTestLogging() {
    testLogging {
        events(
            TestLogEvent.SKIPPED,
        )
        exceptionFormat = TestExceptionFormat.FULL
        // Appium logs to stdout can fill Gradle's pipe and freeze later tests.
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    addTestListener(
        object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}

            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                if (suite.parent != null) {
                    return
                }
                val failed = result.failedTestCount
                val passed = result.successfulTestCount
                val skipped = result.skippedTestCount
                val total = result.testCount
                val ratio = "$passed/$total passed"
                logger.lifecycle("")
                if (failed == 0L) {
                    logger.lifecycle(Ansi.greenBright("TEST SUMMARY: $ratio — ALL PASSED"))
                    if (skipped > 0L) {
                        logger.lifecycle(Ansi.yellowBright("$skipped skipped"))
                    }
                } else {
                    logger.lifecycle(Ansi.redBright("TEST SUMMARY: $ratio — $failed test(s) failed"))
                }
                logger.lifecycle("")
            }

            override fun beforeTest(testDescriptor: TestDescriptor) {}

            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                if (testDescriptor.isComposite) {
                    return
                }
                val className = testDescriptor.className?.substringAfterLast('.') ?: return
                val title = Ansi.highlightTcIds(testDescriptor.displayName ?: testDescriptor.name)
                val status = when (result.resultType) {
                    TestResult.ResultType.SUCCESS -> Ansi.greenBright("PASSED")
                    TestResult.ResultType.FAILURE -> Ansi.redBright("FAIL")
                    TestResult.ResultType.SKIPPED -> Ansi.yellowBright("SKIPPED")
                    else -> result.resultType.name
                }
                logger.lifecycle("    $className > $title $status")
            }
        }
    )
    doLast {
        if (!state.skipped && state.upToDate) {
            val cached = readCachedJUnitSummary(name)
            if (cached != null) {
                logger.lifecycle("Task :$name was UP-TO-DATE — tests were not executed again.")
                logger.lifecycle(formatCachedSummary(cached))
                logger.lifecycle("Force a fresh run: gradlew.bat $name --rerun-tasks")
            }
        }
    }
}

fun formatCachedSummary(cached: String): String {
    val failed = Regex("""(\d+) failed""").find(cached)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
    val total = Regex("""\((\d+) total\)""").find(cached)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
    val passed = Regex("""(\d+) passed""").find(cached)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
    val ratio = "$passed/$total passed"
    return if (failed == 0L) {
        "Cached result: ${Ansi.greenBright("$ratio — ALL PASSED")}"
    } else {
        "Cached result: ${Ansi.yellowBright("$ratio, $failed failed")}"
    }
}

fun readCachedJUnitSummary(taskName: String): String? {
    val folder = when (taskName) {
        "smokeTest" -> "smokeTest"
        else -> "test"
    }
    val dir = File("build/test-results/$folder")
    if (!dir.isDirectory) {
        return null
    }
    var tests = 0
    var failures = 0
    var skipped = 0
    dir.listFiles { file -> file.name.startsWith("TEST-") && file.extension == "xml" }?.forEach { file ->
        val head = file.readText().take(800)
        fun attr(name: String): Int {
            val match = Regex("""$name="(\d+)"""").find(head)
            return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
        }
        tests += attr("tests")
        failures += attr("failures")
        skipped += attr("skipped")
    }
    if (tests == 0) {
        return null
    }
    val passed = tests - failures - skipped
    return "$passed passed, $failures failed, $skipped skipped ($tests total)"
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 1
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    timeout.set(Duration.ofMinutes(50))
    configureAndroidEnv()
    configureTestLogging()
}

tasks.register<Test>("smokeTest") {
    description = "Four fast checks, one per major screen."
    group = "verification"
    maxParallelForks = 1
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform {
        includeTags("smoke")
    }
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    configureAndroidEnv()
    configureTestLogging()
}

tasks.register<Test>("a11yTest") {
    description = "Accessibility label checks on login and catalog (TC-A11Y)."
    group = "verification"
    maxParallelForks = 1
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    useJUnitPlatform {
        includeTags("a11y")
    }
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    configureAndroidEnv()
    configureTestLogging()
}
