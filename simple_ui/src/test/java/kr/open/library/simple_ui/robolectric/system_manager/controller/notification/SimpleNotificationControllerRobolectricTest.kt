package kr.open.library.simple_ui.robolectric.system_manager.controller.notification

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.controller.notification.SimpleNotificationController
import kr.open.library.simple_ui.system_manager.controller.notification.vo.NotificationStyle
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimpleNotificationOptionVo
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimpleNotificationType
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimplePendingIntentOptionVo
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimpleProgressNotificationOptionVo
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
        controller = SimpleNotificationController(application, SimpleNotificationType.ACTIVITY)
        shadowNotificationManager = Shadows.shadowOf(controller.notificationManager)
    }

    @After
    fun tearDown() {
        controller.cleanup()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun createChannel_createsAndRegistersChannel() {
        val channelId = "test_channel"
        val channelName = "Test Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH

        controller.createChannel(channelId, channelName, importance, "Test Description")

        val channel = controller.notificationManager.getNotificationChannel(channelId)
        assertNotNull(channel)
        assertEquals(channelId, channel.id)
        assertEquals(channelName, channel.name.toString())
        assertEquals(importance, channel.importance)
        assertEquals("Test Description", channel.description)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun createChannel_withChannelObject_registersChannel() {
        val channel = NotificationChannel(
            "custom_channel",
            "Custom Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        controller.createChannel(channel)

        val registeredChannel = controller.notificationManager.getNotificationChannel("custom_channel")
        assertNotNull(registeredChannel)
        assertEquals("custom_channel", registeredChannel.id)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun createChannel_withParams_registersChannelWithDescription() {
        val channelId = "param_channel"
        val channelName = "Param Channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val description = "Channel created via params"

        controller.createChannel(channelId, channelName, importance, description)

        val registeredChannel = controller.notificationManager.getNotificationChannel(channelId)
        assertNotNull(registeredChannel)
        assertEquals(channelId, registeredChannel.id)
        assertEquals(channelName, registeredChannel.name.toString())
        assertEquals(importance, registeredChannel.importance)
        assertEquals(description, registeredChannel.description)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun createChannel_withoutDescription_leavesDescriptionNull() {
        val channelId = "no_desc_channel"
        controller.createChannel(channelId, "No Desc", NotificationManager.IMPORTANCE_DEFAULT, null)

        val registeredChannel = controller.notificationManager.getNotificationChannel(channelId)
        assertNotNull(registeredChannel)
        assertEquals(channelId, registeredChannel.id)
        assertEquals("No Desc", registeredChannel.name.toString())
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, registeredChannel.importance)
        assertEquals(null, registeredChannel.description)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_createsNotification() {
        val option = SimpleNotificationOptionVo(
            notificationId = 1,
            title = "Test Title",
            content = "Test Content",
            smallIcon = android.R.drawable.ic_dialog_info,
            style = NotificationStyle.DEFAULT
        )

        val result = controller.showNotification(option)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
        val notification = shadowNotificationManager.getNotification(1)
        assertNotNull(notification)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_bigPictureStyle_createsNotificationWithBigPicture() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val option = SimpleNotificationOptionVo(
            notificationId = 4,
            title = "Big Picture",
            content = "Content",
            smallIcon = android.R.drawable.ic_dialog_info,
            largeIcon = bitmap,
            style = NotificationStyle.BIG_PICTURE
        )

        val result = controller.showNotification(option)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_bigTextStyle_createsNotificationWithBigText() {
        val option = SimpleNotificationOptionVo(
            notificationId = 5,
            title = "Big Text",
            content = "Short content",
            smallIcon = android.R.drawable.ic_dialog_info,
            snippet = "This is a very long text that should be displayed in expanded view",
            style = NotificationStyle.BIG_TEXT
        )

        val result = controller.showNotification(option)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showProgressNotification_createsProgressNotification() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 6,
            title = "Download",
            content = "Downloading...",
            smallIcon = android.R.drawable.ic_dialog_info,
            progressPercent = 50
        )

        val result = controller.showProgressNotification(option)

        assertTrue(result)
        assertEquals(1, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun updateProgress_validRange_updatesProgress() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 7,
            title = "Download",
            progressPercent = 0
        )

        controller.showProgressNotification(option)
        val result = controller.updateProgress(7, 75)

        assertTrue(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun updateProgress_invalidRangeTooHigh_returnsFalse() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 8,
            progressPercent = 0
        )

        controller.showProgressNotification(option)
        val result = controller.updateProgress(8, 101)

        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun updateProgress_invalidRangeNegative_returnsFalse() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 9,
            progressPercent = 0
        )

        controller.showProgressNotification(option)
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
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 10,
            title = "Download",
            progressPercent = 50
        )

        controller.showProgressNotification(option)
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
        val option = SimpleNotificationOptionVo(
            notificationId = 11,
            title = "Test",
            smallIcon = android.R.drawable.ic_dialog_info
        )

        controller.showNotification(option)
        assertEquals(1, shadowNotificationManager.size())

        val result = controller.cancelNotification(notificationId = 11)

        assertTrue(result)
        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun cancelAll_cancelsAllNotifications() {
        val option1 = SimpleNotificationOptionVo(notificationId = 12, smallIcon = android.R.drawable.ic_dialog_info)
        val option2 = SimpleNotificationOptionVo(notificationId = 13, smallIcon = android.R.drawable.ic_dialog_info)

        controller.showNotification(option1)
        controller.showNotification(option2)
        assertEquals(2, shadowNotificationManager.size())

        controller.cancelAll()

        assertEquals(0, shadowNotificationManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getClickShowActivityPendingIntent_createsActivityPendingIntent() {
        val intent = Intent(application, Application::class.java)
        val pendingIntentOption = SimplePendingIntentOptionVo(
            actionId = 1,
            clickIntent = intent
        )

        val pendingIntent = controller.getClickShowActivityPendingIntent(pendingIntentOption)

        assertNotNull(pendingIntent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getClickShowServicePendingIntent_createsServicePendingIntent() {
        val intent = Intent(application, Application::class.java)
        val pendingIntentOption = SimplePendingIntentOptionVo(
            actionId = 2,
            clickIntent = intent
        )

        val pendingIntent = controller.getClickShowServicePendingIntent(pendingIntentOption)

        assertNotNull(pendingIntent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun getClickShowBroadcastPendingIntent_createsBroadcastPendingIntent() {
        val intent = Intent(application, Application::class.java)
        val pendingIntentOption = SimplePendingIntentOptionVo(
            actionId = 3,
            clickIntent = intent
        )

        val pendingIntent = controller.getClickShowBroadcastPendingIntent(pendingIntentOption)

        assertNotNull(pendingIntent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun init_createsDefaultChannel() {
        // Controller 생성 시 기본 채널이 자동으로 생성됨
        val testController = SimpleNotificationController(application, SimpleNotificationType.ACTIVITY)

        val channels = shadowNotificationManager.notificationChannels
        assertTrue(channels.isNotEmpty())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun cleanup_clearsProgressBuilders() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 14,
            progressPercent = 50
        )

        controller.showProgressNotification(option)
        controller.cleanup()

        // cleanup 후 업데이트 시도는 실패해야 함
        val result = controller.updateProgress(14, 75)
        assertFalse(result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_withServiceType_setsServicePendingIntent() {
        val serviceController = SimpleNotificationController(application, SimpleNotificationType.SERVICE)
        val serviceShadow = Shadows.shadowOf(serviceController.notificationManager)

        val option = SimpleNotificationOptionVo(
            notificationId = 20,
            smallIcon = android.R.drawable.ic_dialog_info,
            clickIntent = Intent(application, Application::class.java)
        )

        val result = serviceController.showNotification(option)

        assertTrue(result)
        val notification = serviceShadow.getNotification(20)
        val pendingIntent = notification?.contentIntent
        assertNotNull(pendingIntent)
        val shadowPendingIntent: ShadowPendingIntent = Shadows.shadowOf(requireNotNull(pendingIntent))
        assertTrue(shadowPendingIntent.isServiceIntent)

        serviceController.cleanup()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_withBroadcastType_setsBroadcastPendingIntent() {
        val broadcastController = SimpleNotificationController(application, SimpleNotificationType.BROADCAST)
        val broadcastShadow = Shadows.shadowOf(broadcastController.notificationManager)

        val option = SimpleNotificationOptionVo(
            notificationId = 21,
            smallIcon = android.R.drawable.ic_dialog_info,
            clickIntent = Intent(application, Application::class.java)
        )

        val result = broadcastController.showNotification(option)

        assertTrue(result)
        val notification = broadcastShadow.getNotification(21)
        val pendingIntent = notification?.contentIntent
        assertNotNull(pendingIntent)
        val shadowPendingIntent: ShadowPendingIntent = Shadows.shadowOf(requireNotNull(pendingIntent))
        assertTrue(shadowPendingIntent.isBroadcastIntent)

        broadcastController.cleanup()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showProgressNotification_setsContentIntentWhenProvided() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 22,
            smallIcon = android.R.drawable.ic_dialog_info,
            progressPercent = 10,
            clickIntent = Intent(application, Application::class.java)
        )

        val result = controller.showProgressNotification(option)

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
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 30,
            progressPercent = 5
        )

        mockStatic(Executors::class.java).use { executorsMock ->
            val recordingExecutor = RecordingScheduledExecutor()
            executorsMock.`when`<ScheduledExecutorService> { Executors.newSingleThreadScheduledExecutor() }
                .thenReturn(recordingExecutor)

            controller.showProgressNotification(option)

            val progressMapField = SimpleNotificationController::class.java.getDeclaredField("progressBuilders").apply {
                isAccessible = true
            }
            @Suppress("UNCHECKED_CAST")
            val progressBuilders = progressMapField.get(controller) as MutableMap<Int, Any>
            val info = requireNotNull(progressBuilders[30])
            val infoClass = info::class.java
            val builderField = infoClass.getDeclaredField("builder").apply { isAccessible = true }
            val builder = builderField.get(info) as NotificationCompat.Builder
            val constructor = infoClass.getDeclaredConstructor(NotificationCompat.Builder::class.java, Long::class.javaPrimitiveType).apply {
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
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 31,
            progressPercent = 10
        )

        mockStatic(Executors::class.java).use { executorsMock ->
            val recordingExecutor = RecordingScheduledExecutor()
            executorsMock.`when`<ScheduledExecutorService> { Executors.newSingleThreadScheduledExecutor() }
                .thenReturn(recordingExecutor)

            controller.showProgressNotification(option)

            val progressMapField = SimpleNotificationController::class.java.getDeclaredField("progressBuilders").apply {
                isAccessible = true
            }
            @Suppress("UNCHECKED_CAST")
            val progressBuilders = progressMapField.get(controller) as MutableMap<Int, Any>
            val info = requireNotNull(progressBuilders[31])
            val infoClass = info::class.java
            val builderField = infoClass.getDeclaredField("builder").apply { isAccessible = true }
            val builder = builderField.get(info) as NotificationCompat.Builder
            val constructor = infoClass.getDeclaredConstructor(NotificationCompat.Builder::class.java, Long::class.javaPrimitiveType).apply {
                isAccessible = true
            }
            val staleTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(31)
            progressBuilders[31] = constructor.newInstance(builder, staleTime)

            mockStatic(Logx::class.java).use { logxMock ->
                logxMock.`when`<Unit> { Logx.d("Removed stale progress notification: 31") }
                    .thenThrow(RuntimeException("boom"))
                recordingExecutor.runScheduledTask()
            }

            assertFalse(progressBuilders.containsKey(31))
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun progressCleanupScheduler_whenExecutorCreationFails_doesNotStart() {
        val option = SimpleProgressNotificationOptionVo(
            notificationId = 32,
            progressPercent = 5
        )

        mockStatic(Executors::class.java).use { executorsMock ->
            executorsMock.`when`<ScheduledExecutorService> { Executors.newSingleThreadScheduledExecutor() }
                .thenThrow(RuntimeException("boom"))

            val result = controller.showProgressNotification(option)

            assertTrue(result)
            assertEquals(null, controller.getCleanupSchedulerForTest())
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_withProgressStyle_fallsBackToDefaultBuilder() {
        val option = SimpleNotificationOptionVo(
            notificationId = 23,
            smallIcon = android.R.drawable.ic_dialog_info,
            style = NotificationStyle.PROGRESS
        )

        val result = controller.showNotification(option)

        assertTrue(result)
        assertNotNull(shadowNotificationManager.getNotification(23))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_bigPictureWithoutLargeIcon_stillCreatesNotification() {
        val option = SimpleNotificationOptionVo(
            notificationId = 24,
            smallIcon = android.R.drawable.ic_dialog_info,
            style = NotificationStyle.BIG_PICTURE
        )

        val result = controller.showNotification(option)

        assertTrue(result)
        assertNotNull(shadowNotificationManager.getNotification(24))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotification_bigTextWithoutSnippet_stillCreatesNotification() {
        val option = SimpleNotificationOptionVo(
            notificationId = 25,
            smallIcon = android.R.drawable.ic_dialog_info,
            style = NotificationStyle.BIG_TEXT
        )

        val result = controller.showNotification(option)

        assertTrue(result)
        assertNotNull(shadowNotificationManager.getNotification(25))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotificationBigImage_delegatesToShowNotification() {
        val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val option = SimpleNotificationOptionVo(
            notificationId = 26,
            smallIcon = android.R.drawable.ic_dialog_info,
            largeIcon = bitmap
        )

        val result = controller.showNotificationBigImage(option)

        assertTrue(result)
        assertNotNull(shadowNotificationManager.getNotification(26))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun showNotificationBigText_delegatesToShowNotification() {
        val option = SimpleNotificationOptionVo(
            notificationId = 27,
            smallIcon = android.R.drawable.ic_dialog_info,
            snippet = "Long description"
        )

        val result = controller.showNotificationBigText(option)

        assertTrue(result)
        assertNotNull(shadowNotificationManager.getNotification(27))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun notify_postsProvidedNotification() {
        val builder = NotificationCompat.Builder(application, "default_notification_channel")
            .setContentTitle("Direct")
            .setSmallIcon(android.R.drawable.ic_dialog_info)

        controller.notify(28, builder.build())

        assertNotNull(shadowNotificationManager.getNotification(28))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun cancelNotification_withTagRemovesTaggedNotification() {
        val builder = NotificationCompat.Builder(application, "default_notification_channel")
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
        val tiramisuController = SimpleNotificationController(application, SimpleNotificationType.ACTIVITY)

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
        unit: TimeUnit
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

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = true

    override fun execute(command: Runnable) {
        command.run()
    }

    override fun <V : Any?> submit(task: Callable<V>): Future<V> =
        throw UnsupportedOperationException()

    override fun submit(task: Runnable): Future<*> =
        throw UnsupportedOperationException()

    override fun <V : Any?> submit(task: Runnable, result: V): Future<V> =
        throw UnsupportedOperationException()

    override fun <T : Any?> schedule(callable: Callable<T>, delay: Long, unit: TimeUnit): ScheduledFuture<T> =
        throw UnsupportedOperationException()

    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> =
        throw UnsupportedOperationException()

    override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> =
        throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(tasks: Collection<Callable<T>>): MutableList<Future<T>> =
        throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit
    ): MutableList<Future<T>> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(tasks: Collection<Callable<T>>): T =
        throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T =
        throw UnsupportedOperationException()
}

private object ImmediateScheduledFuture : ScheduledFuture<Unit> {
    override fun getDelay(unit: TimeUnit): Long = 0
    override fun compareTo(other: Delayed): Int = 0
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = true
    override fun isCancelled(): Boolean = false
    override fun isDone(): Boolean = true
    override fun get(): Unit = Unit
    override fun get(timeout: Long, unit: TimeUnit): Unit = Unit
}

private fun SimpleNotificationController.setCleanupSchedulerForTest(executor: ScheduledExecutorService?) {
    val field = SimpleNotificationController::class.java.getDeclaredField("cleanupScheduler").apply { isAccessible = true }
    field.set(this, executor)
}

private fun SimpleNotificationController.getCleanupSchedulerForTest(): ScheduledExecutorService? {
    val field = SimpleNotificationController::class.java.getDeclaredField("cleanupScheduler").apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as? ScheduledExecutorService
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

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = false

    override fun execute(command: Runnable) {
        command.run()
    }

    override fun <V : Any?> submit(task: Callable<V>): Future<V> =
        throw UnsupportedOperationException()

    override fun submit(task: Runnable): Future<*> =
        throw UnsupportedOperationException()

    override fun <V : Any?> submit(task: Runnable, result: V): Future<V> =
        throw UnsupportedOperationException()

    override fun <T : Any?> schedule(callable: Callable<T>, delay: Long, unit: TimeUnit): ScheduledFuture<T> =
        throw UnsupportedOperationException()

    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> =
        throw UnsupportedOperationException()

    override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> = throw UnsupportedOperationException()

    override fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(tasks: Collection<Callable<T>>): MutableList<Future<T>> =
        throw UnsupportedOperationException()

    override fun <T : Any?> invokeAll(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit
    ): MutableList<Future<T>> = throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(tasks: Collection<Callable<T>>): T =
        throw UnsupportedOperationException()

    override fun <T : Any?> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T =
        throw UnsupportedOperationException()
}

private class InterruptScheduler : AwaitFalseScheduler() {
    @Throws(InterruptedException::class)
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        throw InterruptedException("forced")
    }
}
