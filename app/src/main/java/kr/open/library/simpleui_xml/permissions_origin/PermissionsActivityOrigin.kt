package kr.open.library.simpleui_xml.permissions_origin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityPermissionsOriginBinding

class PermissionsActivityOrigin : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionsOriginBinding
    private val viewModel: PermissionsViewModelOrigin by viewModels()
    private lateinit var adapter: PermissionResultAdapter

    // Permission Launchers
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> handlePermissionResults(permissions) }

    // Overlay Permission Launcher (SYSTEM_ALERT_WINDOW)
    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { handleOverlayPermissionResult() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataBinding 설정
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permissions_origin)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PermissionResultAdapter()
        binding.rcvPermissionResults.apply {
            adapter = this@PermissionsActivityOrigin.adapter
            layoutManager = LinearLayoutManager(this@PermissionsActivityOrigin)
        }
    }

    private fun observeViewModel() {
        // Event 처리
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is PermissionEvent.OnClickCameraPermission -> {
                        requestPermissions(listOf(Manifest.permission.CAMERA))
                    }
                    is PermissionEvent.OnClickLocationPermission -> {
                        requestPermissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                    is PermissionEvent.OnClickMultiplePermissions -> {
                        requestPermissions(
                            listOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.SYSTEM_ALERT_WINDOW
                            )
                        )
                    }
                }
            }
        }

        // Permission Results 관찰
        lifecycleScope.launch { viewModel.permissionResults.collect { results -> adapter.submitList(results) } }
    }

    private fun requestPermissions(permissions: List<String>) {
        // 일반 권한과 특수 권한 분리
        val normalPermissions = permissions.filter { it != Manifest.permission.SYSTEM_ALERT_WINDOW }
        val hasOverlayPermission = permissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)

        // 일반 권한 처리
        val normalPermissionsToRequest = normalPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        // 오버레이 권한 상태 확인
        val overlayPermissionGranted = Settings.canDrawOverlays(this)

        // 요청할 권한들 상태 체크
        val allNormalGranted = normalPermissionsToRequest.isEmpty()
        val allOverlayGranted = !hasOverlayPermission || overlayPermissionGranted

        if (allNormalGranted && allOverlayGranted) {
            val message = "All permissions already granted: ${permissions.joinToString(", ")}"
            viewModel.addPermissionResult(message)
            Snackbar.make(binding.root, "All permissions already granted", Snackbar.LENGTH_SHORT).show()
            return
        }

        // 일반 권한 요청
        if (normalPermissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(normalPermissionsToRequest.toTypedArray())
        }

        // 오버레이 권한 요청
        if (hasOverlayPermission && !overlayPermissionGranted) {
            requestOverlayPermission()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        requestOverlayPermissionLauncher.launch(intent)
    }

    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val grantedPermissions = permissions.filterValues { it }.keys
        val deniedPermissions = permissions.filterValues { !it }.keys

        val message = buildString {
            append("Requested: ${permissions.keys.joinToString(", ")}\n")
            if (grantedPermissions.isNotEmpty()) {
                append("✅ Granted: ${grantedPermissions.joinToString(", ")}\n")
            }
            if (deniedPermissions.isNotEmpty()) {
                append("❌ Denied: ${deniedPermissions.joinToString(", ")}")
            }
        }

        viewModel.addPermissionResult(message)

        val snackbarMessage = if (deniedPermissions.isEmpty()) { "All permissions granted!" }
        else { "Some permissions denied" }
        Snackbar.make(binding.root, snackbarMessage, Snackbar.LENGTH_SHORT)
            .setAction("OK") { /* 확인 */ }
            .show()
    }

    private fun handleOverlayPermissionResult() {
        val isGranted = Settings.canDrawOverlays(this)
        val message = buildString {
            append("Special Permission Request Result:\n")
            if (isGranted) { append("✅ SYSTEM_ALERT_WINDOW: Granted") }
            else { append("❌ SYSTEM_ALERT_WINDOW: Denied") }
        }

        viewModel.addPermissionResult(message)

        val snackbarMessage = if (isGranted) { "Overlay permission granted!" }
                            else { "Overlay permission denied" }
        Snackbar.make(binding.root, snackbarMessage, Snackbar.LENGTH_SHORT).setAction("OK") { /* 확인 */ }.show()
    }
}