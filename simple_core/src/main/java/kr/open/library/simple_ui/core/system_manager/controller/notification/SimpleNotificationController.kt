package kr.open.library.simple_ui.core.system_manager.controller.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.controller.notification.NotificationDefaultChannel.DEFAULT_CHANNEL_DESCRIPTION
import kr.open.library.simple_ui.core.system_manager.controller.notification.NotificationDefaultChannel.DEFAULT_CHANNEL_ID
import kr.open.library.simple_ui.core.system_manager.controller.notification.NotificationDefaultChannel.DEFAULT_CHANNEL_NAME
import kr.open.library.simple_ui.core.system_manager.controller.notification.internal.SimpleNotificationBuilder
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigPictureNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.BigTextNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.DefaultNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.ProgressNotificationOption
import kr.open.library.simple_ui.core.system_manager.controller.notification.option.SimpleNotificationOptionBase
import kr.open.library.simple_ui.core.system_manager.extensions.getNotificationManager

/**
 * Controller for managing Android notifications with support for various styles and features.<br>
 * Provides methods for creating channels, displaying notifications, and managing progress notifications.<br><br>
 * 다양한 스타일과 기능을 지원하는 Android 알림 관리 컨트롤러입니다.<br>
 * 채널 생성, 알림 표시, 진행률 알림 관리를 위한 메서드를 제공합니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's NotificationManager requires complex setup with channels, builders, and PendingIntents for each notification.<br>
 * - This class simplifies notification creation by providing a unified API with type-safe options and automatic channel management.<br>
 * - Eliminates boilerplate code for common notification patterns (default, big text, big picture, progress).<br><br>
 * - Android의 NotificationManager는 각 알림마다 채널, 빌더, PendingIntent 설정이 필요한 복잡한 구조입니다.<br>
 * - 이 클래스는 타입 안전한 옵션과 자동 채널 관리를 제공하는 통합 API로 알림 생성을 단순화합니다.<br>
 * - 일반적인 알림 패턴(기본, 긴 텍스트, 큰 이미지, 진행률)에 대한 보일러플레이트 코드를 제거합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Uses sealed class hierarchy (SimpleNotificationOptionBase) for type-safe notification configuration.<br>
 * - Delegates builder logic to SimpleNotificationBuilder for separation of concerns and testability.<br>
 * - Provides automatic progress notification cleanup (30-minute idle timer) to prevent memory leaks.<br>
 * - Supports dynamic channel switching to enable multi-channel notification scenarios.<br><br>
 * - 타입 안전한 알림 구성을 위해 sealed class 계층 구조(SimpleNotificationOptionBase)를 사용합니다.<br>
 * - 관심사 분리와 테스트 가능성을 위해 빌더 로직을 SimpleNotificationBuilder로 위임합니다.<br>
 * - 메모리 누수를 방지하기 위해 자동 진행률 알림 정리(30분 유휴 타이머)를 제공합니다.<br>
 * - 멀티 채널 알림 시나리오를 위해 동적 채널 전환을 지원합니다.<br>
 *
 * **Permission notice / 권한 안내:**<br>
 * - Android 13+ (TIRAMISU) requires POST_NOTIFICATIONS permission at runtime.<br>
 * - Required permission: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`<br><br>
 * - Android 13+ (TIRAMISU)는 런타임에 POST_NOTIFICATIONS 권한이 필요합니다.<br>
 * - 필수 권한: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`<br>
 *
 * **Notification Importance Levels / 알림 중요도 레벨:**<br>
 * - `IMPORTANCE_HIGH`: Urgent, makes sound and appears as heads-up<br>
 * - `IMPORTANCE_DEFAULT`: High priority, makes sound<br>
 * - `IMPORTANCE_LOW`: Medium priority, no sound<br>
 * - `IMPORTANCE_MIN`: Low priority, no sound and not shown in status bar<br><br>
 * - `IMPORTANCE_HIGH`: 긴급, 알림음이 울리며 헤즈업으로 표시<br>
 * - `IMPORTANCE_DEFAULT`: 높은 중요도, 알림음이 울림<br>
 * - `IMPORTANCE_LOW`: 중간 중요도, 알림음이 울리지 않음<br>
 * - `IMPORTANCE_MIN`: 낮은 중요도, 알림음이 없고 상태표시줄에 표시되지 않음<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Create a notification channel using `createChannel()` or use the default channel.<br>
 * 2. Create notification options (DefaultNotificationOption, BigTextNotificationOption, etc.).<br>
 * 3. Call `showNotification()` with the options and show type (Activity, Service, Broadcast).<br>
 * 4. For progress notifications, use `updateProgressNotification()` and `finishProgressNotification()`.<br><br>
 * 1. `createChannel()`로 알림 채널을 생성하거나 기본 채널을 사용하세요.<br>
 * 2. 알림 옵션을 생성하세요 (DefaultNotificationOption, BigTextNotificationOption 등).<br>
 * 3. 옵션과 표시 타입(Activity, Service, Broadcast)으로 `showNotification()`을 호출하세요.<br>
 * 4. 진행률 알림의 경우 `updateProgressNotification()`과 `finishProgressNotification()`을 사용하세요.<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.<br>
 * @param notificationChannel Initially active notification channel (default channel is used if not specified).<br><br>
 *                            초기 활성화될 알림 채널 (지정하지 않으면 기본 채널 사용).<br>
 */
