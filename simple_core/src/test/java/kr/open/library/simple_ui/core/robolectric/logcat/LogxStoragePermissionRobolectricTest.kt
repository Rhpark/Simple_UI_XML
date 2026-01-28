package kr.open.library.simple_ui.core.robolectric.logcat

import android.Manifest
import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class LogxStoragePermissionRobolectricTest {
    @Test(expected = IllegalStateException::class)
    fun setStorageTypePublicExternalWithoutPermissionThrows() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(context).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        context.applicationInfo.flags = context.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE

        Logx.initialize(context)
        Logx.setStorageType(LogStorageType.PUBLIC_EXTERNAL)
    }
}
