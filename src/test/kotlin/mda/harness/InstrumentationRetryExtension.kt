package mda.harness

import mda.session.SessionCleanup
import mda.session.SessionHealth
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

/**
 * One retry when UiAutomator2 dies mid-test (e.g. after cart remove). Re-runs @BeforeEach via a new invocation.
 */
class InstrumentationRetryExtension : InvocationInterceptor {
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext,
    ) {
        val store = extensionContext.getStore(NAMESPACE)
        try {
            invocation.proceed()
        } catch (error: Throwable) {
            if (!SessionHealth.isInstrumentationDead(error) || store.get(RETRIED_KEY) == true) {
                throw error
            }
            store.put(RETRIED_KEY, true)
            val base = extensionContext.requiredTestInstance as? BaseTest
                ?: throw error
            base.recoverSessionForRetry(extensionContext)
            invocation.proceed()
        }
    }

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(InstrumentationRetryExtension::class.java)
        private const val RETRIED_KEY = "instrumentationRetried"
    }
}
