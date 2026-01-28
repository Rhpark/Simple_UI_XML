# Logx SPEC

## 문서 정보
- 문서명: Logx SPEC
- 작성일: 2026-01-23
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.logcat
- 수준: 구현 준비 수준(Implementation-ready)
- 상태: 초안

## 전제/참조
- 기본 규칙/환경은 루트 AGENTS.md에서 연결되는 *_RULE.md를 따른다.
- 상세 요구는 `PRD.md`를 따른다.
- 실제 코드 구현은 `kr.open.library.simple_ui.core.logcat` 패키지 내에서만 진행한다.

## 목표/비목표
- 목표/비목표는 PRD를 따른다.

## 빌드 타입 정책
- Release 빌드에서도 로그 출력 허용(자동 비활성화 없음).
- 출력 제어는 setLogging(false)로만 수행한다.

## 패키지 구조 및 책임
- kr.open.library.simple_ui.core.logcat
  - root: Logx 공개 API
  - config: 설정 모델, 기본값 setter/getter
  - internal:
    - common: 공통 유틸, 상수, 헬퍼
    - filter: 타입/태그 필터 로직
    - formatter: 메시지/메타/JSON/스레드 포맷터
    - extractor: 스택 트레이스 위치 추출
    - pipeline: LogxPipeline 오케스트레이션
    - writer: 콘솔/파일 라이터
  - extension: Kotlin 확장 함수

## 데이터 모델
### LogType
- VERBOSE, DEBUG, INFO, WARN, ERROR, PARENT, JSON, THREAD

### LogType 매핑/출력
- VERBOSE -> v()
- DEBUG -> d()
- INFO -> i()
- WARN -> w()
- ERROR -> e()
- PARENT -> p()
- JSON -> j()
- THREAD -> t()
- {type} 출력 값: V/D/I/W/E/P/J/T

### TagBlockList
- 목록에 포함된 tag는 차단

### StorageType
- INTERNAL, APP_EXTERNAL, PUBLIC_EXTERNAL

### StackFrame(추상 모델)
- fileName: String
- lineNumber: Int
- methodName: String
- className: String

## 설정 기본값(확정)
- isLogging: true
- logTypes: 모든 타입 허용(v/d/i/w/e/p/j/t) // LogType 매핑: v=VERBOSE, d=DEBUG, i=INFO, w=WARN, e=ERROR, p=PARENT, j=JSON, t=THREAD
- isLogTagBlockListEnabled: false
- logTagBlockList: emptySet
- isSave: false
- storageType: APP_EXTERNAL
- appName: "AppName"
- jsonIndent: 4 (고정 상수, 설정 변경 불가)
- skipPackages(기본 목록): packageName prefix 기준 매칭, addSkipPackages로 확장 가능
  - kr.open.library.simple_ui.core.logcat
  - java.
  - kotlin.
  - kotlinx.coroutines.
  - kotlin.coroutines
  - android.util.
  - android.os.
  - dalvik.system.

## 초기화/Context 처리
- Logx.initialize(context: Context)로 Application Context를 주입한다.
- initialize 미호출 상태에서 setSaveEnabled(true)를 호출하면 디버그는 IllegalStateException, 릴리즈는 Log.e로 처리한다.
- PUBLIC_EXTERNAL 설정 시 initialize 미호출이면 디버그는 IllegalStateException, 릴리즈는 Log.e로 처리한다.
- PUBLIC_EXTERNAL + API 28 이하에서 권한 미보유 시 디버그는 SecurityException, 릴리즈는 Log.e로 처리한다.
- Context가 없으면 appName은 "AppName"으로 고정한다.
- 파일 저장은 initialize 이후에만 허용한다(setSaveDirectory 포함).
- initialize 이전 setSaveDirectory 호출 시 경로 값은 보관하되, 디렉터리 생성/파일 저장은 initialize 이후에만 수행한다.
- initialize는 appName을 변경하지 않는다(setAppName이 우선).

## 공통 처리 규칙
### 태그/앱명 합성
- 외부 입력 tag는 null/빈 문자열을 허용하지 않는다.
- 잘못된 tag 입력마다 Log.e 1회 출력 후 tag를 무시하고 AppName만 사용한다.
- 최종 표기: AppName 또는 AppName[tag]
- tag 필터링은 tag 자체 기준으로 적용한다.

### 내부 경고/에러 로그 태그
- 내부 경고/에러 Log.e 태그는 입력 tag가 있으면 해당 tag를 사용한다.
- 입력 tag가 null/빈 문자열이면 "ERROR"를 사용한다.

### 메시지 처리
- msg가 null이면 문자열 "null"로 처리한다.
- msg 인자가 없는 오버로드는 메시지 없이 메타만 출력한다.
- 멀티라인/길이 처리는 Android 기본 Log.* 동작과 동일하게 둔다.
- tag 길이 제한은 두지 않고 원문 그대로 출력한다.

