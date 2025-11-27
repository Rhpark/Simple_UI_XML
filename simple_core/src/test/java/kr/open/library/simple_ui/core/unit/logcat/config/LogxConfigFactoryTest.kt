package kr.open.library.simple_ui.core.unit.logcat.config

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.config.LogxConfigFactory
import kr.open.library.simple_ui.core.logcat.config.LogxPathUtils
import kr.open.library.simple_ui.core.logcat.config.LogxStorageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogxConfigFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `createDefault uses provided storage type`() {
        val config = LogxConfigFactory.createDefault(context)

        assertEquals(LogxStorageType.APP_EXTERNAL, config.storageType)
        assertEquals(
            LogxPathUtils.getLogPath(context, LogxStorageType.APP_EXTERNAL),
            config.saveFilePath,
        )
    }

    @Test
    fun `createInternal returns internal storage config`() {
        val config = LogxConfigFactory.createInternal(context)

        assertEquals(LogxStorageType.INTERNAL, config.storageType)
        assertEquals(LogxPathUtils.getInternalLogPath(context), config.saveFilePath)
    }

    @Test
    fun `createAppExternal returns app external storage config`() {
        val config = LogxConfigFactory.createAppExternal(context)

        assertEquals(LogxStorageType.APP_EXTERNAL, config.storageType)
        assertEquals(LogxPathUtils.getAppExternalLogPath(context), config.saveFilePath)
    }

    @Test
    fun `createPublicExternal returns public storage config`() {
        val config = LogxConfigFactory.createPublicExternal(context)

        assertEquals(LogxStorageType.PUBLIC_EXTERNAL, config.storageType)
        assertEquals(LogxPathUtils.getPublicExternalLogPath(context), config.saveFilePath)
    }

    @Test
    fun `create delegates to specialized functions`() {
        LogxStorageType.entries.forEach { type ->
            val config = LogxConfigFactory.create(context, type)

            assertEquals(type, config.storageType)
            assertNotNull(config.saveFilePath)
        }
    }
}
