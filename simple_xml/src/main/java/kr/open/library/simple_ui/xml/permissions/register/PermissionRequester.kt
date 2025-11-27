package kr.open.library.simple_ui.xml.permissions.register

/**
 * Contract for components capable of initiating permission flows.<br><br>
 * 권한 요청 흐름을 시작할 수 있는 구성요소를 위한 계약입니다.<br>
 */
interface PermissionRequester {

    /**
     * Requests the supplied permissions and returns denied results via callback.<br><br>
     * 전달받은 권한을 요청하고 거부된 목록을 콜백으로 돌려줍니다.<br>
     *
     * @param permissions Permissions to request.<br><br>
     *                    요청할 권한 목록입니다.<br>
     * @param onResult Callback receiving denied permissions.<br><br>
     *                 거부된 권한 목록을 전달받는 콜백입니다.<br>
     */
    fun onRequestPermissions(
        permissions: List<String>,
        onResult: ((deniedPermissions: List<String>) -> Unit),
    )
}
