package kr.open.library.simpleui_xml

import android.content.Intent
import kr.open.library.simple_ui.xml.extensions.view.bold
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.activity_fragment.ActivityFragmentActivity
import kr.open.library.simpleui_xml.databinding.ActivityMainBinding
import kr.open.library.simpleui_xml.extenstions_style.ExtensionsStyleActivity
import kr.open.library.simpleui_xml.logx.LogxActivity
import kr.open.library.simpleui_xml.permission.PermissionsActivity
import kr.open.library.simpleui_xml.permissions_origin.PermissionsActivityOrigin
import kr.open.library.simpleui_xml.recyclerview.new_.RecyclerViewActivity
import kr.open.library.simpleui_xml.recyclerview.origin.RecyclerViewActivityOrigin
import kr.open.library.simpleui_xml.system_service_manager.controller.ServiceManagerControllerActivity
import kr.open.library.simpleui_xml.system_service_manager.info.ServiceManagerInfoActivity

class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {
    override fun onInitBind(binding: ActivityMainBinding) {
        binding.btnNewPermissionActivity.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }

        binding.btnOriginPermissionActivity.setOnClickListener {
            startActivity(Intent(this, PermissionsActivityOrigin::class.java))
        }
        binding.btnLogxActivity.setOnClickListener {
            startActivity(Intent(this, LogxActivity::class.java))
        }

        binding.btnNewRecyclerView.setOnClickListener {
            startActivity(Intent(this, RecyclerViewActivity::class.java))
        }

        binding.btnOriginRecyclerView.setOnClickListener {
            startActivity(Intent(this, RecyclerViewActivityOrigin::class.java))
        }

        binding.btnSystemServiceManagerController.setOnClickListener {
            startActivity(Intent(this, ServiceManagerControllerActivity::class.java))
        }

        binding.btnSystemServiceManagerInfo.setOnClickListener {
            startActivity(Intent(this, ServiceManagerInfoActivity::class.java))
        }

        binding.btnGotoExtensionsStyle.setOnClickListener {
            startActivity(Intent(this, ExtensionsStyleActivity::class.java))
        }

        binding.btnActivityFragmentExample.setOnClickListener {
            startActivity(Intent(this, ActivityFragmentActivity::class.java))
        }

        binding.tvTitle.bold()
    }
}
