package kr.open.library.simple_ui.core.unit.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelEventTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Main Dispatcher를 Test Dispatcher로 교체
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Main Dispatcher 원상복구
        Dispatchers.resetMain()
    }

    // BaseViewModelEvent를 인스턴스화할 수 있는지 검증
    @Test
    fun baseViewModelEvent_canBeInstantiated() {
        val viewModel = object : BaseViewModelEvent<String>() {}

        assertNotNull(viewModel)
        assertNotNull(viewModel.mEventVm)
    }

    // sendEventVm() 호출 시 Flow로 이벤트가 전달되는지 검증
    @Test
    fun sendEvent_emitsToFlow() = runTest {
        val viewModel = object : BaseViewModelEvent<String>() {
            // 테스트를 위해 protected 메서드를 public으로 노출
            fun sendEventPublic(event: String) = sendEventVm(event)
        }

        val events = mutableListOf<String>()

        // Flow 수집 시작
        val job = launch {
            viewModel.mEventVm.collect { events.add(it) }
        }

        // 이벤트 전송
        viewModel.sendEventPublic("Event1")
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        assertEquals(1, events.size)
        assertEquals("Event1", events[0])

        job.cancel()
    }

    // 여러 이벤트가 순서대로 전달되는지 검증
    @Test
    fun multipleEvents_receivedInOrder() = runTest {
        val viewModel = object : BaseViewModelEvent<String>() {
            fun sendEventPublic(event: String) = sendEventVm(event)
        }

        val events = mutableListOf<String>()

        val job = launch {
            viewModel.mEventVm.collect { events.add(it) }
        }

        // 여러 이벤트 전송
        viewModel.sendEventPublic("Event1")
        viewModel.sendEventPublic("Event2")
        viewModel.sendEventPublic("Event3")
        advanceUntilIdle()

        assertEquals(3, events.size)
        assertEquals(listOf("Event1", "Event2", "Event3"), events)

        job.cancel()
    }

    // 다양한 타입의 이벤트를 전송할 수 있는지 검증
    @Test
    fun differentEventTypes_canBeEmitted() = runTest {
        // Int 타입 이벤트
        val intViewModel = object : BaseViewModelEvent<Int>() {
            fun sendEventPublic(event: Int) = sendEventVm(event)
        }

        val intEvents = mutableListOf<Int>()
        val intJob = launch {
            intViewModel.mEventVm.collect { intEvents.add(it) }
        }

        intViewModel.sendEventPublic(100)
        intViewModel.sendEventPublic(200)
        advanceUntilIdle()

        assertEquals(listOf(100, 200), intEvents)

        intJob.cancel()
    }

    // Custom 데이터 클래스 이벤트
    @Test
    fun customDataClass_canBeEmitted() = runTest {
        data class CustomEvent(val id: Int, val message: String)

        val viewModel = object : BaseViewModelEvent<CustomEvent>() {
            fun sendEventPublic(event: CustomEvent) = sendEventVm(event)
        }

        val events = mutableListOf<CustomEvent>()
        val job = launch {
            viewModel.mEventVm.collect { events.add(it) }
        }

        val event1 = CustomEvent(1, "First")
        val event2 = CustomEvent(2, "Second")

        viewModel.sendEventPublic(event1)
        viewModel.sendEventPublic(event2)
        advanceUntilIdle()

        assertEquals(2, events.size)
        assertEquals(event1, events[0])
        assertEquals(event2, events[1])

        job.cancel()
    }

    // onCleared() 호출 시 Channel이 닫히는지 검증
    @Test
    fun onCleared_closesChannel() = runTest {
        val viewModel = object : BaseViewModelEvent<String>() {
            fun sendEventPublic(event: String) = sendEventVm(event)

            // 테스트를 위해 public으로 노출
            fun triggerCleared() {
                onCleared()
            }
        }

        val events = mutableListOf<String>()
        val job = launch {
            viewModel.mEventVm.collect { events.add(it) }
        }

        viewModel.sendEventPublic("Event1")
        advanceUntilIdle()

        assertEquals(1, events.size)

        // Channel 닫기
        viewModel.triggerCleared()
        advanceUntilIdle()

        // Channel이 닫힌 후 Flow 수집 종료
        job.cancel()

        // 이미 수집된 이벤트 확인
        assertEquals(listOf("Event1"), events)
    }

    // Channel이 닫힌 후 Flow는 종료됨
    @Test
    fun afterCleared_flowCompletesNormally() = runTest {
        val viewModel = object : BaseViewModelEvent<String>() {
            fun sendEventPublic(event: String) = sendEventVm(event)

            fun triggerCleared() {
                onCleared()
            }
        }

        val events = mutableListOf<String>()
        var flowCompleted = false

        val job = launch {
            viewModel.mEventVm.collect { events.add(it) }
            flowCompleted = true  // Flow가 정상 종료되면 실행됨
        }

        // 이벤트 전송 후 수집 확인
        viewModel.sendEventPublic("Event1")
        advanceUntilIdle()
        assertEquals(1, events.size)
        assertEquals("Event1", events[0])

        // Channel 닫기
        viewModel.triggerCleared()
        advanceUntilIdle()

        // Flow가 정상적으로 완료됨
        assertTrue(flowCompleted)

        job.cancel()
    }

    // Flow 수집을 여러 번 시작할 수 있는지 검증
    // Channel.receiveAsFlow()는 cold flow이므로, 한 번에 하나의 collector만 수신 가능
    @Test
    fun multipleCollectors_onlyFirstCollectorReceivesEvents() = runTest {
        val viewModel = object : BaseViewModelEvent<String>() {
            fun sendEventPublic(event: String) = sendEventVm(event)
        }

        val events1 = mutableListOf<String>()
        val events2 = mutableListOf<String>()

        val job1 = launch {
            viewModel.mEventVm.collect { events1.add(it) }
        }

        val job2 = launch {
            viewModel.mEventVm.collect { events2.add(it) }
        }

        viewModel.sendEventPublic("Event1")
        advanceUntilIdle()

        // Channel.receiveAsFlow()는 cold flow이므로 첫 번째 collector만 이벤트 수신
        // 두 번째 collector는 이벤트를 받지 못함 (Channel 특성)
        val totalEvents = events1.size + events2.size
        assertEquals(1, totalEvents)
        assertTrue(events1.contains("Event1") || events2.contains("Event1"))

        job1.cancel()
        job2.cancel()
    }

    // Lifecycle 콜백과 함께 동작하는지 검증
    @Test
    fun lifecycleCallbacks_workWithEvents() = runTest {
        var onStartCalled = false

        val viewModel = object : BaseViewModelEvent<String>() {
            fun sendEventPublic(event: String) = sendEventVm(event)

            override fun onStart(owner: LifecycleOwner) {
                onStartCalled = true
                sendEventPublic("OnStartEvent")
            }
        }

        val events = mutableListOf<String>()
        val job = launch {
            viewModel.mEventVm.collect { events.add(it) }
        }

        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onStart(mockOwner)
        advanceUntilIdle()

        assertTrue(onStartCalled)
        assertEquals(1, events.size)
        assertEquals("OnStartEvent", events[0])

        job.cancel()
    }
}
