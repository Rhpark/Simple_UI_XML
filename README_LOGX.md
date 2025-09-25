# Logx vs Android Log - 완벽 비교 가이드

> **“단 한 줄로 끝내는 고급 로깅.”** 기존 `Log` 대비 Logx가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

| 항목 | Android Log | Logx |
|:--|:--:|:--:|
| 출력 포맷 | `D/TAG: message` | `[앱] [패키지] [레벨] (파일:라인).메서드 - message` |
| 파일/라인 자동 표기 | ❌ | ✅ |
| 호출 메서드 자동 표기 | ❌ | ✅ |
| 스레드 ID 자동 표기 | △(수동 처리) | ✅ |
| JSON Pretty Print | △(직접 포맷) | ✅ |
| 파일 저장(로그 아카이브) | △(직접 구현) | ✅ |
| DSL 기반 구성/필터 | ❌ | ✅ |

> **핵심:** Logx는 “알고 싶은 메타정보”를 **자동**으로 붙여 줍니다. 디버깅 속도가 달라집니다.

<br>
</br>

## 💡 왜 중요한가:

- 문제 지점까지의 시간 단축: 파일·라인·메서드를 찾는 수고가 0.
- 재현성 향상: 스레드/컨텍스트 메타가 붙어 원인 파악이 빨라집니다.
- 읽을 수 있는 로그: JSON을 자동 정렬해 데이터 구조가 한눈에 보입니다.
- 운영 편의: 파일 저장/필터/레벨 제어로 개발↔운영 모두 유리합니다.

<br>
</br>

## Logx 출력 예시

**기본 로그 출력 형태:**
```
RhPark[]  kr.open.library.simpleui_xml  V  (LogxActivity.kt:56).demonstrateBasicLogging - VERBOSE LEVEL
RhPark[]  kr.open.library.simpleui_xml  D  (LogxActivity.kt:57).demonstrateBasicLogging - DEBUG LEVEL
RhPark[]  kr.open.library.simpleui_xml  I  (LogxActivity.kt:58).demonstrateBasicLogging - INFO LEVEL
RhPark[]  kr.open.library.simpleui_xml  W  (LogxActivity.kt:59).demonstrateBasicLogging - WARNING LEVEL
RhPark[]  kr.open.library.simpleui_xml  E  (LogxActivity.kt:60).demonstrateBasicLogging - ERROR LEVEL
```

<br>
</br>

**출력 구조 분석:**
```
[앱이름] [패키지명] [레벨] (파일명:라인번호).메서드명 - 메시지
```

<br>
</br>

**각 구성요소:**
- `RhPark[TAG]` - 앱 이름[TAG명] (DSL configure로 변경 가능)
- `kr.open.library.simpleui_xml` - 패키지명 자동 감지
- `V/D/I/W/E` - 로그 레벨 (Verbose/Debug/Info/Warning/Error)
- `(LogxActivity.kt:56)` - 파일명과 라인번호 **자동 추적(IDE에서 클릭 시 이동)**
- `.demonstrateBasicLogging` - 호출한 메서드명 **자동 추적**
- `VERBOSE LEVEL` - 실제 로그 메시지

<br>
</br>

**🎯 핵심 장점:**
- 기존 Android Log: `D/TAG: message`
- **Logx**: `(파일명:라인).메서드명 - message`

**디버깅이 혁신적으로 쉬워집니다!** 어느 파일의 몇 번째 줄, 어떤 메서드에서 호출했는지 한눈에 확인 가능!

<br>
</br>

## 핵심 차이점: 코드 길이 비교

**Logx 장점:**
- Parent Method 호출 추적 (스택 정보)
- Thread ID 자동 표시
- JSON 포맷팅 자동화
- 파일 저장 자동화
- DSL 기반 설정
- 고급 필터링
- TAG는 옵션

---

<br>
</br>

## 실제 코드 비교

<br>
</br>

### 첫째: 호출자 추적 비교

