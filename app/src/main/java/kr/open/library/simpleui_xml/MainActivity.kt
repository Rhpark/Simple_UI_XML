package kr.open.library.simpleui_xml

import android.content.Intent
import android.os.Bundle
import kr.open.library.simple_ui.presenter.extensions.view.bold
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.databinding.ActivityMainBinding
import kr.open.library.simpleui_xml.permission.PermissionsActivity
import kr.open.library.simpleui_xml.permissions_origin.PermissionsActivityOrigin

class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermissionButtons()
    }


    private fun setupPermissionButtons() {
        binding.btnNewPermissionActivity.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }

        binding.btnOriginPermissionActivity.setOnClickListener {
            startActivity(Intent(this, PermissionsActivityOrigin::class.java))
//            requestLocationPermission()
        }

        binding.tvTitle.bold()
    }
}