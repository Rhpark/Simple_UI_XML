package kr.open.library.simple_ui.xml.ui.activity

import android.os.Bundle
import androidx.annotation.LayoutRes

/**
 * A basic Activity that handles layout inflation automatically.<br>
 * Extends RootActivity to inherit system bar control and permission management.<br><br>
 * 레이아웃 인플레이션을 자동으로 처리하는 기본 Activity입니다.<br>
 * RootActivity를 확장하여 시스템 바 제어와 권한 관리를 상속받습니다.<br>
 *
 * Features:<br>
 * - Automatic layout inflation in onCreate<br>
 * - Simple constructor-based layout resource injection<br>
 * - All RootActivity features (system bar control, permissions)<br><br>
 * 기능:<br>
 * - onCreate에서 자동 레이아웃 인플레이션<br>
 * - 생성자 기반의 간단한 레이아웃 리소스 주입<br>
 * - 모든 RootActivity 기능 (시스템 바 제어, 권한)<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class MainActivity : BaseActivity(R.layout.activity_main) {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // Layout is already inflated, start your logic here
 *         // findViewById or view binding can be used
 *     }
 * }
 * ```
 *
 * @param layoutRes The layout resource ID to be inflated.<br><br>
 *                  인플레이션할 레이아웃 리소스 ID.<br>
 *
 * @see RootActivity For base class with system bar and permission features.<br><br>
 *      시스템 바와 권한 기능이 있는 기본 클래스는 RootActivity를 참조하세요.<br>
 *
 * @see BaseBindingActivity For DataBinding-enabled Activity.<br><br>
 *      DataBinding을 사용하는 Activity는 BaseBindingActivity를 참조하세요.<br>
 */
public abstract class BaseActivity(
    @LayoutRes private val layoutRes: Int,
) : RootActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
    }
}
