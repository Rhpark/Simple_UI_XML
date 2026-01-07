package kr.open.library.simple_ui.core.system_manager.controller.notification

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
 * Companion object containing default channel constants.<br><br>
 * 기본 채널 상수를 포함하는 컴패니언 객체입니다.<br>
 */
public object NotificationDefaultChannel {
    /**
     * Default notification channel ID.<br><br>
     * 기본 알림 채널 ID.<br>
     */
    public const val DEFAULT_CHANNEL_ID = "default_notification_channel"

    /**
     * Default notification channel name.<br><br>
     * 기본 알림 채널 이름.<br>
     */
    public const val DEFAULT_CHANNEL_NAME = "Default Notifications"

    /**
     * Default notification channel description.<br><br>
     * 기본 알림 채널 설명.<br>
     */
    public const val DEFAULT_CHANNEL_DESCRIPTION = "Default notification channel for the application"
}
