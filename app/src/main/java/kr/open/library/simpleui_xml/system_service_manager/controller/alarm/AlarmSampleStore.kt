package kr.open.library.simpleui_xml.system_service_manager.controller.alarm

import kr.open.library.simple_ui.system_manager.core.controller.alarm.vo.AlarmData

/**
 * 예제용 알람 저장소입니다.
 * 실제 서비스에서는 DB/파일/Preference 등 영속 저장소로 대체하세요.
 */
public object AlarmSampleStore {
    private val alarmMap: MutableMap<Int, AlarmData> = LinkedHashMap()

    public fun put(alarmData: AlarmData) {
        alarmMap[alarmData.key] = alarmData
    }

    public fun get(key: Int): AlarmData? = alarmMap[key]

    public fun getAll(): List<AlarmData> = alarmMap.values.toList()

    public fun remove(key: Int): Boolean = alarmMap.remove(key) != null

    public fun exists(key: Int): Boolean = alarmMap.containsKey(key)

    public fun clear() {
        alarmMap.clear()
    }
}
