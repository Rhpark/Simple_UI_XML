package kr.open.library.simple_ui.system_manager.controller.notification.vo

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat.Action

/**
 * 알림 스타일을 정의하는 enum 클래스
 * Enum class that defines notification styles
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

public enum class SimpleNotificationType {
    ACTIVITY, SERVICE, BROADCAST,
}

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

public data class SimplePendingIntentOptionVo(
    public val actionId: Int,
    public val clickIntent: Intent,
    public val flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)