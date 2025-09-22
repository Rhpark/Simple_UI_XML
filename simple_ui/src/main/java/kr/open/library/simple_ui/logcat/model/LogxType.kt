package kr.open.library.simple_ui.logcat.model

public enum class LogxType(public val logTypeString: String) {
    VERBOSE("V"),  //Log.v
    DEBUG("D"),  //Log.d
    INFO("I"),  //Log.i
    WARN("W"),  //Log.w
    ERROR("E"),  //Log.e
    PARENT("P"),  //Log.i + Parent method call name check
    JSON("J"),  //Log.v + JSON code parsing
    THREAD_ID("T") //Log d + Current Thread Id
}