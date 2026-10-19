package mda.tests

import mda.config.Config
import mda.harness.BaseTest
import mda.session.StartupTime
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StartupTest : BaseTest() {
    @Test
    @Tag("perf")
    @DisplayName("TC-PERF cold start median stays within the emulator budget")
    fun `cold start median stays within the emulator budget`() {
        val samples = (1..3).map { StartupTime.coldStartMs(driver) }.sorted()
        val median = samples[1]
        assertTrue(
            median <= Config.startupBudgetMs,
            "Median cold start was ${median}ms (samples $samples), budget is ${Config.startupBudgetMs}ms"
        )
    }
}
