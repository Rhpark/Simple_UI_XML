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

    override fun onEventVmCollect(binding: ActivityLogxBinding) {
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
     * 湲곕낯 濡쒓퉭 湲곕뒫 ?쒖뿰
     */
    private fun demonstrateBasicLogging() {
        Logx.v("VERBOSE LEVEL")
        "DEBUG LEVEL".logxD()
        Logx.i("INFO LEVEL")
        Logx.w("WARNING LEVEL")
        Logx.e("ERROR LEVEL")
    }

    /**
     * JSON ?щ㎎??湲곕뒫 ?쒖뿰
     */
    private fun demonstrateJsonLogging() {
        val jsonData =
            """
            {"user": {"name": "?띻만??,"age": 30,"skills": ["Kotlin", "Android", "Java"]},"timestamp": "${System.currentTimeMillis()}"}
            """.trimIndent()

        Logx.j("JSON_DEMO", jsonData)
        Logx.d("NORMAL LOG: $jsonData")
    }

    /**
     * Parent Method 異붿쟻 湲곕뒫 ?쒖뿰
     */
    private fun parentMethodExample() {
        nestedMethodLevel1()
    }

    private fun nestedMethodLevel1() {
        Logx.p("Parent Method 異붿쟻: ?대뼡 ?⑥닔?먯꽌 ?몄텧?섏뿀?붿? ?뺤씤")
        Logx.d("?쇰컲 濡쒓렇: ?몄텧 ?꾩튂媛 ?쒖떆?섏? ?딆쓬")
    }

    /**
     * Thread ID 異붿쟻 湲곕뒫 ?쒖뿰
     */
    private fun demonstrateThreadTracking() {
        // Main Thread
        Logx.t("Main Thread?먯꽌??濡쒓퉭")

        // Background Thread
        lifecycleScope.launch(Dispatchers.IO) { Logx.t("Background Thread?먯꽌??濡쒓퉭") }
    }

    /**
     * ?뚯씪 ???湲곕뒫 ?쒖뿰
     */
    private fun demonstrateFileLogging() {
        Logx.setSaveToFile(true)

        Logx.d("FILE_SAVE", "??濡쒓렇???뚯씪?먮룄 ??λ맗?덈떎")
        Logx.i("FILE_SAVE", "?뚯씪 寃쎈줈: ${Logx.getFilePath()}")
        Logx.w("FILE_SAVE", "??μ냼 ?뺣낫: ${Logx.getStorageInfo()}")

        val storageInfo = Logx.getStorageInfo()
        val currentPath = storageInfo[LogxStorageType.APP_EXTERNAL] ?: "Unknown"
        Logx.i("FILE_SAVE", "?뚯씪 ????꾨즺!\n寃쎈줈: $currentPath")
    }

    /**
     * ??μ냼 ???蹂寃??쒖뿰
     */
    private fun demonstrateStorageConfig() {
        // ?대? ??μ냼濡?蹂寃?
        Logx.setInternalStorage()
        Logx.d("STORAGE", "?대? ??μ냼濡?蹂寃쎈맖")

        // ???꾩슜 ?몃? ??μ냼濡?蹂寃?
        Logx.setAppExternalStorage()
        Logx.d("STORAGE", "???꾩슜 ?몃? ??μ냼濡?蹂寃쎈맖")

        val storageInfo = Logx.getStorageInfo()
        Logx.i("STORAGE", "?ъ슜 媛?ν븳 ??μ냼?? $storageInfo")
    }

    /**
     * DSL 怨좉툒 ?ㅼ젙 ?쒖뿰
     */
    private fun demonstrateAdvancedConfig() {
        // 癒쇱? ??μ냼 ????ㅼ젙
        Logx.setAppExternalStorage()

        // DSL???ъ슜??怨좉툒 ?ㅼ젙
        Logx.configure {
            appName = "RhParkLogx"
            debugMode = true
            debugFilter = false

            fileConfig {
                saveToFile = true
                filePath = Logx.getFilePath()
            }

            logTypes {
                all() // 紐⑤뱺 濡쒓렇 ????덉슜
            }

            filters {
                addAll("DSL_CONFIG", "DEMO")
            }
        }

        Logx.d("DSL_CONFIG", "DSL濡??ㅼ젙 ?꾨즺!")
        Logx.i("DSL_CONFIG", "???대쫫: ${Logx.getAppName()}")
        Logx.i("DSL_CONFIG", "?붾쾭洹?紐⑤뱶: ${Logx.getDebugMode()}")
        Logx.p("DSL_CONFIG", "Parent 異붿쟻???쒖꽦?붾맖")
    }

    /**
     * 濡쒓렇 ?꾪꽣留?湲곕뒫 ?쒖뿰
     */
    private fun demonstrateLogFiltering() {
        // DSL濡??꾪꽣留??ㅼ젙
        Logx.configure {
            debugMode = true
            debugFilter = true

            logTypes {
                +LogxType.ERROR
                +LogxType.WARN
                +LogxType.INFO
                // VERBOSE? DEBUG???쒖쇅
            }

            filters {
                addAll("ALLOWED_TAG", "IMPORTANT")
            }
        }

        // ?꾪꽣留??뚯뒪??
        Logx.v("FILTER_TEST", "??VERBOSE 濡쒓렇???쒖떆?섏? ?딆뒿?덈떎")
        Logx.d("FILTER_TEST", "??DEBUG 濡쒓렇???쒖떆?섏? ?딆뒿?덈떎")
        Logx.i("FILTER_TEST", "??INFO 濡쒓렇???쒖떆?⑸땲??)
        Logx.w("FILTER_TEST", "??WARNING 濡쒓렇???쒖떆?⑸땲??)
        Logx.e("FILTER_TEST", "??ERROR 濡쒓렇???쒖떆?⑸땲??)

        // ?쒓렇 ?꾪꽣留??뚯뒪??
        Logx.i("ALLOWED_TAG", "??濡쒓렇???덉슜???쒓렇?낅땲??)
        Logx.i("BLOCKED_TAG", "??濡쒓렇??李⑤떒???쒓렇?낅땲??)

        // ?꾪꽣 珥덇린??
        lifecycleScope.launch {
            kotlinx.coroutines.delay(3000)
            Logx.configure {
                debugFilter = false
                logTypes {
                    all() // 紐⑤뱺 ????덉슜
                }
                filters {
                    clear() // ?꾪꽣 ?쒓굅
                }
            }
            Logx.i("?꾪꽣留??댁젣??)
        }
        Logx.i("濡쒓렇 ?꾪꽣留??쒖뿰 ?꾨즺!\n3珥????꾪꽣 ?댁젣")
    }
}
