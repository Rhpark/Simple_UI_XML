package kr.open.library.simple_ui.core.robolectric.logcat.config

import android.app.Application
import android.content.ContextWrapper
import android.os.Build
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.config.LogxPathUtils
import kr.open.library.simple_ui.core.logcat.config.LogxStorageType
import org.junit.Assert.assertEquals
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
class LogxPathUtilsRobolectricTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun appExternalPathFallsBackToInternalWhenDirNull() {
        val wrapper = object : ContextWrapper(application) {
            override fun getExternalFilesDir(type: String?): File? = null
        }

        val result = LogxPathUtils.getAppExternalLogPath(wrapper)

        assertEquals(LogxPathUtils.getInternalLogPath(application), result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun publicExternalPathUsesDocumentsDirOnQPlus() {
        val path = LogxPathUtils.getPublicExternalLogPath(application)

        assertTrue(path.contains("AppLogs"))
        assertTrue(path.contains("Documents", ignoreCase = true))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun publicExternalPathFallsBackToLegacyStorageOnPreQ() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        val path = LogxPathUtils.getPublicExternalLogPath(context)

        assertTrue(path.startsWith(Environment.getExternalStorageDirectory().absolutePath))
        assertTrue(path.endsWith("/AppLogs"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun requiresPermissionTrueBeforeQ() {
        assertTrue(LogxPathUtils.requiresPermission(LogxStorageType.PUBLIC_EXTERNAL))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun requiresPermissionFalseOnQPlus() {
        assertFalse(LogxPathUtils.requiresPermission(LogxStorageType.PUBLIC_EXTERNAL))
    }

    @Test
    fun userAccessibilityFlagsMatchSpecification() {
        assertFalse(LogxPathUtils.isUserAccessible(LogxStorageType.INTERNAL))
        assertTrue(LogxPathUtils.isUserAccessible(LogxStorageType.APP_EXTERNAL))
        assertTrue(LogxPathUtils.isUserAccessible(LogxStorageType.PUBLIC_EXTERNAL))
    }

    @Test
    fun getLogPathReturnsInternalForInternalStorage() {
        val path = LogxPathUtils.getLogPath(application, LogxStorageType.INTERNAL)
        assertEquals(LogxPathUtils.getInternalLogPath(application), path)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getLogPathReturnsPublicForPublicStorage() {
        val path = LogxPathUtils.getLogPath(application, LogxStorageType.PUBLIC_EXTERNAL)
        assertTrue(path.contains("AppLogs"))
    }

    @Test
    fun appExternalPathUsesProvidedDirWhenAvailable() {
        val expectedDir = File(application.filesDir, "customDir")
        expectedDir.mkdirs()
        val wrapper = object : ContextWrapper(application) {
            override fun getExternalFilesDir(type: String?): File? = expectedDir
        }

        val path = LogxPathUtils.getAppExternalLogPath(wrapper)
        assertEquals(expectedDir.absolutePath, path)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun publicExternalPathFallsBackToAppExternalWhenDocumentsNull() {
        val wrapper = object : ContextWrapper(application) {
            override fun getExternalFilesDir(type: String?): File? {
                return if (type == Environment.DIRECTORY_DOCUMENTS) null else super.getExternalFilesDir(type)
            }
        }

        val expected = LogxPathUtils.getAppExternalLogPath(wrapper)
        val result = LogxPathUtils.getPublicExternalLogPath(wrapper)
        assertEquals(expected, result)
    }

    @Test
    fun requiresPermissionFalseForInternalAndApp() {
        assertFalse(LogxPathUtils.requiresPermission(LogxStorageType.INTERNAL))
        assertFalse(LogxPathUtils.requiresPermission(LogxStorageType.APP_EXTERNAL))
    }
}