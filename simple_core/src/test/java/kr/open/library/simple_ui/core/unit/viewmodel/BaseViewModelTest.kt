package kr.open.library.simple_ui.core.unit.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kr.open.library.simple_ui.core.viewmodel.BaseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseViewModelTest {

    // BaseViewModel을 인스턴스화할 수 있는지 검증
    @Test
    fun baseViewModel_canBeInstantiated() {
        val viewModel = object : BaseViewModel() {}

        assertNotNull(viewModel)
    }

    // Lifecycle 콜백을 오버라이드하고 직접 호출할 수 있는지 검증
    @Test
    fun lifecycleCallbacks_canBeOverridden_onCreate() {
        var onCreateCalled = false

        val viewModel = object : BaseViewModel() {
            override fun onCreate(owner: LifecycleOwner) {
                onCreateCalled = true
            }
        }

        // Lifecycle 없이 직접 호출
        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onCreate(mockOwner)

        assertTrue(onCreateCalled)
    }

    @Test
    fun lifecycleCallbacks_canBeOverridden_onStart() {
        var onStartCalled = false

        val viewModel = object : BaseViewModel() {
            override fun onStart(owner: LifecycleOwner) {
                onStartCalled = true
            }
        }

        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onStart(mockOwner)

        assertTrue(onStartCalled)
    }

    @Test
    fun lifecycleCallbacks_canBeOverridden_onResume() {
        var onResumeCalled = false

        val viewModel = object : BaseViewModel() {
            override fun onResume(owner: LifecycleOwner) {
                onResumeCalled = true
            }
        }

        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onResume(mockOwner)

        assertTrue(onResumeCalled)
    }

    @Test
    fun lifecycleCallbacks_canBeOverridden_onPause() {
        var onPauseCalled = false

        val viewModel = object : BaseViewModel() {
            override fun onPause(owner: LifecycleOwner) {
                onPauseCalled = true
            }
        }

        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onPause(mockOwner)

        assertTrue(onPauseCalled)
    }

    @Test
    fun lifecycleCallbacks_canBeOverridden_onStop() {
        var onStopCalled = false

        val viewModel = object : BaseViewModel() {
            override fun onStop(owner: LifecycleOwner) {
                onStopCalled = true
            }
        }

        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onStop(mockOwner)

        assertTrue(onStopCalled)
    }

    @Test
    fun lifecycleCallbacks_canBeOverridden_onDestroy() {
        var onDestroyCalled = false

        val viewModel = object : BaseViewModel() {
            override fun onDestroy(owner: LifecycleOwner) {
                onDestroyCalled = true
            }
        }

        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onDestroy(mockOwner)

        assertTrue(onDestroyCalled)
    }

    // Lifecycle 콜백에서 상태 변경을 검증
    @Test
    fun lifecycleCallbacks_canModifyState() {
        var counter = 0

        val viewModel = object : BaseViewModel() {
            override fun onCreate(owner: LifecycleOwner) {
                counter += 1
            }

            override fun onStart(owner: LifecycleOwner) {
                counter += 10
            }

            override fun onResume(owner: LifecycleOwner) {
                counter += 100
            }
        }

        val mockOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = LifecycleRegistry(this)
        }

        viewModel.onCreate(mockOwner)
        assertEquals(1, counter)

        viewModel.onStart(mockOwner)
        assertEquals(11, counter)

        viewModel.onResume(mockOwner)
        assertEquals(111, counter)
    }

    // onCleared()는 ViewModel의 protected 메서드이므로 간접 테스트
    @Test
    fun viewModel_canBeCleared() {
        var onClearedCalled = false

        val viewModel = object : BaseViewModel() {
            override fun onCleared() {
                super.onCleared()
                onClearedCalled = true
            }

            // 테스트를 위한 public 메서드
            fun triggerCleared() {
                onCleared()
            }
        }

        viewModel.triggerCleared()

        assertTrue(onClearedCalled)
    }
}
