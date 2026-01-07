package kr.open.library.simple_ui.core.system_manager.controller.notification.option

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat.Action

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
)

/**
 * Data class containing all options for displaying a notification.<br><br>
 * 알림 표시를 위한 모든 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.
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
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        onGoing = onGoing,
        clickIntent = clickIntent,
        actions = actions
    )

/**
 * Data class containing all options for displaying a notification.<br><br>
 * 알림 표시를 위한 모든 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.
 * @param snippet Extended text for BIG_TEXT style.<br><br>
 *                BIG_TEXT 스타일을 위한 확장 텍스트.
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
    public val snippet: String,
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        onGoing = onGoing,
        clickIntent = clickIntent,
        actions = actions
    )

/**
 * Data class containing all options for displaying a notification.<br><br>
 * 알림 표시를 위한 모든 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.
 * @param bigPicture Bitmap for the large bitmap.<br><br>
 *                  큰 비트맵 이미지.
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
    public val bigPicture: Bitmap,
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        onGoing = onGoing,
        clickIntent = clickIntent,
        actions = actions
    )

/**
 * Data class containing all options for displaying a notification.<br><br>
 * 알림 표시를 위한 모든 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.
 * @param progressPercent Current progress percentage (0-100).<br><br>
 *                        현재 진행률 (0-100).
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
    public val progressPercent: Int,
) : SimpleNotificationOptionBase(
        notificationId = notificationId,
        smallIcon = smallIcon,
        title = title,
        content = content,
        isAutoCancel = isAutoCancel,
        clickIntent = clickIntent,
        onGoing = onGoing,
        actions = actions
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
 *                 PendingIntent의 고유 액션 식별자.
 * @param clickIntent Intent to wrap in the PendingIntent.<br><br>
 *                    PendingIntent에 래핑할 인텐트.
 * @param flags PendingIntent flags.<br><br>
 *              PendingIntent 플래그.
 */
public data class SimplePendingIntentOption(
    public val actionId: Int,
    public val clickIntent: Intent,
    public val flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)
