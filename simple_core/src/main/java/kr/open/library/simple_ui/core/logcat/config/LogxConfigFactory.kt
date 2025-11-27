package kr.open.library.simple_ui.core.logcat.config

import android.content.Context

/**
 * Factory object for creating LogxConfig instances with Android Context dependencies.<br>
 * These methods are separated from the core LogxConfig class to facilitate unit testing without Robolectric.<br><br>
 * Android Context 의존성을 가진 LogxConfig 인스턴스를 생성하는 팩토리 객체입니다.<br>
 * 이 메서드들은 Robolectric 없이 단위 테스트를 용이하게 하기 위해 핵심 LogxConfig 클래스에서 분리되었습니다.<br>
 */
object LogxConfigFactory {

    /**
     * Creates a LogxConfig with the specified storage type.<br>
     * Defaults to APP_EXTERNAL storage if not specified.<br><br>
     * 지정된 저장소 타입으로 LogxConfig를 생성합니다.<br>
     * 지정하지 않으면 APP_EXTERNAL 저장소를 기본값으로 사용합니다.<br>
     *
     * @param context The Android context for accessing storage paths.<br><br>
     *                저장소 경로에 접근하기 위한 Android 컨텍스트.
     *
     * @param storageType The type of storage to use. Defaults to APP_EXTERNAL.<br><br>
     *                    사용할 저장소 타입. 기본값은 APP_EXTERNAL입니다.
     *
     * @return A LogxConfig instance configured for the specified storage type.<br><br>
     *         지정된 저장소 타입으로 구성된 LogxConfig 인스턴스.<br>
     */
    fun createDefault(
        context: Context,
        storageType: LogxStorageType = LogxStorageType.APP_EXTERNAL,
    ): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getLogPath(context, storageType),
        storageType = storageType,
    )

    /**
     * Creates a LogxConfig configured for internal storage.<br>
     * Internal storage requires no permissions but is not accessible to users.<br><br>
     * 내부 저장소용으로 구성된 LogxConfig를 생성합니다.<br>
     * 내부 저장소는 권한이 필요 없지만 사용자가 접근할 수 없습니다.<br>
     *
     * @param context The Android context.<br><br>
     *                Android 컨텍스트.
     *
     * @return A LogxConfig instance for internal storage.<br><br>
     *         내부 저장소용 LogxConfig 인스턴스.<br>
     */
    fun createInternal(context: Context): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getInternalLogPath(context),
        storageType = LogxStorageType.INTERNAL,
    )

    /**
     * Creates a LogxConfig configured for app-specific external storage.<br>
     * This storage requires no permissions and is accessible via file manager, but is deleted when the app is uninstalled.<br><br>
     * 앱 전용 외부 저장소용으로 구성된 LogxConfig를 생성합니다.<br>
     * 이 저장소는 권한이 필요 없고 파일 관리자로 접근 가능하지만, 앱 삭제 시 함께 삭제됩니다.<br>
     *
     * @param context The Android context.<br><br>
     *                Android 컨텍스트.
     *
     * @return A LogxConfig instance for app-specific external storage.<br><br>
     *         앱 전용 외부 저장소용 LogxConfig 인스턴스.<br>
     */
    fun createAppExternal(context: Context): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getAppExternalLogPath(context),
        storageType = LogxStorageType.APP_EXTERNAL,
    )

    /**
     * Creates a LogxConfig configured for public external storage.<br>
     * Behavior differs by Android version (requires permission on API 28 and below).<br><br>
     * 공용 외부 저장소용으로 구성된 LogxConfig를 생성합니다.<br>
     * Android 버전에 따라 동작이 다릅니다 (API 28 이하에서 권한 필요).<br>
     *
     * @param context The Android context.<br><br>
     *                Android 컨텍스트.
     *
     * @return A LogxConfig instance for public external storage.<br><br>
     *         공용 외부 저장소용 LogxConfig 인스턴스.<br>
     */
    fun createPublicExternal(context: Context): LogxConfig = LogxConfig(
        saveFilePath = LogxPathUtils.getPublicExternalLogPath(context),
        storageType = LogxStorageType.PUBLIC_EXTERNAL,
    )

    /**
     * Creates a LogxConfig for the specified storage type using a factory pattern.<br><br>
     * 팩토리 패턴을 사용하여 지정된 저장소 타입의 LogxConfig를 생성합니다.<br>
     *
     * @param context The Android context.<br><br>
     *                Android 컨텍스트.
     *
     * @param storageType The type of storage to configure.<br><br>
     *                    구성할 저장소 타입.
     *
     * @return A LogxConfig instance for the specified storage type.<br><br>
     *         지정된 저장소 타입의 LogxConfig 인스턴스.<br>
     */
    fun create(
        context: Context,
        storageType: LogxStorageType,
    ): LogxConfig = when (storageType) {
        LogxStorageType.INTERNAL -> createInternal(context)
        LogxStorageType.APP_EXTERNAL -> createAppExternal(context)
        LogxStorageType.PUBLIC_EXTERNAL -> createPublicExternal(context)
    }
}
