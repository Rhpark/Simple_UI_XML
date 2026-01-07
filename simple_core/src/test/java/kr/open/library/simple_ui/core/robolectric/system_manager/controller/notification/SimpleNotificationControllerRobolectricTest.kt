package kr.open.library.simple_ui.core.robolectric.system_manager.controller.notification

import android.Manifest
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationController
import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationType
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigPictureNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigTextNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.DefaultNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.ProgressNotificationOption
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager
import org.robolectric.shadows.ShadowPendingIntent
import java.util.concurrent.Callable
import java.util.concurrent.Delayed
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class SimpleNotificationControllerRobolectricTest {
    private lateinit var application: Application
    private lateinit var controller: SimpleNotificationController
    private lateinit var shadowNotificationManager: ShadowNotificationManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        controller = SimpleNotificationController(application)
        shadowNotificationManager = Shadows.shadowOf(controller.notificationManager)
    }

    @After
    fun tearDown() {
        controller.cleanup()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_createsNotification() {
        val option =
            DefaultNotificationOption(
                notificationId = 1,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Test Title",
                content = "Test Content",
            )

        val result = controller.showNotification(option, SimpleNotificationType.ACTIVITY)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
        val notification = shadowNotificationManager.getNotification(1)
        assertNotNull(notification)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_bigPictureStyle_createsNotificationWithBigPicture() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val option =
            BigPictureNotificationOption(
                notificationId = 4,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Big Picture",
                content = "Content",
                bigPicture = bitmap,
            )

        val result = controller.showNotification(option, SimpleNotificationType.ACTIVITY)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_bigTextStyle_createsNotificationWithBigText() {
        val option =
            BigTextNotificationOption(
                notificationId = 5,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Big Text",
                content = "Short content",
                snippet = "This is a very long text that should be displayed in expanded view",
            )

        val result = controller.showNotification(option, SimpleNotificationType.ACTIVITY)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_progress_createsNotificationProgress() {
        val option =
            ProgressNotificationOption(
                notificationId = 6,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Download",
                content = "Downloading...",
                progressPercent = 50,
            )

        val result = controller.showNotification(option, SimpleNotificationType.ACTIVITY)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun updateProgress_validRange_updatesProgress() {
        val option =
            ProgressNotificationOption(
                notificationId = 7,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Download",
                content = null,
                progressPercent = 0,
            )

        controller.showNotification(option, SimpleNotificationType.ACTIVITY)
        val result = controller.updateProgress(7, 75)

        assertTrue(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun updateProgress_invalidRangeTooHigh_returnsFalse() {
        val option =
            ProgressNotificationOption(
                notificationId = 8,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Download",
                content = null,
                progressPercent = 0,
            )

        controller.showNotification(option, SimpleNotificationType.ACTIVITY)
        val result = controller.updateProgress(8, 101)

        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun updateProgress_invalidRangeNegative_returnsFalse() {
        val option =
            ProgressNotificationOption(
                notificationId = 9,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Download",
                content = null,
                progressPercent = 0,
            )

        controller.showNotification(option, SimpleNotificationType.ACTIVITY)
        val result = controller.updateProgress(9, -1)

        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun updateProgress_notExistingNotification_returnsFalse() {
        val result = controller.updateProgress(999, 50)

        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun completeProgress_completesProgressNotification() {
        val option =
            ProgressNotificationOption(
                notificationId = 10,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Download",
                content = null,
                progressPercent = 50,
            )

        controller.showNotification(option, SimpleNotificationType.ACTIVITY)
        val result = controller.completeProgress(10, "Download Complete!")

        assertTrue(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun completeProgress_notExistingNotification_returnsFalse() {
        val result = controller.completeProgress(999)

        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun cancelNotification_cancelsSpecificNotification() {
        val option =
            DefaultNotificationOption(
                notificationId = 11,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Test",
                content = "Content",
            )

        controller.showNotification(option, SimpleNotificationType.ACTIVITY)
        assertEquals(1, shadowNotificationManager.size())

        val result = controller.cancelNotification(notificationId = 11)

        assertTrue(result)
        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun cancelAll_cancelsAllNotifications() {
        val option1 =
            DefaultNotificationOption(
                notificationId = 12,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Title 1",
                content = "Content 1",
            )
        val option2 =
            DefaultNotificationOption(
                notificationId = 13,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Title 2",
                content = "Content 2",
            )

        controller.showNotification(option1, SimpleNotificationType.ACTIVITY)
        controller.showNotification(option2, SimpleNotificationType.ACTIVITY)
        assertEquals(2, shadowNotificationManager.size())

        controller.cancelAll()

        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun init_createsDefaultChannel() {
        val testController = SimpleNotificationController(application)

        val channels = shadowNotificationManager.notificationChannels
        assertTrue(channels.isNotEmpty())

        testController.cleanup()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun cleanup_clearsProgressBuilders() {
        val option =
            ProgressNotificationOption(
                notificationId = 14,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Download",
                content = null,
                progressPercent = 50,
            )

        controller.showNotification(option, SimpleNotificationType.ACTIVITY)
        controller.cleanup()

        val result = controller.updateProgress(14, 75)
        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_withServiceType_setsServicePendingIntent() {
        val option =
            DefaultNotificationOption(
                notificationId = 20,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Service",
                content = "Content",
                clickIntent = Intent(application, Application::class.java),
            )

        val result = controller.showNotification(option, SimpleNotificationType.SERVICE)

        assertTrue(result)
        val notification = shadowNotificationManager.getNotification(20)
        val pendingIntent = notification?.contentIntent
        assertNotNull(pendingIntent)
        val shadowPendingIntent: ShadowPendingIntent = Shadows.shadowOf(requireNotNull(pendingIntent))
        assertTrue(shadowPendingIntent.isServiceIntent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_withBroadcastType_setsBroadcastPendingIntent() {
        val option =
            DefaultNotificationOption(
                notificationId = 21,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Broadcast",
                content = "Content",
                clickIntent = Intent(application, Application::class.java),
            )

        val result = controller.showNotification(option, SimpleNotificationType.BROADCAST)

        assertTrue(result)
        val notification = shadowNotificationManager.getNotification(21)
        val pendingIntent = notification?.contentIntent
        assertNotNull(pendingIntent)
        val shadowPendingIntent: ShadowPendingIntent = Shadows.shadowOf(requireNotNull(pendingIntent))
        assertTrue(shadowPendingIntent.isBroadcastIntent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_progress_setsContentIntentWhenProvided() {
        val option =
            ProgressNotificationOption(
                notificationId = 22,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Progress",
                content = null,
                progressPercent = 10,
                clickIntent = Intent(application, Application::class.java),
            )

        val result = controller.showNotification(option, SimpleNotificationType.ACTIVITY)

        assertTrue(result)
        val notification = shadowNotificationManager.getNotification(22)
        val pendingIntent = notification?.contentIntent
        assertNotNull(pendingIntent)
        val shadowPendingIntent: ShadowPendingIntent = Shadows.shadowOf(requireNotNull(pendingIntent))
        assertTrue(shadowPendingIntent.isActivityIntent)
    }

    @Test
    fun cleanup_whenAwaitTerminationReturnsFalse_callsShutdownNow() {
        val scheduler = AwaitFalseScheduler()
        controller.setCleanupSchedulerForTest(scheduler)

        controller.cleanup()

        assertTrue(scheduler.shutdownNowCalled)
    }

    @Test
    fun cleanup_whenInterruptedExceptionOccurs_resetsThreadInterruptFlag() {
        val scheduler = InterruptScheduler()
        controller.setCleanupSchedulerForTest(scheduler)
        assertFalse(Thread.currentThread().isInterrupted)

        controller.cleanup()

        assertTrue(scheduler.shutdownNowCalled)
        assertTrue("Thread interrupt flag should be set", Thread.interrupted())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun progressCleanupScheduler_removesStaleEntries() {
        val option =
            ProgressNotificationOption(
                notificationId = 30,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Progress",
                content = null,
                progressPercent = 5,
            )

        mockStatic(Executors::class.java).use { executorsMock ->
            val recordingExecutor = RecordingScheduledExecutor()
            executorsMock
                .`when`<ScheduledExecutorService> { Executors.newSingleThreadScheduledExecutor() }
                .thenReturn(recordingExecutor)

            controller.showNotification(option, SimpleNotificationType.ACTIVITY)

            val progressBuilders = controller.getProgressBuildersForTest()
            val info = requireNotNull(progressBuilders[30])
            val infoClass = info::class.java
            val builderField = infoClass.getDeclaredField("builder").apply { isAccessible = true }
            val builder = builderField.get(info) as NotificationCompat.Builder
            val constructor =
                infoClass
                    .getDeclaredConstructor(
                        NotificationCompat.Builder::class.java,
                        Long::class.javaPrimitiveType,
                    ).apply {
                        isAccessible = true
                    }
            val staleTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(31)
            progressBuilders[30] = constructor.newInstance(builder, staleTime)

            recordingExecutor.runScheduledTask()

            assertFalse(progressBuilders.containsKey(30))
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun progressCleanupScheduler_handlesExceptionsInsideTask() {
        val option =
            ProgressNotificationOption(
                notificationId = 31,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Progress",
                content = null,
                progressPercent = 10,
            )

        mockStatic(Executors::class.java).use { executorsMock ->
            val recordingExecutor = RecordingScheduledExecutor()
            executorsMock
                .`when`<ScheduledExecutorService> { Executors.newSingleThreadScheduledExecutor() }
                .thenReturn(recordingExecutor)

            controller.showNotification(option, SimpleNotificationType.ACTIVITY)

            val progressBuilders = controller.getProgressBuildersForTest()
            val info = requireNotNull(progressBuilders[31])
            val infoClass = info::class.java
            val builderField = infoClass.getDeclaredField("builder").apply { isAccessible = true }
            val builder = builderField.get(info) as NotificationCompat.Builder
            val constructor =
                infoClass
                    .getDeclaredConstructor(
                        NotificationCompat.Builder::class.java,
                        Long::class.javaPrimitiveType,
                    ).apply {
                        isAccessible = true
                    }
            val staleTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(31)
            progressBuilders[31] = constructor.newInstance(builder, staleTime)

            mockStatic(Logx::class.java).use { logxMock ->
                logxMock
                    .`when`<Unit> { Logx.d("Removed stale progress notification: 31") }
                    .thenThrow(RuntimeException("boom"))
                recordingExecutor.runScheduledTask()
            }

            assertFalse(progressBuilders.containsKey(31))
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun progressCleanupScheduler_whenExecutorCreationFails_doesNotStart() {
        val option =
            ProgressNotificationOption(
                notificationId = 32,
                smallIcon = android.R.drawable.ic_dialog_info,
                title = "Progress",
                content = null,
                progressPercent = 5,
            )

        mockStatic(Executors::class.java).use { executorsMock ->
            executorsMock
                .`when`<ScheduledExecutorService> { Executors.newSingleThreadScheduledExecutor() }
                .thenThrow(RuntimeException("boom"))

            val result = controller.showNotification(option, SimpleNotificationType.ACTIVITY)

            assertTrue(result)
            assertEquals(null, controller.getCleanupSchedulerForTest())
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun notify_postsProvidedNotification() {
        val builder =
            NotificationCompat
                .Builder(application, "default_notification_channel")
                .setContentTitle("Direct")
                .setSmallIcon(android.R.drawable.ic_dialog_info)

        controller.notify(28, builder.build())

        assertNotNull(shadowNotificationManager.getNotification(28))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun cancelNotification_withTagRemovesTaggedNotification() {
        val builder =
            NotificationCompat
                .Builder(application, "default_notification_channel")
                .setContentTitle("Tagged")
                .setSmallIcon(android.R.drawable.ic_dialog_info)

        controller.notificationManager.notify("tagged", 29, builder.build())
        assertEquals(1, shadowNotificationManager.size())

        val result = controller.cancelNotification(tag = "tagged", notificationId = 29)

        assertTrue(result)
        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun init_onTiramisu_requiresPostNotificationPermission() {
        val tiramisuController = SimpleNotificationController(application)

        val permissionInfo = tiramisuController.getPermissionInfo()

        assertTrue(permissionInfo.containsKey(Manifest.permission.POST_NOTIFICATIONS))

        tiramisuController.cleanup()
    }
}

private class RecordingScheduledExecutor : ScheduledExecutorService {
    private var shutdown = false
    private var scheduledRunnable: Runnable? = null

    fun runScheduledTask() {
        scheduledRunnable?.run()
    }

    override fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit,
    ): ScheduledFuture<*> {
        scheduledRunnable = command
        return ImmediateScheduledFuture
    }

    override fun shutdown() {
        shutdown = true
    }

    override fun shutdownNow(): MutableList<Runnable> {
        shutdown = true
        return mutableListOf()
    }

    override fun isShutdown(): Boolean = shutdown

    override fun isTerminated(): Boolean = shutdown

    override fun awaitTermination(
        timeout: Long,
        unit: TimeUnit,
    ): Boolean = true

    override fun execute(command: Runnable) {
        command.run()
    }

    override fun <V : Any?> submit(task: Callable<V>): Future<V> = throw UnsupportedOperationException()

    override fun submit(task: Runnable): Future<*> = throw UnsupportedOperationException()

    override fun <V : Any?> submit(
        task: Runnable,
        result: V,
    ): Future<V> = throw UnsupportedOperationException()

    override fun <T : Any?> schedule(
        callable: Callable<T>,
        delay: Long,
        unit: TimeUnit,
    ): ScheduledFuture<T> = throw UnsupportedOperationException()

    override fun schedule(
        command: Runnable,
        delay: Long,
        unit: TimeUnit,
    ): ScheduledFuture<*> = throw UnsupportedOperationException()

    override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
    ): ScheduledFuture<*> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(tasks: Collection<Callable<T>>): MutableList<Future<T>> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit,
    ): MutableList<Future<T>> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(tasks: Collection<Callable<T>>): T = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit,
    ): T = throw UnsupportedOperationException()
}

private object ImmediateScheduledFuture : ScheduledFuture<Unit> {
    override fun getDelay(unit: TimeUnit): Long = 0

    override fun compareTo(other: Delayed): Int = 0

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = true

    override fun isCancelled(): Boolean = false

    override fun isDone(): Boolean = true

    override fun get(): Unit = Unit

    override fun get(
        timeout: Long,
        unit: TimeUnit,
    ): Unit = Unit
}

private fun SimpleNotificationController.getBuilderForTest(): Any {
    val field = SimpleNotificationController::class.java.getDeclaredField("builder").apply {
        isAccessible = true
    }
    return field.get(this)
}

private fun SimpleNotificationController.getProgressBuildersForTest(): MutableMap<Int, Any> {
    val builder = getBuilderForTest()
    val field = builder.javaClass.getDeclaredField("progressBuilders").apply {
        isAccessible = true
    }
    @Suppress("UNCHECKED_CAST")
    return field.get(builder) as MutableMap<Int, Any>
}

private fun SimpleNotificationController.setCleanupSchedulerForTest(executor: ScheduledExecutorService?) {
    val builder = getBuilderForTest()
    val field = builder.javaClass.getDeclaredField("cleanupScheduler").apply {
        isAccessible = true
    }
    field.set(builder, executor)
}

private fun SimpleNotificationController.getCleanupSchedulerForTest(): ScheduledExecutorService? {
    val builder = getBuilderForTest()
    val field = builder.javaClass.getDeclaredField("cleanupScheduler").apply {
        isAccessible = true
    }
    @Suppress("UNCHECKED_CAST")
    return field.get(builder) as? ScheduledExecutorService
}

private open class AwaitFalseScheduler : ScheduledExecutorService {
    var shutdownCalled = false
    var shutdownNowCalled = false

    override fun shutdown() {
        shutdownCalled = true
    }

    override fun shutdownNow(): MutableList<Runnable> {
        shutdownCalled = true
        shutdownNowCalled = true
        return mutableListOf()
    }

    override fun isShutdown(): Boolean = shutdownCalled

    override fun isTerminated(): Boolean = shutdownCalled

    override fun awaitTermination(
        timeout: Long,
        unit: TimeUnit,
    ): Boolean = false

    override fun execute(command: Runnable) {
        command.run()
    }

    override fun <V : Any?> submit(task: Callable<V>): Future<V> = throw UnsupportedOperationException()

    override fun submit(task: Runnable): Future<*> = throw UnsupportedOperationException()

    override fun <V : Any?> submit(
        task: Runnable,
        result: V,
    ): Future<V> = throw UnsupportedOperationException()

    override fun <T : Any?> schedule(
        callable: Callable<T>,
        delay: Long,
        unit: TimeUnit,
    ): ScheduledFuture<T> = throw UnsupportedOperationException()

    override fun schedule(
        command: Runnable,
        delay: Long,
        unit: TimeUnit,
    ): ScheduledFuture<*> = throw UnsupportedOperationException()

    override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
    ): ScheduledFuture<*> = throw UnsupportedOperationException()

    override fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit,
    ): ScheduledFuture<*> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(tasks: Collection<Callable<T>>): MutableList<Future<T>> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit,
    ): MutableList<Future<T>> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(tasks: Collection<Callable<T>>): T = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit,
    ): T = throw UnsupportedOperationException()
}

private class InterruptScheduler : AwaitFalseScheduler() {
    @Throws(InterruptedException::class)
    override fun awaitTermination(
        timeout: Long,
        unit: TimeUnit,
    ): Boolean = throw InterruptedException("forced")
}
