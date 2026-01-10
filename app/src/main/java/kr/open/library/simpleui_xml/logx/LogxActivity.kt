package kr.open.library.simpleui_xml.logx

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.logcat.config.LogxStorageType
import kr.open.library.simple_ui.core.logcat.extensions.logxD
import kr.open.library.simple_ui.core.logcat.model.LogxType
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityLogxBinding

class LogxActivity : BaseDataBindingActivity<ActivityLogxBinding>(R.layout.activity_logx) {
    private val vm: LogxActivityVm by viewModels()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        Logx.setSaveToFile(true)
        getBinding().vm = vm
        lifecycle.addObserver(vm)
    }

    override fun onEventVmCollect() {
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is LogxActivityVmEvent.OnClickBasicLogging -> demonstrateBasicLogging()
                    is LogxActivityVmEvent.OnClickJsonLogging -> demonstrateJsonLogging()
                    is LogxActivityVmEvent.OnClickParentTracking -> parentMethodExample()
                    is LogxActivityVmEvent.OnClickThreadTracking -> demonstrateThreadTracking()
                    is LogxActivityVmEvent.OnClickFileLogging -> demonstrateFileLogging()
                    is LogxActivityVmEvent.OnClickStorageConfig -> demonstrateStorageConfig()
                    is LogxActivityVmEvent.OnClickAdvancedConfig -> demonstrateAdvancedConfig()
                    is LogxActivityVmEvent.OnClickLogFiltering -> demonstrateLogFiltering()
                }
            }
        }
    }

    /**
     * 기본 로깅 기능 시연
     */
    private fun demonstrateBasicLogging() {
        Logx.v("VERBOSE LEVEL")
        "DEBUG LEVEL".logxD()
        Logx.i("INFO LEVEL")
        Logx.w("WARNING LEVEL")
        Logx.e("ERROR LEVEL")
    }

    /**
     * JSON 포맷팅 기능 시연
     */
    private fun demonstrateJsonLogging() {
        val jsonData =
            """
            {"user": {"name": "홍길동","age": 30,"skills": ["Kotlin", "Android", "Java"]},"timestamp": "${System.currentTimeMillis()}"}
            """.trimIndent()

        Logx.j("JSON_DEMO", jsonData)
        Logx.d("NORMAL LOG: $jsonData")
    }

    /**
     * Parent Method 추적 기능 시연
     */
    private fun parentMethodExample() {
        nestedMethodLevel1()
    }

    private fun nestedMethodLevel1() {
        Logx.p("Parent Method 추적: 어떤 함수에서 호출되었는지 확인")
        Logx.d("일반 로그: 호출 위치가 표시되지 않음")
    }

    /**
     * Thread ID 추적 기능 시연
     */
    private fun demonstrateThreadTracking() {
        // Main Thread
        Logx.t("Main Thread에서의 로깅")

        // Background Thread
        lifecycleScope.launch(Dispatchers.IO) { Logx.t("Background Thread에서의 로깅") }
    }

    /**
     * 파일 저장 기능 시연
     */
    private fun demonstrateFileLogging() {
        Logx.setSaveToFile(true)

        Logx.d("FILE_SAVE", "이 로그는 파일에도 저장됩니다")
        Logx.i("FILE_SAVE", "파일 경로: ${Logx.getFilePath()}")
        Logx.w("FILE_SAVE", "저장소 정보: ${Logx.getStorageInfo()}")

        val storageInfo = Logx.getStorageInfo()
        val currentPath = storageInfo[LogxStorageType.APP_EXTERNAL] ?: "Unknown"
        Logx.i("FILE_SAVE", "파일 저장 완료!\n경로: $currentPath")
    }

    /**
     * 저장소 타입 변경 시연
     */
    private fun demonstrateStorageConfig() {
        // 내부 저장소로 변경
        Logx.setInternalStorage()
        Logx.d("STORAGE", "내부 저장소로 변경됨")

        // 앱 전용 외부 저장소로 변경
        Logx.setAppExternalStorage()
        Logx.d("STORAGE", "앱 전용 외부 저장소로 변경됨")

        val storageInfo = Logx.getStorageInfo()
        Logx.i("STORAGE", "사용 가능한 저장소들: $storageInfo")
    }

    /**
     * DSL 고급 설정 시연
     */
    private fun demonstrateAdvancedConfig() {
        // 먼저 저장소 타입 설정
        Logx.setAppExternalStorage()

        // DSL을 사용한 고급 설정
        Logx.configure {
            appName = "RhParkLogx"
            debugMode = true
            debugFilter = false

            fileConfig {
                saveToFile = true
                filePath = Logx.getFilePath()
            }

            logTypes {
                all() // 모든 로그 타입 허용
            }

            filters {
                addAll("DSL_CONFIG", "DEMO")
            }
        }

        Logx.d("DSL_CONFIG", "DSL로 설정 완료!")
        Logx.i("DSL_CONFIG", "앱 이름: ${Logx.getAppName()}")
        Logx.i("DSL_CONFIG", "디버그 모드: ${Logx.getDebugMode()}")
        Logx.p("DSL_CONFIG", "Parent 추적도 활성화됨")
    }

    /**
     * 로그 필터링 기능 시연
     */
    private fun demonstrateLogFiltering() {
        // DSL로 필터링 설정
        Logx.configure {
            debugMode = true
            debugFilter = true

            logTypes {
                +LogxType.ERROR
                +LogxType.WARN
                +LogxType.INFO
                // VERBOSE와 DEBUG는 제외
            }

            filters {
                addAll("ALLOWED_TAG", "IMPORTANT")
            }
        }

        // 필터링 테스트
        Logx.v("FILTER_TEST", "이 VERBOSE 로그는 표시되지 않습니다")
        Logx.d("FILTER_TEST", "이 DEBUG 로그는 표시되지 않습니다")
        Logx.i("FILTER_TEST", "이 INFO 로그는 표시됩니다")
        Logx.w("FILTER_TEST", "이 WARNING 로그는 표시됩니다")
        Logx.e("FILTER_TEST", "이 ERROR 로그는 표시됩니다")

        // 태그 필터링 테스트
        Logx.i("ALLOWED_TAG", "이 로그는 허용된 태그입니다")
        Logx.i("BLOCKED_TAG", "이 로그는 차단된 태그입니다")

        // 필터 초기화
        lifecycleScope.launch {
            kotlinx.coroutines.delay(3000)
            Logx.configure {
                debugFilter = false
                logTypes {
                    all() // 모든 타입 허용
                }
                filters {
                    clear() // 필터 제거
                }
            }
            Logx.i("필터링 해제됨")
        }
        Logx.i("로그 필터링 시연 완료!\n3초 후 필터 해제")
    }
}
