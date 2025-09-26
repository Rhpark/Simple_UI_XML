package kr.open.library.simpleui_xml

import android.content.Intent
import android.os.Bundle
import kr.open.library.simple_ui.presenter.extensions.view.bold
import kr.open.library.simple_ui.presenter.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.databinding.ActivityMainBinding
import kr.open.library.simpleui_xml.logx.LogxActivity
import kr.open.library.simpleui_xml.permission.PermissionsActivity
import kr.open.library.simpleui_xml.permissions_origin.PermissionsActivityOrigin
import kr.open.library.simpleui_xml.recyclerview.new_.RecyclerViewActivity
import kr.open.library.simpleui_xml.recyclerview.origin.RecyclerViewActivityOrigin

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
        binding.btnLogxActivity.setOnClickListener {
            startActivity(Intent(this, LogxActivity::class.java))
//            requestLocationPermission()
        }

        binding.btnNewRecyclerView.setOnClickListener {
            startActivity(Intent(this, RecyclerViewActivity::class.java))
//            requestLocationPermission()
        }

        binding.btnOriginRecyclerView.setOnClickListener {
            startActivity(Intent(this, RecyclerViewActivityOrigin::class.java))
//            requestLocationPermission()
        }

        binding.tvTitle.bold()
    }
}