package kr.open.library.simple_ui.core.robolectric.logcat.internal.common

import android.app.Application
import android.content.ContextWrapper
import android.os.Build
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.internal.common.LogxPathResolver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class LogxPathResolverRobolectricTest {
    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun resolveInternalPathUsesFilesDir() {
        val path = LogxPathResolver.resolvePath(app, LogStorageType.INTERNAL)
        val normalized = normalize(path)
        val base = normalize(app.filesDir.absolutePath)
        assertTrue(normalized.startsWith(base))
        assertTrue(normalized.endsWith("/AppLogs"))
    }

    @Test
    fun appExternalFallsBackToInternalWhenNull() {
        val wrapper = object : ContextWrapper(app) {
            override fun getExternalFilesDir(type: String?): File? = null
        }

        val path = LogxPathResolver.resolvePath(wrapper, LogStorageType.APP_EXTERNAL)
        val normalized = normalize(path)
        val base = normalize(wrapper.filesDir.absolutePath)
        assertTrue(normalized.startsWith(base))
        assertTrue(normalized.endsWith("/AppLogs"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun publicExternalUsesDocumentsOnQPlus() {
        val path = LogxPathResolver.resolvePath(app, LogStorageType.PUBLIC_EXTERNAL)
        assertTrue(path.contains("AppLogs"))
        assertTrue(path.contains("Documents", ignoreCase = true))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun publicExternalUsesLegacyStorageOnPreQ() {
        val path = LogxPathResolver.resolvePath(app, LogStorageType.PUBLIC_EXTERNAL)
        assertTrue(path.startsWith(Environment.getExternalStorageDirectory().absolutePath))
        assertTrue(path.endsWith("/AppLogs"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun requiresPermissionTrueBeforeQ() {
        assertTrue(LogxPathResolver.requiresPermission(LogStorageType.PUBLIC_EXTERNAL))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun requiresPermissionFalseOnQPlus() {
        assertFalse(LogxPathResolver.requiresPermission(LogStorageType.PUBLIC_EXTERNAL))
    }

    private fun normalize(path: String): String = path.replace("\\", "/")
}