### 필터 적용 순서
1) isLogging 확인
2) logTypes 필터
3) isLogTagBlockListEnabled + logTagBlockList
4) 포맷/출력

### LogType 필터 규칙
- logTypes는 ALLOWLIST 방식으로 동작한다.
- DEBUG 포함 시 표준 DEBUG 로그만 통과한다.
- PARENT/JSON/THREAD는 각각 포함될 때만 통과한다.
- p/j/t는 DEBUG와 별개로 취급한다.

## 스택 트레이스 추출 규칙
- Thread.currentThread().stackTrace를 사용한다.
- logcat 내부 prefix 마지막 프레임 다음 인덱스부터 순차 탐색한다.
- 내부 prefix를 찾지 못하거나 범위를 벗어나면 fallbackStartIndex = 4부터 탐색한다.
- skipPackages prefix 매칭으로 내부 프레임을 건너뛴다.
- 합성 프레임 중 D8$$SyntheticClass, access$와 fileName Unknown/line<=0은 건너뛴다(람다/$r8$는 file/line이 유효하면 허용).
- 첫 번째 외부 프레임을 현재 위치로 사용한다.
- 두 번째 외부 프레임을 부모 위치로 사용한다.
- 외부 프레임을 찾지 못하면 fallback 요소로 대체한다(가능하면 탐색 시작 인덱스 기준, 없으면 첫 요소).
- 부모 프레임이 없으면 아래 포맷으로 출력한다:
```
AppName : ┌[PARENT]
AppName : └[PARENT] (MainActivity.kt:25).onCreate
```
- 기본 목록은 내부에 유지하고, addSkipPackages로 확장할 수 있어야 한다.
- className/methodName contains 방식(access$/Lambda$)은 테스트 후 검토한다.

## API 설계
### 표준 로그(v/d/i/w/e 공통)
```
object Logx {
    @JvmStatic fun v()
    @JvmStatic fun v(msg: Any?)
    @JvmStatic fun v(tag: String, msg: Any?)

    @JvmStatic fun d()
    @JvmStatic fun d(msg: Any?)
    @JvmStatic fun d(tag: String, msg: Any?)

    @JvmStatic fun i()
    @JvmStatic fun i(msg: Any?)
    @JvmStatic fun i(tag: String, msg: Any?)

    @JvmStatic fun w()
    @JvmStatic fun w(msg: Any?)
    @JvmStatic fun w(tag: String, msg: Any?)

    @JvmStatic fun e()
    @JvmStatic fun e(msg: Any?)
    @JvmStatic fun e(tag: String, msg: Any?)
}
```
- v/d/i/w/e 모두 동일한 오버로드 시그니처를 가진다.
- Throwable 오버로드는 1차 개발 범위에서 제외(추후 추가 예정).

### 확장 로그
```
object Logx {
    @JvmStatic fun p()
    @JvmStatic fun p(msg: Any?)
    @JvmStatic fun p(tag: String, msg: Any?)

    @JvmStatic fun j(json: String)
    @JvmStatic fun j(tag: String, json: String)

    @JvmStatic fun t()
    @JvmStatic fun t(msg: Any?)
    @JvmStatic fun t(tag: String, msg: Any?)
}
```
- p/j/t는 LogType 필터 규칙을 따른다.
- p/j/t 콘솔 출력 시 android.util.Log.d()를 사용한다.

### 초기화/설정 Setter
```
object Logx {
    @JvmStatic fun initialize(context: Context)
    @JvmStatic fun setLogging(enabled: Boolean)
    @JvmStatic fun setLogTypes(types: Set<LogType>)
    @JvmStatic fun setLogTagBlockListEnabled(enabled: Boolean)
    @JvmStatic fun setLogTagBlockList(tags: Set<String>)
    @JvmStatic fun setSaveEnabled(enabled: Boolean)
    @JvmStatic fun setStorageType(type: StorageType)
    @JvmStatic fun setSaveDirectory(path: String)
    @JvmStatic fun setAppName(name: String)
    @JvmStatic fun addSkipPackages(packages: Set<String>)
}
```
- setSaveDirectory는 사용자가 직접 경로를 지정할 때 사용한다.
- initialize가 없으면 save 경로 자동 계산은 수행하지 않는다.
- logTagBlockList는 빈/공백 값을 허용하지 않으며, 제거 후 Log.e로 1회 알린다.

