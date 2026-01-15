package kr.open.library.simple_ui.xml.robolectric.ui.layout.constraint.normal

import android.content.Context
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import kr.open.library.simple_ui.xml.ui.layout.constraint.normal.BaseConstraintLayout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseConstraintLayoutRobolectricTest {
    @Test
    fun attachBindsAndDetachUnbindsLifecycleObserver() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = controller.get()
        val container = FrameLayout(activity)
        activity.setContentView(container)
        val view = TrackingConstraintLayout(activity)

        container.addView(view)
        view.layout(0, 0, 10, 10)
        view.viewTreeObserver.dispatchOnGlobalLayout()

        assertEquals(1, view.createCount)

        container.removeView(view)
        controller.pause().stop().destroy()
        assertEquals(0, view.destroyCount)
    }

    private class TrackingConstraintLayout(
        context: Context
    ) : BaseConstraintLayout(context) {
        var createCount = 0
        var destroyCount = 0

        override fun onCreate(owner: LifecycleOwner) {
            createCount++
        }

        override fun onDestroy(owner: LifecycleOwner) {
            destroyCount++
        }
    }
}
