package kr.open.library.simple_ui.core.logcat.config

/**
 * Immutable snapshot of current Logx configuration.<br><br>
 * 현재 Logx 설정의 불변 스냅샷이다.<br>
 *
 * @property isLogging Whether logging is enabled.<br><br>
 *                     로그 출력 활성화 여부.<br>
 * @property logTypes Allowed log types (allowlist).<br><br>
 *                    허용된 로그 타입 목록(허용 목록).<br>
 * @property isLogTagBlockListEnabled Whether tag blocklist is enabled.<br><br>
 *                                   태그 차단 목록 사용 여부.<br>
 * @property logTagBlockList Tags to block when blocklist is enabled.<br><br>
 *                          차단할 태그 목록.<br>
 * @property isSaveEnabled Whether file logging is enabled.<br><br>
 *                         파일 저장 활성화 여부.<br>
 * @property storageType Storage type for file output.<br><br>
 *                       파일 저장소 타입.<br>
 * @property saveDirectory Optional custom directory path.<br><br>
 *                         사용자 지정 저장 경로(선택).<br>
 * @property appName Application name used in tag/prefix.<br><br>
 *                  태그/프리픽스에 사용하는 앱 이름.<br>
 * @property skipPackages Package prefixes to skip when resolving stack frames.<br><br>
 *                        스택 프레임 해석 시 제외할 패키지 prefix 목록.<br>
 */
internal data class LogxConfigSnapshot(
    val isLogging: Boolean,
    val logTypes: Set<LogType>,
    val isLogTagBlockListEnabled: Boolean,
    val logTagBlockList: Set<String>,
    val isSaveEnabled: Boolean,
    val storageType: StorageType,
    val saveDirectory: String?,
    val appName: String,
    val skipPackages: Set<String>,
)
