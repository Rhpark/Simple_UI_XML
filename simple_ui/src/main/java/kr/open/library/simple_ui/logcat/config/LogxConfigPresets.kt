package kr.open.library.simple_ui.logcat.config

import kr.open.library.simple_ui.logcat.model.LogxType


/**
 * 사전 정의된 Logx 설정 모음
 */
object LogxConfigPresets {

    /**
     * 개발용 설정 (모든 로그 활성화)
     */
    fun development(appName: String = "RhPark"): LogxConfig = logxConfig {
        debugMode = true
        debugFilter = false
        this.appName = appName
        logTypes { all() }
    }

    /**
     * 프로덕션용 설정 (에러와 경고만)
     */
    fun production(appName: String = "RhPark"): LogxConfig = logxConfig {
        debugMode = true
        debugFilter = true
        this.appName = appName
        logTypes {
            +LogxType.WARN
            +LogxType.ERROR
        }
    }

    /**
     * 파일 저장용 설정
     */
    fun fileLogging(
        appName: String = "RhPark",
        filePath: String = LogxPathUtils.getDefaultLogPath()
    ): LogxConfig = logxConfig {
        debugMode = true
        this.appName = appName
        fileConfig {
            saveToFile = true
            this.filePath = filePath
        }
        logTypes { all() }
    }

    /**
     * 네트워크 디버깅용 설정
     */
    fun networkDebugging(appName: String = "RhPark"): LogxConfig = logxConfig {
        debugMode = true
        debugFilter = true
        this.appName = appName
        filters {
            +"Network"
            +"API"
            +"HTTP"
        }
        logTypes { all() }
    }
}