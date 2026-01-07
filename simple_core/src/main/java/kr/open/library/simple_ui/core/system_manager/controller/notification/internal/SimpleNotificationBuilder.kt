package kr.open.library.simple_ui.core.system_manager.controller.notification.internal

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.controller.notification.SimpleNotificationType
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigPictureNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigTextNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.ProgressNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.SimpleNotificationOptionBase
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.SimplePendingIntentOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal class SimpleNotificationBuilder(
    private val context: Context
) {
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
    public data class ProgressNotificationInfo(
        val builder: NotificationCompat.Builder,
        val lastUpdateTime: Long = System.currentTimeMillis(),
    )

    /**
     * Creates a notification builder with standard configuration.<br><br>
     * 표준 구성으로 알림 빌더를 생성합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return Configured NotificationCompat.Builder instance.<br><br>
     *         구성된 NotificationCompat.Builder 인스턴스.<br>
     */
    public fun getBuilder(
        notificationChannel: NotificationChannel,
        notificationOption: SimpleNotificationOptionBase,
        showType: SimpleNotificationType
    ): NotificationCompat.Builder = with(notificationOption) {
        val builder = NotificationCompat.Builder(context, notificationChannel.id).apply {
            setSmallIcon(smallIcon)
            setContentTitle(title)
            content?.let { setContentText(it) }
            setAutoCancel(isAutoCancel)
            setOngoing(onGoing)
            clickIntent?.let {
                setContentIntent(
                    getPendingIntentType(
                        SimplePendingIntentOption(notificationId, it),
                        showType
                    )
                )
            }
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
    public fun getProgressBuilder(
        notificationChannel: NotificationChannel,
        notificationOption: ProgressNotificationOption,
        showType: SimpleNotificationType
    ): NotificationCompat.Builder {
        val builder = getBuilder(notificationChannel, notificationOption, showType).apply {
            setProgress(100, notificationOption.progressPercent, false)
            setOnlyAlertOnce(true)
        }

        progressBuilders[notificationOption.notificationId] =
            ProgressNotificationInfo(builder) // 진행률 업데이트를 위해 빌더 저장

        // 첫 번째 진행률 알림 생성 시 스케줄러 시작
        if (cleanupScheduler == null) {
            startProgressCleanupScheduler()
        }

        return builder
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
                        stopProgressCleanupSchedulerIfIdle()
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

    private fun stopProgressCleanupSchedulerIfIdle() {
        if (progressBuilders.isNotEmpty()) return

        cleanupScheduler?.let { scheduler ->
            scheduler.shutdown()
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        }
        cleanupScheduler = null
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
    public fun getBigPictureBuilder(
        notificationChannel: NotificationChannel,
        notificationOption: BigPictureNotificationOption,
        showType: SimpleNotificationType
    ): NotificationCompat.Builder =
        getBuilder(notificationChannel, notificationOption, showType).apply {
            setStyle(NotificationCompat.BigPictureStyle().bigPicture(notificationOption.bigPicture))
        }

    /**
     * Creates a notification builder with BigText style.<br><br>
     * BigText 스타일의 알림 빌더를 생성합니다.<br>
     *
     * @param notificationOption Notification configuration options.<br><br>
     *                           알림 구성 옵션.
     * @return NotificationCompat.Builder with BigText style.<br><br>
     *         BigText 스타일이 적용된 NotificationCompat.Builder.<br>
     */
    public fun getBigTextBuilder(
        notificationChannel: NotificationChannel,
        notificationOption: BigTextNotificationOption,
        showType: SimpleNotificationType
    ): NotificationCompat.Builder =
        getBuilder(notificationChannel, notificationOption, showType).apply {
            setStyle(NotificationCompat.BigTextStyle().bigText(notificationOption.snippet))
        }

    /**
     * Cleans up resources. Should be called when Activity/Service is destroyed.<br><br>
     * 리소스를 정리합니다. Activity/Service가 종료될 때 호출해야 합니다.<br>
     */
    public fun cleanup() {
        try {
            progressBuilders.clear()
            stopProgressCleanupSchedulerIfIdle()
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
     * Cancels all active notifications.<br><br>
     * 모든 활성 알림을 취소합니다.<br>
     */
    public fun cancelAll() {
        progressBuilders.clear()
        stopProgressCleanupSchedulerIfIdle()
    }

    /**
     * Cancels a specific notification by ID.<br><br>
     * ID로 특정 알림을 취소합니다.<br>
     *
     * @param notificationId Unique notification identifier.<br><br>
     *                       고유 알림 식별자.<br>
     */
    public fun cancelNotification(notificationId: Int) {
        progressBuilders.remove(notificationId) // 진행률 빌더도 함께 제거
        stopProgressCleanupSchedulerIfIdle()
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
    public fun completeProgress(
        notificationId: Int,
        completedContent: String? = null
    ): ProgressNotificationInfo? = safeCatch(null) {
        progressBuilders[notificationId]?.let { info ->
            info.builder.setProgress(0, 0, false) // 진행률 바 제거
            info.builder.setOnlyAlertOnce(false) // 완료 시 한 번 알림 허용
            info.builder.setOngoing(false)
            info.builder.setAutoCancel(true)
            completedContent?.let { info.builder.setContentText(it) }
            val data: ProgressNotificationInfo? = progressBuilders.get(notificationId)
            progressBuilders.remove(notificationId) // 빌더 제거로 메모리 정리
            stopProgressCleanupSchedulerIfIdle()
            return data
        } ?: run {
            Logx.w("Progress notification not found for ID: $notificationId")
            return null
        }
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
    public fun updateProgress(
        notificationId: Int,
        progressPercent: Int
    ): ProgressNotificationInfo? = safeCatch(null) {
        progressBuilders[notificationId]?.let { info ->
            info.builder.setProgress(100, progressPercent, false)
            // 업데이트 시간 갱신
            progressBuilders[notificationId] =
                info.copy(lastUpdateTime = System.currentTimeMillis())
            return progressBuilders[notificationId]
        }

        return null
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
    private fun getPendingIntentType(
        pendingIntentOption: SimplePendingIntentOption,
        showType: SimpleNotificationType
    ) = when (showType) {
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
    private fun getClickShowActivityPendingIntent(pendingIntentOption: SimplePendingIntentOption): PendingIntent =
        with(pendingIntentOption) {
            PendingIntent.getActivity(
                context,
                actionId,
                clickIntent,
                flags
            )
        }

    /**
     * Creates a PendingIntent that starts a Service when triggered.<br><br>
     * 트리거될 때 Service를 시작하는 PendingIntent를 생성합니다.<br>
     *
     * @param pendingIntentOption PendingIntent configuration options.<br><br>
     *                            PendingIntent 구성 옵션.
     * @return PendingIntent for Service start.<br><br>
     *         Service 시작을 위한 PendingIntent.<br>
     */
    private fun getClickShowServicePendingIntent(pendingIntentOption: SimplePendingIntentOption): PendingIntent =
        with(pendingIntentOption) {
            PendingIntent.getService(
                context,
                actionId,
                clickIntent,
                flags
            )
        }

    /**
     * Creates a PendingIntent that sends a Broadcast when triggered.<br><br>
     * 트리거될 때 Broadcast를 전송하는 PendingIntent를 생성합니다.<br>
     *
     * @param pendingIntentOption PendingIntent configuration options.<br><br>
     *                            PendingIntent 구성 옵션.
     * @return PendingIntent for Broadcast send.<br><br>
     *         Broadcast 전송을 위한 PendingIntent.<br>
     */
    private fun getClickShowBroadcastPendingIntent(pendingIntentOption: SimplePendingIntentOption): PendingIntent =
        with(pendingIntentOption) {
            PendingIntent.getBroadcast(
                context,
                actionId,
                clickIntent,
                flags
            )
        }
}
