# Logx SPEC

## 臾몄꽌 ?뺣낫
- 臾몄꽌紐? Logx SPEC
- ?묒꽦?? 2026-01-23
- ???紐⑤뱢: simple_core
- ?⑦궎吏: kr.open.library.simple_ui.core.logcat
- ?섏?: 援ы쁽 以鍮??섏?(Implementation-ready)
- ?곹깭: 珥덉븞

## ?꾩젣/李몄“
- 湲곕낯 洹쒖튃/?섍꼍? 猷⑦듃 AGENTS.md?먯꽌 ?곌껐?섎뒗 *_RULE.md瑜??곕Ⅸ??
- ?곸꽭 ?붽뎄??`PRD.md`瑜??곕Ⅸ??
- ?ㅼ젣 肄붾뱶 援ы쁽? `kr.open.library.simple_ui.core.logcat` ?⑦궎吏 ?댁뿉?쒕쭔 吏꾪뻾?쒕떎.

## 紐⑺몴/鍮꾨ぉ??- 紐⑺몴/鍮꾨ぉ?쒕뒗 PRD瑜??곕Ⅸ??

## 鍮뚮뱶 ????뺤콉
- Release 鍮뚮뱶?먯꽌??濡쒓렇 異쒕젰 ?덉슜(?먮룞 鍮꾪솢?깊솕 ?놁쓬).
- 異쒕젰 ?쒖뼱??setLogging(false)濡쒕쭔 ?섑뻾?쒕떎.

## ?⑦궎吏 援ъ“ 諛?梨낆엫
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

## ?곗씠??紐⑤뜽
### LogType
- VERBOSE, DEBUG, INFO, WARN, ERROR, PARENT, JSON, THREAD

### LogType 留ㅽ븨/異쒕젰
- VERBOSE -> v()
- DEBUG -> d()
- INFO -> i()
- WARN -> w()
- ERROR -> e()
- PARENT -> p()
- JSON -> j()
- THREAD -> t()
- {type} 異쒕젰 媛? V/D/I/W/E/P/J/T

### TagBlockList
- 紐⑸줉???ы븿??tag??李⑤떒

### StorageType
- INTERNAL, APP_EXTERNAL, PUBLIC_EXTERNAL

### StackFrame(異붿긽 紐⑤뜽)
- fileName: String
- lineNumber: Int
- methodName: String
- className: String

## ?ㅼ젙 湲곕낯媛??뺤젙)
- isLogging: true
- logTypes: 紐⑤뱺 ????덉슜(v/d/i/w/e/p/j/t) // LogType 留ㅽ븨: v=VERBOSE, d=DEBUG, i=INFO, w=WARN, e=ERROR, p=PARENT, j=JSON, t=THREAD
- isLogTagBlockListEnabled: false
- logTagBlockList: emptySet
- isSave: false
- storageType: APP_EXTERNAL
- appName: "AppName"
- jsonIndent: 4 (怨좎젙 ?곸닔, ?ㅼ젙 蹂寃?遺덇?)
- skipPackages(湲곕낯 紐⑸줉): packageName prefix 湲곗? 留ㅼ묶, addSkipPackages濡??뺤옣 媛??  - kr.open.library.simple_ui.core.logcat
  - java.
  - kotlin.
  - kotlinx.coroutines.
  - kotlin.coroutines
  - android.util.
  - android.os.
  - dalvik.system.

## 珥덇린??Context 泥섎━
- Logx.initialize(context: Context)濡?Application Context瑜?二쇱엯?쒕떎.
- initialize 誘명샇異??곹깭?먯꽌 setSaveEnabled(true)瑜??몄텧?섎㈃ IllegalStateException???섏쭊??
- initialize 미호출 상태에서 setStorageType(PUBLIC_EXTERNAL) 호출 시 IllegalStateException을 던진다.
- Context媛 ?놁쑝硫?appName? "AppName"?쇰줈 怨좎젙?쒕떎.
- ?뚯씪 ??μ? initialize ?댄썑?먮쭔 ?덉슜?쒕떎(setSaveDirectory ?ы븿).
- initialize ?댁쟾 setSaveDirectory ?몄텧 ??寃쎈줈 媛믪? 蹂닿??섎릺, ?붾젆?곕━ ?앹꽦/?뚯씪 ??μ? initialize ?댄썑?먮쭔 ?섑뻾?쒕떎.
- initialize??appName??蹂寃쏀븯吏 ?딅뒗??setAppName???곗꽑).

