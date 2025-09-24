package kr.open.library.simple_ui.system_manager.info.network.connectivity.data

public open class NetworkBase(res: Any) {

    private val resStr = res.toString()
    protected fun splitStr(start: String, end: String, splitPoint: String): List<String>? =
        resStr.split(start, end)?.split(splitPoint)

    protected fun splitStr(start: String, end: String): String? = resStr.split(start,end)

    protected fun String.split(start: String, end: String): String? = if (contains(start)) {
        val splitRes = split(start)
        if(splitRes.size > 1) {
            splitRes[1].split(end)[0]
        } else null
    } else {
        null
    }

    protected fun isContains(str: String): Boolean = resStr.contains(str)

    protected fun getResStr(): String = resStr
}