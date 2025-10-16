package kr.open.library.simple_ui.logcat

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.config.LogxConfigManager
import kr.open.library.simple_ui.logcat.model.LogxType
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.EnumSet

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
    fun `기본_설정이_올바르게_초기화된다`() {
        // Given & When
        val config = configManager.config

        // Then
        assertNotNull("설정이 null이 아니어야 합니다", config)
        assertTrue("기본적으로 디버그 모드가 켜져있어야 합니다", config.isDebug)
    }

    // ========== 2. 디버그 모드 설정 테스트 ==========

    @Test
    fun `디버그_모드를_false로_설정할_수_있다`() {
        // Given
        configManager.setDebugMode(true)

        // When
        configManager.setDebugMode(false)

        // Then
        assertFalse("디버그 모드가 꺼져야 합니다", configManager.config.isDebug)
    }

    @Test
    fun `디버그_모드를_true로_설정할_수_있다`() {
        // Given
        configManager.setDebugMode(false)

        // When
        configManager.setDebugMode(true)

        // Then
        assertTrue("디버그 모드가 켜져야 합니다", configManager.config.isDebug)
    }

    // ========== 3. 파일 저장 설정 테스트 ==========

    @Test
    fun `파일_저장_설정을_변경할_수_있다`() {
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
    fun `앱_이름을_설정할_수_있다`() {
        // Given
        val appName = "TestApp"

        // When
        configManager.setAppName(appName)

        // Then
        assertEquals("앱 이름이 설정되어야 합니다", appName, configManager.config.appName)
    }

    @Test
    fun `빈_문자열로_앱_이름을_설정할_수_있다`() {
        // Given
        val appName = ""

        // When
        configManager.setAppName(appName)

        // Then
        assertEquals("빈 앱 이름이 설정되어야 합니다", appName, configManager.config.appName)
    }

    // ========== 5. 로그 타입 필터링 테스트 ==========

    @Test
    fun `로그_타입을_설정할_수_있다`() {
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
    fun `모든_로그_타입을_허용할_수_있다`() {
        // Given
        val allTypes = EnumSet.allOf(LogxType::class.java)

        // When
        configManager.setDebugLogTypeList(allTypes)

        // Then
        assertEquals("모든 로그 타입이 허용되어야 합니다", 5, configManager.config.debugLogTypeList.size)
        assertTrue("DEBUG가 포함되어야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.DEBUG))
        assertTrue("INFO가 포함되어야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.INFO))
        assertTrue("WARN이 포함되어야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.WARN))
        assertTrue("ERROR가 포함되어야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.ERROR))
        assertTrue("VERBOSE가 포함되어야 합니다",
            configManager.config.debugLogTypeList.contains(LogxType.VERBOSE))
    }

    // ========== 6. 디버그 필터 설정 테스트 ==========

    @Test
    fun `디버그_필터를_설정할_수_있다`() {
        // Given
        configManager.setDebugFilter(false)

        // When
        configManager.setDebugFilter(true)

        // Then
        assertTrue("디버그 필터가 켜져야 합니다", configManager.config.isDebugFilter)
    }

    @Test
    fun `디버그_필터_태그_리스트를_설정할_수_있다`() {
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
    fun `빈_필터_리스트를_설정할_수_있다`() {
        // Given
        val emptyList = emptyList<String>()

        // When
        configManager.setDebugFilterList(emptyList)

        // Then
        assertTrue("필터 리스트가 비어있어야 합니다", configManager.config.debugFilterList.isEmpty())
    }

    // ========== 7. 설정 변경 리스너 테스트 ==========

    @Test
    fun `설정_변경_시_리스너가_호출된다`() {
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
    fun `여러_리스너를_등록할_수_있다`() {
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
    fun `모든_리스너를_제거할_수_있다`() {
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
    fun `전체_설정을_한번에_업데이트할_수_있다`() {
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
}
