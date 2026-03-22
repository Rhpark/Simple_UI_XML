package kr.open.library.simple_ui.xml.robolectric.permissions.flow

import android.content.Intent
import android.os.Looper
import androidx.activity.ComponentActivity
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.classifier.PermissionType
import kr.open.library.simple_ui.core.permissions.classifier.RuntimePermissionRequestability
import kr.open.library.simple_ui.core.permissions.handler.RolePermissionHandler
import kr.open.library.simple_ui.core.permissions.handler.SpecialPermissionHandler
import kr.open.library.simple_ui.core.permissions.model.PermissionDeferredPolicy
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import kr.open.library.simple_ui.xml.permissions.coordinator.RequestEntry
import kr.open.library.simple_ui.xml.permissions.flow.PermissionFlowProcessor
import kr.open.library.simple_ui.xml.permissions.flow.RuntimePermissionHandler
import kr.open.library.simple_ui.xml.permissions.host.PermissionHostAdapter
import kr.open.library.simple_ui.xml.permissions.result.PermissionResultAggregator
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class PermissionFlowProcessorRobolectricTest {
    @Test
    fun process_runtimeRationaleWait_autoCancelsWhenCallbackReturnsWithoutAction() =
        runBlocking {
            val permission = "android.permission.CAMERA"
            val testContext = createRuntimeTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "runtime-rationale-no-action",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onRationaleNeeded = {
                    // Intentionally no-op. Returning without defer/proceed/cancel must auto-cancel.
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            testContext.processor.process(entry)

            assertSingleDenied(deniedResults, permission)
        }

    @Test
    fun process_runtimeRationaleWait_deferCancelOnStopCancelsWhenHostStops() =
        runBlocking {
            val permission = "android.permission.CAMERA"
            val testContext = createRuntimeTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            var rationaleRequest: PermissionRationaleRequest? = null
            val entry = createEntry(
                requestId = "runtime-rationale-stop",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onRationaleNeeded = { request ->
                    rationaleRequest = request
                    request.defer(PermissionDeferredPolicy.CANCEL_ON_STOP)
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            val processingJob = launch {
                testContext.processor.process(entry)
            }

            yield()
            assertNotNull(rationaleRequest)
            assertTrue(processingJob.isActive)

            testContext.activityController.pause().stop()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            processingJob.join()

            assertSingleDenied(deniedResults, permission)
        }

    @Test
    fun process_runtimeRationaleWait_deferCancelOnDestroySurvivesStopAndCancelsOnDestroy() =
        runBlocking {
            val permission = "android.permission.CAMERA"
            val testContext = createRuntimeTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "runtime-rationale-destroy",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onRationaleNeeded = { request ->
                    request.defer(PermissionDeferredPolicy.CANCEL_ON_DESTROY)
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            val processingJob = launch {
                testContext.processor.process(entry)
            }

            yield()
            testContext.activityController.pause().stop()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertTrue(processingJob.isActive)
            assertFalse(processingJob.isCompleted)

            testContext.activityController.destroy()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            processingJob.join()

            assertSingleDenied(deniedResults, permission)
        }

    @Test
    fun process_settingsWait_autoCancelsWhenCallbackReturnsWithoutAction() =
        runBlocking {
            val permission = "android.permission.SYSTEM_ALERT_WINDOW"
            val testContext = createSpecialTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "settings-no-action",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onNavigateToSettings = {
                    // Intentionally no-op. Returning without defer/proceed/cancel must auto-cancel.
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            testContext.processor.process(entry)

            assertSingleDenied(deniedResults, permission)
        }

    @Test
    fun process_settingsWait_deferCancelOnStopCancelsWhenHostStops() =
        runBlocking {
            val permission = "android.permission.SYSTEM_ALERT_WINDOW"
            val testContext = createSpecialTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "settings-stop",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onNavigateToSettings = { request ->
                    request.defer(PermissionDeferredPolicy.CANCEL_ON_STOP)
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            val processingJob = launch {
                testContext.processor.process(entry)
            }

            yield()
            assertTrue(processingJob.isActive)

            testContext.activityController.pause().stop()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            processingJob.join()

            assertSingleDenied(deniedResults, permission)
        }

    @Test
    fun process_settingsWait_deferCancelOnDestroySurvivesStopAndCancelsOnDestroy() =
        runBlocking {
            val permission = "android.permission.SYSTEM_ALERT_WINDOW"
            val testContext = createSpecialTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "settings-destroy",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onNavigateToSettings = { request ->
                    request.defer(PermissionDeferredPolicy.CANCEL_ON_DESTROY)
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            val processingJob = launch {
                testContext.processor.process(entry)
            }

            yield()
            testContext.activityController.pause().stop()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertTrue(processingJob.isActive)
            assertFalse(processingJob.isCompleted)

            testContext.activityController.destroy()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            processingJob.join()

            assertSingleDenied(deniedResults, permission)
        }

    @Test
    fun process_settingsWait_cancelCalledTwiceStillCompletesAsDenied() =
        runBlocking {
            val permission = "android.permission.SYSTEM_ALERT_WINDOW"
            val testContext = createSpecialTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "settings-cancel-twice",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onNavigateToSettings = { request ->
                    request.cancel()
                    request.cancel()
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            testContext.processor.process(entry)

            assertSingleDenied(deniedResults, permission)
        }

    @Test
    fun process_runtimeRationaleWait_proceedCalledTwiceDoesNotCrashAndAdvancesToRuntimeLaunch() =
        runBlocking {
            val permission = "android.permission.CAMERA"
            val testContext = createRuntimeTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "runtime-rationale-proceed-twice",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onRationaleNeeded = { request ->
                    request.proceed()
                    request.proceed()
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            val processingJob = launch {
                testContext.processor.process(entry)
            }

            yield()
            assertTrue(processingJob.isActive)
            assertNull(deniedResults)

            processingJob.cancelAndJoin()
        }

    @Test
    fun process_runtimeRationaleWait_autoCancelsWhenCallbackThrows() =
        runBlocking {
            val permission = "android.permission.CAMERA"
            val testContext = createRuntimeTestContext(permission)
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = createEntry(
                requestId = "runtime-rationale-throw",
                permission = permission,
                onDeniedResult = { deniedResults = it },
                onRationaleNeeded = {
                    error("boom")
                },
            )

            registerEntry(testContext, entry)
            testContext.activityController.start().resume()

            testContext.processor.process(entry)

            assertSingleDenied(deniedResults, permission)
        }

    private fun createRuntimeTestContext(permission: String): ProcessorTestContext {
        val activityController = Robolectric.buildActivity(ComponentActivity::class.java).create()
        val activity = activityController.get()
        val host = mock(PermissionHostAdapter::class.java)
        val classifier = mock(PermissionClassifier::class.java)
        val specialHandler = mock(SpecialPermissionHandler::class.java)
        val roleHandler = mock(RolePermissionHandler::class.java)
        val stateStore = PermissionStateStore()
        val stateSnapshot = stateStore.getSnapshot()
        val queue = PermissionQueue(stateSnapshot.requestQueue)
        val requests = mutableMapOf<String, RequestEntry>()
        val inFlightWaiters = mutableMapOf<String, MutableSet<String>>()
        val resultAggregator = PermissionResultAggregator(
            stateStore = stateStore,
            stateSnapshot = stateSnapshot,
            queue = queue,
            requests = requests,
            inFlightWaiters = inFlightWaiters,
            classifier = classifier,
        )

        `when`(host.context).thenReturn(activity)
        `when`(host.activityResultCaller).thenReturn(activity)
        `when`(host.lifecycleOwner).thenReturn(activity)
        `when`(host.shouldShowRequestPermissionRationale(permission)).thenReturn(true)
        `when`(classifier.classify(permission)).thenReturn(PermissionType.RUNTIME)
        `when`(classifier.isSupported(permission)).thenReturn(true)
        `when`(classifier.getRuntimeRequestability(permission)).thenReturn(RuntimePermissionRequestability.REQUESTABLE)

        val runtimeHandler = RuntimePermissionHandler(
            host = host,
            requestedHistory = mutableSetOf(),
        )
        val processor = PermissionFlowProcessor(
            host = host,
            classifierProvider = { classifier },
            runtimeHandler = runtimeHandler,
            specialHandlerProvider = { specialHandler },
            roleHandlerProvider = { roleHandler },
            resultAggregatorProvider = { resultAggregator },
        )

        return ProcessorTestContext(
            activityController = activityController,
            processor = processor,
            resultAggregator = resultAggregator,
        )
    }

    private fun createSpecialTestContext(permission: String): ProcessorTestContext {
        val activityController = Robolectric.buildActivity(ComponentActivity::class.java).create()
        val activity = activityController.get()
        val host = mock(PermissionHostAdapter::class.java)
        val classifier = mock(PermissionClassifier::class.java)
        val specialHandler = mock(SpecialPermissionHandler::class.java)
        val roleHandler = mock(RolePermissionHandler::class.java)
        val stateStore = PermissionStateStore()
        val stateSnapshot = stateStore.getSnapshot()
        val queue = PermissionQueue(stateSnapshot.requestQueue)
        val requests = mutableMapOf<String, RequestEntry>()
        val inFlightWaiters = mutableMapOf<String, MutableSet<String>>()
        val resultAggregator = PermissionResultAggregator(
            stateStore = stateStore,
            stateSnapshot = stateSnapshot,
            queue = queue,
            requests = requests,
            inFlightWaiters = inFlightWaiters,
            classifier = classifier,
        )

        `when`(host.context).thenReturn(activity)
        `when`(host.activityResultCaller).thenReturn(activity)
        `when`(host.lifecycleOwner).thenReturn(activity)
        `when`(classifier.classify(permission)).thenReturn(PermissionType.SPECIAL)
        `when`(classifier.isSupported(permission)).thenReturn(true)
        `when`(specialHandler.isGranted(permission)).thenReturn(false)
        `when`(specialHandler.buildSettingsIntent(permission)).thenReturn(Intent("settings"))

        val runtimeHandler = RuntimePermissionHandler(
            host = host,
            requestedHistory = mutableSetOf(),
        )
        val processor = PermissionFlowProcessor(
            host = host,
            classifierProvider = { classifier },
            runtimeHandler = runtimeHandler,
            specialHandlerProvider = { specialHandler },
            roleHandlerProvider = { roleHandler },
            resultAggregatorProvider = { resultAggregator },
        )

        return ProcessorTestContext(
            activityController = activityController,
            processor = processor,
            resultAggregator = resultAggregator,
        )
    }

    private fun createEntry(
        requestId: String,
        permission: String,
        onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
        onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)? = null,
        onNavigateToSettings: ((kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest) -> Unit)? = null,
    ): RequestEntry = RequestEntry(
        requestId = requestId,
        permissions = listOf(permission),
        results = mutableMapOf(),
        isRestored = false,
        onDeniedResult = onDeniedResult,
        onRationaleNeeded = onRationaleNeeded,
        onNavigateToSettings = onNavigateToSettings,
    )

    private fun registerEntry(testContext: ProcessorTestContext, entry: RequestEntry) {
        testContext.resultAggregator.registerRequest(entry)
        testContext.resultAggregator.registerWaiters(entry.requestId, entry.permissions)
    }

    private fun assertSingleDenied(deniedResults: List<PermissionDeniedItem>?, permission: String) {
        assertNotNull(deniedResults)
        assertEquals(1, deniedResults?.size)
        assertEquals(permission, deniedResults?.first()?.permission)
        assertEquals(PermissionDeniedType.DENIED, deniedResults?.first()?.result)
    }

    private data class ProcessorTestContext(
        val activityController: ActivityController<ComponentActivity>,
        val processor: PermissionFlowProcessor,
        val resultAggregator: PermissionResultAggregator,
    )
}