<details>
<summary><strong>기존 Android Log - Stack 추적</strong></summary>

```kotlin
// 호출자 추적 - 복잡한 구현
private fun trackMethodCalls() {
    val stackTrace = Thread.currentThread().stackTrace
    val currentMethod = stackTrace[2]
    val callerMethod = if (stackTrace.size > 3) stackTrace[3] else null

    val methodInfo = buildString {
        append("Current: ${currentMethod.className}.${currentMethod.methodName}")
        append(" (${currentMethod.fileName}:${currentMethod.lineNumber})")

        if (callerMethod != null) {
            append("\nCalled by: ${callerMethod.className}.${callerMethod.methodName}")
            append(" (${callerMethod.fileName}:${callerMethod.lineNumber})")
        }
    }

    Log.d("STACK_TRACE", methodInfo)
}

// 사용 예시
private fun parentMethod() {
    childMethod()
}

private fun childMethod() {
    trackMethodCalls() // 복잡한 추적 호출
    Log.d("NORMAL", "일반적인 로그 출력 완료")
}
```
**문제점:** 복잡한 StackTrace 파싱, 구현 어려움, 여러 라인 필요
</details>

<details>
<summary><strong>Simple UI Logx - Stack 추적</strong></summary>

```kotlin
// 호출자 추적 - 한 줄 완료
private fun parentMethod() {
    childMethod()
}

private fun childMethod() {
    Logx.p("Parent Method 추적: 어떤 함수에서 호출되었는지 확인") // 끝!
    Logx.d("일반 로그: 호출 위치가 표시되지 않음")
}
```
**결과:** 자동 호출자 추적, 파일명/라인번호, 클래스명 모두 자동!
</details>

<br>
</br>

### 둘째: JSON 포맷팅 비교

<details>
<summary><strong>기존 Android Log - JSON 포맷팅</strong></summary>

```kotlin
// JSON 포맷팅 - 복잡한 처리과정
private fun logJsonData() {
    val jsonData = """{"user":{"name":"홍길동","age":30},"timestamp":"${System.currentTimeMillis()}"}"""

    // 1. JSON 파싱 시도
    try {
        val jsonObject = JSONObject(jsonData)
        val prettyJson = jsonObject.toString(2) // 들여쓰기

        // 2. 여러 라인으로 나누어 출력 (라인 길이 제한)
        val lines = prettyJson.split("\n")
        for (line in lines) {
            Log.d("JSON_LOG", line)
        }
    } catch (e: Exception) {
        // 3. 파싱 실패시 원본 출력
        Log.d("JSON_LOG", "Raw JSON: $jsonData")
        Log.e("JSON_LOG", "JSON parsing failed", e)
    }
}
```
**문제점:** 복잡한 파싱, 예외처리, 여러 라인 분할, 오류 처리 필요
</details>

<details>
<summary><strong>Simple UI Logx - JSON 포맷팅</strong></summary>

```kotlin
// JSON 포맷팅 - 한 줄 완료
private fun logJsonData() {
    val jsonData = """{"user":{"name":"홍길동","age":30},"timestamp":"${System.currentTimeMillis()}"}"""

    Logx.j("JSON_DEMO", jsonData) // 끝!
}
```
**결과:** 자동 JSON 파싱, 예쁜 들여쓰기, 오류 처리 모두 자동!
</details>

<br>
</br>

### 셋째: Thread ID 추적

<details>
<summary><strong>기존 Android Log - Thread 추적</strong></summary>

```kotlin
// 현재 Thread 정보 수집
private fun trackThreads() {
    val currentThread = Thread.currentThread()
    val threadInfo = buildString {
        append("Thread Name: ${currentThread.name}")
        append(", ID: ${currentThread.id}")
        append(", Priority: ${currentThread.priority}")
        append(", State: ${currentThread.state}")
        append(", Group: ${currentThread.threadGroup?.name ?: "N/A"}")
    }

    Log.d("THREAD_INFO", threadInfo)
}

// 사용 예시
private fun demonstrateThreadTracking() {
    trackThreads() // Main Thread

    GlobalScope.launch(Dispatchers.IO) {
        trackThreads() // Background Thread
    }
}
```
**문제점:** 현재 Thread 정보 수집, 여러 속성, 복잡한 구현
</details>

