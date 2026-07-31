package kr.open.library.simple_ui.xml.unit.ui.adapter.normal.result

import kr.open.library.simple_ui.xml.ui.adapter.normal.result.NormalAdapterResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NormalAdapterResultTest {
    @Test
    fun `fold invokes applied branch only`() {
        var invokedBranch = ""

        NormalAdapterResult.Applied.fold(
            onApplied = { invokedBranch = "applied" },
            onRejected = { invokedBranch = "rejected" },
        )

        assertEquals("applied", invokedBranch)
    }

    @Test
    fun `fold passes rejected result to rejected branch`() {
        val expected = NormalAdapterResult.Rejected.InvalidPosition
        var actual: NormalAdapterResult.Rejected? = null

        expected.fold(
            onApplied = {},
            onRejected = { actual = it },
        )

        assertSame(expected, actual)
    }
}