## 怨듯넻 泥섎━ 洹쒖튃
### ?쒓렇/?깅챸 ?⑹꽦
- ?몃? ?낅젰 tag??null/鍮?臾몄옄?댁쓣 ?덉슜?섏? ?딅뒗??
- ?섎せ??tag ?낅젰留덈떎 Log.e 1??異쒕젰 ??tag瑜?臾댁떆?섍퀬 AppName留??ъ슜?쒕떎.
- 理쒖쥌 ?쒓린: AppName ?먮뒗 AppName[tag]
- tag ?꾪꽣留곸? tag ?먯껜 湲곗??쇰줈 ?곸슜?쒕떎.

### ?대? 寃쎄퀬/?먮윭 濡쒓렇 ?쒓렇
- ?대? 寃쎄퀬/?먮윭 Log.e ?쒓렇???낅젰 tag媛 ?덉쑝硫??대떦 tag瑜??ъ슜?쒕떎.
- ?낅젰 tag媛 null/鍮?臾몄옄?댁씠硫?"ERROR"瑜??ъ슜?쒕떎.

### 硫붿떆吏 泥섎━
- msg媛 null?대㈃ 臾몄옄??"null"濡?泥섎━?쒕떎.
- msg ?몄옄媛 ?녿뒗 ?ㅻ쾭濡쒕뱶??硫붿떆吏 ?놁씠 硫뷀?留?異쒕젰?쒕떎.
- 硫?곕씪??湲몄씠 泥섎━??Android 湲곕낯 Log.* ?숈옉怨??숈씪?섍쾶 ?붾떎.
- tag 湲몄씠 ?쒗븳? ?먯? ?딄퀬 ?먮Ц 洹몃?濡?異쒕젰?쒕떎.

### ?꾪꽣 ?곸슜 ?쒖꽌
1) isLogging ?뺤씤
2) logTypes ?꾪꽣
3) isLogTagBlockListEnabled + logTagBlockList
4) ?щ㎎/異쒕젰

### LogType ?꾪꽣 洹쒖튃
- logTypes??ALLOWLIST 諛⑹떇?쇰줈 ?숈옉?쒕떎.
- DEBUG ?ы븿 ???쒖? DEBUG 濡쒓렇留??듦낵?쒕떎.
- PARENT/JSON/THREAD??媛곴컖 ?ы븿???뚮쭔 ?듦낵?쒕떎.
- p/j/t??DEBUG? 蹂꾧컻濡?痍④툒?쒕떎.

## ?ㅽ깮 ?몃젅?댁뒪 異붿텧 洹쒖튃
- Thread.currentThread().stackTrace瑜??ъ슜?쒕떎.
- logcat ?대? prefix 留덉?留??꾨젅???ㅼ쓬 ?몃뜳?ㅻ????쒖감 ?먯깋?쒕떎.
- ?대? prefix瑜?李얠? 紐삵븯嫄곕굹 踰붿쐞瑜?踰쀬뼱?섎㈃ fallbackStartIndex = 4遺???먯깋?쒕떎.
- skipPackages prefix 留ㅼ묶?쇰줈 ?대? ?꾨젅?꾩쓣 嫄대꼫?대떎.
- ?⑹꽦 ?꾨젅??以?D8$$SyntheticClass, access$? fileName Unknown/line<=0? 嫄대꼫?대떎(?뚮떎/$r8$??file/line???좏슚?섎㈃ ?덉슜).
- 泥?踰덉㎏ ?몃? ?꾨젅?꾩쓣 ?꾩옱 ?꾩튂濡??ъ슜?쒕떎.
- ??踰덉㎏ ?몃? ?꾨젅?꾩쓣 遺紐??꾩튂濡??ъ슜?쒕떎.
- ?몃? ?꾨젅?꾩쓣 李얠? 紐삵븯硫?fallback ?붿냼濡??泥댄븳??媛?ν븯硫??먯깋 ?쒖옉 ?몃뜳??湲곗?, ?놁쑝硫?泥??붿냼).
- 遺紐??꾨젅?꾩씠 ?놁쑝硫??꾨옒 ?щ㎎?쇰줈 異쒕젰?쒕떎:
```
AppName : ??PARENT]
AppName : ??PARENT] (MainActivity.kt:25).onCreate
```
- 湲곕낯 紐⑸줉? ?대????좎??섍퀬, addSkipPackages濡??뺤옣?????덉뼱???쒕떎.
- className/methodName contains 諛⑹떇(access$/Lambda$)? ?뚯뒪????寃?좏븳??