<details>
<summary><strong>Simple UI Logx - Thread 추적</strong></summary>

```kotlin
// 자동 Thread 추적 - 한 줄 완료
private fun demonstrateThreadTracking() {
    Logx.t("Main Thread에서 실행") // Main Thread 정보 자동

    lifecycleScope.launch(Dispatchers.IO) {
        Logx.t("Background Thread에서 실행") // Background Thread 정보 자동
    }
}
```
**결과:** Thread 이름, ID, 우선순위 모두 자동!
</details>

<br>
</br>

### 넷째: 파일 저장 기능

<details>
<summary><strong>기존 Android Log - 파일 저장</strong></summary>

```kotlin
// 복잡한 파일 저장 로직
class LogFileManager(private val context: Context) {
    private var fileWriter: FileWriter? = null
    private var bufferedWriter: BufferedWriter? = null

    fun initFileLogging() {
        try {
            // 1. 저장 폴더 설정
            val logDir = File(context.getExternalFilesDir(null), "logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }

            // 2. 파일명 생성 (날짜별)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val logFile = File(logDir, "log_${dateFormat.format(Date())}.txt")

            // 3. FileWriter 초기화
            fileWriter = FileWriter(logFile, true)
            bufferedWriter = BufferedWriter(fileWriter)

        } catch (e: IOException) {
            Log.e("FILE_LOG", "Failed to initialize file logging", e)
        }
    }

    fun writeToFile(tag: String, message: String, level: String) {
        try {
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] $level/$tag: $message\n"

            bufferedWriter?.write(logEntry)
            bufferedWriter?.flush()

        } catch (e: IOException) {
            Log.e("FILE_LOG", "Failed to write to file", e)
        }
    }

    fun closeFileLogging() {
        try {
            bufferedWriter?.close()
            fileWriter?.close()
        } catch (e: IOException) {
            Log.e("FILE_LOG", "Failed to close file", e)
        }
    }
}

// 사용법
private val logFileManager = LogFileManager(this)

private fun setupFileLogging() {
    logFileManager.initFileLogging()
}

private fun logWithFile(tag: String, message: String) {
    Log.d(tag, message)
    logFileManager.writeToFile(tag, message, "D")
}
```
**문제점:** 50+ 라인의 복잡한 로직, 예외 처리, 파일관리, 리소스 해제
</details>

<details>
<summary><strong>Simple UI Logx - 파일 저장</strong></summary>

```kotlin
// 자동 파일 저장 - 설정 한 줄
private fun setupFileLogging() {
    Logx.setSaveToFile(true) // 끝!
}

private fun logWithFile(tag: String, message: String) {
    Logx.d(tag, message) // 동시에 Logcat + 파일에 자동 저장!
}
```
**결과:** 파일 생성, 저장 경로, 타임스탬프, 저장 관리 모두 자동!
</details>

<br>
</br>

### 다섯째: 설정 관리 기능

<details>
<summary><strong>기존 Android Log - 설정 관리</strong></summary>

```kotlin
// 복잡한 설정 관리
class LogConfig {
    companion object {
        private var isDebugMode = true
        private var saveToFile = false
        private var logLevel = Log.DEBUG
        private val allowedTags = mutableSetOf<String>()
        private var logDirectory = ""

        fun setDebugMode(debug: Boolean) {
            isDebugMode = debug
        }

        fun enableFileLogging(enable: Boolean, directory: String = "") {
            saveToFile = enable
            if (directory.isNotEmpty()) {
                logDirectory = directory
            }
        }

        fun setLogLevel(level: Int) {
            logLevel = level
        }

        fun setAllowedTags(tags: List<String>) {
            allowedTags.clear()
            allowedTags.addAll(tags)
        }

        fun isLogAllowed(tag: String, level: Int): Boolean {
            if (!isDebugMode) return false
            if (level < logLevel) return false
            if (allowedTags.isNotEmpty() && !allowedTags.contains(tag)) return false
            return true
        }
    }
}

// 설정 적용
private fun setupLogging() {
    LogConfig.setDebugMode(true)
    LogConfig.enableFileLogging(true, "/storage/logs")
    LogConfig.setLogLevel(Log.INFO)
    LogConfig.setAllowedTags(listOf("IMPORTANT", "ERROR"))
}
```
**문제점:** 복잡한 설정, 여러개 포함 메서드, 상태 관리 어려움
</details>

