package kr.open.library.simple_ui.presenter.ui.fragment.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.permissions.register.PermissionRequester
import kr.open.library.simple_ui.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.system_manager.extensions.getDisplayInfo

public abstract class RootDialogFragment() : DialogFragment(), PermissionRequester {

    private var onPositiveClickListener: ((View) -> Unit)? = null
    private var onNegativeClickListener: ((View) -> Unit)? = null
    private var onOtherClickListener: ((View) -> Unit)? = null

    @StyleRes
    private var animationStyle: Int? = null
    private var dialogGravity: Int = Gravity.CENTER
    private var dialogCancelable: Boolean = true

    /************************
     *   Permission Check   *
     ************************/
    protected lateinit var permissionDelegate : PermissionDelegate<DialogFragment>

    public interface OnItemClick {
        public fun onItemClickListener(v: View)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(dialogCancelable)

            // Apply animation if set
            animationStyle?.let { style ->
                window?.attributes?.windowAnimations = style
            }

            // Apply gravity if not center
            if (dialogGravity != Gravity.CENTER) {
                window?.setGravity(dialogGravity)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }

    protected fun resizeDialog(widthRatio: Float, heightRatio: Float) {
        dialog?.window?.let {
            val screenSize = requireContext().getDisplayInfo().getScreen()
            Logx.d("Screen Size $screenSize, " + requireContext().getDisplayInfo().getFullScreenSize())
            val x = (screenSize.x * widthRatio).toInt()
            val y = (screenSize.y * heightRatio).toInt()
            it.setLayout(x, y)
        }?: Logx.e("Error dialog window is null!")
    }

    /**
     * Sets the custom animation style for dialog appearance/disappearance
     */
    public fun setAnimationStyle(@StyleRes style: Int) {
        this.animationStyle = style
        dialog?.window?.attributes?.windowAnimations = style
    }

    /**
     * Sets the position of the dialog on screen
     * @param gravity Gravity value (e.g., Gravity.BOTTOM)
     */
    public fun setDialogGravity(gravity: Int) {
        this.dialogGravity = gravity
        dialog?.window?.setGravity(gravity)
    }

    /**
     * Sets whether the dialog can be canceled by pressing back or touching outside
     */
    public fun setCancelableDialog(cancelable: Boolean) {
        this.dialogCancelable = cancelable
        dialog?.setCancelable(cancelable)
    }

    public fun setOnPositiveClickListener(listener: (View) -> Unit) {
        onPositiveClickListener = listener
    }

    public fun setOnNegativeClickListener(listener: (View) -> Unit) {
        onNegativeClickListener = listener
    }

    public fun setOnOtherClickListener(listener: (View) -> Unit) {
        onOtherClickListener = listener
    }

    public fun safeDismiss() {
        try {
            dismiss()
        } catch (e: Exception) {
            Logx.e("Error $e")
        }
    }

    public fun safeShow(fragmentManager: FragmentManager, tag: String) {
        try {
            show(fragmentManager, tag)
        } catch (e:Exception) {
            Logx.e("Error $e")
        }
    }

    override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        permissionDelegate.requestPermissions(permissions, onResult)
    }
}