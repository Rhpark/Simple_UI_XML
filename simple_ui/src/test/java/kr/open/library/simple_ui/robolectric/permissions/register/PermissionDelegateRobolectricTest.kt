package kr.open.library.simple_ui.robolectric.permissions.register

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.Fragment
import junit.framework.TestCase.assertNull
import kr.open.library.simple_ui.permissions.manager.PermissionManager
import kr.open.library.simple_ui.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.permissions.vo.PermissionSpecialType
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import kotlinx.coroutines.test.runTest
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class PermissionDelegateRobolectricTest {

    private lateinit var activityController: ActivityController<FragmentActivity>
    private lateinit var activity: FragmentActivity
    private lateinit var delegate: PermissionDelegate<FragmentActivity>
    private val permissionManager = PermissionManager.getInstance()

    private lateinit var pendingRequestsField: Field
    private lateinit var activeDelegatesField: Field
    private lateinit var currentRequestIdField: Field
    private lateinit var cleanupMethod: Method

    @Before
    fun setUp() {
        pendingRequestsField = permissionManager.javaClass.getDeclaredField("pendingRequests").apply { isAccessible = true }
        activeDelegatesField = permissionManager.javaClass.getDeclaredField("activeDelegates").apply { isAccessible = true }

        activityController = Robolectric.buildActivity(FragmentActivity::class.java)
        activityController.create()
        activity = activityController.get()
        // Create delegate BEFORE starting/resuming the activity
        delegate = PermissionDelegate(activity)

        currentRequestIdField = PermissionDelegate::class.java.getDeclaredField("currentRequestId").apply { isAccessible = true }
        cleanupMethod = PermissionDelegate::class.java.getDeclaredMethod("cleanup").apply { isAccessible = true }

        clearPermissionManagerState()

        // Now start and resume the activity
        activityController.start().resume().visible()
    }

    @After
    fun tearDown() {
        clearPermissionManagerState()
        runCatching { activityController.destroy() }
    }

    @Test
    fun constructor_withActivityInitializesLaunchers() {
        val launcher = delegate.getSpecialLauncher(PermissionSpecialType.SYSTEM_ALERT_WINDOW.permission)
        Assert.assertNotNull("special launcher should be registered", launcher)
    }

    @Test
    fun constructor_withUnsupportedContextThrows() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            PermissionDelegate(Any())
        }
    }

    @Test
    fun arePermissionsGranted_reflectsContextPermissions() {
        val permission = Manifest.permission.CAMERA
        shadowOf(activity.application).grantPermissions(permission)
        Assert.assertTrue(delegate.arePermissionsGranted(listOf(permission)))

        shadowOf(activity.application).denyPermissions(permission)
        Assert.assertFalse(delegate.arePermissionsGranted(listOf(permission)))
        Assert.assertEquals(listOf(permission), delegate.getDeniedPermissions(listOf(permission)))
    }

    @Test
    fun onSaveAndRestoreInstanceState_reregistersWhenRequestActive() {
        val requestId = UUID.randomUUID().toString()
        setCurrentRequestId(requestId)

        insertPendingRequest(requestId, listOf(Manifest.permission.CAMERA))

        val bundle = Bundle()
        delegate.onSaveInstanceState(bundle)
        Assert.assertEquals(requestId, bundle.getString(getKeyRequestId()))

        clearPermissionManagerState()
        insertPendingRequest(requestId, listOf(Manifest.permission.CAMERA))
        setCurrentRequestId(null)

        val saved = Bundle().apply { putString(getKeyRequestId(), requestId) }
        delegate.onRestoreInstanceState(saved)

        val activeDelegates = activeDelegatesField.get(permissionManager) as MutableMap<String, *>
        Assert.assertTrue(activeDelegates.containsKey(requestId))
        Assert.assertEquals(requestId, getCurrentRequestId())
    }

    @Test
    fun onRestoreInstanceState_clearsRequestWhenInactive() {
        val requestId = UUID.randomUUID().toString()
        val saved = Bundle().apply { putString(getKeyRequestId(), requestId) }

        delegate.onRestoreInstanceState(saved)

        assertNull(getCurrentRequestId())
    }

    @Test
    fun cleanup_clearsActiveRequestWhenActivityFinishes() = runTest {
        val requestId = UUID.randomUUID().toString()
        setCurrentRequestId(requestId)
        insertPendingRequest(requestId, listOf(Manifest.permission.CAMERA))
        permissionManager.registerDelegate(requestId, delegate)

        activity.finish()
        runCatching { activityController.destroy() }

        cleanupMethod.invoke(delegate)

        val pendingRequests = pendingRequestsField.get(permissionManager) as MutableMap<*, *>
        Assert.assertFalse(pendingRequests.containsKey(requestId))
        assertNull(getCurrentRequestId())
    }

    @Test
    fun cleanup_clearsWhenFragmentIsRemoving() = runTest {
        class RegisteringFragment : Fragment() {
            lateinit var delegate: PermissionDelegate<Fragment>
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                delegate = PermissionDelegate(this)
            }
        }

        val fragment = RegisteringFragment()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "test")
            .commitNow()

        val fragmentDelegate = fragment.delegate

        val currentField = PermissionDelegate::class.java.getDeclaredField("currentRequestId").apply { isAccessible = true }
        val requestId = UUID.randomUUID().toString()
        currentField.set(fragmentDelegate, requestId)

        insertPendingRequest(requestId, listOf(Manifest.permission.ACCESS_FINE_LOCATION))
        permissionManager.registerDelegate(requestId, fragmentDelegate)

        activity.supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commitNow()

        PermissionDelegate::class.java.getDeclaredMethod("cleanup").apply {
            isAccessible = true
            invoke(fragmentDelegate)
        }

        val pendingRequests = pendingRequestsField.get(permissionManager) as MutableMap<*, *>
        Assert.assertFalse(pendingRequests.containsKey(requestId))
        Assert.assertNull(currentField.get(fragmentDelegate))
    }

    @Test
    fun requestPermissions_existingRequestAddsCallback() {
        val permission = Manifest.permission.CAMERA
        val requestId = UUID.randomUUID().toString()
        setCurrentRequestId(requestId)
        val pendingRequest = insertPendingRequest(requestId, listOf(permission))

        val callbacksField = pendingRequest.javaClass.getDeclaredField("callbacks").apply { isAccessible = true }
        val callbacks = callbacksField.get(pendingRequest) as MutableList<*>

        delegate.requestPermissions(listOf(permission)) {}

        Assert.assertEquals(1, callbacks.size)
        Assert.assertEquals(requestId, getCurrentRequestId())
    }

    @Test
    fun requestPermissions_existingMismatchKeepsRequestAndLogs() {
        val requestId = UUID.randomUUID().toString()
        setCurrentRequestId(requestId)
        insertPendingRequest(requestId, listOf(Manifest.permission.CAMERA))

        delegate.requestPermissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION)) {}

        Assert.assertEquals(requestId, getCurrentRequestId())
    }

    @Test
    fun getSpecialLauncher_returnsNullForUnknownPermission() {
        Assert.assertNull(delegate.getSpecialLauncher("unknown.permission"))
    }

    @Test
    fun createSpecialLauncher_returnsLauncherForAllSpecialTypes() {
        PermissionSpecialType.entries.forEach { type ->
            Assert.assertNotNull("Launcher missing for ${type.permission}", delegate.getSpecialLauncher(type.permission))
        }
    }

    private fun insertPendingRequest(requestId: String, permissions: List<String>): Any {
        val pendingRequests = pendingRequestsField.get(permissionManager) as MutableMap<String, Any>
        val clazz = Class.forName("kr.open.library.simple_ui.permissions.manager.PermissionManager\$PendingRequest")
        val ctor = clazz.getDeclaredConstructor(MutableList::class.java, Long::class.javaPrimitiveType, List::class.java, List::class.java)
        ctor.isAccessible = true
        val pending = ctor.newInstance(
            mutableListOf<(List<String>) -> Unit>(),
            System.currentTimeMillis(),
            permissions,
            emptyList<String>()
        )
        pendingRequests[requestId] = pending
        return pending
    }

    private fun getKeyRequestId(): String {
        val field = PermissionDelegate::class.java.getDeclaredField("KEY_REQUEST_ID").apply { isAccessible = true }
        return field.get(delegate) as String
    }

    private fun getCurrentRequestId(): String? = currentRequestIdField.get(delegate) as String?

    private fun setCurrentRequestId(value: String?) {
        currentRequestIdField.set(delegate, value)
    }

    private fun clearPermissionManagerState() {
        (pendingRequestsField.get(permissionManager) as MutableMap<*, *>).clear()
        (activeDelegatesField.get(permissionManager) as MutableMap<*, *>).clear()
    }

    @Test
    fun getCurrentRequestId_returnsCurrentId() {
        val requestId = UUID.randomUUID().toString()
        setCurrentRequestId(requestId)

        Assert.assertEquals(requestId, delegate.getCurrentRequestId())
    }

    @Test
    fun getCurrentRequestId_returnsNullWhenNoRequest() {
        Assert.assertNull(delegate.getCurrentRequestId())
    }

    @Test
    fun isPermissionGranted_checksPermissionState() {
        val permission = Manifest.permission.CAMERA
        shadowOf(activity.application).denyPermissions(permission)

        Assert.assertFalse(delegate.isPermissionGranted(permission))

        shadowOf(activity.application).grantPermissions(permission)
        Assert.assertTrue(delegate.isPermissionGranted(permission))
    }

    @Test
    fun requestPermissions_startsNewRequestWhenNoActiveRequest() {
        val permission = Manifest.permission.CAMERA
        shadowOf(activity.application).denyPermissions(permission)

        var callbackInvoked = false
        delegate.requestPermissions(listOf(permission)) { deniedPermissions ->
            callbackInvoked = true
            Assert.assertEquals(listOf(permission), deniedPermissions)
        }

        // Verify request was created
        Assert.assertNotNull(getCurrentRequestId())

        // The callback is not invoked until permission result is processed
        // This tests that the request mechanism is initiated
    }

    @Test
    fun requestPermissions_raceCondition_startsNewRequest() {
        val oldRequestId = UUID.randomUUID().toString()
        setCurrentRequestId(oldRequestId)
        // Do not insert pending request - simulates REQUEST_NOT_FOUND scenario

        val permission = Manifest.permission.CAMERA
        delegate.requestPermissions(listOf(permission)) { }

        // Should detect race condition and start new request
        // The request ID will change or remain null depending on request() result
        Assert.assertTrue(getCurrentRequestId() != oldRequestId || getCurrentRequestId() == null)
    }

    @Test
    fun onRestoreInstanceState_withNullBundle_handledGracefully() {
        delegate.onRestoreInstanceState(null)
        Assert.assertNull(getCurrentRequestId())
    }

    @Test
    fun onSaveInstanceState_withNullRequestId_doesNotSaveKey() {
        setCurrentRequestId(null)
        val bundle = Bundle()
        delegate.onSaveInstanceState(bundle)

        Assert.assertFalse(bundle.containsKey(getKeyRequestId()))
    }

    @Test
    fun lifecycleObserver_onStartWithRestoredState_attemptsReregistration() {
        // This tests the ON_START branch when hasRestoredState is true
        val requestId = UUID.randomUUID().toString()
        insertPendingRequest(requestId, listOf(Manifest.permission.CAMERA))

        // Create new activity and delegate to trigger lifecycle
        val newActivityController = Robolectric.buildActivity(FragmentActivity::class.java)
        newActivityController.create()
        val newActivity = newActivityController.get()
        val newDelegate = PermissionDelegate(newActivity)

        // Restore state - this will set hasRestoredState = true
        val bundle = Bundle().apply { putString(getKeyRequestId(), requestId) }

        // Call onRestoreInstanceState through public method
        newDelegate.onRestoreInstanceState(bundle)

        // Now trigger ON_START - the delegate should attempt auto-reregistration
        newActivityController.start()

        // Clean up
        runCatching { newActivityController.destroy() }
    }

    @Test
    fun getContext_withFragment_returnsFragmentContext() {
        class TestContextFragment : Fragment() {
            lateinit var delegate: PermissionDelegate<Fragment>
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                delegate = PermissionDelegate(this)
            }
        }

        val fragment = TestContextFragment()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "test_context")
            .commitNow()

        val fragmentDelegate = fragment.delegate

        // Test that getContext() works for Fragment
        val permission = Manifest.permission.CAMERA
        shadowOf(activity.application).grantPermissions(permission)
        Assert.assertTrue(fragmentDelegate.isPermissionGranted(permission))
    }

    class TestFragment : Fragment()
}
