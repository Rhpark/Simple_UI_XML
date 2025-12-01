package kr.open.library.simpleui_xml.system_service_manager.controller.floating

import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.xml.extensions.view.setGone
import kr.open.library.simple_ui.xml.extensions.view.setVisible
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.system_manager.controller.window.FloatingViewController
import kr.open.library.simple_ui.xml.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.xml.system_manager.controller.window.fixed.FloatingFixedView
import kr.open.library.simple_ui.xml.system_manager.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.xml.system_manager.controller.window.vo.FloatingViewTouchType
import kr.open.library.simple_ui.xml.ui.activity.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityFloatingControllerBinding

class FloatingViewControllerActivity : BaseActivity(R.layout.activity_floating_controller) {
    private lateinit var binding: ActivityFloatingControllerBinding
    private val floatingViewController by lazy { FloatingViewController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFloatingControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    @SuppressLint("MissingPermission")
    private fun initListener() {
        binding.run {
            btnFloatingViewDrag.setOnClickListener {
                onRequestPermissions(listOf(SYSTEM_ALERT_WINDOW)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val icon =
                            getImageView(R.drawable.ic_launcher_foreground).apply {
                                setBackgroundColor(Color.WHITE)
                            }

                        val dragView =
                            FloatingDragView(icon, 100, 100).apply {
                                lifecycleScope.launch {
                                    sfCollisionStateFlow.collect { item ->
                                        when (item.first) {
                                            FloatingViewTouchType.TOUCH_DOWN -> {
                                                showFloatingView()
                                            }
                                            FloatingViewTouchType.TOUCH_MOVE -> {
                                                moveFloatingView(item)
                                            }
                                            FloatingViewTouchType.TOUCH_UP -> {
                                                upFloatingView(this@apply, item)
                                            }
                                        }
                                    }
                                }
                            }
                        floatingViewController.addFloatingDragView(dragView)
                        toastShowShort("Drag view added")
                    } else {
                        toastShowShort("Permission Denied: $deniedPermissions")
                    }
                }
            }

            btnFloatingViewFix.setOnClickListener {
                onRequestPermissions(listOf(SYSTEM_ALERT_WINDOW)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val icon =
                            getImageView(R.drawable.ic_launcher_foreground).apply {
                                setBackgroundColor(Color.GREEN)
                            }
                        val fixedView = FloatingFixedView(icon, 200, 300)
                        floatingViewController.setFloatingFixedView(fixedView)
                        toastShowShort("Fixed view set")
                    } else {
                        toastShowShort("Permission Denied: $deniedPermissions")
                    }
                }
            }

            btnFloatingViewRemove.setOnClickListener {
                floatingViewController.removeAllFloatingView()
                toastShowShort("All floating views removed")
            }
        }
    }

    private fun getImageView(res: Int): ImageView =
        ImageView(this).apply {
            setImageResource(res)
        }

    private fun showFloatingView() {
        floatingViewController.getFloatingFixedView()?.view?.let {
            it.setVisible()
            showAnimScale(it, null)
        }
    }

    private fun moveFloatingView(item: Pair<FloatingViewTouchType, FloatingViewCollisionsType>) {
        floatingViewController.getFloatingFixedView()?.view?.let {
            if (item.second == FloatingViewCollisionsType.OCCURING) {
                val rotationAnim = ObjectAnimator.ofFloat(it, "rotation", 0.0f, 180.0f)
                rotationAnim.duration = 300
                rotationAnim.start()
            }
        }
    }

    private fun upFloatingView(
        floatingView: FloatingDragView,
        item: Pair<FloatingViewTouchType, FloatingViewCollisionsType>,
    ) {
        floatingViewController.getFloatingFixedView()?.view?.let {
            hideAnimScale(
                it,
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        floatingViewController.getFloatingFixedView()?.let { it.view.setGone() }
                        if (item.second == FloatingViewCollisionsType.OCCURING) {
                            floatingViewController.removeFloatingDragView(floatingView)
                        }
                    }
                },
            )
        }
    }

    private fun hideAnimScale(
        view: View,
        listener: Animator.AnimatorListener?,
    ) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.0f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = 300
            listener?.let { addListener(it) }
            start()
        }
    }

    private fun showAnimScale(
        view: View,
        listener: Animator.AnimatorListener?,
    ) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.0f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.0f, 1.0f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = 300
            listener?.let { addListener(it) }
            start()
        }
    }
}
