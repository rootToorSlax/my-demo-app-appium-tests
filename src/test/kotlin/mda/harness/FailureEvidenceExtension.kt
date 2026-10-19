package mda.harness

import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler

class FailureEvidenceExtension : AfterTestExecutionCallback, LifecycleMethodExecutionExceptionHandler {
    override fun afterTestExecution(context: ExtensionContext) {
        if (context.executionException.isPresent) {
            capture(context)
        }
    }

    override fun handleBeforeEachMethodExecutionException(context: ExtensionContext, throwable: Throwable) {
        capture(context)
        throw throwable
    }

    private fun capture(context: ExtensionContext) {
        try {
            val name = context.requiredTestMethod.name
            EvidenceRun.save(name)
        } catch (error: Exception) {
            System.err.println("Could not save failure evidence: ${error.message}")
        }
    }
}
