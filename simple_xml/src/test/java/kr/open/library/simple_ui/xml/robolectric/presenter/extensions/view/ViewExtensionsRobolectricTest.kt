package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.view.forEachChild
import kr.open.library.simple_ui.xml.extensions.view.setGone
import kr.open.library.simple_ui.xml.extensions.view.setHeight
import kr.open.library.simple_ui.xml.extensions.view.setHeightMatchParent
import kr.open.library.simple_ui.xml.extensions.view.setHeightWrapContent
import kr.open.library.simple_ui.xml.extensions.view.setInvisible
import kr.open.library.simple_ui.xml.extensions.view.setMargin
import kr.open.library.simple_ui.xml.extensions.view.setMargins
import kr.open.library.simple_ui.xml.extensions.view.setOnDebouncedClickListener
import kr.open.library.simple_ui.xml.extensions.view.setPadding
import kr.open.library.simple_ui.xml.extensions.view.setSize
import kr.open.library.simple_ui.xml.extensions.view.setVisible
import kr.open.library.simple_ui.xml.extensions.view.setWidth
import kr.open.library.simple_ui.xml.extensions.view.setWidthMatchParent
import kr.open.library.simple_ui.xml.extensions.view.setWidthWrapContent
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ViewExtensionsRobolectricTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    // View 가시성 관련 확장 함수들이 예상대로 상태를 변경하는지 확인
    @Test
    fun setVisibleAndGoneUpdatesVisibilityState() {
        val view = View(context)

        view.setVisible()
        assertEquals(View.VISIBLE, view.visibility)

        view.setGone()
        assertEquals(View.GONE, view.visibility)

        view.setInvisible()
        assertEquals(View.INVISIBLE, view.visibility)
    }

    @Test
    fun setVisibleWhenAlreadyVisibleLeavesStateUnchanged() {
        val view = View(context)
        view.visibility = View.VISIBLE

        view.setVisible()

        assertEquals(View.VISIBLE, view.visibility)
    }

    @Test
    fun setVisibleFromGoneUpdatesToVisible() {
        val view = View(context)
        view.visibility = View.GONE

        view.setVisible()

        assertEquals(View.VISIBLE, view.visibility)
    }

    @Test
    fun setGoneWhenAlreadyGoneLeavesStateUnchanged() {
        val view = View(context)
        view.visibility = View.GONE

        view.setGone()

        assertEquals(View.GONE, view.visibility)
    }

    @Test
    fun setInvisibleWhenAlreadyInvisibleLeavesStateUnchanged() {
        val view = View(context)
        view.visibility = View.INVISIBLE

        view.setInvisible()

        assertEquals(View.INVISIBLE, view.visibility)
    }

    // 디바운스 클릭 리스너가 빠른 중복 클릭을 막는지 검증
    @Test
    fun setOnDebouncedClickListenerBlocksRapidClicks() {
        // Robolectric의 SystemClock은 0부터 시작하므로, 초기 시간을 앞으로 진행
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000))

        val view = View(context)
        var clickCount = 0

        view.setOnDebouncedClickListener(debounceTime = 500L) {
            clickCount += 1
        }

        view.performClick()
        // Advance less than debounce window - click should be ignored
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
        view.performClick()

        assertEquals(1, clickCount)

        // Advance beyond debounce window - click should be accepted
        ShadowSystemClock.advanceBy(Duration.ofMillis(500))
        view.performClick()

        assertEquals(2, clickCount)
    }

    @Test
    fun setOnDebouncedClickListenerUsesDefaultDebounceTime() {
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000))

        val view = View(context)
        var clickCount = 0

        view.setOnDebouncedClickListener {
            clickCount += 1
        }

        view.performClick()
        ShadowSystemClock.advanceBy(Duration.ofMillis(100))
        view.performClick()
        assertEquals(1, clickCount)

        ShadowSystemClock.advanceBy(Duration.ofMillis(600))
        view.performClick()
        assertEquals(2, clickCount)
    }

    // forEachChild 확장 함수가 모든 자식 View를 순회하는지 확인
    @Test
    fun forEachChildVisitsAllChildren() {
        val layout = LinearLayout(context)
        val childA = View(context)
        val childB = View(context)
        val childC = View(context)
        layout.addView(childA)
        layout.addView(childB)
        layout.addView(childC)

        val visited = mutableSetOf<View>()
        layout.forEachChild { visited += it }

        assertEquals(setOf(childA, childB, childC), visited)
    }

    @Test
    fun forEachChildOnEmptyViewGroupDoesNothing() {
        val layout = LinearLayout(context)
        var invoked = false

        layout.forEachChild { invoked = true }

        assertEquals(false, invoked)
    }

    // setMargins 확장 함수가 LayoutParams의 마진을 정상적으로 반영하는지 검증
    @Test
    fun setMarginsUpdatesLayoutParams() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

        view.setMargins(left = 12, top = 8, right = 4, bottom = 16)

        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        assertEquals(12, params.leftMargin)
        assertEquals(8, params.topMargin)
        assertEquals(4, params.rightMargin)
        assertEquals(16, params.bottomMargin)
    }

    // setMargin 확장 함수로 네 방향 마진이 동일하게 설정되는지 확인
    @Test
    fun setMarginAppliesUniformMargins() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

        view.setMargin(20)

        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        assertEquals(20, params.leftMargin)
        assertEquals(20, params.topMargin)
        assertEquals(20, params.rightMargin)
        assertEquals(20, params.bottomMargin)
    }

    @Test
    fun setMarginsWithoutLayoutParamsLeavesLayoutParamsNull() {
        val view = View(context)

        view.setMargins(left = 10, top = 20, right = 30, bottom = 40)

        assertEquals(null, view.layoutParams)
    }

    // setPadding 확장 함수로 네 방향 패딩이 동일하게 적용되는지 확인
    @Test
    fun setPaddingAppliesUniformPadding() {
        val view = View(context)
        view.setPadding(24)

        assertEquals(24, view.paddingLeft)
        assertEquals(24, view.paddingTop)
        assertEquals(24, view.paddingRight)
        assertEquals(24, view.paddingBottom)
    }

    // setWidth 확장 함수가 LayoutParams.width를 변경하는지 확인
    @Test
    fun setWidthUpdatesLayoutParams() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

        view.setWidth(180)

        assertEquals(180, view.layoutParams.width)
    }

    @Test
    fun setWidthWithoutLayoutParamsKeepsLayoutParamsNull() {
        val view = View(context)

        view.setWidth(200)

        assertEquals(null, view.layoutParams)
    }

    // setHeight 확장 함수가 LayoutParams.height를 변경하는지 확인
    @Test
    fun setHeightUpdatesLayoutParams() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

        view.setHeight(220)

        assertEquals(220, view.layoutParams.height)
    }

    @Test
    fun setHeightWithoutLayoutParamsKeepsLayoutParamsNull() {
        val view = View(context)

        view.setHeight(220)

        assertEquals(null, view.layoutParams)
    }

    // setSize 확장 함수가 폭과 높이를 동시에 설정하는지 확인
    @Test
    fun setSizeUpdatesWidthAndHeightTogether() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

        view.setSize(width = 160, height = 140)

        assertEquals(160, view.layoutParams.width)
        assertEquals(140, view.layoutParams.height)
    }

    @Test
    fun setSizeWithoutLayoutParamsKeepsLayoutParamsNull() {
        val view = View(context)

        view.setSize(width = 160, height = 140)

        assertEquals(null, view.layoutParams)
    }

    // setWidthMatchParent 확장 함수가 MATCH_PARENT로 설정되는지 확인
    @Test
    fun setWidthMatchParentAppliesMatchParent() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

        view.setWidthMatchParent()

        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, view.layoutParams.width)
    }

    // setHeightMatchParent 확장 함수가 MATCH_PARENT로 설정되는지 확인
    @Test
    fun setHeightMatchParentAppliesMatchParent() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )

        view.setHeightMatchParent()

        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, view.layoutParams.height)
    }

    // setWidthWrapContent 확장 함수가 WRAP_CONTENT로 설정되는지 확인
    @Test
    fun setWidthWrapContentAppliesWrapContent() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

        view.setWidthWrapContent()

        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, view.layoutParams.width)
    }

    // setHeightWrapContent 확장 함수가 WRAP_CONTENT로 설정되는지 확인
    @Test
    fun setHeightWrapContentAppliesWrapContent() {
        val view = View(context)
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

        view.setHeightWrapContent()

        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, view.layoutParams.height)
    }
}