### Getter
```
object Logx {
    @JvmStatic fun isLogging(): Boolean
    @JvmStatic fun getLogTypes(): Set<LogType>
    @JvmStatic fun isLogTagBlockListEnabled(): Boolean
    @JvmStatic fun getLogTagBlockList(): Set<String>
    @JvmStatic fun isSaveEnabled(): Boolean
    @JvmStatic fun getStorageType(): StorageType
    @JvmStatic fun getSaveDirectory(): String?
    @JvmStatic fun getAppName(): String
    @JvmStatic fun getSkipPackages(): Set<String>
}
```
- getter는 현재 설정 상태를 조회하며, 컬렉션은 unmodifiable Set을 반환한다.

### Kotlin 확장 함수
```
// 표준 로그
fun Any.logv() = Logx.v(this)
fun Any.logv(tag: String) = Logx.v(tag, this)
fun Any.logd() = Logx.d(this)
fun Any.logd(tag: String) = Logx.d(tag, this)
fun Any.logi() = Logx.i(this)
fun Any.logi(tag: String) = Logx.i(tag, this)
fun Any.logw() = Logx.w(this)
fun Any.logw(tag: String) = Logx.w(tag, this)
fun Any.loge() = Logx.e(this)
fun Any.loge(tag: String) = Logx.e(tag, this)

// 확장 로그
fun Any.logp() = Logx.p(this)
fun Any.logp(tag: String) = Logx.p(tag, this)
fun Any.logt() = Logx.t(this)
fun Any.logt(tag: String) = Logx.t(tag, this)
fun String.logj() = Logx.j(this)
fun String.logj(tag: String) = Logx.j(tag, this)
```
- 객체.logd()는 msg로 처리한다.
- 객체.logd(tag)는 tag + msg로 처리한다.
- 문자열.logj()는 json 문자열로 처리한다.
- logv/logi/logw/loge/logp/logt도 동일한 패턴을 따른다.

## 콘솔 출력 포맷 규칙
### 기본 포맷
- prefix: `AppName` 또는 `AppName[tag]`
- meta: `(FileName:Line).method`
- message: ` - {msg}` (msg 인자가 있을 때만)
```
AppName : (MainActivity.kt:25).onCreate
AppName : (MainActivity.kt:25).onCreate - msg
AppName[tag] : (MainActivity.kt:25).onCreate - msg
```

### p() 포맷
- 두 줄 출력
- 상단: ┌[PARENT] + 부모 위치
- 하단: └[PARENT] + 현재 위치 + msg(있을 때만)

### t() 포맷
```
AppName : [TID = 1231](MainActivity.kt:25).onCreate
AppName : [TID = 1231](MainActivity.kt:25).onCreate - msg
AppName[tag] : [TID = 1231](MainActivity.kt:25).onCreate - msg
```

### j() 포맷
```
AppName : [JSON](MainActivity.kt:25).onCreate -
{
    "key": "value"
}
[End]
```
- [JSON] 시작 마커와 [End] 종료 마커는 고정.
- 콘솔(JSON) 출력은 한 번의 Log 호출로 멀티라인 메시지를 출력한다(프리픽스는 첫 줄 기준).
- json 문자열이 비어 있으면 원문 그대로 출력한다.

## JSON 처리
- 수동 pretty-print 로직으로 포맷한다.
- indent 4(고정)로 출력한다(옵션 없음).
- 파싱 실패 시 원문 그대로 출력한다.

## 동시성 전략
- 콘솔 출력: Android Log.*가 thread-safe이므로 추가 동기화 불필요.
- Config 스냅샷: 단일 캐시 스냅샷을 유지하며 setter에서 copy-on-write로 교체한다.
- Config 컬렉션: unmodifiable Set 반환(내부에서 copy-on-write로 갱신).
- 파일 생성/쓰기 시점에만 synchronized 블록을 적용한다.
- 파일은 BufferedWriter를 유지한 채 실시간 기록하며, write 후 즉시 flush한다.
- 파일 쓰기는 코루틴(Dispatchers.IO) 단일 소비자 큐로 비동기 처리해 호출 스레드 차단을 최소화한다.
- isSave = false(기본값)일 때는 동기화 비용 없음.

## 파일 저장
### 경로 선택
- setSaveDirectory가 설정되면 해당 경로를 최우선으로 사용한다.
- setSaveDirectory가 없을 때 storageType 기준으로 경로를 계산한다.
- 로그 디렉터리명은 "AppLogs"로 고정한다.
- StorageType 경로 규칙:
  - INTERNAL: `{context.filesDir}/AppLogs`
  - APP_EXTERNAL: `{context.getExternalFilesDir("AppLogs")}` (null이면 INTERNAL로 폴백)
  - PUBLIC_EXTERNAL:
    - API 29+: `{context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/AppLogs` (null이면 APP_EXTERNAL로 폴백)
    - API 28 이하: `{Environment.getExternalStorageDirectory()}/AppLogs`
