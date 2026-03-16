package kr.open.library.simple_ui.xml.robolectric.permissions.flow

import android.os.Looper
import androidx.activity.ComponentActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.classifier.PermissionType
import kr.open.library.simple_ui.core.permissions.classifier.RuntimePermissionRequestability
import kr.open.library.simple_ui.core.permissions.handler.RolePermissionHandler
import kr.open.library.simple_ui.core.permissions.handler.SpecialPermissionHandler
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import kr.open.library.simple_ui.xml.permissions.coordinator.RequestEntry
import kr.open.library.simple_ui.xml.permissions.flow.PermissionFlowProcessor
import kr.open.library.simple_ui.xml.permissions.flow.RuntimePermissionHandler
import kr.open.library.simple_ui.xml.permissions.host.PermissionHostAdapter
import kr.open.library.simple_ui.xml.permissions.result.PermissionResultAggregator
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class PermissionFlowProcessorRobolectricTest {
    @Test
    fun process_runtimeRationaleWait_autoCancelsWhenHostIsDestroyed() =
        runBlocking {
            val permission = "android.permission.CAMERA"
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
            var deniedResults: List<PermissionDeniedItem>? = null
            val entry = RequestEntry(
                requestId = "runtime-rationale-destroy",
                permissions = listOf(permission),
                results = mutableMapOf(),
                isRestored = false,
                onDeniedResult = { deniedItems ->
                    deniedResults = deniedItems
                },
                onRationaleNeeded = {
                    // Intentionally no-op to keep awaitUserDecision suspended until host destroy.
                },
                onNavigateToSettings = null,
            )

            resultAggregator.registerRequest(entry)
            resultAggregator.registerWaiters(entry.requestId, entry.permissions)

            activityController.start().resume()

            val processingJob = launch {
                processor.process(entry)
            }

            yield()
            activityController.pause().stop().destroy()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            processingJob.join()

            assertNotNull(deniedResults)
            assertEquals(1, deniedResults?.size)
            assertEquals(permission, deniedResults?.first()?.permission)
            assertEquals(PermissionDeniedType.DENIED, deniedResults?.first()?.result)
        }
}
