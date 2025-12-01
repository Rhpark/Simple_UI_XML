package kr.open.library.simple_ui.core.logcat.config

import android.util.Log
import kr.open.library.simple_ui.core.logcat.model.LogxType
import java.util.EnumSet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe configuration manager for Logx logging system.<br>
 * Manages configuration updates and notifies registered listeners when configuration changes.<br><br>
 * Logx 로깅 시스템의 스레드 안전 설정 관리자입니다.<br>
 * 설정 업데이트를 관리하고 설정 변경 시 등록된 리스너에게 알립니다.<br>
 *
 * Thread-safety: All public methods are thread-safe using ReentrantReadWriteLock.<br>
 * - Read operations (config access) use read lock for concurrent access<br>
 * - Write operations (config updates, listener management) use write lock for exclusive access<br><br>
 * 스레드 안전성: 모든 public 메서드는 ReentrantReadWriteLock을 사용하여 스레드 안전합니다.<br>
 * - 읽기 작업 (config 접근)은 동시 접근을 위해 읽기 잠금 사용<br>
 * - 쓰기 작업 (config 업데이트, 리스너 관리)은 배타적 접근을 위해 쓰기 잠금 사용<br>
 *
 * @param initialConfig The initial configuration to use. Defaults to a default LogxConfig instance.<br><br>
 *                      사용할 초기 설정. 기본값은 기본 LogxConfig 인스턴스입니다.
 */
