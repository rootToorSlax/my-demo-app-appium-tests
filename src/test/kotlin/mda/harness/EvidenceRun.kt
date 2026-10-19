package mda.harness

import io.appium.java_client.android.AndroidDriver
import mda.config.Config
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object EvidenceRun {
    val runId: String = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now())

    val directory: File
        get() = File("build/failure-evidence", runId)

    fun prepare() {
        directory.mkdirs()
    }

    fun markCompleteAndPrune() {
        directory.mkdirs()
        File(directory, "complete").writeText("complete\n")
        val root = File("build/failure-evidence")
        val finished = root.listFiles()
            ?.filter { folder -> folder.isDirectory && File(folder, "complete").isFile }
            ?.sortedByDescending { folder -> folder.name }
            ?: return
        val keep = Config.screenshotsRunsToKeep
        finished.drop(keep).forEach { folder -> folder.deleteRecursively() }
    }

    fun save(testName: String) {
        val driver = DriverSlot.driver ?: return
        directory.mkdirs()
        val base = testName.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { "test" }
        val shot = (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
        shot.copyTo(File(directory, "$base.png"), overwrite = true)
        if (Config.deviceLogsOnFailure) {
            writeLog(driver, File(directory, "$base.log"))
        }
    }

    private fun writeLog(driver: AndroidDriver, file: File) {
        val entries = driver.manage().logs().get("logcat")
        val text = entries.joinToString(separator = "\n") { entry -> "${entry.level} ${entry.message}" }
        file.writeText(text)
    }
}
