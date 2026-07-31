package kr.open.library.simple_ui.core.robolectric.permissions.extensions

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import kr.open.library.simple_ui.core.permissions.extensions.readDeclaredManifestPermissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kr.open.library.simple_ui.core.permissions.internal.readDeclaredManifestPermissions as readDeclaredManifestPermissionsInternal

/**
 * Robolectric tests for reading permissions declared in the application manifest.<br><br>
 * 애플리케이션 Manifest에 선언된 권한 조회를 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ManifestPermissionReaderRobolectricTest {
    /**
     * Verifies that API 33+ uses PackageInfoFlags and returns a de-duplicated permission set.<br><br>
     * API 33 이상에서 PackageInfoFlags를 사용하고 중복이 제거된 권한 집합을 반환하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun readDeclaredManifestPermissions_usesPackageInfoFlags_onApi33AndAbove() {
        val packageManager = mock(PackageManager::class.java)
        val context = createContext(packageManager)
        val packageInfo = createPackageInfo(CAMERA_PERMISSION, MICROPHONE_PERMISSION, CAMERA_PERMISSION)
        val flagsCaptor = ArgumentCaptor.forClass(PackageManager.PackageInfoFlags::class.java)
        `when`(
            packageManager.getPackageInfo(
                org.mockito.ArgumentMatchers.eq(PACKAGE_NAME),
                org.mockito.ArgumentMatchers.any(PackageManager.PackageInfoFlags::class.java),
            ),
        ).thenReturn(packageInfo)

        val permissions = context.readDeclaredManifestPermissions()

        org.mockito.Mockito.verify(packageManager).getPackageInfo(
            org.mockito.ArgumentMatchers.eq(PACKAGE_NAME),
            flagsCaptor.capture(),
        )
        assertEquals(PackageManager.GET_PERMISSIONS.toLong(), flagsCaptor.value.value)
        assertEquals(setOf(CAMERA_PERMISSION, MICROPHONE_PERMISSION), permissions)
    }

    /**
     * Verifies that API 32 and below use the legacy integer flag overload.<br><br>
     * API 32 이하에서 기존 정수 플래그 오버로드를 사용하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2])
    fun readDeclaredManifestPermissions_usesLegacyFlags_belowApi33() {
        val packageManager = mock(PackageManager::class.java)
        val context = createContext(packageManager)
        val packageInfo = createPackageInfo(CAMERA_PERMISSION, MICROPHONE_PERMISSION)
        @Suppress("DEPRECATION")
        `when`(packageManager.getPackageInfo(PACKAGE_NAME, PackageManager.GET_PERMISSIONS)).thenReturn(packageInfo)

        val permissions = context.readDeclaredManifestPermissions()

        assertEquals(setOf(CAMERA_PERMISSION, MICROPHONE_PERMISSION), permissions)
        @Suppress("DEPRECATION")
        verify(packageManager).getPackageInfo(PACKAGE_NAME, PackageManager.GET_PERMISSIONS)
    }

    /**
     * Verifies that an absent requestedPermissions array is represented as an empty set.<br><br>
     * requestedPermissions 배열이 없을 때 빈 집합으로 표현하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun readDeclaredManifestPermissions_returnsEmptySet_whenNoPermissionsAreDeclared() {
        val packageManager = mock(PackageManager::class.java)
        val context = createContext(packageManager)
        `when`(
            packageManager.getPackageInfo(
                org.mockito.ArgumentMatchers.eq(PACKAGE_NAME),
                org.mockito.ArgumentMatchers.any(PackageManager.PackageInfoFlags::class.java),
            ),
        ).thenReturn(PackageInfo())

        val permissions = context.readDeclaredManifestPermissions()

        assertTrue(permissions.isEmpty())
    }

    /**
     * Verifies that PackageManager lookup failures are converted to an empty set.<br><br>
     * PackageManager 조회 실패를 빈 집합으로 변환하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2])
    fun readDeclaredManifestPermissions_returnsEmptySet_whenPackageLookupFails() {
        val packageManager = mock(PackageManager::class.java)
        val context = createContext(packageManager)
        @Suppress("DEPRECATION")
        `when`(packageManager.getPackageInfo(PACKAGE_NAME, PackageManager.GET_PERMISSIONS))
            .thenThrow(PackageManager.NameNotFoundException(PACKAGE_NAME))

        val permissions = context.readDeclaredManifestPermissions()

        assertTrue(permissions.isEmpty())
    }

    /**
     * Verifies that the deprecated internal path forwards to the public replacement API.<br><br>
     * Deprecated된 internal 경로가 공개 대체 API로 위임하는지 검증합니다.<br>
     */
    @Suppress("DEPRECATION")
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun deprecatedInternalPath_forwardsToPublicExtension() {
        val packageManager = mock(PackageManager::class.java)
        val context = createContext(packageManager)
        val packageInfo = createPackageInfo(CAMERA_PERMISSION)
        `when`(
            packageManager.getPackageInfo(
                org.mockito.ArgumentMatchers.eq(PACKAGE_NAME),
                org.mockito.ArgumentMatchers.any(PackageManager.PackageInfoFlags::class.java),
            ),
        ).thenReturn(packageInfo)

        val permissions = context.readDeclaredManifestPermissionsInternal()

        assertEquals(setOf(CAMERA_PERMISSION), permissions)
    }

    private fun createContext(packageManager: PackageManager): Context = mock(Context::class.java).also { context ->
        `when`(context.packageName).thenReturn(PACKAGE_NAME)
        `when`(context.packageManager).thenReturn(packageManager)
    }

    private fun createPackageInfo(vararg permissions: String): PackageInfo = PackageInfo().apply {
        requestedPermissions = permissions
    }

    private companion object {
        const val PACKAGE_NAME = "com.example.permission.test"
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        const val MICROPHONE_PERMISSION = "android.permission.RECORD_AUDIO"
    }
}
