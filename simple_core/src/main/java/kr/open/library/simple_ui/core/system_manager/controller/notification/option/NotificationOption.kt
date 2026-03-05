package kr.open.library.simple_ui.core.system_manager.controller.notification.option

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat.Action
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion

/**
 * Base sealed class containing common options for all notification types.<br><br>
 * 모든 알림 타입의 공통 옵션을 포함하는 베이스 sealed 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.<br>
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.<br>
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.<br>
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.<br>
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.<br>
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.<br>
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.<br>
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.<br>
 * @param pendingIntentFlags PendingIntent flags used for clickIntent (validated on Android 12+).<br><br>
 *                          clickIntent에 사용하는 PendingIntent 플래그이며 Android 12+에서 검증됩니다.<br>
 */
sealed class SimpleNotificationOptionBase(
    public open val notificationId: Int,
    public open val smallIcon: Int,
    public open val title: String,
    public open val content: String?,
    public open val isAutoCancel: Boolean,
    public open val onGoing: Boolean,
    public open val clickIntent: Intent? = null,
    public open val actions: List<Action>? = null,
    public open val pendingIntentFlags: Int
) {
    init {
        clickIntent?.let {
            checkSdkVersion(Build.VERSION_CODES.S) {
                val hasImmutable = pendingIntentFlags and PendingIntent.FLAG_IMMUTABLE != 0
                val hasMutable = pendingIntentFlags and PendingIntent.FLAG_MUTABLE != 0

                require(hasImmutable || hasMutable) {
                    "Android 12+ requires FLAG_IMMUTABLE or FLAG_MUTABLE in pendingIntentFlags."
                }
                require(!(hasImmutable && hasMutable)) {
                    "pendingIntentFlags must not include both FLAG_IMMUTABLE and FLAG_MUTABLE."
                }
            }
        }
    }
}

/**
 * Data class containing options for displaying a standard notification with title and content.<br><br>
 * 제목과 내용이 있는 표준 알림을 표시하기 위한 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.<br>
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.<br>
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.<br>
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.<br>
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.<br>
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.<br>
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.<br>
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.<br>
 */
public data class DefaultNotificationOption(
    override val notificationId: Int,
    override val smallIcon: Int,
    override val title: String,
    override val content: String?,
    override val isAutoCancel: Boolean = true,
    override val onGoing: Boolean = false,
    override val clickIntent: Intent? = null,
    override val actions: List<Action>? = null,
    override val pendingIntentFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        onGoing = onGoing,
        clickIntent = clickIntent,
        actions = actions,
        pendingIntentFlags = pendingIntentFlags
    )

/**
 * Data class containing options for displaying an expandable notification with long text content.<br>
 * Uses NotificationCompat.BigTextStyle to show extended text when expanded.<br><br>
 * 긴 텍스트 내용이 있는 확장 가능한 알림을 표시하기 위한 옵션을 포함하는 데이터 클래스입니다.<br>
 * NotificationCompat.BigTextStyle을 사용하여 확장 시 긴 텍스트를 표시합니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.<br>
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.<br>
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.<br>
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.<br>
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.<br>
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.<br>
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.<br>
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.<br>
 * @param snippet Extended text for BIG_TEXT style.<br><br>
 *                BIG_TEXT 스타일을 위한 확장 텍스트.<br>
 */
public data class BigTextNotificationOption(
    override val notificationId: Int,
    override val smallIcon: Int,
    override val title: String,
    override val content: String?,
    override val isAutoCancel: Boolean = true,
    override val onGoing: Boolean = false,
    override val clickIntent: Intent? = null,
    override val actions: List<Action>? = null,
    override val pendingIntentFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    public val snippet: String
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        onGoing = onGoing,
        clickIntent = clickIntent,
        actions = actions,
        pendingIntentFlags = pendingIntentFlags
    )

/**
 * Data class containing options for displaying an expandable notification with a large image.<br>
 * Uses NotificationCompat.BigPictureStyle to show a full-width bitmap when expanded.<br><br>
 * 큰 이미지가 있는 확장 가능한 알림을 표시하기 위한 옵션을 포함하는 데이터 클래스입니다.<br>
 * NotificationCompat.BigPictureStyle을 사용하여 확장 시 전체 너비 비트맵을 표시합니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.<br>
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.<br>
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.<br>
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.<br>
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.<br>
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.<br>
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.<br>
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.<br>
 * @param bigPicture Bitmap for the large bitmap.<br><br>
 *                  큰 비트맵 이미지.<br>
 */
public data class BigPictureNotificationOption(
    override val notificationId: Int,
    override val smallIcon: Int,
    override val title: String,
    override val content: String?,
    override val isAutoCancel: Boolean = true,
    override val onGoing: Boolean = false,
    override val clickIntent: Intent? = null,
    override val actions: List<Action>? = null,
    override val pendingIntentFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    public val bigPicture: Bitmap,
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        onGoing = onGoing,
        clickIntent = clickIntent,
        actions = actions,
        pendingIntentFlags = pendingIntentFlags
    )

/**
 * Data class containing options for displaying a notification with a progress bar.<br>
 * Validates that progressPercent is between 0 and 100, suitable for tracking long-running operations.<br><br>
 * 진행률 표시줄이 있는 알림을 표시하기 위한 옵션을 포함하는 데이터 클래스입니다.<br>
 * progressPercent가 0에서 100 사이인지 검증하며, 장시간 실행되는 작업 추적에 적합합니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.<br>
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.<br>
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.<br>
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.<br>
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.<br>
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.<br>
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.<br>
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.<br>
 * @param progressPercent Current progress percentage (0-100).<br><br>
 *                        현재 진행률 (0-100).<br>
 */
public data class ProgressNotificationOption(
    override val notificationId: Int,
    override val smallIcon: Int,
    override val title: String,
    override val content: String?,
    override val isAutoCancel: Boolean = true,
    override val onGoing: Boolean = false,
    override val clickIntent: Intent? = null,
    override val actions: List<Action>? = null,
    override val pendingIntentFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    public val progressPercent: Int,
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        clickIntent = clickIntent,
        onGoing = onGoing,
        actions = actions,
        pendingIntentFlags = pendingIntentFlags
    ) {
    init {
        require(progressPercent in 0..100) { "Progress percent must be between 0 and 100" }
    }
}

/**
 * Data class containing options for creating a PendingIntent.<br><br>
 * PendingIntent 생성을 위한 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param actionId Unique action identifier for the PendingIntent.<br><br>
 *                 PendingIntent의 고유 액션 식별자.<br>
 * @param clickIntent Intent to wrap in the PendingIntent.<br><br>
 *                    PendingIntent에 래핑할 인텐트.<br>
 * @param flags PendingIntent flags.<br><br>
 *              PendingIntent 플래그.<br>
 */
public data class SimplePendingIntentOption(
    public val actionId: Int,
    public val clickIntent: Intent,
    public val flags: Int,
)