class LogxConfigManager(
    initialConfig: LogxConfig = LogxConfig(),
) {
    private val lock = ReentrantReadWriteLock()
    private var _config = initialConfig

    /**
     * Returns the current configuration in a thread-safe manner.<br>
     * Uses read lock to allow concurrent access from multiple threads.<br><br>
     * 스레드 안전 방식으로 현재 설정을 반환합니다.<br>
     * 여러 스레드의 동시 접근을 허용하기 위해 읽기 잠금을 사용합니다.<br>
     */
    val config: LogxConfig
        get() = lock.read { _config }

    /**
     * Callback interface for receiving configuration change notifications.<br><br>
     * 설정 변경 알림을 받기 위한 콜백 인터페이스입니다.<br>
     */
    interface ConfigChangeListener {
        /**
         * Called when the configuration has been updated.<br><br>
         * 설정이 업데이트되었을 때 호출됩니다.<br>
         *
         * @param newConfig The new configuration that was applied.<br><br>
         *                  적용된 새로운 설정.
         */
        fun onConfigChanged(newConfig: LogxConfig)
    }

    private val listeners = mutableSetOf<ConfigChangeListener>()

    /**
     * Registers a configuration change listener in a thread-safe manner.<br><br>
     * 스레드 안전 방식으로 설정 변경 리스너를 등록합니다.<br>
     *
     * @param listener The listener to register for configuration change notifications.<br><br>
     *                 설정 변경 알림을 받을 리스너.
     */
    fun addConfigChangeListener(listener: ConfigChangeListener) {
        lock.write {
            listeners.add(listener)
        }
    }

    /**
     * Removes a configuration change listener in a thread-safe manner.<br><br>
     * 스레드 안전 방식으로 설정 변경 리스너를 제거합니다.<br>
     *
     * @param listener The listener to remove from configuration change notifications.<br><br>
     *                 설정 변경 알림에서 제거할 리스너.
     */
    fun removeConfigChangeListener(listener: ConfigChangeListener) {
        lock.write {
            listeners.remove(listener)
        }
    }

    /**
     * Removes all configuration change listeners in a thread-safe manner.<br><br>
     * 스레드 안전 방식으로 모든 설정 변경 리스너를 제거합니다.<br>
     */
    fun removeAllConfigChangeListener() {
        lock.write {
            listeners.clear()
        }
    }

    /**
     * Updates the entire configuration and notifies all registered listeners.<br>
     * Creates a snapshot of listeners to avoid holding the lock during notifications.<br><br>
     * 전체 설정을 업데이트하고 등록된 모든 리스너에게 알립니다.<br>
     * 알림 중 잠금을 유지하지 않도록 리스너의 스냅샷을 생성합니다.<br>
     *
     * @param newConfig The new configuration to apply.<br><br>
     *                  적용할 새로운 설정.
     */
    fun updateConfig(newConfig: LogxConfig) {
        val snapshot =
            lock.write {
                _config = newConfig
                listeners.toList() // Create snapshot to avoid holding lock during notifications | 알림 중 잠금을 피하기 위한 스냅샷
            }

        snapshot.forEach { listener ->
            try {
                listener.onConfigChanged(newConfig)
            } catch (e: Exception) {
                Log.e("LogxConfigManager", "Error notifying listener", e)
            }
        }
    }

    /**
     * Updates the debug mode setting.<br><br>
     * 디버그 모드 설정을 업데이트합니다.<br>
     *
     * @param isDebug Whether to enable debug logging.<br><br>
     *                디버그 로깅을 활성화할지 여부.
     */
    fun setDebugMode(isDebug: Boolean) {
        updateConfig(_config.copy(isDebug = isDebug))
    }

    /**
     * Updates the debug filter setting.<br><br>
     * 디버그 필터 설정을 업데이트합니다.<br>
     *
     * @param isFilter Whether to enable tag-based filtering.<br><br>
     *                 태그 기반 필터링을 활성화할지 여부.
     */
    fun setDebugFilter(isFilter: Boolean) {
        updateConfig(_config.copy(isDebugFilter = isFilter))
    }

    /**
     * Updates the file saving setting.<br><br>
     * 파일 저장 설정을 업데이트합니다.<br>
     *
     * @param isSave Whether to save logs to a file.<br><br>
     *               로그를 파일에 저장할지 여부.
     */
    fun setSaveToFile(isSave: Boolean) {
        updateConfig(_config.copy(isDebugSave = isSave))
    }

    /**
     * Updates the log file path.<br><br>
     * 로그 파일 경로를 업데이트합니다.<br>
     *
     * @param path The absolute path where log files will be saved.<br><br>
     *             로그 파일이 저장될 절대 경로.
     */
    fun setFilePath(path: String) {
        updateConfig(_config.copy(saveFilePath = path))
    }

    /**
     * Updates the application name used in log file naming and organization.<br><br>
     * 로그 파일 이름 지정 및 구성에 사용되는 애플리케이션 이름을 업데이트합니다.<br>
     *
     * @param name The application name.<br><br>
     *             애플리케이션 이름.
     */
    fun setAppName(name: String) {
        updateConfig(_config.copy(appName = name))
    }

    /**
     * Updates the set of log types to display.<br><br>
     * 표시할 로그 타입 집합을 업데이트합니다.<br>
     *
     * @param types The EnumSet of log types to enable (VERBOSE, DEBUG, INFO, WARN, ERROR, etc.).<br><br>
     *              활성화할 로그 타입의 EnumSet (VERBOSE, DEBUG, INFO, WARN, ERROR 등).
     */
    fun setDebugLogTypeList(types: EnumSet<LogxType>) {
        updateConfig(_config.copy(debugLogTypeList = types))
    }

    /**
     * Updates the set of tag filters for debug logging.<br>
     * Only logs matching these tags will be displayed when debug filtering is enabled.<br><br>
     * 디버그 로깅을 위한 태그 필터 집합을 업데이트합니다.<br>
     * 디버그 필터링이 활성화된 경우 이 태그와 일치하는 로그만 표시됩니다.<br>
     *
     * @param tags The list of tag filters to apply.<br><br>
     *             적용할 태그 필터 목록.
     */
    fun setDebugFilterList(tags: List<String>) {
        updateConfig(_config.copy(debugFilterList = tags.toSet()))
    }

    private fun notifyListeners(newConfig: LogxConfig) {
        listeners.forEach { listener ->
            try {
                listener.onConfigChanged(newConfig)
            } catch (e: Exception) {
                Log.e("LogxConfigManager", "Error notifying config change listener", e)
            }
        }
    }
}
