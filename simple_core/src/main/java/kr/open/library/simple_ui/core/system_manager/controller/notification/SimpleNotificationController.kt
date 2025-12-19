package kr.open.library.simple_ui.core.system_manager.controller.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.NotificationStyle
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimpleNotificationOptionVo
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimpleNotificationType
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimplePendingIntentOptionVo
import kr.open.library.simple_ui.core.system_manager.controller.notification.vo.SimpleProgressNotificationOptionVo
import kr.open.library.simple_ui.core.system_manager.extensions.getNotificationManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Controller for managing Android notifications with support for various styles and features.<br>
 * Provides methods for creating channels, displaying notifications, and managing progress notifications.<br><br>
 * 다양한 스타일과 기능을 지원하는 Android 알림 관리 컨트롤러입니다.<br>
 * 채널 생성, 알림 표시, 진행률 알림 관리를 위한 메서드를 제공합니다.<br>
 *
 * Required permission:<br>
 * `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`<br><br>
 * 필수 권한:<br>
 * `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`<br>
 *
 * Notification Importance Levels:<br>
 * - `IMPORTANCE_HIGH`: Urgent, makes sound and appears as heads-up<br>
 * - `IMPORTANCE_DEFAULT`: High priority, makes sound<br>
 * - `IMPORTANCE_LOW`: Medium priority, no sound<br>
 * - `IMPORTANCE_MIN`: Low priority, no sound and not shown in status bar<br><br>
 * 알림 중요도 레벨:<br>
 * - `IMPORTANCE_HIGH`: 긴급, 알림음이 울리며 헤즈업으로 표시<br>
 * - `IMPORTANCE_DEFAULT`: 높은 중요도, 알림음이 울림<br>
 * - `IMPORTANCE_LOW`: 중간 중요도, 알림음이 울리지 않음<br>
 * - `IMPORTANCE_MIN`: 낮은 중요도, 알림음이 없고 상태표시줄에 표시되지 않음<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 * @param showType Action type when notification is clicked (Activity, Service, Broadcast).<br><br>
 *                 알림 클릭 시 동작 유형 (Activity, Service, Broadcast).
 */
