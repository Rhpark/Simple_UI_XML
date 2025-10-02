package kr.open.library.simpleui_xml.system_service_manager.controller

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
import kr.open.library.simple_ui.extensions.date.toLocalDateTime
import kr.open.library.simple_ui.presenter.extensions.view.setGone
import kr.open.library.simple_ui.presenter.extensions.view.setVisible
import kr.open.library.simple_ui.presenter.extensions.view.toastShort
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simple_ui.system_manager.controller.alarm.vo.AlarmVo
import kr.open.library.simple_ui.system_manager.controller.window.FloatingViewController
import kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.system_manager.controller.window.fixed.FloatingFixedView
import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewCollisionsType
import kr.open.library.simple_ui.system_manager.controller.window.vo.FloatingViewTouchType
import kr.open.library.simple_ui.system_manager.extensions.getAlarmController
import kr.open.library.simple_ui.system_manager.extensions.getSoftKeyboardController
import kr.open.library.simple_ui.system_manager.extensions.getVibratorController
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityServiceManagerControllerBinding
import kr.open.library.simpleui_xml.system_service_manager.controller.alarm.AlarmReceiver

class ServiceManagerControllerActivity : BaseBindingActivity<ActivityServiceManagerControllerBinding>(R.layout.activity_service_manager_controller) {

    private val floatingViewController by lazy { FloatingViewController(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initListener()
    }

    @SuppressLint("MissingPermission")
    private fun initListener() {
        binding.run {
            btnSoftKeyboard.setOnClickListener {
                edtTest01.isFocusable = true
                getSoftKeyboardController().show(edtTest01)
            }

            btnVibrator.setOnClickListener { getVibratorController().vibrate(1000) }

            btnAlarm.setOnClickListener {
                val date = (System.currentTimeMillis()+30000).toLocalDateTime()
                val alarmVo = AlarmVo(
                    key = 1,
                    title = "Dump Title",
                    message = "Dump Message",
                    soundUri = null,
                    hour = date.hour,
                    minute = date.minute,
                    second = date.second,
                )
                getAlarmController().registerAlarmClock(AlarmReceiver::class.java, alarmVo)
            }

            btnFloatingViewDrag.setOnClickListener {
                onRequestPermissions(listOf(SYSTEM_ALERT_WINDOW)) { deniedPermissions ->
                    if(deniedPermissions.isEmpty()) {

                        val icon = getImageView(R.drawable.ic_launcher_foreground).apply { setBackgroundColor(Color.WHITE) }

                        val dragView = FloatingDragView(icon, 100, 100).apply {
                            lifecycleScope.launch {
                                sfCollisionStateFlow.collect { item ->
                                    when (item.first) {
                                        FloatingViewTouchType.TOUCH_DOWN -> { showFloatingView() }
                                        FloatingViewTouchType.TOUCH_MOVE -> { moveFloatingView(item) }
                                        FloatingViewTouchType.TOUCH_UP -> { upFloatingView(this@apply,item) }
                                    }
                                }
                            }
                        }
                        floatingViewController.addFloatingDragView(dragView)
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnFloatingViewFix.setOnClickListener {
                onRequestPermissions(listOf(SYSTEM_ALERT_WINDOW)) { deniedPermissions ->
                    if(deniedPermissions.isEmpty()) {

                        val icon = getImageView(R.drawable.ic_launcher_foreground).apply { setBackgroundColor(Color.GREEN) }
                        val fixedView = FloatingFixedView(icon, 200, 300) // or FloatingDragView(icon, 200, 300)
                        floatingViewController.setFloatingFixedView(fixedView)
                    } else {
                        toastShort("Permission Denied $deniedPermissions")
                    }
                }
            }

            btnFloatingViewRemove.setOnClickListener { floatingViewController.removeAllFloatingView() }
        }
    }

    private fun getImageView(res: Int): ImageView = ImageView(this).apply {
        setImageResource(res)
//        setOnClickListener { Logx.d("OnClick Listener") }
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

    private fun upFloatingView(floatingView:FloatingDragView,item: Pair<FloatingViewTouchType, FloatingViewCollisionsType>) {
        floatingViewController.getFloatingFixedView()?.view?.let {
            hideAnimScale(it, object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    floatingViewController.getFloatingFixedView()?.let { it.view.setGone() }
                    if (item.second == FloatingViewCollisionsType.OCCURING) {
                        floatingViewController.removeFloatingDragView(floatingView)
                    }
                }
            })
        }
    }

    private fun hideAnimScale(view: View, listener: Animator.AnimatorListener?) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.0f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = 300
            listener?.let { addListener(it) }
            start()
        }
    }

    private fun showAnimScale(view: View, listener: Animator.AnimatorListener?) {
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