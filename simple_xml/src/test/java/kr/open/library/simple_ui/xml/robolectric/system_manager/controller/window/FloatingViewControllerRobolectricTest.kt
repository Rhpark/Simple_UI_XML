package kr.open.library.simple_ui.xml.robolectric.system_manager.controller.window

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.xml.system_manager.controller.window.FloatingViewController
import kr.open.library.simple_ui.xml.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.xml.system_manager.controller.window.drag.FloatingDragViewConfig
import kr.open.library.simple_ui.xml.system_manager.controller.window.fixed.FloatingFixedView
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class FloatingViewControllerRobolectricTest {
    private lateinit var activity: Activity
    private lateinit var application: Application
    private var originalWindowService: Any? = null

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.window.decorView.layout(0, 0, 1080, 1920)
        application = ApplicationProvider.getApplicationContext()
        originalWindowService = application.getSystemService(Context.WINDOW_SERVICE)
    }

    @After
    fun tearDown() {
        Shadows.shadowOf(application).setSystemService(Context.WINDOW_SERVICE, originalWindowService)
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
    fun `setFloatingFixedView replaces old fixed view when add and remove succeed`() {
        val mockWindowManager = mock(WindowManager::class.java)
        val controller = createController(mockWindowManager)
        val oldView = FloatingFixedView(View(activity), 0, 0)
        val newView = FloatingFixedView(View(activity), 10, 10)
        setPrivateField(controller, "floatingFixedView", oldView)

        doNothing().`when`(mockWindowManager).addView(newView.view, newView.params)
        doNothing().`when`(mockWindowManager).removeView(oldView.view)

        val result = controller.setFloatingFixedView(newView)

        assertTrue(result)
        assertSame(newView, controller.getFloatingFixedView())
        verify(mockWindowManager).addView(newView.view, newView.params)
        verify(mockWindowManager).removeView(oldView.view)
    }

    @Test
    fun `setFloatingFixedView keeps old view when old remove fails and rollbacks new`() {
        val mockWindowManager = mock(WindowManager::class.java)
        val controller = createController(mockWindowManager)
        val oldView = FloatingFixedView(View(activity), 0, 0)
        val newView = FloatingFixedView(View(activity), 10, 10)
        setPrivateField(controller, "floatingFixedView", oldView)

        doNothing().`when`(mockWindowManager).addView(newView.view, newView.params)
        doThrow(RuntimeException("remove old fail")).`when`(mockWindowManager).removeView(oldView.view)
        doNothing().`when`(mockWindowManager).removeView(newView.view)

        val result = controller.setFloatingFixedView(newView)

        assertFalse(result)
        assertSame(oldView, controller.getFloatingFixedView())
        verify(mockWindowManager).addView(newView.view, newView.params)
        verify(mockWindowManager).removeView(oldView.view)
        verify(mockWindowManager).removeView(newView.view)
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

    private fun createController(windowManager: WindowManager? = null): FloatingViewController {
        windowManager?.let {
            Shadows.shadowOf(application).setSystemService(Context.WINDOW_SERVICE, it)
        }
        val controller = FloatingViewController(application)
        forcePermissionGranted(controller)
        return controller
    }

    private fun forcePermissionGranted(controller: FloatingViewController) {
        val requiredPermissionsField = BaseSystemService::class.java.getDeclaredField("requiredPermissions")
        requiredPermissionsField.isAccessible = true
        requiredPermissionsField.set(controller, null)

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
