package kr.open.library.simple_ui.core.robolectric.system_manager.base

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
class BaseSystemServiceRobolectricTest {

    private lateinit var context: Context
    private lateinit var shadowApp: ShadowApplication

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(context as android.app.Application)
    }

    @Test
    fun init_withNoPermissions_logsAllGranted() {
        val service = TestSystemService(context, null)
        assertTrue(service.testIsPermissionAllGranted())
        assertEquals(0, service.testGetDeniedPermissionList().size)
    }

    @Test
    fun init_withGrantedPermissions_logsAllGranted() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(context, listOf(Manifest.permission.CAMERA))

        assertTrue(service.testIsPermissionAllGranted())
        assertEquals(0, service.testGetDeniedPermissionList().size)
    }

    @Test
    fun init_withDeniedPermissions_logsMissingPermissions() {
        val service = TestSystemService(context, listOf(Manifest.permission.CAMERA))

        assertFalse(service.testIsPermissionAllGranted())
        assertEquals(1, service.testGetDeniedPermissionList().size)
        assertTrue(service.testGetDeniedPermissionList().contains(Manifest.permission.CAMERA))
    }

    @Test
    fun getDeniedPermissionList_returnsCorrectList() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(
            context,
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )

        val denied = service.testGetDeniedPermissionList()
        assertEquals(1, denied.size)
        assertTrue(denied.contains(Manifest.permission.RECORD_AUDIO))
    }

    @Test
    fun isPermissionAllGranted_returnsTrueWhenAllGranted() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        val service = TestSystemService(
            context,
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )

        assertTrue(service.testIsPermissionAllGranted())
    }

    @Test
    fun isPermissionAllGranted_returnsFalseWhenSomeDenied() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(
            context,
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )

        assertFalse(service.testIsPermissionAllGranted())
    }

    @Test
    fun tryCatchSystemManager_returnDefaultValueWhenPermissionDenied() {
        val service = TestSystemService(context, listOf(Manifest.permission.CAMERA))

        val result = service.testTryCatch()
        assertEquals("default", result)
    }

    @Test
    fun tryCatchSystemManager_executesBlockWhenPermissionsGranted() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(context, listOf(Manifest.permission.CAMERA))

        val result = service.testTryCatch()
        assertEquals("success", result)
    }

    @Test
    fun tryCatchSystemManager_returnDefaultValueOnException() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(context, listOf(Manifest.permission.CAMERA))

        val result = service.testTryCatchWithException()
        assertEquals("default", result)
    }

    @Test
    fun refreshPermissions_updatesPermissionStatus() {
        val service = TestSystemService(context, listOf(Manifest.permission.CAMERA))

        assertFalse(service.testIsPermissionAllGranted())

        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        service.refreshPermissions()

        assertTrue(service.testIsPermissionAllGranted())
    }

    @Test
    fun refreshPermissions_withNoRequiredPermissions_doesNothing() {
        val service = TestSystemService(context, null)
        service.refreshPermissions()

        assertTrue(service.testIsPermissionAllGranted())
    }

    @Test
    fun getPermissionInfo_returnsCorrectMap() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(
            context,
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )

        val info = service.getPermissionInfo()
        assertEquals(2, info.size)
        assertEquals(true, info[Manifest.permission.CAMERA])
        assertEquals(false, info[Manifest.permission.RECORD_AUDIO])
    }

    @Test
    fun getPermissionInfo_withNoPermissions_returnsEmptyMap() {
        val service = TestSystemService(context, null)

        val info = service.getPermissionInfo()
        assertTrue(info.isEmpty())
    }

    @Test
    fun isPermissionGranted_returnsTrueForGrantedPermission() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(
            context,
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )

        assertTrue(service.isPermissionGranted(Manifest.permission.CAMERA))
    }

    @Test
    fun isPermissionGranted_returnsFalseForDeniedPermission() {
        shadowApp.grantPermissions(Manifest.permission.CAMERA)
        val service = TestSystemService(
            context,
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )

        assertFalse(service.isPermissionGranted(Manifest.permission.RECORD_AUDIO))
    }

    @Test
    fun onDestroy_callsSuccessfully() {
        val service = TestSystemService(context, null)
        service.onDestroy()
    }

    private class TestSystemService(
        context: Context,
        permissions: List<String>?
    ) : BaseSystemService(context, permissions) {

        fun testIsPermissionAllGranted() = isPermissionAllGranted()
        fun testGetDeniedPermissionList() = getDeniedPermissionList()

        fun testTryCatch(): String {
            return tryCatchSystemManager("default") {
                "success"
            }
        }

        fun testTryCatchWithException(): String {
            return tryCatchSystemManager("default") {
                throw RuntimeException("Test exception")
            }
        }
    }
}
