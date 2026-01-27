package kr.open.library.simpleui_xml.logx

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.logcat.config.StorageType
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityLogxBinding

class LogxActivity :
    BaseDataBindingActivity<ActivityLogxBinding>(R.layout.activity_logx) {
    private val vm: LogxActivityVm by viewModels()

    override fun onCreate(binding: ActivityLogxBinding, savedInstanceState: Bundle?) {
        super.onCreate(binding, savedInstanceState)
        binding.vm = vm
        initLogxForFileLogging()
        updateStatus("준비 완료. 버튼을 눌러 Logcat/파일을 확인하세요.")
    }

    override fun onEventVmCollect(binding: ActivityLogxBinding) {
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    LogxActivityVmEvent.OnClickBasicLogging -> demonstrateBasicLogging()
                    LogxActivityVmEvent.OnClickJsonLogging -> demonstrateJsonLogging()
                    LogxActivityVmEvent.OnClickParentLogging -> demonstrateParentLogging()
                    LogxActivityVmEvent.OnClickThreadLogging -> demonstrateThreadLogging()
                    LogxActivityVmEvent.OnClickFileLogging -> demonstrateFileLogging()
                    LogxActivityVmEvent.OnClickStorageConfig -> demonstrateStorageConfig()
                    LogxActivityVmEvent.OnClickTagBlockList -> demonstrateTagBlockList()
                    LogxActivityVmEvent.OnClickSkipPackages -> demonstrateSkipPackages()
                    LogxActivityVmEvent.OnClickSaveDirectory -> demonstrateSaveDirectory()
                }
            }
        }
    }

    private fun initLogxForFileLogging() {
        Logx.initialize(applicationContext)
        Logx.setAppName("TempLogcatDemo")
    }

    private fun demonstrateBasicLogging() {
        Logx.v("BASIC", "VERBOSE 로그")
        Logx.d("BASIC", "DEBUG 로그")
        Logx.i("BASIC", "INFO 로그")
        Logx.w("BASIC", "WARN 로그")
        Logx.e("BASIC", "ERROR 로그")
        updateStatus("기본 로그 출력 완료")
    }

    private fun demonstrateJsonLogging() {
        val json =
            """
            {
                "name": "Lee",
                "items": [1, 2, 3]
            }
            """.trimIndent()

        Logx.j("JSON", json)
        updateStatus("JSON 로그 출력 완료")
    }

    private fun demonstrateParentLogging() {
        parentLevel1()
        updateStatus("PARENT 로그 출력 완료")
    }

    private fun parentLevel1() {
        parentLevel2()
    }

    private fun parentLevel2() {
        Logx.p("PARENT", "호출 위치 확인용 로그")
    }

    private fun demonstrateThreadLogging() {
        Logx.t("THREAD", "Main Thread 로그")
        lifecycleScope.launch(Dispatchers.IO) {
            Logx.t("THREAD", "IO Thread 로그")
        }
        updateStatus("THREAD 로그 출력 완료")
    }

    private fun demonstrateFileLogging() {
        Logx.setSaveEnabled(true)
        Logx.d("FILE", "파일 저장 활성화")
        val saveDirectory = Logx.getSaveDirectory() ?: "기본 경로"
        updateStatus("파일 저장 ON (storage=${Logx.getStorageType()}, dir=$saveDirectory)")
    }

    private fun demonstrateStorageConfig() {
        Logx.setStorageType(StorageType.INTERNAL)
        Logx.d("STORAGE", "INTERNAL 설정")
        Logx.setStorageType(StorageType.APP_EXTERNAL)
        Logx.d("STORAGE", "APP_EXTERNAL 설정")

        try {
            Logx.setStorageType(StorageType.PUBLIC_EXTERNAL)
            Logx.d("STORAGE", "PUBLIC_EXTERNAL 설정")
            updateStatus("PUBLIC_EXTERNAL 설정 완료")
        } catch (e: Exception) {
            updateStatus("PUBLIC_EXTERNAL 설정 실패: ${e.message}")
        }
    }

    private fun demonstrateTagBlockList() {
        Logx.setLogTagBlockListEnabled(true)
        Logx.setLogTagBlockList(setOf("BLOCKED_TAG"))
        Logx.d("ALLOWED_TAG", "출력되는 태그")
        Logx.d("BLOCKED_TAG", "차단되는 태그")
        updateStatus("태그 블록리스트 적용 완료 (BLOCKED_TAG 차단)")
    }

    private fun demonstrateSkipPackages() {
        TempLogcatStackHelper.logFromHelper("SKIP_BEFORE")
        Logx.addSkipPackages(setOf(TempLogcatStackHelper::class.java.name))
        TempLogcatStackHelper.logFromHelper("SKIP_AFTER")
        updateStatus("skipPackages 추가 전/후 로그 위치를 비교하세요")
    }

    private fun demonstrateSaveDirectory() {
        val baseDir = getExternalFilesDir(null) ?: filesDir
        val customDir = File(baseDir, "logx_custom").absolutePath
        Logx.setSaveDirectory(customDir)
        Logx.setSaveEnabled(true)
        Logx.d("FILE", "저장 경로 설정: $customDir")
        updateStatus("저장 경로 설정 완료: $customDir")
    }

    private fun updateStatus(message: String) {
        getBinding().tvLastAction.text = message
    }

    private object TempLogcatStackHelper {
        fun logFromHelper(label: String) {
            Logx.d("SKIP", "헬퍼 호출 ($label)")
        }
    }
}
