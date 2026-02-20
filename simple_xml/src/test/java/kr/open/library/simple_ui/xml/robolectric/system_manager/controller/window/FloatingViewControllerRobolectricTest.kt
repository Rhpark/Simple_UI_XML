package kr.open.library.simple_ui.xml.robolectric.system_manager.controller.window

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.xml.system_manager.controller.window.FloatingViewController
import kr.open.library.simple_ui.xml.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.xml.system_manager.controller.window.drag.FloatingDragViewConfig
import kr.open.library.simple_ui.xml.system_manager.controller.window.fixed.FloatingFixedView
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FloatingViewControllerRobolectricTest {
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.window.decorView.layout(0, 0, 1080, 1920)
    }

    @Test
    fun `addFloatingDragView fails then drag config is not registered`() {
        val controller = createController()
        val view = View(activity)
        FrameLayout(activity).addView(view)
        val dragView = FloatingDragView(view, 0, 0)

        val added = controller.addFloatingDragView(dragView)
        val removed = controller.removeFloatingDragView(dragView)

        assertFalse(added)
        assertFalse(removed)
    }

    @Test
    fun `setFloatingFixedView fails then fixed reference remains null`() {
        val controller = createController()
        val view = View(activity)
        FrameLayout(activity).addView(view)
        val fixedView = FloatingFixedView(view, 0, 0)

        val result = controller.setFloatingFixedView(fixedView)

        assertFalse(result)
        assertNull(controller.getFloatingFixedView())
    }

    @Test
    fun `removeFloatingFixedView fails then fixed reference is kept`() {
        val controller = createController()
        val fixedView = FloatingFixedView(View(activity), 0, 0)
        setPrivateField(controller, "floatingFixedView", fixedView)

        val removed = controller.removeFloatingFixedView()

        assertFalse(removed)
        assertSame(fixedView, controller.getFloatingFixedView())
    }

    @Test
    fun `removeFloatingDragView fails then config remains in list`() {
        val controller = createController()
        val dragView = FloatingDragView(View(activity), 0, 0)
        val config = FloatingDragViewConfig(dragView)
        setPrivateField(controller, "floatingDragViewInfoList", mutableListOf(config))

        val removed = controller.removeFloatingDragView(dragView)

        assertFalse(removed)
        val listAfter = getPrivateField(controller, "floatingDragViewInfoList") as List<*>
        assertSame(config, listAfter.firstOrNull())
    }

    @Test
    fun `removeAllFloatingView fails when drag remove fails and keeps state`() {
        val controller = createController()
        val dragView = FloatingDragView(View(activity), 0, 0)
        val config = FloatingDragViewConfig(dragView)
        setPrivateField(controller, "floatingDragViewInfoList", mutableListOf(config))

        val removedAll = controller.removeAllFloatingView()

        assertFalse(removedAll)
        val listAfter = getPrivateField(controller, "floatingDragViewInfoList") as List<*>
        assertSame(config, listAfter.firstOrNull())
    }

    private fun createController(): FloatingViewController {
        val controller = FloatingViewController(ApplicationProvider.getApplicationContext())
        forcePermissionGranted(controller)
        return controller
    }

    private fun forcePermissionGranted(controller: FloatingViewController) {
        val remainPermissionsField = BaseSystemService::class.java.getDeclaredField("remainPermissions")
        remainPermissionsField.isAccessible = true
        remainPermissionsField.set(controller, emptyList<String>())
    }

    private fun setPrivateField(
        target: Any,
        fieldName: String,
        value: Any?,
    ) {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    private fun getPrivateField(
        target: Any,
        fieldName: String,
    ): Any? {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(target)
    }
}