## API ?ㅺ퀎
### ?쒖? 濡쒓렇(v/d/i/w/e 怨듯넻)
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
- v/d/i/w/e 紐⑤몢 ?숈씪???ㅻ쾭濡쒕뱶 ?쒓렇?덉쿂瑜?媛吏꾨떎.
- Throwable ?ㅻ쾭濡쒕뱶??1李?媛쒕컻 踰붿쐞?먯꽌 ?쒖쇅(異뷀썑 異붽? ?덉젙).

### ?뺤옣 濡쒓렇
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
- p/j/t??LogType ?꾪꽣 洹쒖튃???곕Ⅸ??
- p/j/t 肄섏넄 異쒕젰 ??android.util.Log.d()瑜??ъ슜?쒕떎.

### 珥덇린???ㅼ젙 Setter
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
- setSaveDirectory???ъ슜?먭? 吏곸젒 寃쎈줈瑜?吏?뺥븷 ???ъ슜?쒕떎.
- initialize媛 ?놁쑝硫?save 寃쎈줈 ?먮룞 怨꾩궛? ?섑뻾?섏? ?딅뒗??
- logTagBlockList??鍮?怨듬갚 媛믪쓣 ?덉슜?섏? ?딆쑝硫? ?쒓굅 ??Log.e濡?1???뚮┛??

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
- getter???꾩옱 ?ㅼ젙 ?곹깭瑜?議고쉶?섎ŉ, 而щ젆?섏? 諛⑹뼱??蹂듭궗蹂몄쓣 諛섑솚?쒕떎.

### Kotlin ?뺤옣 ?⑥닔
```
// ?쒖? 濡쒓렇
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

// ?뺤옣 濡쒓렇
fun Any.logp() = Logx.p(this)
fun Any.logp(tag: String) = Logx.p(tag, this)
fun Any.logt() = Logx.t(this)
fun Any.logt(tag: String) = Logx.t(tag, this)
fun String.logj() = Logx.j(this)
fun String.logj(tag: String) = Logx.j(tag, this)
```
- 媛앹껜.logd()??msg濡?泥섎━?쒕떎.
- 媛앹껜.logd(tag)??tag + msg濡?泥섎━?쒕떎.
- 臾몄옄??logj()??json 臾몄옄?대줈 泥섎━?쒕떎.
- logv/logi/logw/loge/logp/logt???숈씪???⑦꽩???곕Ⅸ??

## 肄섏넄 異쒕젰 ?щ㎎ 洹쒖튃
### 湲곕낯 ?щ㎎
- prefix: `AppName` ?먮뒗 `AppName[tag]`
- meta: `(FileName:Line).method`
- message: ` - {msg}` (msg ?몄옄媛 ?덉쓣 ?뚮쭔)
```
AppName : (MainActivity.kt:25).onCreate
AppName : (MainActivity.kt:25).onCreate - msg
AppName[tag] : (MainActivity.kt:25).onCreate - msg
```

### p() ?щ㎎
- ??以?異쒕젰
- ?곷떒: ??PARENT] + 遺紐??꾩튂
- ?섎떒: ??PARENT] + ?꾩옱 ?꾩튂 + msg(?덉쓣 ?뚮쭔)

### t() ?щ㎎
```
AppName : [TID = 1231](MainActivity.kt:25).onCreate
AppName : [TID = 1231](MainActivity.kt:25).onCreate - msg
AppName[tag] : [TID = 1231](MainActivity.kt:25).onCreate - msg
```

### j() ?щ㎎
```
AppName : [JSON](MainActivity.kt:25).onCreate -
{
    "key": "value"
}
[End]
```
- [JSON] ?쒖옉 留덉빱? [End] 醫낅즺 留덉빱??怨좎젙.
- 肄섏넄(JSON) 異쒕젰? ??踰덉쓽 Log ?몄텧濡?硫?곕씪??硫붿떆吏瑜?異쒕젰?쒕떎(?꾨━?쎌뒪??泥?以?湲곗?).
- json 臾몄옄?댁씠 鍮꾩뼱 ?덉쑝硫??먮Ц 洹몃?濡?異쒕젰?쒕떎.

## JSON 泥섎━
- 수동 pretty-print(문자열 파서)로 JSON 포맷팅한다.
- indent는 4 고정(옵션 없음).
- ?뚯떛 ?ㅽ뙣 ???먮Ц 洹몃?濡?異쒕젰?쒕떎.

