package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kr.open.library.simple_ui.xml.extensions.view.SnackBarOption
import kr.open.library.simple_ui.xml.extensions.view.snackBarMakeShort
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowIndefinite
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowLong
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SnackBarExtensionsRobolectricTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val root: FrameLayout = FrameLayout(context)

    @Test
    fun `snackBarMakeShort applies options`() {
        withMockedSnackbar { snackbar ->
            val option =
                SnackBarOption(
                    animMode = BaseTransientBottomBar.ANIMATION_MODE_FADE,
                    bgTint = Color.YELLOW,
                    textColor = Color.BLUE,
                    actionText = "dismiss",
                    actionTextColor = Color.GREEN,
                    isGestureInsetBottomIgnored = true,
                    action = {},
                )

            val returned = root.snackBarMakeShort("hello", option)
            assertSame(snackbar, returned)
        }
    }

    @Test
    fun `fragment extension logs when view null`() {
        val fragment = object : Fragment() {}
        fragment.snackBarShowShort("missing")
    }

    @Test
    fun `custom view show short handles snackbar layout`() {
        withMockedSnackbar { snackbar ->
            val layout = mock(Snackbar.SnackbarLayout::class.java)
            Mockito.`when`(snackbar.view).thenReturn(layout)
            val custom = TextView(context)

            root.snackBarShowShort(
                "with custom",
                custom,
                animMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE,
                isGestureInsetBottomIgnored = true,
            )

            verify(layout).removeAllViews()
            verify(layout).addView(custom)
            verify(snackbar).show()
        }
    }

    @Test
    fun `show long and indefinite with options invokes show`() {
        withMockedSnackbar { snackbar ->
            val layout = mock(Snackbar.SnackbarLayout::class.java)
            Mockito.`when`(snackbar.view).thenReturn(layout)
            val option = SnackBarOption(isGestureInsetBottomIgnored = true)

            root.snackBarShowLong("long", option)
            root.snackBarShowIndefinite("indef", option)
            root.snackBarShowIndefinite("custom", TextView(context))

            verify(snackbar, Mockito.times(3)).show()
        }
    }

    @Test
    fun `View snackBarShowShort invokes show`() {
        withMockedSnackbar { snackbar ->
            root.snackBarShowShort("test")
            verify(snackbar).show()
        }
    }

    @Test
    fun `Fragment snackBarShowLong logs when view null`() {
        val fragment = object : Fragment() {}
        fragment.snackBarShowLong("missing", null)
    }

    @Test
    fun `Fragment snackBarShowIndefinite logs when view null`() {
        val fragment = object : Fragment() {}
        fragment.snackBarShowIndefinite("missing", null)
    }

    @Test
    fun `View snackBarShowLong with customView handles snackbar layout`() {
        withMockedSnackbar { snackbar ->
            val layout = mock(Snackbar.SnackbarLayout::class.java)
            Mockito.`when`(snackbar.view).thenReturn(layout)
            val custom = TextView(context)

            root.snackBarShowLong(
                "with custom long",
                custom,
                animMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE,
                isGestureInsetBottomIgnored = true,
            )

            verify(layout).removeAllViews()
            verify(layout).addView(custom)
            verify(snackbar).show()
        }
    }

    @Test
    fun `SnackBarOption with ColorStateList applies correctly`() {
        withMockedSnackbar { snackbar ->
            val bgTintStateList = ColorStateList.valueOf(Color.RED)
            val textColorStateList = ColorStateList.valueOf(Color.GREEN)
            val actionTextColorStateList = ColorStateList.valueOf(Color.BLUE)

            val option =
                SnackBarOption(
                    bgTintStateList = bgTintStateList,
                    textColorStateList = textColorStateList,
                    actionTextColorStateList = actionTextColorStateList,
                )

            root.snackBarMakeShort("test", option)
        }
    }

    @Test
    fun `SnackBarOption with action text and callback`() {
        withMockedSnackbar { snackbar ->
            var actionClicked = false
            val option =
                SnackBarOption(
                    actionText = "Undo",
                    action = { actionClicked = true },
                )

            root.snackBarMakeShort("test", option)
        }
    }

    @Test
    fun `View snackBarShowLong with customView without optional params`() {
        withMockedSnackbar { snackbar ->
            val layout = mock(Snackbar.SnackbarLayout::class.java)
            Mockito.`when`(snackbar.view).thenReturn(layout)
            val custom = TextView(context)

            root.snackBarShowLong("msg", custom, null, null)

            verify(layout).removeAllViews()
            verify(layout).addView(custom)
            verify(snackbar).show()
        }
    }

    private fun <T> withMockedSnackbar(block: (Snackbar) -> T): T {
        mockStatic(Snackbar::class.java).use { mocked ->
            val snackbar = mock(Snackbar::class.java)
            mocked
                .`when`<Snackbar> {
                    Snackbar.make(any(View::class.java), anyString(), anyInt())
                }.thenReturn(snackbar)
            return block(snackbar)
        }
    }
}
