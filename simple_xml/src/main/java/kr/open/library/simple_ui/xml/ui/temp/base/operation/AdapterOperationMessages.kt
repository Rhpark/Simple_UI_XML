package kr.open.library.simple_ui.xml.ui.temp.base.operation

/**
 * Centralized error messages for adapter operations.<br><br>
 * 어댑터 연산용 에러 메시지를 중앙 관리합니다.<br>
 */
internal object AdapterOperationMessages {
    /**
     * Message for invalid insert position.<br><br>
     * 유효하지 않은 삽입 위치 메시지입니다.<br>
     */
    fun invalidInsertPosition(position: Int, listSize: Int): String =
        "Cannot add item at position $position. Valid range: 0..$listSize"

    /**
     * Message for invalid access position.<br><br>
     * 유효하지 않은 접근 위치 메시지입니다.<br>
     */
    fun invalidAccessPosition(position: Int, listSize: Int): String =
        "Cannot access item at position $position. Valid range: 0 until $listSize"

    /**
     * Message for invalid remove position.<br><br>
     * 유효하지 않은 제거 위치 메시지입니다.<br>
     */
    fun invalidRemovePosition(position: Int, listSize: Int): String =
        "Cannot remove item at position $position. Valid range: 0 until $listSize"

    /**
     * Message for invalid replace position.<br><br>
     * 유효하지 않은 교체 위치 메시지입니다.<br>
     */
    fun invalidReplacePosition(position: Int, listSize: Int): String =
        "Cannot replace item at position $position. Valid range: 0 until $listSize"

    /**
     * Message for invalid move positions.<br><br>
     * 유효하지 않은 이동 위치 메시지입니다.<br>
     */
    fun invalidMovePosition(listSize: Int): String =
        "Cannot move item. Valid range: 0 until $listSize"

    /**
     * Message for item not found.<br><br>
     * 아이템을 찾을 수 없음 메시지입니다.<br>
     */
    fun itemNotFound(): String =
        "Item not found in the list"
}
