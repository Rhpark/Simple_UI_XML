package kr.open.library.simple_ui.core.robolectric.extensions.trycatch

import android.os.Build
import kr.open.library.simple_ui.core.extensions.trycatch.requireMaxSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.requireMinSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.throwMinSdkVersion
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SDK 최소·최대 버전 검증 함수의 Android 런타임 경계를 확인합니다.<br><br>
 * Verifies Android runtime boundaries for the minimum and maximum SDK guards.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TryCatchExtensionsRobolectricTest {
    @Test
    fun requireMinSdkVersion_acceptsCurrentAndLowerVersions() {
        requireMinSdkVersion(Build.VERSION_CODES.P)
        requireMinSdkVersion(Build.VERSION_CODES.O_MR1)
    }

    @Test
    fun requireMinSdkVersion_throwsForHigherVersion() {
        assertThrows(UnsupportedOperationException::class.java) {
            requireMinSdkVersion(Build.VERSION_CODES.Q)
        }
    }

    @Test
    fun throwMinSdkVersion_alwaysThrows() {
        assertThrows(UnsupportedOperationException::class.java) {
            throwMinSdkVersion(Build.VERSION_CODES.Q)
        }
    }

    @Test
    fun requireMaxSdkVersion_acceptsCurrentAndHigherVersions() {
        requireMaxSdkVersion(Build.VERSION_CODES.P)
        requireMaxSdkVersion(Build.VERSION_CODES.Q)
    }

    @Test
    fun requireMaxSdkVersion_throwsForLowerVersion() {
        assertThrows(UnsupportedOperationException::class.java) {
            requireMaxSdkVersion(Build.VERSION_CODES.O_MR1)
        }
    }
}
