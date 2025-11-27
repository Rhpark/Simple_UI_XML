package kr.open.library.simple_ui.core.system_manager.controller.notification.vo

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat.Action

/**
 * Enum class that defines notification styles.<br><br>
 * 알림 스타일을 정의하는 enum 클래스입니다.<br>
 */
public enum class NotificationStyle {
    /**
     * 기본 알림 스타일
     * Default notification style
     */
    DEFAULT,

    /**
     * 큰 이미지를 포함하는 알림 스타일
     * Notification style with big picture
     */
    BIG_PICTURE,

    /**
     * 긴 텍스트를 포함하는 알림 스타일
     * Notification style with expanded text
     */
    BIG_TEXT,

    /**
     * 진행률 바를 포함하는 알림 스타일
     * Notification style with progress bar
     */
    PROGRESS
}

/**
 * Enum defining the type of action when notification is clicked.<br><br>
 * 알림 클릭 시 동작 유형을 정의하는 열거형입니다.<br>
 */
public enum class SimpleNotificationType {
    /**
     * Launch an Activity when notification is clicked.<br><br>
     * 알림 클릭 시 Activity를 실행합니다.<br>
     */
    ACTIVITY,

    /**
     * Start a Service when notification is clicked.<br><br>
     * 알림 클릭 시 Service를 시작합니다.<br>
     */
    SERVICE,

    /**
     * Send a Broadcast when notification is clicked.<br><br>
     * 알림 클릭 시 Broadcast를 전송합니다.<br>
     */
    BROADCAST,
}

/**
 * Data class containing all options for displaying a notification.<br><br>
 * 알림 표시를 위한 모든 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.
 * @param largeIcon Bitmap for the large icon.<br><br>
 *                  큰 아이콘의 비트맵.
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.
 * @param snippet Extended text for BIG_TEXT style.<br><br>
 *                BIG_TEXT 스타일을 위한 확장 텍스트.
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.
 * @param style Notification style to use.<br><br>
 *              사용할 알림 스타일.
 */
public data class SimpleNotificationOptionVo(
    public val notificationId: Int,
    public val title: String? = null,
    public val content: String? = null,
    public val isAutoCancel: Boolean = false,
    public val smallIcon: Int? = null,
    public val largeIcon: Bitmap? = null,
    public val clickIntent: Intent? = null,
    public val snippet: String? = null,
    public val actions: List<Action>? = null,
    public val onGoing: Boolean = false,
    public val style: NotificationStyle = NotificationStyle.DEFAULT
)

/**
 * Data class containing options for displaying a progress notification.<br><br>
 * 진행률 알림 표시를 위한 옵션을 포함하는 데이터 클래스입니다.<br>
 *
 * @param notificationId Unique identifier for the notification.<br><br>
 *                       알림의 고유 식별자.
 * @param title Notification title text.<br><br>
 *              알림 제목 텍스트.
 * @param content Notification content text.<br><br>
 *                알림 내용 텍스트.
 * @param isAutoCancel Whether to automatically dismiss when clicked.<br><br>
 *                     클릭 시 자동으로 닫힐지 여부.
 * @param smallIcon Resource ID for the small icon.<br><br>
 *                  작은 아이콘의 리소스 ID.
 * @param clickIntent Intent to execute when notification is clicked.<br><br>
 *                    알림 클릭 시 실행할 인텐트.
 * @param actions List of action buttons to display.<br><br>
 *                표시할 액션 버튼 목록.
 * @param progressPercent Current progress percentage (0-100).<br><br>
 *                        현재 진행률 (0-100).
 * @param onGoing Whether this is an ongoing notification.<br><br>
 *                진행 중인 알림인지 여부.
 * @param style Notification style (should be PROGRESS).<br><br>
 *              알림 스타일 (PROGRESS여야 함).
 */
public data class SimpleProgressNotificationOptionVo(
    public val notificationId: Int,
    public val title: String? = null,
    public val content: String? = null,
    public val isAutoCancel: Boolean = false,
    public val smallIcon: Int? = null,
    public val clickIntent: Intent? = null,
    public val actions: List<Action>? = null,
    public val progressPercent: Int,
    public val onGoing: Boolean = false,
    public val style: NotificationStyle = NotificationStyle.PROGRESS
)

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
public data class SimplePendingIntentOptionVo(
    public val actionId: Int,
    public val clickIntent: Intent,
    public val flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)