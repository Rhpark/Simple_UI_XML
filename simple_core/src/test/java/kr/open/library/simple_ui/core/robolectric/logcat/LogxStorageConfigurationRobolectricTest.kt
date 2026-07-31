package kr.open.library.simple_ui.core.robolectric.logcat

import android.Manifest
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigStore
import kr.open.library.simple_ui.core.logcat.internal.pipeline.LogxPipeline
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.File

/**
 * Verifies the public initialization and file-storage configuration boundaries of Logx.<br><br>
 * Logx 공개 초기화 및 파일 저장 설정의 경계 계약을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxStorageConfigurationRobolectricTest {
    private lateinit var app: Application
    private lateinit var pipeline: LogxPipeline
    private val cleanupTargets = mutableListOf<File>()

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        pipeline = mock(LogxPipeline::class.java)
        resetApplicationContextForTest()
        Logx.setPipelineForTest(pipeline)
        resetConfig()
        ShadowLog.clear()
    }

    @After
    fun tearDown() {
        Logx.resetPipelineForTest()
        resetApplicationContextForTest()
        resetConfig()
        cleanupTargets.forEach { target -> target.deleteRecursively() }
        ShadowLog.clear()
    }

    /**
     * Verifies that development builds reject file saving before initialization.<br><br>
     * 개발 환경에서는 초기화 전 파일 저장 활성화를 예외로 거부하는지 검증합니다.<br>
     */
    @Test
    fun setSaveEnabled_throwsBeforeInitializationInDevelopmentMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(true)

        val exception =
            assertThrows(IllegalStateException::class.java) {
                Logx.setSaveEnabled(true)
            }

        assertTrue(exception.message.orEmpty().contains("initialize(context)"))
        assertFalse(Logx.isSaveEnabled())
    }

    /**
     * Verifies that release behavior logs and keeps file saving disabled before initialization.<br><br>
     * 릴리스 환경에서는 초기화 전 활성화 요청을 경고하고 저장을 비활성 상태로 유지하는지 검증합니다.<br>
     */
    @Test
    fun setSaveEnabled_logsAndStaysDisabledBeforeInitializationInReleaseMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(false)

        Logx.setSaveEnabled(true)

        assertFalse(Logx.isSaveEnabled())
        assertTrue(
            ShadowLog.getLogsForTag("Logx").any { item ->
                item.type == Log.ERROR && item.msg.contains("initialize(context)")
            },
        )
    }

    /**
     * Verifies that initialization enables file saving without replacing a custom app name.<br><br>
     * 초기화 후 파일 저장을 활성화할 수 있고 사용자 앱 이름은 유지되는지 검증합니다.<br>
     */
    @Test
    fun initialize_allowsFileSavingAndPreservesAppName() {
        Logx.setAppName(APP_NAME)

        Logx.initialize(app)
        Logx.setSaveEnabled(true)

        verify(pipeline).setDevelopmentMode(app)
        assertTrue(Logx.isSaveEnabled())
        assertEquals(APP_NAME, Logx.getAppName())
    }

    /**
     * Verifies that a custom directory is stored but not created before initialization.<br><br>
     * 초기화 전 사용자 경로는 보관하되 디렉터리를 생성하지 않는지 검증합니다.<br>
     */
    @Test
    fun setSaveDirectory_storesTrimmedPathWithoutCreatingDirectoryBeforeInitialization() {
        val directory = cleanup(File(app.filesDir, "logx-preinitialize"))

        Logx.setSaveDirectory("  ${directory.absolutePath}  ")

        assertEquals(directory.absolutePath, Logx.getSaveDirectory())
        assertFalse(directory.exists())
    }

    /**
     * Verifies that blank, relative, and existing-file paths are rejected.<br><br>
     * 공백·상대 경로·기존 파일 경로를 저장 디렉터리로 거부하는지 검증합니다.<br>
     */
    @Test
    fun setSaveDirectory_rejectsInvalidPathShapes() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(true)
        val existingFile = cleanup(File(app.cacheDir, "logx-existing-file.txt"))
        existingFile.writeText("not a directory")

        assertThrows(IllegalStateException::class.java) { Logx.setSaveDirectory("   ") }
        assertThrows(IllegalStateException::class.java) { Logx.setSaveDirectory("relative/logs") }
        assertThrows(IllegalStateException::class.java) { Logx.setSaveDirectory(existingFile.absolutePath) }

        assertNull(Logx.getSaveDirectory())
    }

    /**
     * Verifies Android 10+ accepts app-private paths and rejects unsupported external paths.<br><br>
     * Android 10 이상에서 앱 전용 경로만 허용하고 지원하지 않는 외부 경로는 거부하는지 검증합니다.<br>
     */
    @Test
    fun setSaveDirectory_enforcesScopedStorageBoundaryOnAndroid10Plus() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(true)
        val allowedDirectory = cleanup(File(app.filesDir, "logx-allowed"))
        val unsupportedDirectory = cleanup(File(System.getProperty("java.io.tmpdir"), "logx-unsupported-${System.nanoTime()}"))
        Logx.initialize(app)

        assertThrows(IllegalStateException::class.java) {
            Logx.setSaveDirectory(unsupportedDirectory.absolutePath)
        }
        Logx.setSaveDirectory(allowedDirectory.absolutePath)

        assertEquals(allowedDirectory.absolutePath, Logx.getSaveDirectory())
    }

    /**
     * Verifies that initialization rejects a previously stored unsupported path in development mode.<br><br>
     * 개발 환경에서는 초기화 전에 보관한 미지원 경로를 초기화 시점에 예외로 거부하는지 검증합니다.<br>
     */
    @Test
    fun initialize_throwsForStoredUnsupportedDirectoryInDevelopmentMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(true)
        val unsupportedDirectory = cleanup(File(System.getProperty("java.io.tmpdir"), "logx-stored-dev-${System.nanoTime()}"))
        Logx.setSaveDirectory(unsupportedDirectory.absolutePath)

        val exception =
            assertThrows(IllegalStateException::class.java) {
                Logx.initialize(app)
            }

        assertTrue(exception.message.orEmpty().contains("Stored custom save directory"))
        assertEquals(unsupportedDirectory.absolutePath, Logx.getSaveDirectory())
    }

    /**
     * Verifies that release initialization clears a previously stored unsupported path and logs it.<br><br>
     * 릴리스 환경에서는 초기화 전에 보관한 미지원 경로를 제거하고 경고하는지 검증합니다.<br>
     */
    @Test
    fun initialize_clearsStoredUnsupportedDirectoryInReleaseMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(false)
        val unsupportedDirectory = cleanup(File(System.getProperty("java.io.tmpdir"), "logx-stored-release-${System.nanoTime()}"))
        Logx.setSaveDirectory(unsupportedDirectory.absolutePath)

        Logx.initialize(app)

        assertNull(Logx.getSaveDirectory())
        assertTrue(
            ShadowLog.getLogsForTag("Logx").any { item ->
                item.type == Log.ERROR && item.msg.contains("Stored custom save directory")
            },
        )
    }

    /**
     * Verifies that blank and file-name-invalid application names are rejected.<br><br>
     * 공백 및 파일명 금지 문자를 포함한 앱 이름을 거부하는지 검증합니다.<br>
     */
    @Test
    fun setAppName_rejectsInvalidNamesInDevelopmentMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(true)

        assertThrows(IllegalStateException::class.java) { Logx.setAppName("   ") }
        assertThrows(IllegalStateException::class.java) { Logx.setAppName("invalid/name") }

        assertEquals("AppName", Logx.getAppName())
    }

    /**
     * Verifies that release behavior logs an invalid application name and preserves the previous value.<br><br>
     * 릴리스 환경에서는 잘못된 앱 이름을 경고하고 기존 값을 유지하는지 검증합니다.<br>
     */
    @Test
    fun setAppName_logsAndPreservesPreviousValueForInvalidNameInReleaseMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(false)
        Logx.setAppName(APP_NAME)

        Logx.setAppName("invalid:name")

        assertEquals(APP_NAME, Logx.getAppName())
        assertTrue(
            ShadowLog.getLogsForTag("Logx").any { item ->
                item.type == Log.ERROR && item.msg.contains("invalid file-name characters")
            },
        )
    }

    /**
     * Verifies that legacy PUBLIC_EXTERNAL storage requires initialization first.<br><br>
     * API 28의 PUBLIC_EXTERNAL 저장소는 먼저 초기화해야 하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun setStorageType_publicExternalThrowsBeforeInitializationOnLegacyAndroid() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(true)

        val exception =
            assertThrows(IllegalStateException::class.java) {
                Logx.setStorageType(LogStorageType.PUBLIC_EXTERNAL)
            }

        assertTrue(exception.message.orEmpty().contains("before setting PUBLIC_EXTERNAL"))
        assertEquals(LogStorageType.APP_EXTERNAL, Logx.getStorageType())
    }

    /**
     * Verifies that release behavior logs and rejects legacy PUBLIC_EXTERNAL before initialization.<br><br>
     * 릴리스 환경에서는 초기화 전 API 28 PUBLIC_EXTERNAL 요청을 경고하고 거부하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun setStorageType_publicExternalLogsBeforeInitializationInLegacyReleaseMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(false)

        Logx.setStorageType(LogStorageType.PUBLIC_EXTERNAL)

        assertEquals(LogStorageType.APP_EXTERNAL, Logx.getStorageType())
        assertTrue(
            ShadowLog.getLogsForTag("Logx").any { item ->
                item.type == Log.ERROR && item.msg.contains("before setting PUBLIC_EXTERNAL")
            },
        )
    }

    /**
     * Verifies that release behavior logs and rejects legacy PUBLIC_EXTERNAL without permission.<br><br>
     * 릴리스 환경에서는 권한 없는 API 28 PUBLIC_EXTERNAL 요청을 경고하고 거부하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun setStorageType_publicExternalLogsWithoutPermissionInLegacyReleaseMode() {
        `when`(pipeline.isDevelopmentMode()).thenReturn(false)
        Shadows.shadowOf(app).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Logx.initialize(app)

        Logx.setStorageType(LogStorageType.PUBLIC_EXTERNAL)

        assertEquals(LogStorageType.APP_EXTERNAL, Logx.getStorageType())
        assertTrue(
            ShadowLog.getLogsForTag("Logx").any { item ->
                item.type == Log.ERROR && item.msg.contains("permission is not granted")
            },
        )
    }

    /**
     * Verifies that legacy PUBLIC_EXTERNAL storage is accepted after initialization with permission.<br><br>
     * API 28에서 초기화와 저장소 권한을 충족하면 PUBLIC_EXTERNAL을 허용하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun setStorageType_publicExternalSucceedsWithPermissionOnLegacyAndroid() {
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Logx.initialize(app)

        Logx.setStorageType(LogStorageType.PUBLIC_EXTERNAL)

        assertEquals(LogStorageType.PUBLIC_EXTERNAL, Logx.getStorageType())
    }

    private fun cleanup(target: File): File {
        target.deleteRecursively()
        cleanupTargets += target
        return target
    }

    private fun resetApplicationContextForTest() {
        Logx::class.java.getDeclaredField("appContext").apply {
            isAccessible = true
            set(null, null)
        }
    }

    private fun resetConfig() {
        LogxConfigStore.setLogging(true)
        LogxConfigStore.setLogTypes(enumValues<LogType>().toSet())
        LogxConfigStore.setLogTagBlockListEnabled(false)
        LogxConfigStore.setLogTagBlockList(emptySet())
        LogxConfigStore.setSaveEnabled(false)
        LogxConfigStore.setStorageType(LogStorageType.APP_EXTERNAL)
        LogxConfigStore.setSaveDirectory(null)
        LogxConfigStore.setAppName("AppName")
    }

    private companion object {
        const val APP_NAME = "StorageTestApp"
    }
}
