package systems.danger.kotlin.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.internal.MainDispatcherFactory
import java.lang.Runnable
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

private const val threadName = "Jvm"
private val executor = Executors.newSingleThreadScheduledExecutor() {
    Thread(it, threadName).also {
        it.isDaemon = true
    }
}

@OptIn(InternalCoroutinesApi::class)
val Dispatchers.Jvm: JvmDispatcher
    get() = Jvm

@InternalCoroutinesApi
sealed class JvmDispatcher : MainCoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext, block: Runnable) = executor.execute(block)

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val scheduled = executor.schedule(Callable {
            with(continuation) { resumeUndispatched(Unit) }
        }, timeMillis, TimeUnit.MILLISECONDS)

        continuation.invokeOnCancellation { scheduled.cancel(false) }
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable): DisposableHandle {
        val scheduled = executor.schedule(block, timeMillis, TimeUnit.MILLISECONDS)

        return object : DisposableHandle {
            override fun dispose() {
                scheduled.cancel(false)
            }
        }
    }

}

@InternalCoroutinesApi
internal class JvmDispatcherFactory : MainDispatcherFactory {
    override val loadPriority: Int = 0

    override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher = Jvm
}

@InternalCoroutinesApi
private object ImmediateJvmDispatcher : JvmDispatcher() {
    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = Thread.currentThread().name != threadName

    override fun toString() = "Jvm [immediate]"
}

@InternalCoroutinesApi
internal object Jvm : JvmDispatcher() {
    override val immediate: MainCoroutineDispatcher
        get() = ImmediateJvmDispatcher

    override fun toString() = "Jvm"
}