## ?숈떆???꾨왂
- 肄섏넄 異쒕젰: Android Log.*媛 thread-safe?대?濡?異붽? ?숆린??遺덊븘??
- Config ?꾨뱶: @Volatile (?⑥닚 ?꾨뱶) + synchronized 諛⑹뼱??蹂듭궗 (Set ??而щ젆??.
- ?뚯씪 ?앹꽦/?곌린 ?쒖젏?먮쭔 synchronized 釉붾줉???곸슜?쒕떎.
- ?뚯씪? BufferedWriter瑜??좎???梨??ㅼ떆媛?湲곕줉?섎ŉ, write ??利됱떆 flush?쒕떎.
- ?뚯씪 ?곌린??肄붾（??Dispatchers.IO) ?⑥씪 ?뚮퉬???먮줈 鍮꾨룞湲?泥섎━???몄텧 ?ㅻ젅??李⑤떒??理쒖냼?뷀븳??
- isSave = false(湲곕낯媛????뚮뒗 ?숆린??鍮꾩슜 ?놁쓬.

## ?뚯씪 ???### 寃쎈줈 ?좏깮
- setSaveDirectory媛 ?ㅼ젙?섎㈃ ?대떦 寃쎈줈瑜?理쒖슦?좎쑝濡??ъ슜?쒕떎.
- setSaveDirectory媛 ?놁쓣 ??storageType 湲곗??쇰줈 寃쎈줈瑜?怨꾩궛?쒕떎.
- 濡쒓렇 ?붾젆?곕━紐낆? "AppLogs"濡?怨좎젙?쒕떎.
- StorageType 寃쎈줈 洹쒖튃:
  - internal:
    - common: 공통 유틸, 상수, 헬퍼
    - filter: 타입/태그 필터 로직
    - formatter: 메시지/메타/JSON/스레드 포맷터
    - extractor: 스택 트레이스 위치 추출
    - pipeline: LogxPipeline 오케스트레이션
    - writer: 콘솔/파일 라이터
  - APP_EXTERNAL: `{context.getExternalFilesDir("AppLogs")}` (null?대㈃ INTERNAL濡??대갚)
  - PUBLIC_EXTERNAL:
    - API 29+: `{context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/AppLogs` (null?대㈃ APP_EXTERNAL濡??대갚)
    - API 28 ?댄븯: `{Environment.getExternalStorageDirectory()}/AppLogs`
- PUBLIC_EXTERNAL + API 28 이하에서 권한이 없으면 SecurityException을 던진다.
- ???寃쎈줈 ?붾젆?곕━???꾩슂 ???먮룞 ?앹꽦?섎ŉ, ?앹꽦 ?ㅽ뙣 ??Log.e濡?留??쒕룄 寃쎄퀬?섍퀬 ??μ쓣 以묐떒?쒕떎.
- Context媛 ?놁쑝硫??뚯씪 ??μ쓣 鍮꾪솢?깊솕?쒕떎.

### ?뚯씪紐??щ㎎
- ?뚯씪 ?앹꽦 ?쒖젏: save ?ㅼ젙 ?댄썑 泥?濡쒓렇 ?몄텧 ??- ?뚯씪 ?ъ궗?? ???寃쎈줈/storageType/appName??蹂寃쎈릺吏 ?딅뒗 ????醫낅즺 ?꾧퉴吏 ?숈씪 ?뚯씪 ?ъ슜
- ???寃쎈줈/storageType/appName??蹂寃쎈릺硫??ㅼ쓬 濡쒓렇 ?쒖젏?????뚯씪???앹꽦?쒕떎(湲곗〈 ?뚯씪? ?좎?).
- 湲곕낯 ?뚯씪紐? `{appName}_yyyy_MM_dd__HH-mm-ss-SSS.txt`
- ?뚯씪 ?щ㎎: `{timestamp} [{type}] {prefix} : {payload}`
- timestamp ?щ㎎: `yyyy-MM-dd HH:mm:ss.SSS`
- prefix: `AppName` ?먮뒗 `AppName[tag]`
- payload: `(FileName:Line).method - msg` ?먮뒗 `(FileName:Line).method`
- type: LogType 1湲???쒓린(V/D/I/W/E/P/J/T)
- isSave false -> true ?꾪솚 ?쒖젏???뚯씪????踰덈룄 ?앹꽦???곸씠 ?놁쑝硫??앹꽦?섍퀬, ?대? ?앹꽦??寃쎌슦 湲곗〈 ?뚯씪???댁뼱??湲곕줉?쒕떎.
 - ?ㅼ떆媛?湲곕줉? BufferedWriter ?좎? + flush 諛⑹떇?쇰줈 ?섑뻾?쒕떎.

### ?곌린 ?뺤콉
- 利됱떆 flush 以묒떖(濡쒓렇 ?좎떎 理쒖냼??.
- ?숈떆??蹂댄샇瑜??꾪빐 ?뚯씪 ?앹꽦/?뚯씪 ?곌린 ?쒖젏?먮쭔 synchronized 釉붾줉???곸슜?쒕떎.
- ?뚯씪 ?곌린 ?ㅽ뙣?????щ옒?쒕? ?좊컻?섏? ?딅룄濡??덉쇅瑜??쇳궎怨?肄섏넄濡??먮윭 濡쒓렇瑜??④릿??留??쒕룄).
- 諛섎났 ?ㅽ뙣 ?쒖뿉??留ㅻ쾲 ?먮윭 濡쒓렇瑜?異쒕젰?쒕떎.
- isLogging = false?대㈃ ?뚯씪 ??λ룄 ?섑뻾?섏? ?딅뒗??
- 硫?곕씪??濡쒓렇 ?뚯씪 湲곕줉 洹쒖튃:
  - p(): 紐⑤뱺 以꾩뿉 ?숈씪 timestamp/prefix瑜?遺숈뿬 湲곕줉?쒕떎.
  - j(): 泥?以꾨쭔 timestamp/prefix瑜?遺숈씠怨? JSON 蹂몃Ц? timestamp ?놁씠 湲곕줉?쒕떎.

### ?쇱씠?꾩궗?댄겢 ?곕룞
- ProcessLifecycleOwner瑜??ъ슜????諛깃렇?쇱슫??吏꾩엯 ???뚯씪 writer瑜?close?쒕떎.
- ?ы룷洹몃씪?대뱶 ???ㅼ쓬 濡쒓렇 ??writer???먮룞?쇰줈 ?ㅼ떆 ?대┛??
- ProcessLifecycleOwner ?ъ슜???꾪빐 lifecycle-process ?섏〈?깆쓣 異붽??쒕떎.

### ?뚮윭???몃━嫄?- ?꾩옱 利됱떆 flush 諛⑹떇?대?濡?蹂꾨룄 ?몃━嫄?遺덊븘??
- 異뷀썑 踰꾪띁留?諛⑹떇?쇰줈 ?꾪솚 ???곸슜 ?덉젙(諛깃렇?쇱슫???꾪솚, 醫낅즺, 硫붾え由??뺣컯, ?щ옒??.

## Context ?녿뒗 ?섍꼍 泥섎━
- isSave = true?대뜑?쇰룄 Context媛 ?놁쑝硫??뚯씪 ??μ쓣 以묐떒?쒕떎.
- ?대떦 ?곹깭??寃쎄퀬 濡쒓렇瑜?1?뚮쭔 異쒕젰?쒕떎(諛섎났 ?몄씠利?諛⑹?).
- initialize 誘명샇異쒕줈 Context媛 ?녿뒗 寃쎌슦???숈씪 寃쎄퀬濡?泥섎━?섏뿬 以묐났 異쒕젰?섏? ?딅뒗??

## ?뚯뒪???꾨왂(援ы쁽 以鍮?
### ?⑥쐞 ?뚯뒪??- StackTraceExtractor: skip 紐⑸줉 ?곸슜, ?꾩옱/遺紐??꾨젅??異붿텧
- Formatter: 湲곕낯/JSON/THREAD/Parent ?щ㎎ 寃利?- Filter: ?덈꺼/?쒓렇/紐⑤뱶蹂??꾪꽣留?- Config: 湲곕낯媛? setter ?곸슜, skip 紐⑸줉 ?뺤옣

### Robolectric ?뚯뒪??- PathResolver: storageType 蹂?寃쎈줈 怨꾩궛
- FileWriter: ?ㅼ젣 ?뚯씪 湲곕줉/利됱떆 flush ?숈옉
- Context ?녿뒗 ?섍꼍 泥섎━(?뚯씪 ???鍮꾪솢?깊솕)
- PUBLIC_EXTERNAL 권한 미보유 시 SecurityException 동작

## ?ㅽ뵂 ?댁뒋
- 濡쒓렇 ?뚯쟾(?ш린/湲곌컙 ?쒗븳) ?뺤콉 遺??- 由대━利?鍮뚮뱶?먯꽌 ?쇱씤 ?뺣낫 異뺤빟 媛?μ꽦




