package kr.open.library.simple_ui.core.robolectric.logcat.internal.writer

import android.Manifest
import android.app.Application
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFilePathResolver
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.File

/**
 * Verifies directory creation, invalid paths, scoped storage, and legacy permission boundaries.<br><br>
 * 디렉터리 생성·잘못된 경로·Scoped Storage·레거시 권한 경계를 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxFilePathResolverRobolectricTest {
    private lateinit var app: Application
    private lateinit var resolver: LogxFilePathResolver
    private val cleanupTargets = mutableListOf<File>()

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        resolver = LogxFilePathResolver()
        ShadowLog.clear()
    }

    @After
    fun tearDown() {
        cleanupTargets
            .sortedByDescending { target -> target.absolutePath.length }
            .forEach { target -> target.deleteRecursively() }
        ShadowLog.clear()
    }

    /**
     * Verifies that a missing app-private custom directory is created.<br><br>
     * 존재하지 않는 앱 전용 사용자 경로를 자동으로 생성하는지 검증합니다.<br>
     */
    @Test
    fun resolveDirectory_createsMissingAppPrivateCustomDirectory() {
        val directory = cleanup(File(app.filesDir, "logx-path-custom"))

        val result = resolver.resolveDirectory(app, config(directory.absolutePath), ERROR_TAG)

        assertEquals(directory.absolutePath, result?.absolutePath)
        assertTrue(directory.isDirectory)
    }

    /**
     * Verifies that the default internal directory is resolved and created.<br><br>
     * 사용자 경로가 없으면 기본 내부 저장소 경로를 계산하고 생성하는지 검증합니다.<br>
     */
    @Test
    fun resolveDirectory_usesDefaultInternalDirectoryWhenCustomPathIsAbsent() {
        val expected = cleanup(File(app.filesDir, "AppLogs"))

        val result = resolver.resolveDirectory(app, config(saveDirectory = null), ERROR_TAG)

        assertEquals(expected.absolutePath, result?.absolutePath)
        assertTrue(expected.isDirectory)
    }

    /**
     * Verifies that an existing file cannot be used as a directory.<br><br>
     * 기존 파일 경로를 저장 디렉터리로 사용할 수 없는지 검증합니다.<br>
     */
    @Test
    fun resolveDirectory_rejectsExistingFilePath() {
        val file = cleanup(File(app.filesDir, "logx-path-file")).apply { writeText("file") }

        val result = resolver.resolveDirectory(app, config(file.absolutePath), ERROR_TAG)

        assertNull(result)
        assertErrorContains("not a directory")
    }

    /**
     * Verifies that a file in the parent chain blocks directory creation.<br><br>
     * 상위 경로에 파일이 있으면 하위 디렉터리 생성을 중단하는지 검증합니다.<br>
     */
    @Test
    fun resolveDirectory_rejectsPathWithBlockingFileParent() {
        val blockingParent = cleanup(File(app.filesDir, "logx-blocking-parent")).apply { writeText("file") }
        val directory = File(blockingParent, "child")

        val result = resolver.resolveDirectory(app, config(directory.absolutePath), ERROR_TAG)

        assertNull(result)
        assertErrorContains("Parent path is not a directory")
    }

    /**
     * Verifies that Android 10+ rejects custom paths outside app-private roots.<br><br>
     * Android 10 이상에서 앱 전용 루트 밖의 사용자 경로를 거부하는지 검증합니다.<br>
     */
    @Test
    fun resolveDirectory_rejectsUnsupportedCustomDirectoryOnAndroid10Plus() {
        val directory = cleanup(File(System.getProperty("java.io.tmpdir"), "logx-path-unsupported-${System.nanoTime()}"))

        val result = resolver.resolveDirectory(app, config(directory.absolutePath), ERROR_TAG)

        assertNull(result)
        assertErrorContains("Storage permission required for log directory")
    }

    /**
     * Verifies that legacy PUBLIC_EXTERNAL without permission is rejected before path creation.<br><br>
     * API 28에서 권한 없는 기본 PUBLIC_EXTERNAL 경로를 생성 전에 거부하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun resolveDirectory_rejectsDefaultPublicExternalWithoutLegacyPermission() {
        Shadows.shadowOf(app).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val result = resolver.resolveDirectory(
            app,
            config(saveDirectory = null, storageType = LogStorageType.PUBLIC_EXTERNAL),
            ERROR_TAG,
        )

        assertNull(result)
        assertErrorContains("PUBLIC_EXTERNAL on API 28 and below")
    }

    /**
     * Verifies that granted legacy permission allows the default PUBLIC_EXTERNAL directory.<br><br>
     * API 28에서 권한을 보유하면 기본 PUBLIC_EXTERNAL 디렉터리를 생성하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun resolveDirectory_allowsDefaultPublicExternalWithLegacyPermission() {
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val expected = cleanup(File(Environment.getExternalStorageDirectory(), "AppLogs"))

        val result = resolver.resolveDirectory(
            app,
            config(saveDirectory = null, storageType = LogStorageType.PUBLIC_EXTERNAL),
            ERROR_TAG,
        )

        assertEquals(expected.absolutePath, result?.absolutePath)
        assertTrue(expected.isDirectory)
    }

    /**
     * Verifies that a legacy custom public path also requires external-storage permission.<br><br>
     * API 28의 사용자 지정 공용 경로에도 외부 저장소 권한이 필요한지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun resolveDirectory_rejectsCustomPublicExternalWithoutLegacyPermission() {
        Shadows.shadowOf(app).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val directory = cleanup(File(Environment.getExternalStorageDirectory(), "logx-custom-denied"))

        val result = resolver.resolveDirectory(app, config(directory.absolutePath), ERROR_TAG)

        assertNull(result)
        assertErrorContains("Storage permission required for log directory")
    }

    /**
     * Verifies that a legacy app-private custom path does not require external permission.<br><br>
     * API 28의 앱 내부 사용자 경로에는 외부 저장소 권한이 필요하지 않은지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun resolveDirectory_allowsAppPrivateCustomDirectoryWithoutLegacyPermission() {
        Shadows.shadowOf(app).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val directory = cleanup(File(app.filesDir, "logx-custom-private"))

        val result = resolver.resolveDirectory(app, config(directory.absolutePath), ERROR_TAG)

        assertEquals(directory.absolutePath, result?.absolutePath)
        assertTrue(directory.isDirectory)
    }

    /**
     * Verifies that a granted legacy permission allows a custom public directory.<br><br>
     * API 28에서 권한을 보유하면 사용자 지정 공용 경로를 생성하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun resolveDirectory_allowsCustomPublicExternalWithLegacyPermission() {
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val directory = cleanup(File(Environment.getExternalStorageDirectory(), "logx-custom-granted"))

        val result = resolver.resolveDirectory(app, config(directory.absolutePath), ERROR_TAG)

        assertEquals(directory.absolutePath, result?.absolutePath)
        assertTrue(directory.isDirectory)
    }

    private fun config(
        saveDirectory: String?,
        storageType: LogStorageType = LogStorageType.INTERNAL,
    ): LogxConfigSnapshot = LogxConfigSnapshot(
        isLogging = true,
        logTypes = enumValues<LogType>().toSet(),
        isLogTagBlockListEnabled = false,
        logTagBlockList = emptySet(),
        isSaveEnabled = true,
        storageType = storageType,
        saveDirectory = saveDirectory,
        appName = "PathResolverTestApp",
        skipPackages = emptySet(),
    )

    private fun cleanup(target: File): File {
        target.deleteRecursively()
        cleanupTargets += target
        return target
    }

    private fun assertErrorContains(expectedMessage: String) {
        assertTrue(
            ShadowLog.getLogsForTag(ERROR_TAG).any { item ->
                item.type == Log.ERROR && item.msg.contains(expectedMessage)
            },
        )
    }

    private companion object {
        const val ERROR_TAG = "PATH_ERROR"
    }
}
