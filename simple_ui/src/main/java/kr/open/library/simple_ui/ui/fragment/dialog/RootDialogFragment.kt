package kr.open.library.simple_ui.ui.fragment.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kr.open.library.logcat.Logx
import kr.open.library.systemmanager.extenstions.getDisplayInfo

public abstract class RootDialogFragment() : DialogFragment() {

    private var onPositiveClickListener: ((View) -> Unit)? = null
    private var onNegativeClickListener: ((View) -> Unit)? = null
    private var onOtherClickListener: ((View) -> Unit)? = null

    @StyleRes
    private var animationStyle: Int? = null
    private var dialogGravity: Int = Gravity.CENTER
    private var dialogCancelable: Boolean = true

    public interface OnItemClick {
        public fun onItemClickListener(v: View)
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
}