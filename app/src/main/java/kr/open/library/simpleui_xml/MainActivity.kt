package kr.open.library.simpleui_xml

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.open.library.easy_extensions.conditional.checkSdkVersion
import kr.open.library.logcat.Logx
import kr.open.library.simple_ui.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.databinding.ActivityMainBinding

class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Logx.d()
    }

    override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
        super.onCreateView(rootView, savedInstanceState)
        setupWindowInsets()
        setupPermissionButtons()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupPermissionButtons() {
        binding.btnCameraPermission.setOnClickListener {
            requestCameraPermission()
        }

        binding.btnLocationPermission.setOnClickListener {
            requestLocationPermission()
        }

        binding.btnStoragePermission.setOnClickListener {
            requestStoragePermission()
        }

        binding.btnMultiplePermissions.setOnClickListener {
            requestMultiplePermissions()
        }

        // API 33+ 알림 권한 테스트를 위한 추가 버튼 (있다면)
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
            // 알림 권한 요청 기능 추가 가능
        }
    }

    private fun requestCameraPermission() {
        updatePermissionStatus("카메라 권한을 요청합니다...")

        checkSdkVersion(Build.VERSION_CODES.M,
            positiveWork = {
                requestPermissions(listOf(Manifest.permission.CAMERA)) { deniedPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        updatePermissionStatus("✅ 카메라 권한이 허용되었습니다")
                    } else {
                        updatePermissionStatus("❌ 카메라 권한이 거부되었습니다")
                    }
                }
            },
            negativeWork = {
                updatePermissionStatus("✅ 카메라 권한이 허용되었습니다 (API 23 미만)")
            }
        )
    }

    private fun requestLocationPermission() {
        updatePermissionStatus("위치 권한을 요청합니다...")

        val locationPermissions = mutableListOf<String>().apply {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)

            checkSdkVersion(Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        requestPermissions(locationPermissions) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                val backgroundNote = checkSdkVersion(Build.VERSION_CODES.Q,
                    positiveWork = { " (백그라운드 포함)" },
                    negativeWork = { "" }
                )
                updatePermissionStatus("✅ 위치 권한이 허용되었습니다$backgroundNote")
            } else {
                updatePermissionStatus("❌ 위치 권한이 거부되었습니다\n거부된 권한: ${deniedPermissions.joinToString(", ")}")
            }
        }
    }

    private fun requestStoragePermission() {
        updatePermissionStatus("저장소 권한을 요청합니다...")

        val storagePermissions = checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE) },
            negativeWork = {
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        )

        requestPermissions(storagePermissions) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                val apiNote = checkSdkVersion(Build.VERSION_CODES.R,
                    positiveWork = { " (MANAGE_EXTERNAL_STORAGE)" },
                    negativeWork = { " (READ/WRITE)" }
                )
                updatePermissionStatus("✅ 저장소 권한이 허용되었습니다$apiNote")
            } else {
                updatePermissionStatus("❌ 저장소 권한이 거부되었습니다\n거부된 권한: ${deniedPermissions.joinToString(", ")}")
            }
        }
    }

    private fun requestMultiplePermissions() {
        updatePermissionStatus("여러 권한을 동시에 요청합니다...")

        val allPermissions = mutableListOf<String>().apply {
            // 카메라 권한 (API 23+)
            checkSdkVersion(Build.VERSION_CODES.M) {
                add(Manifest.permission.CAMERA)
            }

            // 위치 권한
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            checkSdkVersion(Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            // 저장소 권한 (SDK별 분기)
            addAll(checkSdkVersion(Build.VERSION_CODES.R,
                positiveWork = { listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE) },
                negativeWork = {
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            ))

            // 알림 권한 (API 33+)
            checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        requestPermissions(allPermissions) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                updatePermissionStatus("✅ 모든 권한이 허용되었습니다 (SDK ${Build.VERSION.SDK_INT} 기준)")
            } else {
                updatePermissionStatus("⚠️ 일부 권한이 거부되었습니다\n거부된 권한: ${deniedPermissions.joinToString(", ")}")
            }
        }
    }

    private fun updatePermissionStatus(message: String) {
        binding.tvPermissionStatus.text = message
    }
}