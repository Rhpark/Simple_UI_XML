package kr.open.library.simple_ui.xml.ui.layout.base.bind

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import kr.open.library.simple_ui.xml.extensions.view.doOnLayout

/**
 * Sets LifecycleOwner for DataBinding with a one-time layout retry if needed.<br><br>
 * DataBinding에 LifecycleOwner를 설정하고 필요 시 레이아웃 완료 시점에 1회 재시도합니다.<br>
 *
 * @param binding The target ViewDataBinding instance.<br><br>
 *                대상 ViewDataBinding 인스턴스입니다.<br>
 */
internal fun View.bindLifecycleOwnerOnce(binding: ViewDataBinding) {
    val owner = findViewTreeLifecycleOwner() ?: (context as? LifecycleOwner)
    if (owner != null) {
        binding.lifecycleOwner = owner
        return
    }

    doOnLayout {
        if (!isAttachedToWindow) return@doOnLayout
        val retryOwner = findViewTreeLifecycleOwner() ?: (context as? LifecycleOwner)
        if (retryOwner != null) {
            binding.lifecycleOwner = retryOwner
        }
    }
}
