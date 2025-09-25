package kr.open.library.simpleui_xml

import android.app.Application
import kr.open.library.simple_ui.logcat.Logx

class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Logx.init(this)
    }
}