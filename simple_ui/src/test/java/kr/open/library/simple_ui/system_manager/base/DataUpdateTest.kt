package kr.open.library.simple_ui.system_manager.base

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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

        dataUpdate.update("initial") // no emission expected
        val next = async {
            dataUpdate.state.drop(1).first()
        }

        dataUpdate.update("next")

        assertEquals("next", next.await())
    }
}
