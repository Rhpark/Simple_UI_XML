package kr.open.library.simpleui_xml.deviceverification.xml

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import kr.open.library.simpleui_xml.recyclerview.new_.RecyclerViewActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RecyclerViewIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun xmlP0005_recyclerDataMutationsAndScrollReachPhysicalView() {
        ActivityScenario.launch(RecyclerViewActivity::class.java).use { scenario ->
            var initialCount = 0
            scenario.onActivity { activity ->
                val recycler = activity.findViewById<RecyclerView>(R.id.rcvItems)
                initialCount = recycler.adapter?.itemCount ?: 0
                assertTrue(initialCount > 0)
                activity.findViewById<android.view.View>(R.id.btnAddItem).performClick()
            }
            awaitItemCount(scenario, initialCount + 1)
            scenario.onActivity { activity ->
                val recycler = activity.findViewById<RecyclerView>(R.id.rcvItems)
                assertEquals(initialCount + 1, recycler.adapter?.itemCount)
                recycler.scrollToPosition(initialCount)
            }
            awaitLastVisiblePosition(scenario, initialCount)
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.btnClearItems).performClick()
            }
            awaitItemCount(scenario, 0)
            scenario.onActivity { activity ->
                assertEquals(0, activity.findViewById<RecyclerView>(R.id.rcvItems).adapter?.itemCount)
            }
        }
    }

    private fun awaitItemCount(
        scenario: ActivityScenario<RecyclerViewActivity>,
        expectedCount: Int,
    ) {
        repeat(MAX_RETRIES) {
            var actualCount = RecyclerView.NO_POSITION
            scenario.onActivity { activity ->
                actualCount = activity.findViewById<RecyclerView>(R.id.rcvItems).adapter?.itemCount
                    ?: RecyclerView.NO_POSITION
            }
            if (actualCount == expectedCount) return
            Thread.sleep(RETRY_INTERVAL_MILLIS)
        }
        fail("RecyclerView itemCount가 제한 시간 안에 $expectedCount(으)로 변경되지 않았습니다.")
    }

    private fun awaitLastVisiblePosition(
        scenario: ActivityScenario<RecyclerViewActivity>,
        expectedMinimum: Int,
    ) {
        repeat(MAX_RETRIES) {
            var lastVisiblePosition = RecyclerView.NO_POSITION
            scenario.onActivity { activity ->
                val recycler = activity.findViewById<RecyclerView>(R.id.rcvItems)
                lastVisiblePosition =
                    (recycler.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }
            if (lastVisiblePosition >= expectedMinimum) return
            Thread.sleep(RETRY_INTERVAL_MILLIS)
        }
        fail("RecyclerView가 제한 시간 안에 $expectedMinimum 위치까지 스크롤되지 않았습니다.")
    }

    private companion object {
        const val MAX_RETRIES = 100
        const val RETRY_INTERVAL_MILLIS = 50L
    }
}
