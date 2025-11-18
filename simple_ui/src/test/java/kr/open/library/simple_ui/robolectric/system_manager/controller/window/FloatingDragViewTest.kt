package kr.open.library.simple_ui.robolectric.system_manager.controller.window

import android.os.Build
import android.view.View
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewTouchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class FloatingDragViewTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `updateCollisionState updates flow and triggers callbacks`() {
        val view = View(context)
        val recordedPhases = mutableListOf<Pair<FloatingViewTouchType, FloatingViewCollisionsType>>()
        val dragView = FloatingDragView(
            view = view,
            startX = 10,
            startY = 20,
            collisionsWhileTouchDown = { _, type -> recordedPhases += FloatingViewTouchType.TOUCH_DOWN to type },
            collisionsWhileDrag = { _, type -> recordedPhases += FloatingViewTouchType.TOUCH_MOVE to type },
            collisionsWhileTouchUp = { _, type -> recordedPhases += FloatingViewTouchType.TOUCH_UP to type }
        )

        assertEquals(
            FloatingViewTouchType.TOUCH_UP to FloatingViewCollisionsType.UNCOLLISIONS,
            dragView.sfCollisionStateFlow.value
        )

        assertTrue(dragView.updateCollisionState(FloatingViewTouchType.TOUCH_DOWN, FloatingViewCollisionsType.OCCURING))
        assertEquals(
            FloatingViewTouchType.TOUCH_DOWN to FloatingViewCollisionsType.OCCURING,
            dragView.sfCollisionStateFlow.value
        )

        assertTrue(dragView.updateCollisionState(FloatingViewTouchType.TOUCH_MOVE, FloatingViewCollisionsType.OCCURING))
        assertEquals(
            FloatingViewTouchType.TOUCH_MOVE to FloatingViewCollisionsType.OCCURING,
            dragView.sfCollisionStateFlow.value
        )

        assertTrue(dragView.updateCollisionState(FloatingViewTouchType.TOUCH_UP, FloatingViewCollisionsType.UNCOLLISIONS))
        assertEquals(
            FloatingViewTouchType.TOUCH_UP to FloatingViewCollisionsType.UNCOLLISIONS,
            dragView.sfCollisionStateFlow.value
        )

        assertEquals(
            listOf(
                FloatingViewTouchType.TOUCH_DOWN to FloatingViewCollisionsType.OCCURING,
                FloatingViewTouchType.TOUCH_MOVE to FloatingViewCollisionsType.OCCURING,
                FloatingViewTouchType.TOUCH_UP to FloatingViewCollisionsType.UNCOLLISIONS
            ),
            recordedPhases
        )
    }

    @Test
    fun `updateCollisionState without callbacks does not crash`() {
        val view = View(context)
        val dragView = FloatingDragView(
            view = view,
            startX = 0,
            startY = 0
        )

        assertTrue(dragView.updateCollisionState(FloatingViewTouchType.TOUCH_DOWN, FloatingViewCollisionsType.OCCURING))
        assertEquals(
            FloatingViewTouchType.TOUCH_DOWN to FloatingViewCollisionsType.OCCURING,
            dragView.sfCollisionStateFlow.value
        )

        assertTrue(dragView.updateCollisionState(FloatingViewTouchType.TOUCH_MOVE, FloatingViewCollisionsType.OCCURING))
        assertEquals(
            FloatingViewTouchType.TOUCH_MOVE to FloatingViewCollisionsType.OCCURING,
            dragView.sfCollisionStateFlow.value
        )

        assertTrue(dragView.updateCollisionState(FloatingViewTouchType.TOUCH_UP, FloatingViewCollisionsType.UNCOLLISIONS))
        assertEquals(
            FloatingViewTouchType.TOUCH_UP to FloatingViewCollisionsType.UNCOLLISIONS,
            dragView.sfCollisionStateFlow.value
        )
    }
}