public open class SimpleNotificationController(
    context: Context,
    /**
     * Currently active notification channel.<br><br>
     * 현재 활성화된 알림 채널입니다.<br>
     */
    private var notificationChannel: NotificationChannel = NotificationChannel(
        DEFAULT_CHANNEL_ID,
        DEFAULT_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = DEFAULT_CHANNEL_DESCRIPTION
    }
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

    private val builder = SimpleNotificationBuilder(context)

    init {
        /**
         * Creates and registers a NotificationChannel, then sets it as the current channel.<br><br>
         * NotificationChannel을 생성 및 등록하고 현재 채널로 설정합니다.<br>
         */
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * Displays a notification with the appropriate style based on the option.<br><br>
     * 옵션에 따라 적절한 스타일로 알림을 표시합니다.<br>
     *
     * @param option Notification configuration options.<br><br>
     *               알림 구성 옵션.<br>
     * @param showType Action type when notification is clicked (Activity, Service, Broadcast).<br><br>
     *                 알림 클릭 시 동작 유형 (Activity, Service, Broadcast).<br>
     * @return `true` if notification was shown successfully, `false` otherwise.<br><br>
     *         알림 표시 성공 시 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun showNotification(option: SimpleNotificationOptionBase, showType: SimpleNotificationType): Boolean =
        tryCatchSystemManager(false) {
            when (option) {
                is DefaultNotificationOption -> {
                    showNotification(option.notificationId, builder.getBuilder(notificationChannel, option, showType))
                }
                is BigTextNotificationOption -> {
                    showNotification(option.notificationId, builder.getBigTextBuilder(notificationChannel, option, showType))
                }
                is BigPictureNotificationOption -> {
                    showNotification(option.notificationId, builder.getBigPictureBuilder(notificationChannel, option, showType))
                }
                is ProgressNotificationOption -> {
                    showNotification(option.notificationId, builder.getProgressBuilder(notificationChannel, option, showType))
                }
            }
            true
        }

    /**
     * Creates and registers a NotificationChannel, then sets it as the current channel.<br><br>
     * NotificationChannel을 생성 및 등록하고 현재 채널로 설정합니다.<br>
     *
     * @param notificationChannel The notification channel to create.<br><br>
     *                            생성할 알림 채널.<br>
     */
    public fun createChannel(notificationChannel: NotificationChannel) {
        this.notificationChannel = notificationChannel
        notificationManager.createNotificationChannel(notificationChannel)
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
     * Directly displays a notification using a pre-built Notification object.<br><br>
     * 미리 빌드된 Notification 객체를 사용하여 직접 알림을 표시합니다.<br>
     *
     * @param notificationId Unique notification identifier.<br><br>
     *                       고유 알림 식별자.
     * @param build Pre-built Notification object.<br><br>
     *              미리 빌드된 알림 객체.<br>
     */
    @RequiresPermission(POST_NOTIFICATIONS)
    public fun notify(notificationId: Int, build: Notification) = tryCatchSystemManager(false) {
        notificationManager.notify(notificationId, build)
        true
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
    public fun updateProgress(notificationId: Int, progressPercent: Int): Boolean = tryCatchSystemManager(false) {
        // 진행률 범위 검증
        if (progressPercent !in 0..100) {
            Logx.w("Invalid progress: $progressPercent (must be 0 ~ 100)")
            return false
        }
        builder.updateProgress(notificationId, progressPercent)?.let {
            showNotification(notificationId, it.builder)
            return true
        }
        Logx.w("Progress notification not found for ID: $notificationId")
        return false
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
        val result = builder.completeProgress(notificationId, completedContent)
        result?.builder?.let {
            showNotification(notificationId, it)
            return true
        }
        Logx.w("Progress notification not found for ID: $notificationId")
        return false
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
        builder.cancelNotification(notificationId) // 진행률 빌더도 함께 제거
        return true
    }

    /**
     * Cancels all active notifications.<br><br>
     * 모든 활성 알림을 취소합니다.<br>
     */
    public fun cancelAll() {
        notificationManager.cancelAll()
        builder.cancelAll()
    }

    /**
     * Cleans up resources. Should be called when Activity/Service is destroyed.<br><br>
     * 리소스를 정리합니다. Activity/Service가 종료될 때 호출해야 합니다.<br>
     */
    public fun cleanup() {
        builder.cleanup()
    }
}