- PUBLIC_EXTERNAL + API 28 이하에서는 권한이 필요하므로 경고 가능.
- PUBLIC_EXTERNAL + API 28 이하에서 권한이 없으면 파일 저장을 중단하고 Log.e로 매 시도 경고한다.
- 저장 경로 디렉터리는 필요 시 자동 생성하며, 생성 실패 시 Log.e로 매 시도 경고하고 저장을 중단한다.
- Context가 없으면 파일 저장을 비활성화한다.

### 파일명/포맷
- 파일 생성 시점: save 설정 이후 첫 로그 호출 시
- 파일 재사용: 저장 경로/storageType/appName이 변경되지 않는 한 앱 종료 전까지 동일 파일 사용
- 저장 경로/storageType/appName이 변경되면 다음 로그 시점에 새 파일을 생성한다(기존 파일은 유지).
- 기본 파일명: `{appName}_yyyy_MM_dd__HH-mm-ss-SSS.txt`
- 로테이션 파일명: `{appName}_yyyy_MM_dd__HH-mm-ss-SSS_{count}.txt` (count는 1부터 증가)
- 파일 포맷: `{timestamp} [{type}] {prefix} : {payload}`
- timestamp 포맷: `yyyy-MM-dd HH:mm:ss.SSS`
- prefix: `AppName` 또는 `AppName[tag]`
- payload: `(FileName:Line).method - msg` 또는 `(FileName:Line).method`
- type: LogType 1글자 표기(V/D/I/W/E/P/J/T)
- isSave false -> true 전환 시점에 파일이 한 번도 생성된 적이 없으면 생성하고, 이미 생성된 경우 기존 파일에 이어서 기록한다.
- 실시간 기록은 BufferedWriter 유지 + flush 방식으로 수행한다.

### 로테이션 정책
- 파일 크기 ≥ 10MB이면 다음 로그부터 새 파일로 기록한다.
- 로테이션 파일은 count를 증가시키며 생성하고, 파일 개수 제한은 두지 않는다.

### 쓰기 정책
- 즉시 flush 중심(로그 유실 최소화).
- 동시성 보호를 위해 파일 생성/파일 쓰기 시점에만 synchronized 블록을 적용한다.
- 파일 쓰기 실패는 앱 크래시를 유발하지 않도록 예외를 삼키고 콘솔로 에러 로그를 남긴다(매 시도).
- 반복 실패 시에도 매번 에러 로그를 출력한다.
- isLogging = false이면 파일 저장도 수행하지 않는다.
- 멀티라인 로그 파일 기록 규칙:
  - p(): 모든 줄에 동일 timestamp/prefix를 붙여 기록한다.
  - j(): 첫 줄만 timestamp/prefix를 붙이고, JSON 본문은 timestamp 없이 기록한다.
- 로테이션 발생 시 현재 writer를 flush/close한 뒤 새 파일을 생성한다.

### 라이프사이클 연동
- ProcessLifecycleOwner를 사용해 앱 백그라운드 진입 시 파일 writer를 close한다.
- 재포그라운드 후 다음 로그 시 writer는 자동으로 다시 열린다.
- ProcessLifecycleOwner 사용을 위해 lifecycle-process 의존성을 추가한다.

### 플러시 트리거
- 기본은 즉시 flush 방식이다.
- 앱 백그라운드/종료 시 writer close로 flush를 보장한다.
- 로테이션 시에도 flush/close 후 새 writer로 전환한다.
- 추후 버퍼링 방식으로 전환 시 별도 트리거를 추가한다(백그라운드 전환, 종료, 메모리 압박, 크래시).

## Context 없는 환경 처리
- isSave = true이더라도 Context가 없으면 파일 저장을 중단한다.
- 해당 상태는 경고 로그를 1회만 출력한다(반복 노이즈 방지).
- initialize 미호출로 Context가 없는 경우도 동일 경고로 처리하여 중복 출력하지 않는다.

## 테스트 전략(구현 준비)
### 단위 테스트
- StackTraceExtractor: skip 목록 적용, 현재/부모 프레임 추출
- Formatter: 기본/JSON/THREAD/Parent 포맷 검증
- Filter: 레벨/태그/모드별 필터링
- Config: 기본값, setter 적용, skip 목록 확장
- Config: addSkipPackages는 빈/공백 prefix를 무시한다.

### Robolectric 테스트
- PathResolver: storageType 별 경로 계산
- FileWriter: 실제 파일 기록/즉시 flush 동작
- Context 없는 환경 처리(파일 저장 비활성화)
- PUBLIC_EXTERNAL 권한 미보유 시 저장 중단/경고 동작

## 오픈 이슈
- 로그 기간 제한 정책 부재
- 릴리즈 빌드에서 라인 정보 축약 가능성
