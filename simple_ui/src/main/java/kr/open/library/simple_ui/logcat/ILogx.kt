package kr.open.library.simple_ui.logcat

import android.content.Context
import kr.open.library.simple_ui.logcat.model.LogxType

import java.util.EnumSet

/**
 * Logx 라이브러리의 핵심 인터페이스
 * 테스트 가능성과 확장성을 위한 추상화 레이어
 */
interface ILogx {


    fun init(context: Context)

    // 기본 로깅 메서드
    fun v()
    fun v(msg: Any? = "")
    fun v(tag: String, msg: Any?)

    fun d()
    fun d(msg: Any? = "")
    fun d(tag: String, msg: Any?)

    fun i()
    fun i(msg: Any? = "")
    fun i(tag: String, msg: Any?)

    fun w()
    fun w(msg: Any? = "")
    fun w(tag: String, msg: Any?)

    fun e()
    fun e(msg: Any? = "")
    fun e(tag: String, msg: Any?)

    // 확장 기능
    fun p()
    fun p(msg: Any? = "")
    fun p(tag: String, msg: Any?)

    fun t()
    fun t(msg: Any? = "")
    fun t(tag: String, msg: Any?)

    fun j(msg: String)
    fun j(tag: String, msg: String)

    // 설정 메서드
    fun setDebugMode(isDebug: Boolean)
    fun setDebugFilter(isFilter: Boolean)
    fun setSaveToFile(isSave: Boolean)
    fun setFilePath(path: String)
    fun setAppName(name: String)
//    fun setDebugLogTypeList(types: List<LogxType>)
    fun setDebugLogTypeList(types: EnumSet<LogxType>)
    fun setDebugFilterList(tags: List<String>)
}