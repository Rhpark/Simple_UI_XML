package kr.open.library.simple_ui.core.unit.system_manager.info.network.connectivity.data

import kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data.NetworkBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkBaseTest {

    private class TestNetworkBase(res: String) : NetworkBase(res) {
        fun splitWithDelimiter(start: String, end: String, delimiter: String) = splitStr(start, end, delimiter)
        fun splitSingle(start: String, end: String) = splitStr(start, end)
        fun containsText(text: String) = isContains(text)
        fun exposedRes() = getResStr()
        fun useInnerSplit(target: String, start: String, end: String): String? = with(target) { split(start, end) }
    }

    @Test
    fun splitStr_withDelimiter_returnsList() {
        val base = TestNetworkBase("prefixSTARTvalue1|value2ENDsuffix")

        val result = base.splitWithDelimiter("START", "END", "|")

        assertEquals(listOf("value1", "value2"), result)
    }

    @Test
    fun splitStr_withoutDelimiter_returnsSubstring() {
        val base = TestNetworkBase("prefixSTARTvalue1,value2ENDsuffix")

        val result = base.splitSingle("START", "END")

        assertEquals("value1,value2", result)
    }

    @Test
    fun splitStr_returnsNullWhenMarkersMissing() {
        val base = TestNetworkBase("prefix value1 value2 suffix")

        assertNull(base.splitWithDelimiter("START", "END", ","))
        assertNull(base.splitSingle("START", "END"))
    }

    @Test
    fun innerSplit_extensionHandlesMissingStart() {
        val base = TestNetworkBase("irrelevant")

        val result = base.useInnerSplit("no markers here", "START", "END")

        assertNull(result)
    }

    @Test
    fun innerSplit_returnsNullWhenEndMissing() {
        val base = TestNetworkBase("irrelevant")

        val result = base.useInnerSplit("prefixSTARTvalueWithoutEnd", "START", "END")

        assertNull(result)
    }

    @Test
    fun innerSplit_returnsNullWhenOnlyStartMarkerExists() {
        val base = TestNetworkBase("irrelevant")

        // START 문자열만 있는 경우 split 후 size = 1인 경우 (Line 119 커버)
        // "START".split("START") → ["", ""] → size = 2 (아직 커버 안됨)
        // 실제로 size <= 1이 되려면 빈 문자열이어야 함
        val result = base.useInnerSplit("", "START", "END")

        assertNull("Should return null for empty string", result)
    }

    @Test
    fun innerSplit_handlesEmptyContentBetweenMarkers() {
        val base = TestNetworkBase("irrelevant")

        val result = base.useInnerSplit("prefixSTARTENDsuffix", "START", "END")

        assertEquals("Should return empty string for adjacent markers", "", result)
    }

    @Test
    fun innerSplit_handlesMultipleStartMarkers() {
        val base = TestNetworkBase("irrelevant")

        // 첫 번째 START부터 첫 번째 END까지 추출
        // "prefixSTARTfirstSTARTsecondEND" split "START" → ["prefix", "firstSTARTsecondEND"]
        // "firstSTARTsecondEND" split "END" → ["firstSTARTsecond", ""]
        val result = base.useInnerSplit("prefixSTARTfirstENDSTARTsecondEND", "START", "END")

        assertEquals("Should extract from first START to first END", "first", result)
    }

    @Test
    fun innerSplit_handlesSpecialCharactersInContent() {
        val base = TestNetworkBase("irrelevant")

        val result = base.useInnerSplit("START!@#$%^&*()END", "START", "END")

        assertEquals("Should handle special characters", "!@#$%^&*()", result)
    }

    @Test
    fun isContains_detectsSubstring() {
        val base = TestNetworkBase("network status: CONNECTED")

        assertTrue(base.containsText("CONNECTED"))
        assertFalse(base.containsText("DISCONNECTED"))
    }

    @Test
    fun getResStr_returnsOriginalString() {
        val raw = "raw-network-response"
        val base = TestNetworkBase(raw)

        assertEquals(raw, base.exposedRes())
    }

    @Test
    fun splitStr_withoutDelimiter_returnsNullWhenOnlyStartExists() {
        val base = TestNetworkBase("prefixSTART")

        val result = base.splitSingle("START", "END")

        // "prefixSTART"에서 START 이후가 빈 문자열이고, END가 없으므로 null 반환
        assertNull("Should return null when END marker is missing", result)
    }
}
