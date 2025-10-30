package kr.open.library.simple_ui.unit.system_manager.base

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kr.open.library.simple_ui.system_manager.base.DataUpdate
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test


class DataUpdateTest {

    @Test
    fun update_changesCurrentValue() {
        val dataUpdate = DataUpdate(initialValue = 0)

        dataUpdate.update(5)

        assertEquals(5, dataUpdate.currentValue)
    }

    @Test
    fun state_emitsOnlyWhenValueChanges() = runBlocking {
        val dataUpdate = DataUpdate(initialValue = "initial")

        dataUpdate.update("initial") // 같은 값이므로 emit 안 됨

        // 다음 emit된 값을 비동기로 수집
        val nextValue = async(start = CoroutineStart.LAZY) {
            dataUpdate.state.drop(1).first()
        }

        nextValue.start()
        yield() // async 코루틴이 대기 상태로 진입할 시간 제공

        dataUpdate.update("next") // 값이 변경되어 emit됨

        assertEquals("next", nextValue.await())
    }
}
