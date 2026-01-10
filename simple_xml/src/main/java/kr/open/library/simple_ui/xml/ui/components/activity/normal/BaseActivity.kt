package kr.open.library.simple_ui.xml.ui.components.activity.normal

import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.components.activity.root.RootActivity

/**
 * A basic Activity that handles layout inflation automatically.<br>
 * Extends RootActivity to inherit permission management.<br><br>
 * 레이아웃 인플레이션을 자동으로 처리하는 기본 Activity입니다.<br>
 * RootActivity를 확장하여 권한 관리를 상속받습니다.<br>
 *
 * Features:<br>
 * - Automatic layout inflation in onCreate<br>
 * - Simple constructor-based layout resource injection<br>
 * 기능:<br>
 * - onCreate에서 자동 레이아웃 인플레이션<br>
 * - 생성자 기반의 간단한 레이아웃 리소스 주입<br>
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
 * @see BaseDataBindingActivity For DataBinding-enabled Activity.<br><br>
 *      DataBinding을 사용하는 Activity는 BaseBindingActivity를 참조하세요.<br>
 */
public abstract class BaseActivity(
    @LayoutRes private val layoutRes: Int,
) : RootActivity() {
    /**
     * Called when the activity is starting.
     * Automatically inflates the layout resource specified in the constructor.<br><br>
     * 액티비티가 시작될 때 호출됩니다.<br>
     * 생성자에서 지정된 레이아웃 리소스를 자동으로 인플레이션합니다.<br>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState.<br><br>
     *                           액티비티가 이전에 종료된 후 다시 초기화되는 경우,
     *                           이 Bundle에는 onSaveInstanceState에서 가장 최근에 제공된 데이터가 포함됩니다.
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(layoutRes)
    }
}
