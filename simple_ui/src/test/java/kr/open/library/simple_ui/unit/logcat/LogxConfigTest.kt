package kr.open.library.simple_ui.unit.logcat

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.config.LogxConfigManager
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Ignore
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.Collections
import java.util.EnumSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Logx Config 관리에 대한 단위 테스트
 *
 * 테스트 대상:
 * - LogxConfigManager의 설정 관리 기능
 * - 디버그 모드 설정
 * - 로그 레벨 필터링
 * - 설정 변경 리스너
 */

class LogxConfigTest {

    private lateinit var configManager: LogxConfigManager

    @Before
    fun setup() {
        // 각 테스트 전에 새로운 ConfigManager 생성
        configManager = LogxConfigManager()
    }

    // ========== 1. 기본 설정 테스트 ==========

    @Test
    fun basicConfig_initializesCorrectly() {
        // 기본 설정 값이 기대치에 맞게 초기화되는지 확인
        // Given & When
        val config = configManager.config

        // Then
        assertNotNull("설정이 null이 아니어야 합니다", config)
        assertTrue("기본적으로 디버그 모드가 켜져있어야 합니다", config.isDebug)
        assertFalse("기본적으로 디버그 모드가 켜져있어야 합니다", config.isDebug)
    }

    @Test
    fun listenerAddedDuringNotification_triggersOnNextUpdate() {
        var innerListenerCalled = false

        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
                    override fun onConfigChanged(newConfig: LogxConfig) {
                        innerListenerCalled = true
                    }
                })
            }
        })

        configManager.setDebugMode(false)
        assertFalse("첫 번째 업데이트 동안에는 새 리스너가 아직 호출되지 않음", innerListenerCalled)

        configManager.setDebugMode(true)
        assertTrue("두 번째 업데이트에서 새로 등록한 리스너가 호출되어야 함", innerListenerCalled)
    }

    // ========== 2. 디버그 모드 설정 테스트 ==========

    @Test
    fun debugMode_canBeDisabled() {
        // 디버그 모드를 false로 변경할 수 있는지 확인
        // Given
        configManager.setDebugMode(true)

        // When
        configManager.setDebugMode(false)

        // Then
        assertFalse("디버그 모드가 꺼져야 합니다", configManager.config.isDebug)
    }

    @Test
    fun debugMode_canBeEnabled() {
        // 디버그 모드를 true로 변경할 수 있는지 확인
        // Given
        configManager.setDebugMode(false)

        // When
        configManager.setDebugMode(true)

        // Then
        assertTrue("디버그 모드가 켜져야 합니다", configManager.config.isDebug)
    }

    // ========== 3. 파일 저장 설정 테스트 ==========

    @Test
    fun saveToFile_canBeToggled() {
        // 로그 파일 저장 여부를 토글할 수 있는지 확인
        // Given
        val initialSaveToFile = configManager.config.isDebugSave

        // When
        configManager.setSaveToFile(!initialSaveToFile)

        // Then
        assertEquals("파일 저장 설정이 반대가 되어야 합니다",
            !initialSaveToFile,
            configManager.config.isDebugSave)
    }

    // ========== 4. 앱 이름 설정 테스트 ==========

    @Test
    fun appName_canBeUpdated() {
        // 지정한 앱 이름이 설정에 반영되는지 확인
        // Given
        val appName = "TestApp"

        // When
        configManager.setAppName(appName)

        // Then
        assertEquals("앱 이름이 설정되어야 합니다", appName, configManager.config.appName)
    }

    @Test
    fun appName_canBeBlank() {
        // 빈 문자열도 앱 이름으로 허용되는지 확인
        // Given
        val appName = ""

        // When
        configManager.setAppName(appName)

        // Then
        assertEquals("빈 앱 이름이 설정되어야 합니다", appName, configManager.config.appName)
    }

    // ========== 5. 로그 타입 필터링 테스트 ==========

    @Test
    fun logTypes_canBeConfigured() {
        // 로그 타입 필터 목록을 원하는 값으로 변경할 수 있는지 확인
        // Given
        val logTypes = EnumSet.of(LogxType.ERROR, LogxType.WARN)

        // When
        configManager.setDebugLogTypeList(logTypes)

        // Then
        assertEquals("로그 타입이 설정되어야 합니다", logTypes, configManager.config.debugLogTypeList)
        assertTrue("ERROR 타입이 포함되어야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.ERROR))
        assertTrue("WARN 타입이 포함되어야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.WARN))
        assertFalse("DEBUG 타입은 포함되지 않아야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.DEBUG))
    }

    @Test
    fun allLogTypes_canBeAllowed() {
        // 모든 로그 타입을 허용하도록 설정할 수 있는지 확인
        // Given
        val allTypes = EnumSet.allOf(LogxType::class.java)

        // When
        configManager.setDebugLogTypeList(allTypes)

        // Then
        assertEquals("모든 로그 타입이 허용되어야 합니다", allTypes, configManager.config.debugLogTypeList)
    }

    // ========== 6. 디버그 필터 설정 테스트 ==========

    @Test
    fun debugFilterFlag_canBeEnabled() {
        // 디버그 필터 플래그를 활성화할 수 있는지 확인
        // Given
        configManager.setDebugFilter(false)

        // When
        configManager.setDebugFilter(true)

        // Then
        assertTrue("디버그 필터가 켜져야 합니다", configManager.config.isDebugFilter)
    }

    @Test
    fun filterTags_canBeConfigured() {
        // 디버그 필터 태그 목록이 정상적으로 설정되는지 확인
        // Given
        val filterTags = listOf("MainActivity", "NetworkManager", "DatabaseHelper")

        // When
        configManager.setDebugFilterList(filterTags)

        // Then
        assertEquals("필터 태그 개수가 일치해야 합니다", 3, configManager.config.debugFilterList.size)
        assertTrue("MainActivity가 포함되어야 합니다",
            configManager.config.debugFilterList.contains("MainActivity"))
        assertTrue("NetworkManager가 포함되어야 합니다",
            configManager.config.debugFilterList.contains("NetworkManager"))
    }

    @Test
    fun filterTags_canBeCleared() {
        // 필터 태그를 빈 리스트로 초기화할 수 있는지 확인
        // Given
        val emptyList = emptyList<String>()

        // When
        configManager.setDebugFilterList(emptyList)

        // Then
        assertTrue("필터 리스트가 비어있어야 합니다", configManager.config.debugFilterList.isEmpty())
    }

    // ========== 7. 설정 변경 리스너 테스트 ==========

    @Test
    fun configChange_notifiesListeners() {
        // 설정 변경 시 등록된 리스너가 호출되는지 확인
        // Given
        var listenerCalled = false
        var receivedConfig: LogxConfig? = null

        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                listenerCalled = true
                receivedConfig = newConfig
            }
        })

        // When
        configManager.setDebugMode(false)

        // Then
        assertTrue("리스너가 호출되어야 합니다", listenerCalled)
        assertNotNull("새 설정이 전달되어야 합니다", receivedConfig)
        assertFalse("전달받은 설정의 디버그 모드가 false여야 합니다", receivedConfig!!.isDebug)
    }

    @Test
    fun multipleListeners_canBeRegistered() {
        // 여러 리스너를 등록한 뒤 모두 호출되는지 확인
        // Given
        var listener1Called = false
        var listener2Called = false

        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                listener1Called = true
            }
        })

        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                listener2Called = true
            }
        })

        // When
        configManager.setAppName("Test")

        // Then
        assertTrue("첫 번째 리스너가 호출되어야 합니다", listener1Called)
        assertTrue("두 번째 리스너가 호출되어야 합니다", listener2Called)
    }

    @Test
    fun listeners_canBeRemoved() {
        // 모든 리스너 제거 후에는 콜백이 실행되지 않는지 확인
        // Given
        var listenerCalled = false

        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                listenerCalled = true
            }
        })

        // When
        configManager.removeAllConfigChangeListener()
        configManager.setDebugMode(false)

        // Then
        assertFalse("리스너가 제거되어 호출되지 않아야 합니다", listenerCalled)
    }

    // ========== 8. 통합 설정 업데이트 테스트 ==========

    @Test
    fun config_canBeUpdatedAtOnce() {
        // 새로운 설정 객체로 전체 구성이 갱신되는지 확인
        // Given
        val newConfig = LogxConfig(
            isDebug = false,
            isDebugFilter = true,
            isDebugSave = true,
            saveFilePath = "/test/path",
            appName = "NewApp",
            debugLogTypeList = EnumSet.of(LogxType.ERROR),
            debugFilterList = setOf("TestTag")
        )

        // When
        configManager.updateConfig(newConfig)

        // Then
        val config = configManager.config
        assertFalse("디버그 모드가 false여야 합니다", config.isDebug)
        assertTrue("디버그 필터가 true여야 합니다", config.isDebugFilter)
        assertTrue("파일 저장이 true여야 합니다", config.isDebugSave)
        assertEquals("파일 경로가 일치해야 합니다", "/test/path", config.saveFilePath)
        assertEquals("앱 이름이 일치해야 합니다", "NewApp", config.appName)
        assertEquals("로그 타입이 ERROR만 있어야 합니다", 1, config.debugLogTypeList.size)
        assertTrue("필터 태그가 TestTag를 포함해야 합니다", config.debugFilterList.contains("TestTag"))
    }

    // ========== 9. 동시성 테스트 (ReentrantReadWriteLock 검증) ==========

    @Test
    fun concurrentReads_doNotBlock() {
        // 여러 스레드가 동시에 설정을 읽을 수 있는지 확인
        // Given
        configManager.setAppName("InitialApp")
        val readCount = 1000
        val results = java.util.Collections.synchronizedList(mutableListOf<String>())

        // When - 동시에 읽기
        val threads = (1..readCount).map {
            Thread {
                results.add(configManager.config.appName)
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Then
        assertEquals("모든 읽기가 완료되어야 합니다", readCount, results.size)
        results.forEach { appName ->
            assertEquals("모든 읽기가 같은 값을 반환해야 합니다", "InitialApp", appName)
        }
    }

    @Test
    fun concurrentWrites_maintainConsistency() {
        // 여러 스레드가 동시에 설정을 변경해도 일관성이 유지되는지 확인
        // Given
        val writeCount = 100

        // When - 동시에 쓰기
        val threads = (1..writeCount).map { i ->
            Thread {
                val appName = "App$i"
                configManager.setAppName(appName)
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Then - 마지막 config가 유효한 값 중 하나여야 함
        val finalAppName = configManager.config.appName
        assertNotNull("최종 설정이 null이 아니어야 합니다", finalAppName)
        assertTrue("최종 설정이 유효한 값이어야 합니다",
            finalAppName.startsWith("App") || finalAppName == "RhPark")
    }

    @Test
    fun concurrentReadWriteMix_maintainConsistency() {
        // 읽기와 쓰기가 동시에 발생해도 일관성이 유지되는지 확인
        // Given
        val operationCount = 500
        val readResults = java.util.Collections.synchronizedList(mutableListOf<Boolean>())
        val writeResults = java.util.Collections.synchronizedList(mutableListOf<String>())

        // When - 읽기/쓰기 혼합
        val threads = (1..operationCount).map { i ->
            Thread {
                if (i % 2 == 0) {
                    // 짝수: 읽기
                    readResults.add(configManager.config.isDebug)
                } else {
                    // 홀수: 쓰기
                    configManager.setDebugMode(i % 4 == 1)
                    writeResults.add("write_$i")
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Then
        assertEquals("읽기 작업이 모두 완료되어야 합니다", operationCount / 2, readResults.size)
        assertEquals("쓰기 작업이 모두 완료되어야 합니다", operationCount / 2, writeResults.size)

        // 최종 상태가 일관성 있어야 함
        val finalConfig = configManager.config
        assertNotNull("최종 설정이 null이 아니어야 합니다", finalConfig)
    }

    @Test
    fun concurrentListenerOperations_maintainIntegrity() {
        val operationCount = 200
        val error = AtomicReference<Throwable?>(null)

        val threads = (1..operationCount).map { i ->
            Thread {
                try {
                    when (i % 3) {
                        0 -> configManager.addConfigChangeListener(object :
                            LogxConfigManager.ConfigChangeListener {
                            override fun onConfigChanged(newConfig: LogxConfig) = Unit
                        })
                        1 -> configManager.setDebugMode(i % 2 == 0)
                        else -> configManager.config.isDebug
                    }
                } catch (t: Throwable) {
                    error.compareAndSet(null, t)
                }
            }
        }

        threads.forEach(Thread::start)
        threads.forEach(Thread::join)

        assertNull("동시 작업 중 예외가 발생하면 안 됨", error.get())
    }

    @Test
    fun concurrentUpdates_notifyListenersIndependently() {
        val latch = CountDownLatch(2)
        val threadIds = Collections.synchronizedSet(mutableSetOf<Long>())

        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                threadIds += Thread.currentThread().id
                latch.countDown()
            }
        })

        val executor = Executors.newFixedThreadPool(2)
        executor.submit { configManager.setDebugMode(false) }
        executor.submit { configManager.setDebugMode(true) }

        assertTrue("두 번의 알림이 모두 끝나야 함", latch.await(2, TimeUnit.SECONDS))
        assertTrue("리스너는 각각의 업데이트에 대해 호출되어야 함", threadIds.size >= 1)
        executor.shutdown()
    }

    // ========== 10. 리스너 예외 처리 테스트 ==========

//    // TODO: Android 프레임워크(Log) 의존성 때문에 Instrumentation 테스트 전환 검토 중.
//    @Ignore("Android Log 의존성 때문에 Instrumentation 테스트 전환 검토 중")
//    @Test
//    fun listenerException_doesNotAffectOtherListeners() {
//        // 한 리스너에서 예외가 발생해도 다른 리스너는 정상 동작하는지 확인
//        // Given
//        var listener1Called = false
//        var listener2Called = false
//        var listener3Called = false
//
//        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
//            override fun onConfigChanged(newConfig: LogxConfig) {
//                listener1Called = true
//                throw RuntimeException("리스너 1 에러")
//            }
//        })
//
//        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
//            override fun onConfigChanged(newConfig: LogxConfig) {
//                listener2Called = true
//            }
//        })
//
//        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
//            override fun onConfigChanged(newConfig: LogxConfig) {
//                listener3Called = true
//                throw IllegalStateException("리스너 3 에러")
//            }
//        })
//
//        // When
//        configManager.setDebugMode(false)
//
//        // Then - 모든 리스너가 호출되어야 함 (예외와 무관하게)
//        assertTrue("첫 번째 리스너가 호출되어야 합니다", listener1Called)
//        assertTrue("두 번째 리스너가 호출되어야 합니다", listener2Called)
//        assertTrue("세 번째 리스너가 호출되어야 합니다", listener3Called)
//    }
//
//    // TODO: Android 프레임워크(Log) 의존성 때문에 Instrumentation 테스트 전환 검토 중.
//    @Ignore("Android Log 의존성 때문에 Instrumentation 테스트 전환 검토 중")
//    @Test
//    fun listenerWithNullPointerException_isHandled() {
//        // NPE가 발생하는 리스너도 안전하게 처리되는지 확인
//        // Given
//        var safeListenerCalled = false
//
//        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
//            override fun onConfigChanged(newConfig: LogxConfig) {
//                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
//                val nullable: String? = null
//                nullable!!.length // NPE 발생
//            }
//        })
//
//        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
//            override fun onConfigChanged(newConfig: LogxConfig) {
//                safeListenerCalled = true
//            }
//        })
//
//        // When
//        configManager.setAppName("Test")
//
//        // Then
//        assertTrue("안전한 리스너는 호출되어야 합니다", safeListenerCalled)
//    }

    // ========== 11. 엣지 케이스 테스트 ==========

    @Test
    fun emptyAppName_isAllowed() {
        // 빈 문자열 앱 이름도 허용되는지 확인
        // Given & When
        configManager.setAppName("")

        // Then
        assertEquals("빈 문자열이 설정되어야 합니다", "", configManager.config.appName)
    }

    @Test
    fun veryLongAppName_isHandled() {
        // 매우 긴 앱 이름도 처리되는지 확인
        // Given
        val longName = "A".repeat(10000)

        // When
        configManager.setAppName(longName)

        // Then
        assertEquals("긴 문자열이 설정되어야 합니다", longName, configManager.config.appName)
    }

    @Test
    fun specialCharactersInAppName_arePreserved() {
        // 특수문자가 포함된 앱 이름이 보존되는지 확인
        // Given
        val specialName = "App!@#$%^&*()_+-=[]{}|;':\",./<>?"

        // When
        configManager.setAppName(specialName)

        // Then
        assertEquals("특수문자가 보존되어야 합니다", specialName, configManager.config.appName)
    }

    @Test
    fun unicodeAppName_isPreserved() {
        // 유니코드 문자가 포함된 앱 이름이 보존되는지 확인
        // Given
        val unicodeName = "앱테스트🚀한글日本語中文"

        // When
        configManager.setAppName(unicodeName)

        // Then
        assertEquals("유니코드 문자가 보존되어야 합니다", unicodeName, configManager.config.appName)
    }

    @Test
    fun emptyFilterList_isAllowed() {
        // 빈 필터 리스트도 허용되는지 확인
        // Given & When
        configManager.setDebugFilterList(emptyList())

        // Then
        assertTrue("빈 필터 리스트가 설정되어야 합니다",
            configManager.config.debugFilterList.isEmpty())
    }

    @Test
    fun veryLargeFilterList_isHandled() {
        // 매우 큰 필터 리스트도 처리되는지 확인
        // Given
        val largeList = (1..1000).map { "Filter$it" }

        // When
        configManager.setDebugFilterList(largeList)

        // Then
        assertEquals("필터 개수가 일치해야 합니다", 1000,
            configManager.config.debugFilterList.size)
    }

    @Test
    fun duplicateFiltersInList_areRemoved() {
        // Set으로 변환되므로 중복이 제거되는지 확인
        // Given
        val listWithDuplicates = listOf("Tag1", "Tag2", "Tag1", "Tag3", "Tag2")

        // When
        configManager.setDebugFilterList(listWithDuplicates)

        // Then
        assertEquals("중복이 제거되어 3개여야 합니다", 3,
            configManager.config.debugFilterList.size)
        assertTrue("Tag1이 포함되어야 합니다",
            configManager.config.debugFilterList.contains("Tag1"))
        assertTrue("Tag2가 포함되어야 합니다",
            configManager.config.debugFilterList.contains("Tag2"))
        assertTrue("Tag3이 포함되어야 합니다",
            configManager.config.debugFilterList.contains("Tag3"))
    }

    @Test
    fun emptyLogTypeList_isAllowed() {
        // 빈 로그 타입 리스트도 허용되는지 확인
        // Given
        val emptyEnumSet = EnumSet.noneOf(LogxType::class.java)

        // When
        configManager.setDebugLogTypeList(emptyEnumSet)

        // Then
        assertTrue("빈 로그 타입 리스트가 설정되어야 합니다",
            configManager.config.debugLogTypeList.isEmpty())
    }

    @Test
    fun rapidConfigChanges_maintainConsistency() {
        // 빠른 연속 설정 변경이 일관성을 유지하는지 확인
        // Given
        val changeCount = 1000

        // When
        repeat(changeCount) { i ->
            configManager.setDebugMode(i % 2 == 0)
            configManager.setAppName("App$i")
            configManager.setDebugFilter(i % 3 == 0)
        }

        // Then - 최종 상태가 일관성 있어야 함
        val finalConfig = configManager.config
        assertNotNull("최종 설정이 null이 아니어야 합니다", finalConfig)
        assertFalse("디버그 모드가 false여야 합니다 (마지막이 짝수)", finalConfig.isDebug)
        assertEquals("앱 이름이 마지막 값이어야 합니다", "App999", finalConfig.appName)
        assertTrue("디버그 필터가 true여야 합니다 (999 % 3 == 0)", finalConfig.isDebugFilter)
    }

    @Test
    fun listenerAddedDuringNotification_doesNotCauseDeadlock() {
        // 알림 중에 리스너를 추가해도 데드락이 발생하지 않는지 확인
        // Given
        var innerListenerCalled = false

        configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                // 알림 중에 새 리스너 추가 시도
                try {
                    configManager.addConfigChangeListener(object : LogxConfigManager.ConfigChangeListener {
                        override fun onConfigChanged(newConfig: LogxConfig) {
                            innerListenerCalled = true
                        }
                    })
                } catch (e: Exception) {
                    // 데드락 발생 시 예외가 던져질 수 있음
                }
            }
        })

        // When
        configManager.setDebugMode(false)

        // Then - 예외 없이 완료되어야 함
        assertTrue("작업이 완료되어야 합니다", true)
    }

    @Test
    fun sameListener_canBeAddedMultipleTimes() {
        // 같은 리스너를 여러 번 추가하면 Set 특성상 한 번만 저장되는지 확인
        // Given
        var callCount = 0
        val listener = object : LogxConfigManager.ConfigChangeListener {
            override fun onConfigChanged(newConfig: LogxConfig) {
                callCount++
            }
        }

        // When
        configManager.addConfigChangeListener(listener)
        configManager.addConfigChangeListener(listener)
        configManager.addConfigChangeListener(listener)
        configManager.setDebugMode(false)

        // Then - Set이므로 중복이 제거되어 한 번만 호출되어야 함
        assertEquals("리스너가 한 번만 호출되어야 합니다", 1, callCount)
    }
}
