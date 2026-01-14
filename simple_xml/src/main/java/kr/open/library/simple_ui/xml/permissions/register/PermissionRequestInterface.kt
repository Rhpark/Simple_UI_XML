package kr.open.library.simple_ui.xml.permissions.register

import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest

/**
 * Contract for components capable of initiating permission flows.<br><br>
 * 권한 요청 흐름을 시작할 수 있는 구성요소를 위한 계약입니다.<br>
 */
interface PermissionRequestInterface {
    /**
     * Requests multiple permissions and returns denied results via callback.<br><br>
     * 여러 권한을 요청하고 거부 결과를 콜백으로 반환합니다.<br>
     *
     * @param permissions Permissions to request.<br><br>
     *                    요청할 권한 목록입니다.<br>
     * @param onDeniedResult Callback invoked with denied items.<br><br>
     *                       거부 항목을 전달받는 콜백입니다.<br>
     * @param onRationaleNeeded Callback for rationale UI when needed.<br><br>
     *                          필요 시 rationale UI를 제공하는 콜백입니다.<br>
     * @param onNavigateToSettings Callback for settings navigation when needed.<br><br>
     *                             필요 시 설정 이동을 안내하는 콜백입니다.<br>
     */
    fun requestPermissions(
        permissions: List<String>,
        onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
        onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)?,
        onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)?,
    )

    /**
     * Requests multiple permissions and returns denied results via callback.<br><br>
     * 여러 권한을 요청하고 거부 결과를 콜백으로 반환합니다.<br>
     *
     * @param permissions Permissions to request.<br><br>
     *                    요청할 권한 목록입니다.<br>
     * @param onDeniedResult Callback invoked with denied items.<br><br>
     *                       거부 항목을 전달받는 콜백입니다.<br>
     */
    fun requestPermissions(
        permissions: List<String>,
        onDeniedResult: (List<PermissionDeniedItem>) -> Unit
    )
}
