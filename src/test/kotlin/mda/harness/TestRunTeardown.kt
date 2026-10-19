package mda.harness

import mda.session.EmulatorLifecycle
import java.util.concurrent.atomic.AtomicBoolean

object TestRunTeardown {
    private val registered = AtomicBoolean(false)

    fun registerShutdownHook() {
        if (!registered.compareAndSet(false, true)) {
            return
        }
        Runtime.getRuntime().addShutdownHook(
            Thread(
                {
                    EmulatorLifecycle.killSuiteEmulators()
                },
                "mda-test-run-teardown"
            )
        )
    }
}
