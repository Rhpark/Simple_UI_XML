package kr.open.library.simple_ui.system_manager.controller.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.controller.notification.vo.NotificationStyle
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimpleNotificationOptionVo
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimpleNotificationType
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimplePendingIntentOptionVo
import kr.open.library.simple_ui.system_manager.controller.notification.vo.SimpleProgressNotificationOptionVo
import kr.open.library.simple_ui.system_manager.extensions.getNotificationManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 *
 * NotificationManager.IMPORTANCE_HIGH	긴급 상황이며 알림음이 울리며 헤드업으로 표시
 * NotificationManager.IMPORTANCE_DEFAULT	높은 중요도이며 알림음이 울림
 * NotificationManager.IMPORTANCE_LOW	중간 중요도이며 알림음이 울리지 않음
 * NotificationManager.IMPORTANCE_MIN   낮은 중요도이며 알림음이 없고 상태표시줄에 표시되지 않음
 *
 * @param context 컨텍스트
 * @param showType 알림 클릭 시 동작 유형 (Activity, Service, Broadcast)
 */
public open class SimpleNotificationController(context: Context, private val showType: SimpleNotificationType) :
    BaseSystemService(
        context,
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU,
            positiveWork = { listOf(POST_NOTIFICATIONS) },
            negativeWork = { null })
    ) {

    public val notificationManager: NotificationManager by lazy { context.getNotificationManager() }
    
    init {
        // 기본 채널 자동 생성
        ensureDefaultChannel()
    }

    // 진행률 알림 빌더들을 저장하는 맵 (ID별 관리) - Thread-safe
    private val progressBuilders = ConcurrentHashMap<Int, ProgressNotificationInfo>()
    
    // 진행률 알림 정리를 위한 스케줄러 (필요시 생성)
    private var cleanupScheduler: ScheduledExecutorService? = null
    
    // 진행률 알림 정보를 담는 데이터 클래스
    private data class ProgressNotificationInfo(
        val builder: NotificationCompat.Builder,
        val lastUpdateTime: Long = System.currentTimeMillis()
    )

    // 현재 설정된 알림 채널
    private var currentChannel: NotificationChannel? = null
    
    // 기본 채널 상수
    companion object {
        private const val DEFAULT_CHANNEL_ID = "default_notification_channel"
        private const val DEFAULT_CHANNEL_NAME = "Default Notifications"
        private const val DEFAULT_CHANNEL_DESCRIPTION = "Default notification channel for the application"
    }

    /**
     * NotificationChannel을 생성 및 등록하고 현재 채널로 설정.
     */
    public fun createChannel(notificationChannel: NotificationChannel) {
        currentChannel = notificationChannel
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * 알림 채널을 생성하여 등록합니다.
     * @param channelId 채널 ID
     * @param channelName 채널 이름
     * @param importance 중요도 (IMPORTANCE_HIGH, DEFAULT, LOW, MIN)
     * @param description 채널 설명 (선택사항)
     */
    public fun createChannel(channelId: String, channelName: String, importance: Int, description: String? = null) {
        createChannel(NotificationChannel(channelId, channelName, importance).apply {
            description?.let { this.description = it }
        })
    }

    /**
     * 알림을 표시합니다. 스타일에 따라 적절한 형태로 표시됩니다.
     * @param notificationOption 알림 옵션
     * @return 성공 여부
     */
    public fun showNotification(notificationOption: SimpleNotificationOptionVo): Boolean =
        tryCatchSystemManager(false) {
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
            true
        }

    /**
     * 알림 빌더를 생성합니다.
     * @param notificationOption 알림 옵션
     * @return NotificationCompat.Builder
     */
    public fun getBuilder(notificationOption: SimpleNotificationOptionVo):NotificationCompat.Builder {
        return with(notificationOption) {
            val channel = currentChannel ?: getOrCreateDefaultChannel()
            val builder = NotificationCompat.Builder(context, channel.id).apply {
                title?.let { setContentTitle(it) }
                content?.let { setContentText(it) }
                setAutoCancel(isAutoCancel)
                smallIcon?.let { setSmallIcon(it) }
                largeIcon?.let { setLargeIcon(it) }
                setOngoing(onGoing)
                clickIntent?.let {
                    setContentIntent(getPendingIntentType(SimplePendingIntentOptionVo(notificationId, it)))
                }
                actions?.forEach { addAction(it) }
            }
            builder
        }
    }

    /**
     * Progress 알림 빌더를 생성합니다.
     * @param notificationOption 진행률 알림 옵션
     * @return NotificationCompat.Builder (진행률 바 포함)
     */
    public fun getProgressBuilder(notificationOption: SimpleProgressNotificationOptionVo):NotificationCompat.Builder {
        return with(notificationOption) {
            val channel = currentChannel ?: getOrCreateDefaultChannel()
            val builder = NotificationCompat.Builder(context, channel.id).apply {
                title?.let { setContentTitle(it) }
                content?.let { setContentText(it) }
                setAutoCancel(isAutoCancel)
                smallIcon?.let { setSmallIcon(it) }
                setOngoing(onGoing)
                clickIntent?.let {
                    setContentIntent(getPendingIntentType(SimplePendingIntentOptionVo(notificationId, it)))
                }
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
    }

    /**
     * 알림을 실제로 표시하는 내부 메서드
     */
    private fun showNotification(notificationId: Int, builder: NotificationCompat.Builder) {
        notificationManager.notify(notificationId, builder.build())
    }

    private fun getPendingIntentType(pendingIntentOption: SimplePendingIntentOptionVo) = when(showType) {
        SimpleNotificationType.ACTIVITY -> getClickShowActivityPendingIntent(pendingIntentOption)
        SimpleNotificationType.SERVICE -> getClickShowServicePendingIntent(pendingIntentOption)
        SimpleNotificationType.BROADCAST -> getClickShowBroadcastPendingIntent(pendingIntentOption)
    }

    public fun getClickShowActivityPendingIntent(pendingIntentOption: SimplePendingIntentOptionVo): PendingIntent =
        with(pendingIntentOption) { PendingIntent.getActivity(context, actionId, clickIntent, flags) }

    public fun getClickShowServicePendingIntent(pendingIntentOption: SimplePendingIntentOptionVo): PendingIntent =
        with(pendingIntentOption) { PendingIntent.getService(context, actionId, clickIntent, flags) }

    public fun getClickShowBroadcastPendingIntent(pendingIntentOption: SimplePendingIntentOptionVo): PendingIntent =
        with(pendingIntentOption) { PendingIntent.getBroadcast(context, actionId, clickIntent, flags) }

    /**
     * 직접 알림을 표시합니다.
     * @param notificationId 알림 ID
     * @param build 빌드된 알림 객체
     */
    public fun notify(notificationId: Int, build: Notification) {
        notificationManager.notify(notificationId, build)
    }

    /**
     * 큰 이미지 스타일 알림 빌더를 생성합니다.
     * @param notificationOption 알림 옵션
     * @return NotificationCompat.Builder
     */
    private fun getBigPictureBuilder(notificationOption: SimpleNotificationOptionVo): NotificationCompat.Builder {
        return getBuilder(notificationOption).apply {
            notificationOption.largeIcon?.let { bitmap ->
                setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            } ?: run {
                Logx.w("BigPicture style requires largeIcon bitmap")
            }
        }
    }
    
    /**
     * 큰 이미지 스타일 알림을 표시합니다.
     * @param notificationOption 알림 옵션
     * @return 성공 여부
     * @deprecated showNotification()을 style 파라미터와 함께 사용하세요
     */
    @Deprecated("Use showNotification() with style parameter instead", ReplaceWith("showNotification(notificationOption.copy(style = NotificationStyle.BIG_PICTURE))"))
    public fun showNotificationBigImage(notificationOption: SimpleNotificationOptionVo): Boolean =
        showNotification(notificationOption.copy(style = NotificationStyle.BIG_PICTURE))

    /**
     * 긴 텍스트 스타일 알림 빌더를 생성합니다.
     * @param notificationOption 알림 옵션
     * @return NotificationCompat.Builder
     */
    private fun getBigTextBuilder(notificationOption: SimpleNotificationOptionVo): NotificationCompat.Builder {
        return getBuilder(notificationOption).apply {
            notificationOption.snippet?.let { text ->
                setStyle(NotificationCompat.BigTextStyle().bigText(text))
            } ?: run {
                Logx.w("BigText style requires snippet text")
            }
        }
    }
    
    /**
     * 긴 텍스트 스타일 알림을 표시합니다.
     * @param notificationOption 알림 옵션
     * @return 성공 여부
     * @deprecated showNotification()을 style 파라미터와 함께 사용하세요
     */
    @Deprecated("Use showNotification() with style parameter instead", ReplaceWith("showNotification(notificationOption.copy(style = NotificationStyle.BIG_TEXT))"))
    public fun showNotificationBigText(notificationOption: SimpleNotificationOptionVo): Boolean =
        showNotification(notificationOption.copy(style = NotificationStyle.BIG_TEXT))

    /**
     * 진행률 알림을 생성합니다.
     * @param simpleProgressNotificationOption 진행률 알림 옵션
     * @return 생성 성공 여부
     */
    public fun showProgressNotification(
        simpleProgressNotificationOption: SimpleProgressNotificationOptionVo
    ): Boolean = tryCatchSystemManager(false) {
        with(simpleProgressNotificationOption) {
            val builder = getProgressBuilder(simpleProgressNotificationOption)
            showNotification(notificationId, builder)
        }
        true
    }

    /**
     * 진행률을 업데이트합니다.
     * @param notificationId 알림 ID
     * @param progressPercent 진행률 (0~100)
     * @return 업데이트 성공 여부
     */
    public fun updateProgress(notificationId: Int, progressPercent: Int): Boolean {
        // 진행률 범위 검증
        if (progressPercent !in 0..100) {
            Logx.w("Invalid progress: $progressPercent (must be 0 ~ 100)")
            return false
        }
        return tryCatchSystemManager( false) {
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
     * 진행률 알림을 완료 상태로 변경합니다.
     * @param notificationId 알림 ID
     * @param completedContent 완료 메시지
     * @return 완료 처리 성공 여부
     */
    public fun completeProgress(notificationId: Int, completedContent: String? = null): Boolean =
        tryCatchSystemManager(false) {
            progressBuilders[notificationId]?.let { info ->
                info.builder.setProgress(0, 0, false) // 진행률 바 제거
                completedContent?.let { info.builder.setContentText(it) }
                showNotification(notificationId, info.builder)
                progressBuilders.remove(notificationId) // 빌더 제거로 메모리 정리
                true
            } ?: run {
                Logx.w("Progress notification not found for ID: $notificationId")
                false
            }
        }

    /**
     * 특정 알림을 취소합니다.
     * @param tag 알림 태그 (선택사항)
     * @param notificationId 알림 ID
     * @return 취소 성공 여부
     */
    public fun cancelNotification(tag: String?, notificationId: Int): Boolean =
        tryCatchSystemManager( false) {
            tag?.let { notificationManager.cancel(tag, notificationId) }
                ?: notificationManager.cancel(notificationId)
            progressBuilders.remove(notificationId) // 진행률 빌더도 함께 제거
            true
        }

    /**
     * 모든 알림을 취소합니다.
     */
    public fun cancelAll() {
        notificationManager.cancelAll()
        progressBuilders.clear()
    }

    /**
     * 리소스를 정리합니다. Activity/Service가 종료될 때 호출해야 합니다.
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
     * 기본 채널이 존재하는지 확인하고 없으면 생성합니다.
     */
    private fun ensureDefaultChannel() {
        if (currentChannel == null) {
            getOrCreateDefaultChannel()
        }
    }
    
    /**
     * 기본 채널을 가져오거나 생성합니다.
     * @return 기본 NotificationChannel
     */
    private fun getOrCreateDefaultChannel(): NotificationChannel {
        return currentChannel ?: run {
            val defaultChannel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = DEFAULT_CHANNEL_DESCRIPTION
            }
            createChannel(defaultChannel)
            defaultChannel
        }
    }
    
    /**
     * 진행률 알림 정리 스케줄러를 시작합니다.
     * 오래된 진행률 알림 빌더를 주기적으로 정리하여 메모리 누수를 방지합니다.
     */
    private fun startProgressCleanupScheduler() {
        if (cleanupScheduler == null) {
            try {
                cleanupScheduler = Executors.newSingleThreadScheduledExecutor()
                cleanupScheduler?.scheduleWithFixedDelay({
                    try {
                        val currentTime = System.currentTimeMillis()
                        val staleThreshold = 30 * 60 * 1000L // 30분
                        
                        val staleNotifications = progressBuilders.filter { (_, info) ->
                            currentTime - info.lastUpdateTime > staleThreshold
                        }.keys
                        
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
}