public open class SimpleNotificationController(
    context: Context,
    private val showType: SimpleNotificationType,
) : BaseSystemService(
        context,
        checkSdkVersion(
            Build.VERSION_CODES.TIRAMISU,
            positiveWork = { listOf(POST_NOTIFICATIONS) },
            negativeWork = { null }
        )
    ) {
    /**
     * Lazy-initialized NotificationManager instance for managing notifications.<br><br>
     * 알림 관리를 위한 지연 초기화된 NotificationManager 인스턴스입니다.<br>
     */
    public val notificationManager: NotificationManager by lazy { context.getNotificationManager() }

    init {
        // 기본 채널 자동 생성
        ensureDefaultChannel()
    }

    /**
     * Thread-safe map storing progress notification builders by notification ID.<br>
     * Used for updating progress without recreating the builder.<br><br>
     * 알림 ID별로 진행률 알림 빌더를 저장하는 스레드 세이프 맵입니다.<br>
     * 빌더를 재생성하지 않고 진행률을 업데이트하는 데 사용됩니다.<br>
     */
    private val progressBuilders = ConcurrentHashMap<Int, ProgressNotificationInfo>()

    /**
     * Scheduler for cleaning up stale progress notifications (created on demand).<br><br>
     * 오래된 진행률 알림을 정리하기 위한 스케줄러(필요 시 생성).<br>
     */
    private var cleanupScheduler: ScheduledExecutorService? = null

    /**
     * Data class holding progress notification information.<br><br>
     * 진행률 알림 정보를 담는 데이터 클래스입니다.<br>
     *
     * @param builder The notification builder instance.<br><br>
     *                알림 빌더 인스턴스.
     * @param lastUpdateTime Timestamp of last update in milliseconds.<br><br>
     *                       마지막 업데이트 시간(밀리초).
     */
    private data class ProgressNotificationInfo(
        val builder: NotificationCompat.Builder,
        val lastUpdateTime: Long = System.currentTimeMillis(),
    )

    /**
     * Currently active notification channel.<br><br>
     * 현재 활성화된 알림 채널입니다.<br>
     */
    private var currentChannel: NotificationChannel? = null

    /**
     * Companion object containing default channel constants.<br><br>
     * 기본 채널 상수를 포함하는 컴패니언 객체입니다.<br>
     */
    companion object {
        /**
         * Default notification channel ID.<br><br>
         * 기본 알림 채널 ID.<br>
         */
        private const val DEFAULT_CHANNEL_ID = "default_notification_channel"

        /**
         * Default notification channel name.<br><br>
         * 기본 알림 채널 이름.<br>
         */
        private const val DEFAULT_CHANNEL_NAME = "Default Notifications"

        /**
         * Default notification channel description.<br><br>
         * 기본 알림 채널 설명.<br>
         */
        private const val DEFAULT_CHANNEL_DESCRIPTION = "Default notification channel for the application"
    }

    /**
     * Creates and registers a NotificationChannel, then sets it as the current channel.<br><br>
     * NotificationChannel을 생성 및 등록하고 현재 채널로 설정합니다.<br>
     *
     * @param notificationChannel The notification channel to create.<br><br>
     *                            생성할 알림 채널.
     */
    public fun createChannel(notificationChannel: NotificationChannel) {
        currentChannel = notificationChannel
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * Creates and registers a notification channel with the specified parameters.<br><br>
     * 지정된 파라미터로 알림 채널을 생성하여 등록합니다.<br>
     *
     * @param channelId Channel unique identifier.<br><br>
     *                  채널 고유 식별자.
     * @param channelName Channel display name.<br><br>
     *                    채널 표시 이름.
     * @param importance Channel importance level (IMPORTANCE_HIGH, DEFAULT, LOW, MIN).<br><br>
     *                   채널 중요도 레벨 (IMPORTANCE_HIGH, DEFAULT, LOW, MIN).
     * @param description Channel description (optional).<br><br>
     *                    채널 설명 (선택사항).
     */
    public fun createChannel(
        channelId: String,
        channelName: String,
        importance: Int,
        description: String? = null,
    ) {
        createChannel(
            NotificationChannel(channelId, channelName, importance).apply {
                description?.let { this.description = it }
            },
        )
    }

    /**
     * Displays a notification with the appropriate style based on the option.<br><br>
     * 옵션에 따라 적절한 스타일로 알림을 표시합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return `true` if notification was shown successfully, `false` otherwise.<br><br>
     *         알림 표시 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun showNotification(notificationOption: SimpleNotificationOptionVo): Boolean = tryCatchSystemManager(false) {
        val builder = when (notificationOption.style) {
            NotificationStyle.DEFAULT -> getBuilder(notificationOption)
            NotificationStyle.BIG_PICTURE -> getBigPictureBuilder(notificationOption)
            NotificationStyle.BIG_TEXT -> getBigTextBuilder(notificationOption)
            NotificationStyle.PROGRESS -> {
                // Progress 스타일은 SimpleProgressNotificationOption 사용 권장
                Logx.w("Progress style should use SimpleProgressNotificationOption instead")
                getBuilder(notificationOption)
            }
        }
        showNotification(notificationOption.notificationId, builder)
        return true
    }

    /**
     * Creates a notification builder with standard configuration.<br><br>
     * 표준 구성으로 알림 빌더를 생성합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return Configured NotificationCompat.Builder instance.<br><br>
     *         구성된 NotificationCompat.Builder 인스턴스.<br>
     */
    public fun getBuilder(notificationOption: SimpleNotificationOptionVo): NotificationCompat.Builder = with(notificationOption) {
        val channel = currentChannel ?: getOrCreateDefaultChannel()
        val builder = NotificationCompat.Builder(context, channel.id).apply {
            title?.let { setContentTitle(it) }
            content?.let { setContentText(it) }
            setAutoCancel(isAutoCancel)
            smallIcon?.let { setSmallIcon(it) }
            largeIcon?.let { setLargeIcon(it) }
            setOngoing(onGoing)
            clickIntent?.let { setContentIntent(getPendingIntentType(SimplePendingIntentOptionVo(notificationId, it))) }
            actions?.forEach { addAction(it) }
        }
        builder
    }

    /**
     * Creates a notification builder with progress bar configuration.<br><br>
     * 진행률 바 구성으로 알림 빌더를 생성합니다.<br>
     *
     * @param notificationOption Progress notification configuration options.<br><br>
     *                           진행률 알림 구성 옵션.
     * @return NotificationCompat.Builder instance with progress bar.<br><br>
     *         진행률 바가 포함된 NotificationCompat.Builder 인스턴스.<br>
     */
    public fun getProgressBuilder(notificationOption: SimpleProgressNotificationOptionVo): NotificationCompat.Builder =
        with(notificationOption) {
            val channel = currentChannel ?: getOrCreateDefaultChannel()
            val builder = NotificationCompat.Builder(context, channel.id).apply {
                title?.let { setContentTitle(it) }
                content?.let { setContentText(it) }
                setAutoCancel(isAutoCancel)
                smallIcon?.let { setSmallIcon(it) }
                setOngoing(onGoing)
                clickIntent?.let { setContentIntent(getPendingIntentType(SimplePendingIntentOptionVo(notificationId, it))) }
                actions?.forEach { addAction(it) }
                setPriority(PRIORITY_LOW)
                setProgress(100, progressPercent, false)
            }

            progressBuilders[notificationId] = ProgressNotificationInfo(builder) // 진행률 업데이트를 위해 빌더 저장

            // 첫 번째 진행률 알림 생성 시 스케줄러 시작
            if (cleanupScheduler == null) {
                startProgressCleanupScheduler()
            }

            builder
        }

    /**
     * Internal method that actually displays the notification.<br><br>
     * 알림을 실제로 표시하는 내부 메서드입니다.<br>
     *
     * @param notificationId Unique notification identifier.<br><br>
     *                       고유 알림 식별자.
     * @param builder Configured notification builder.<br><br>
     *                구성된 알림 빌더.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    private fun showNotification(notificationId: Int, builder: NotificationCompat.Builder) {
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * Returns the appropriate PendingIntent based on the configured show type.<br><br>
     * 구성된 표시 유형에 따라 적절한 PendingIntent를 반환합니다.<br>
     *
     * @param pendingIntentOption PendingIntent configuration options.<br><br>
     *                            PendingIntent 구성 옵션.
     * @return Configured PendingIntent instance.<br><br>
     *         구성된 PendingIntent 인스턴스.<br>
     */
    private fun getPendingIntentType(pendingIntentOption: SimplePendingIntentOptionVo) = when (showType) {
        SimpleNotificationType.ACTIVITY -> getClickShowActivityPendingIntent(pendingIntentOption)
        SimpleNotificationType.SERVICE -> getClickShowServicePendingIntent(pendingIntentOption)
        SimpleNotificationType.BROADCAST -> getClickShowBroadcastPendingIntent(pendingIntentOption)
    }

    /**
     * Creates a PendingIntent that launches an Activity when triggered.<br><br>
     * 트리거될 때 Activity를 실행하는 PendingIntent를 생성합니다.<br>
     *
     * @param pendingIntentOption PendingIntent configuration options.<br><br>
     *                            PendingIntent 구성 옵션.
     * @return PendingIntent for Activity launch.<br><br>
     *         Activity 실행을 위한 PendingIntent.<br>
     */
    public fun getClickShowActivityPendingIntent(pendingIntentOption: SimplePendingIntentOptionVo): PendingIntent =
        with(pendingIntentOption) { PendingIntent.getActivity(context, actionId, clickIntent, flags) }

    /**
     * Creates a PendingIntent that starts a Service when triggered.<br><br>
     * 트리거될 때 Service를 시작하는 PendingIntent를 생성합니다.<br>
     *
     * @param pendingIntentOption PendingIntent configuration options.<br><br>
     *                            PendingIntent 구성 옵션.
     * @return PendingIntent for Service start.<br><br>
     *         Service 시작을 위한 PendingIntent.<br>
     */
    public fun getClickShowServicePendingIntent(pendingIntentOption: SimplePendingIntentOptionVo): PendingIntent =
        with(pendingIntentOption) { PendingIntent.getService(context, actionId, clickIntent, flags) }

    /**
     * Creates a PendingIntent that sends a Broadcast when triggered.<br><br>
     * 트리거될 때 Broadcast를 전송하는 PendingIntent를 생성합니다.<br>
     *
     * @param pendingIntentOption PendingIntent configuration options.<br><br>
     *                            PendingIntent 구성 옵션.
     * @return PendingIntent for Broadcast send.<br><br>
     *         Broadcast 전송을 위한 PendingIntent.<br>
     */
    public fun getClickShowBroadcastPendingIntent(pendingIntentOption: SimplePendingIntentOptionVo): PendingIntent =
        with(pendingIntentOption) { PendingIntent.getBroadcast(context, actionId, clickIntent, flags) }

    /**
     * Directly displays a notification using a pre-built Notification object.<br><br>
     * 미리 빌드된 Notification 객체를 사용하여 직접 알림을 표시합니다.<br>
     *
     * @param notificationId Unique notification identifier.<br><br>
     *                       고유 알림 식별자.
     * @param build Pre-built Notification object.<br><br>
     *              미리 빌드된 알림 객체.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun notify(notificationId: Int, build: Notification) {
        notificationManager.notify(notificationId, build)
    }

    /**
     * Creates a notification builder with BigPicture style.<br><br>
     * BigPicture 스타일의 알림 빌더를 생성합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return NotificationCompat.Builder with BigPicture style.<br><br>
     *         BigPicture 스타일이 적용된 NotificationCompat.Builder.<br>
     */
    private fun getBigPictureBuilder(notificationOption: SimpleNotificationOptionVo): NotificationCompat.Builder =
        getBuilder(notificationOption).apply {
            notificationOption.largeIcon?.let { bitmap ->
                setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            } ?: run {
                Logx.w("BigPicture style requires largeIcon bitmap")
            }
        }

    /**
     * Displays a notification with BigPicture style.<br><br>
     * BigPicture 스타일로 알림을 표시합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return `true` if notification was shown successfully, `false` otherwise.<br><br>
     *         알림 표시 성공 시 `true`, 그렇지 않으면 `false`.<br>
     * @deprecated Use showNotification() with style parameter instead.<br><br>
     *             style 파라미터와 함께 showNotification()을 사용하세요.
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    @Deprecated(
        "Use showNotification() with style parameter instead",
        ReplaceWith("showNotification(notificationOption.copy(style = NotificationStyle.BIG_PICTURE))"),
    )
    public fun showNotificationBigImage(notificationOption: SimpleNotificationOptionVo): Boolean =
        showNotification(notificationOption.copy(style = NotificationStyle.BIG_PICTURE))

    /**
     * Creates a notification builder with BigText style.<br><br>
     * BigText 스타일의 알림 빌더를 생성합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return NotificationCompat.Builder with BigText style.<br><br>
     *         BigText 스타일이 적용된 NotificationCompat.Builder.<br>
     */
    private fun getBigTextBuilder(notificationOption: SimpleNotificationOptionVo): NotificationCompat.Builder =
        getBuilder(notificationOption).apply {
            notificationOption.snippet?.let { text ->
                setStyle(NotificationCompat.BigTextStyle().bigText(text))
            } ?: run {
                Logx.w("BigText style requires snippet text")
            }
        }

    /**
     * Displays a notification with BigText style.<br><br>
     * BigText 스타일로 알림을 표시합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return `true` if notification was shown successfully, `false` otherwise.<br><br>
     *         알림 표시 성공 시 `true`, 그렇지 않으면 `false`.<br>
     * @deprecated Use showNotification() with style parameter instead.<br><br>
     *             style 파라미터와 함께 showNotification()을 사용하세요.
     */
    @Deprecated(
        "Use showNotification() with style parameter instead",
        ReplaceWith("showNotification(notificationOption.copy(style = NotificationStyle.BIG_TEXT))"),
    )
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun showNotificationBigText(notificationOption: SimpleNotificationOptionVo): Boolean =
        showNotification(notificationOption.copy(style = NotificationStyle.BIG_TEXT))

    /**
     * Creates and displays a progress notification.<br><br>
     * 진행률 알림을 생성하고 표시합니다.<br>
     *
     * @param simpleProgressNotificationOption Progress notification configuration options.<br><br>
     *                                         진행률 알림 구성 옵션.
     * @return `true` if notification was created successfully, `false` otherwise.<br><br>
     *         알림 생성 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun showProgressNotification(simpleProgressNotificationOption: SimpleProgressNotificationOptionVo): Boolean =
        tryCatchSystemManager(false) {
            with(simpleProgressNotificationOption) {
                val builder = getProgressBuilder(simpleProgressNotificationOption)
                showNotification(notificationId, builder)
            }
            return true
        }

    /**
     * Updates the progress percentage of an existing progress notification.<br><br>
     * 기존 진행률 알림의 진행률을 업데이트합니다.<br>
     *
     * @param notificationId Unique notification identifier.<br><br>
     *                       고유 알림 식별자.
     * @param progressPercent Progress percentage (0-100).<br><br>
     *                        진행률 (0-100).
     * @return `true` if update was successful, `false` otherwise.<br><br>
     *         업데이트 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun updateProgress(notificationId: Int, progressPercent: Int): Boolean {
        // 진행률 범위 검증
        if (progressPercent !in 0..100) {
            Logx.w("Invalid progress: $progressPercent (must be 0 ~ 100)")
            return false
        }
        return tryCatchSystemManager(false) {
            progressBuilders[notificationId]?.let { info ->
                info.builder.setProgress(100, progressPercent, false)
                showNotification(notificationId, info.builder)
                // 업데이트 시간 갱신
                progressBuilders[notificationId] = info.copy(lastUpdateTime = System.currentTimeMillis())
                true
            } ?: run {
                Logx.w("Progress notification not found for ID: $notificationId")
                false
            }
        }
    }

    /**
     * Marks a progress notification as complete and optionally updates the message.<br><br>
     * 진행률 알림을 완료 상태로 표시하고 선택적으로 메시지를 업데이트합니다.<br>
     *
     * @param notificationId Unique notification identifier.<br><br>
     *                       고유 알림 식별자.
     * @param completedContent Completion message (optional).<br><br>
     *                         완료 메시지 (선택사항).
     * @return `true` if completion was successful, `false` otherwise.<br><br>
     *         완료 처리 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun completeProgress(notificationId: Int, completedContent: String? = null): Boolean = tryCatchSystemManager(false) {
        progressBuilders[notificationId]?.let { info ->
            info.builder.setProgress(0, 0, false) // 진행률 바 제거
            completedContent?.let { info.builder.setContentText(it) }
            showNotification(notificationId, info.builder)
            progressBuilders.remove(notificationId) // 빌더 제거로 메모리 정리
            return true
        } ?: run {
            Logx.w("Progress notification not found for ID: $notificationId")
            return false
        }
    }

    /**
     * Cancels a specific notification by ID.<br><br>
     * ID로 특정 알림을 취소합니다.<br>
     *
     * @param tag Notification tag (optional).<br><br>
     *            알림 태그 (선택사항).
     * @param notificationId Unique notification identifier.<br><br>
     *                       고유 알림 식별자.
     * @return `true` if cancellation was successful, `false` otherwise.<br><br>
     *         취소 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun cancelNotification(tag: String? = null, notificationId: Int): Boolean = tryCatchSystemManager(false) {
        tag?.let { notificationManager.cancel(tag, notificationId) }
            ?: notificationManager.cancel(notificationId)
        progressBuilders.remove(notificationId) // 진행률 빌더도 함께 제거
        return true
    }

    /**
     * Cancels all active notifications.<br><br>
     * 모든 활성 알림을 취소합니다.<br>
     */
    public fun cancelAll() {
        notificationManager.cancelAll()
        progressBuilders.clear()
    }

    /**
     * Cleans up resources. Should be called when Activity/Service is destroyed.<br><br>
     * 리소스를 정리합니다. Activity/Service가 종료될 때 호출해야 합니다.<br>
     */
    public fun cleanup() {
        try {
            progressBuilders.clear()
            cleanupScheduler?.let { scheduler ->
                scheduler.shutdown()
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow()
                }
            }
            cleanupScheduler = null
        } catch (e: InterruptedException) {
            cleanupScheduler?.shutdownNow()
            cleanupScheduler = null
            Thread.currentThread().interrupt()
        }
    }

    /**
     * Ensures that a default channel exists, creating it if necessary.<br><br>
     * 기본 채널이 존재하는지 확인하고 필요한 경우 생성합니다.<br>
     */
    private fun ensureDefaultChannel() {
        if (currentChannel != null) {
            return
        }
        getOrCreateDefaultChannel()
    }

    /**
     * Gets the default channel or creates it if it doesn't exist.<br><br>
     * 기본 채널을 가져오거나 존재하지 않으면 생성합니다.<br>
     *
     * @return The default NotificationChannel instance.<br><br>
     *         기본 NotificationChannel 인스턴스.<br>
     */
    private fun getOrCreateDefaultChannel(): NotificationChannel = currentChannel ?: run {
        val defaultChannel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            DEFAULT_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = DEFAULT_CHANNEL_DESCRIPTION
        }
        createChannel(defaultChannel)
        defaultChannel
    }

    /**
     * Starts a scheduler that periodically cleans up stale progress notifications.<br>
     * Removes progress notification builders that haven't been updated for 30 minutes.<br><br>
     * 오래된 진행률 알림을 주기적으로 정리하는 스케줄러를 시작합니다.<br>
     * 30분 동안 업데이트되지 않은 진행률 알림 빌더를 제거하여 메모리 누수를 방지합니다.<br>
     */
    private fun startProgressCleanupScheduler() {
        if (cleanupScheduler != null) {
            return
        }
        try {
            cleanupScheduler = Executors.newSingleThreadScheduledExecutor()
            cleanupScheduler?.scheduleWithFixedDelay({
                try {
                    val currentTime = System.currentTimeMillis()
                    val staleThreshold = 30 * 60 * 1000L // 30분

                    val staleNotifications =
                        progressBuilders.filter { (_, info) -> currentTime - info.lastUpdateTime > staleThreshold }.keys

                    staleNotifications.forEach { notificationId ->
                        progressBuilders.remove(notificationId)
                        Logx.d("Removed stale progress notification: $notificationId")
                    }

                    if (staleNotifications.isNotEmpty()) {
                        Logx.d("Cleaned up ${staleNotifications.size} stale progress notifications")
                    }
                } catch (e: Exception) {
                    Logx.e("Error during progress notification cleanup: ${e.message}")
                }
            }, 5, 5, TimeUnit.MINUTES) // 5분 후 시작, 5분마다 실행

            Logx.d("Progress cleanup scheduler started")
        } catch (e: Exception) {
            Logx.e("Failed to start progress cleanup scheduler: ${e.message}")
        }
    }
}