<details>
<summary><strong>Simple UI Logx - DSL 설정</strong></summary>

```kotlin
// DSL로 간편한 설정 - 한 번에 블록!
private fun setupLogging() {
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
            addAll("IMPORTANT", "ERROR")
        }
    }
}
```
**결과:** 직관적인 설정, 타입 안전성, 가독성 좋은 DSL, 한번에 처리!
</details>

<br>
</br>

---

## Simple UI Logx의 핵심 장점

<br>
</br>

### 1. **압도적인 생산성 향상**
- **JSON 포맷팅**: 복잡한 파싱 로직 → `Logx.j()` 한 줄
- **Stack 추적**: 현재 StackTrace → `Logx.p()` 한 줄
- **Thread 추적**: 현재 Thread 정보 → `Logx.t()` 한 줄

<br>
</br>

### 2. **완전 자동화된 파일 저장 기능**
- **자동 저장 경로**: Internal/External/Public 저장소 중 선택
- **저장 관리 자동**: 저장소별 저장 라이프사이클 자동 관리
- **리소스 해제**: Android Lifecycle과 연동 자동화

<br>
</br>

### 3. **고급 DSL 설정 기능**
- **DSL 기반**: Kotlin DSL로 가독성 좋은 설정
- **런타임 변경**: Runtime 설정 변경 가능
- **타입 안전성**: 컴파일 타임에 설정 검증

<br>
</br>

### 4. **개발자 친화적 도구**
- **추적 도구**: 호출 경로, 실행 스레드 도구
- **구조적 출력**: JSON 데이터의 체계적 출력
- **코드 간소화**: 유지보수 편리한 구조

---

<br>
</br>

## 개발자들의 후기

> **"JSON 로깅이 이렇게 간단할 줄 몰랐어!"**
>
> **"Parent Method 추적으로 복잡한 호출 관계도 한 눈에 파악!"**
>
> **"파일 저장 설정 한 줄로 모든 로그 자동 백업!"**
>
> **"DSL 설정으로 팀 전체 로깅 규칙을 통일했어!"**

---

<br>
</br>

## 결론: 로깅의 새로운 표준

**Simple UI Logx**는 기존의 로깅 방식을 완전히 바꿉니다.
**복잡한 로깅 코드를 95% 단축**시키고, **개발 생산성을 크게 향상**시키며 **직관적인 로깅 경험**을 제공합니다.

**JSON 파싱, Stack 추적, Thread 정보, 파일 저장**
모든 고급기능이 **Logx**로, 간단하고 **강력하게**.

지금 바로 시작하세요! ✨

---

<br>
</br>

## 실제 구현 예제보기

**라이브 예제 코드:**
> - Logx 예제: `app/src/main/java/kr/open/library/simpleui_xml/logx/LogxActivity`
> - 실제로 앱을 구동 시켜서 실제 구현 예제를 확인해 보세요!

<br>
</br>

**테스트 가능한 기능:**
- 기본 로깅 vs 고급 로깅
- 실시간 JSON 데이터 처리
- 실시간 Parent Method 호출 추적
- 실시간 Thread ID 및 스레드 추적
- 실시간 로그 파일 저장
- 실시간 저장소 및 경로 변경
- 고급 DSL 기반 설정
- 로그 필터링 및 레벨 도구