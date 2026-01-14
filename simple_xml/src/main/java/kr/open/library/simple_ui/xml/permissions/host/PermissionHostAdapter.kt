package kr.open.library.simple_ui.xml.permissions.host

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

/**
 * Adapter that exposes Activity/Fragment capabilities for permission requests.<br><br>
 * 권한 요청에 필요한 Activity/Fragment 기능을 노출하는 어댑터입니다.<br>
 */
sealed class PermissionHostAdapter {
    /**
     * Context used for permission checks and system services.<br><br>
     * 권한 확인과 시스템 서비스 접근에 사용하는 컨텍스트입니다.<br>
     */
    abstract val context: Context

    /**
     * ActivityResult caller used to register launchers.<br><br>
     * 런처 등록에 사용하는 ActivityResult 호출자입니다.<br>
     */
    abstract val activityResultCaller: ActivityResultCaller

    /**
     * Lifecycle owner for observing host lifecycle events.<br><br>
     * 호스트 생명주기 이벤트 관찰에 사용하는 LifecycleOwner입니다.<br>
     */
    abstract val lifecycleOwner: LifecycleOwner

    /**
     * Returns whether the host should show a rationale UI for [permission].<br><br>
     * 호스트가 [permission]에 대한 설명 UI를 표시해야 하는지 반환합니다.<br>
     *
     * @param permission Permission string to inspect.<br><br>
     *                  확인할 권한 문자열입니다.<br>
     * @return Return value: true when rationale should be shown. Log behavior: none.<br><br>
     *         반환값: 설명이 필요하면 true. 로그 동작: 없음.<br>
     */
    abstract fun shouldShowRequestPermissionRationale(permission: String): Boolean

    /**
     * Activity-backed host adapter implementation.<br><br>
     * Activity 기반 호스트 어댑터 구현입니다.<br>
     *
     * @param activity Activity used as the host.<br><br>
     *                 호스트로 사용하는 Activity입니다.<br>
     */
    class ActivityHost(
        private val activity: ComponentActivity,
    ) : PermissionHostAdapter() {
        /**
         * Context bound to the Activity host.<br><br>
         * Activity 호스트에 바인딩된 컨텍스트입니다.<br>
         */
        override val context: Context = activity

        /**
         * ActivityResult caller bound to the Activity host.<br><br>
         * Activity 호스트에 바인딩된 ActivityResult 호출자입니다.<br>
         */
        override val activityResultCaller: ActivityResultCaller = activity

        /**
         * Lifecycle owner bound to the Activity host.<br><br>
         * Activity 호스트에 바인딩된 LifecycleOwner입니다.<br>
         */
        override val lifecycleOwner: LifecycleOwner = activity

        /**
         * Returns whether the Activity should show a rationale UI for [permission].<br><br>
         * Activity가 [permission]에 대한 설명 UI를 표시해야 하는지 반환합니다.<br>
         *
         * @param permission Permission string to inspect.<br><br>
         *                  확인할 권한 문자열입니다.<br>
         * @return Return value: true when rationale should be shown. Log behavior: none.<br><br>
         *         반환값: 설명이 필요하면 true. 로그 동작: 없음.<br>
         */
        override fun shouldShowRequestPermissionRationale(permission: String): Boolean =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Fragment-backed host adapter implementation.<br><br>
     * Fragment 기반 호스트 어댑터 구현입니다.<br>
     *
     * @param fragment Fragment used as the host.<br><br>
     *                 호스트로 사용하는 Fragment입니다.<br>
     */
    class FragmentHost(
        private val fragment: Fragment,
    ) : PermissionHostAdapter() {
        /**
         * Context bound to the Fragment host.<br><br>
         * Fragment 호스트에 바인딩된 컨텍스트입니다.<br>
         */
        override val context: Context = fragment.requireContext()

        /**
         * ActivityResult caller bound to the Fragment host.<br><br>
         * Fragment 호스트에 바인딩된 ActivityResult 호출자입니다.<br>
         */
        override val activityResultCaller: ActivityResultCaller = fragment

        /**
         * Lifecycle owner bound to the Fragment host.<br><br>
         * Fragment 호스트에 바인딩된 LifecycleOwner입니다.<br>
         */
        override val lifecycleOwner: LifecycleOwner = fragment

        /**
         * Returns whether the Fragment should show a rationale UI for [permission].<br><br>
         * Fragment가 [permission]에 대한 설명 UI를 표시해야 하는지 반환합니다.<br>
         *
         * @param permission Permission string to inspect.<br><br>
         *                  확인할 권한 문자열입니다.<br>
         * @return Return value: true when rationale should be shown. Log behavior: none.<br><br>
         *         반환값: 설명이 필요하면 true. 로그 동작: 없음.<br>
         */
        override fun shouldShowRequestPermissionRationale(permission: String): Boolean =
            fragment.shouldShowRequestPermissionRationale(permission)
    }
}
