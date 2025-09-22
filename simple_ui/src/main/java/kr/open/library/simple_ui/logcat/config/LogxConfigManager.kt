package kr.open.library.simple_ui.logcat.config


import kr.open.library.simple_ui.logcat.model.LogxType
import java.util.EnumSet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Logx 설정 관리를 담당하는 클래스
 */
class LogxConfigManager(initialConfig: LogxConfig = LogxConfig()) {
    
    private val lock = ReentrantReadWriteLock()
    private var _config = initialConfig
    
    /**
     * 현재 설정을 읽기 전용으로 반환
     */
    val config: LogxConfig
        get() = lock.read { _config }
    
    /**
     * 설정 변경 콜백 인터페이스
     */
    interface ConfigChangeListener {
        fun onConfigChanged(newConfig: LogxConfig)
    }
    
    private val listeners = mutableSetOf<ConfigChangeListener>()
    
    /**
     * 설정 변경 리스너 등록
     */
    fun addConfigChangeListener(listener: ConfigChangeListener) {
        lock.write {
            listeners.add(listener)
        }
    }
    
    /**
     * 설정 변경 리스너 제거
     */
    fun removeConfigChangeListener(listener: ConfigChangeListener) {
        lock.write {
            listeners.remove(listener)
        }
    }

    /**
     * 설정 변경 리스너 모두 제거
     */
    fun removeAllConfigChangeListener() {
        lock.write {
            listeners.clear()
        }
    }

    /**
     * 전체 설정을 업데이트
     */
    fun updateConfig(newConfig: LogxConfig) {
        lock.write {
            _config = newConfig
            notifyListeners(newConfig)
        }
    }
    
    /**
     * 디버그 모드 설정
     */
    fun setDebugMode(isDebug: Boolean) {
        updateConfig(_config.copy(isDebug = isDebug))
    }
    
    /**
     * 디버그 필터 설정
     */
    fun setDebugFilter(isFilter: Boolean) {
        updateConfig(_config.copy(isDebugFilter = isFilter))
    }
    
    /**
     * 파일 저장 설정
     */
    fun setSaveToFile(isSave: Boolean) {
        updateConfig(_config.copy(isDebugSave = isSave))
    }
    
    /**
     * 파일 경로 설정
     */
    fun setFilePath(path: String) {
        updateConfig(_config.copy(saveFilePath = path))
    }
    
    /**
     * 앱 이름 설정
     */
    fun setAppName(name: String) {
        updateConfig(_config.copy(appName = name))
    }
    
    /**
     * 디버그 로그 타입 목록 설정
     */
    fun setDebugLogTypeList(types: EnumSet<LogxType>) {
        updateConfig(_config.copy(debugLogTypeList = types))
    }
    
    /**
     * 디버그 필터 목록 설정
     */
    fun setDebugFilterList(tags: List<String>) {
        updateConfig(_config.copy(debugFilterList = tags.toSet()))
    }
    
    private fun notifyListeners(newConfig: LogxConfig) {
        listeners.forEach { listener ->
            try {
                listener.onConfigChanged(newConfig)
            } catch (e: Exception) {
                android.util.Log.e("LogxConfigManager", "Error notifying config change listener", e)
            }
        }
    }
}