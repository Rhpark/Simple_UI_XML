# Logx PRD

## 臾몄꽌 ?뺣낫
- 臾몄꽌紐? Logx PRD
- ?묒꽦?? 2026-01-23
- ???紐⑤뱢: simple_core
- ?⑦궎吏: kr.open.library.simple_ui.core.logcat
- ?곹깭: 珥덉븞

## 諛곌꼍/臾몄젣 ?뺤쓽
- Android 湲곕낯 Log??tag/message 諛섎났 ?낅젰??留롪퀬, ?뚯씪/?쇱씤/硫붿꽌???ㅻ젅??媛숈? ?붾쾭源?硫뷀? ?뺣낫瑜?留ㅻ쾲 ?섎룞?쇰줈 ?뺤씤?댁빞 ?쒕떎.
- JSON 濡쒓렇瑜?蹂닿린 醫뗪쾶 異쒕젰?섍굅???몄텧 寃쎈줈瑜?異붿쟻?섎뒗 湲곕뒫???⑹뼱???덉뼱 ?쇨????댁쁺 濡쒓렇 愿由ш? ?대졄??

## 紐⑺몴
- 湲곕낯 Log ?ъ슜?깆쓣 ?좎??섎㈃???몄텧 ?꾩튂/?ㅻ젅??JSON ??異붽? ?뺣낫瑜??먮룞 ?쒓났?쒕떎.
- ?ㅼ젙/?꾪꽣/?뚯씪 ??μ쓣 ?⑥씪 吏꾩엯?먯쑝濡??듯빀?쒕떎.
- ?댁쁺 濡쒓렇(?뚯씪 ???源뚯? ?뺤옣 媛?ν븳 援ъ“濡??ㅺ퀎?쒕떎.

## 鍮꾨ぉ??- 濡쒓렇 ?낅줈???먭꺽 ?섏쭛/遺꾩꽍 ?쒖뒪???쒓났.
- UI 而댄룷?뚰듃/酉??섏〈 濡쒖쭅 ?꾩엯.
- 濡쒓렇 ?뚯쟾(?ш린/湲곌컙 ?쒗븳) ?뺤콉 ?뺤쓽.

## 踰붿쐞
### 湲곕낯 洹쒖튃
- 湲곕낯 媛쒕컻 ?섍꼍/洹쒖튃? 猷⑦듃 AGENTS.md?먯꽌 ?곌껐?섎뒗 *_RULE.md瑜??곕Ⅸ??

### 吏???몄뼱
- Kotlin, Java

### 鍮뚮뱶 ????뺤콉
- Release 鍮뚮뱶?먯꽌??濡쒓렇 異쒕젰 ?덉슜

## ?ㅺ퀎 ?먯튃
- SRP 湲곕컲 ?⑦궎吏 遺꾨━濡??좎?蹂댁닔?깃낵 ?뺤옣?깆쓣 ?뺣낫?쒕떎.
- ?꾩튂 ?뺣낫???ㅽ깮 ?몃젅?댁뒪 湲곕컲?쇰줈 怨꾩궛?쒕떎.
- ?깅뒫 ??섎? 以꾩씠湲??꾪빐 濡쒓렇 鍮꾪솢?????щ㎎?낆쓣 理쒖냼?뷀븳??
- ?뚯씪 ??μ? 利됱떆 flush 以묒떖?쇰줈 濡쒓렇 ?좎떎??理쒖냼?뷀븳??
- ?뚯씪 ??μ? 肄붾（??湲곕컲 諛깃렇?쇱슫??writer濡?泥섎━???몄텧 ?ㅻ젅??李⑤떒??理쒖냼?뷀븳??
- ???쇱씠?꾩궗?댄겢(ProcessLifecycleOwner) 湲곕컲?쇰줈 諛깃렇?쇱슫??吏꾩엯 ??writer瑜??뺣━?쒕떎.

## 紐⑤뱢/?⑦궎吏 援ъ“(珥덉븞)
- kr.open.library.simple_ui.core.logcat
  - root: Logx 공개 API
  - config: 설정 모델/기본값
  - internal:
    - common: 공통 유틸/상수/헬퍼
    - filter: 타입/태그 필터
    - formatter: 메시지 포맷터(메타/JSON/스레드)
    - extractor: 스택 트레이스 위치 추출
    - pipeline: LogxPipeline 오케스트레이션
    - writer: 콘솔/파일 라이터
  - extension: Kotlin 확장 함수

## ?듭떖 湲곕뒫
### ?쒖? 濡쒓렇
- v/d/i/w/e ?몄텧濡?湲곕낯 濡쒓렇 異쒕젰.
- ?몄텧 ?꾩튂 ?먮룞 遺李? (?뚯씪紐??쇱씤).硫붿꽌??- Throwable ?ㅻ쾭濡쒕뱶??1李?媛쒕컻 踰붿쐞?먯꽌 ?쒖쇅(異뷀썑 異붽? ?덉젙)

### ?뺤옣 濡쒓렇
- p(): 遺紐??몄텧 寃쎈줈 2?④퀎 異쒕젰 (Logcat?먯꽌 ??以꾨줈 異쒕젰)
- j(): JSON ?щ㎎ 異쒕젰(??긽 pretty, ?쒖옉/醫낅즺 留덉빱 ?ы븿)
- t(): ?ㅻ젅??ID 異쒕젰
- JSON Logcat 異쒕젰? ??踰덉쓽 Log ?몄텧濡?硫?곕씪??硫붿떆吏 ?뺥깭濡?異쒕젰?쒕떎.

### ?먮룞 硫뷀? ?뺣낫
- (?뚯씪紐??쇱씤).硫붿꽌???먮룞 遺李?
## ?곸꽭 ?ㅺ퀎 李멸퀬
- API ?쒓렇?덉쿂, 異쒕젰 ?щ㎎, ?ㅽ깮 異붿텧 洹쒖튃, JSON 泥섎━, Context 泥섎━, ?뚯뒪???꾨왂? `SPEC.md`瑜??곕Ⅸ??

## ?ъ슜踰??덉떆)
```
Logx.d() -> AppName : (MainActivity.kt:25).onCreate
Logx.d(msg) -> AppName : (MainActivity.kt:25).onCreate - msg
Logx.d(tag, msg) -> AppName[tag] : (MainActivity.kt:25).onCreate - msg

Logx.p()
AppName : ??PARENT] (AppCompatActivity.kt:25).onCreate
AppName : ??PARENT] (MainActivity.kt:25).onCreate

Logx.p(msg)
AppName : ??PARENT] (AppCompatActivity.kt:25).onCreate
AppName : ??PARENT] (MainActivity.kt:25).onCreate - msg

Logx.p(tag, msg)
AppName[tag] : ??PARENT] (AppCompatActivity.kt:25).onCreate
AppName[tag] : ??PARENT] (MainActivity.kt:25).onCreate - msg

Logx.j("{ \"key\": \"value\" }")
AppName : [JSON](MainActivity.kt:25).onCreate -
{
    "key": "value"
}
[End]

Logx.j(tag, "{ \"key\": \"value\" }")
AppName[tag] : [JSON](MainActivity.kt:25).onCreate -
{
    "key": "value"
}
[End]

Logx.t() -> AppName : [TID = 1231](MainActivity.kt:25).onCreate
Logx.t(msg) -> AppName : [TID = 1231](MainActivity.kt:25).onCreate - msg
Logx.t(tag, msg) -> AppName[tag] : [TID = 1231](MainActivity.kt:25).onCreate - msg
```

### Kotlin ?뺤옣 ?⑥닔(?덉떆)
```
// ?쒖? 濡쒓렇
"AFEF".logv() -> AppName : (MainActivity.kt:25).onCreate - AFEF
"AFEF".logv(tag) -> AppName[tag] : (MainActivity.kt:25).onCreate - AFEF
"AFEF".logd() -> AppName : (MainActivity.kt:25).onCreate - AFEF
"AFEF".logd(tag) -> AppName[tag] : (MainActivity.kt:25).onCreate - AFEF
"AFEF".logi() -> AppName : (MainActivity.kt:25).onCreate - AFEF
"AFEF".logi(tag) -> AppName[tag] : (MainActivity.kt:25).onCreate - AFEF
"AFEF".logw() -> AppName : (MainActivity.kt:25).onCreate - AFEF
"AFEF".logw(tag) -> AppName[tag] : (MainActivity.kt:25).onCreate - AFEF
"AFEF".loge() -> AppName : (MainActivity.kt:25).onCreate - AFEF
"AFEF".loge(tag) -> AppName[tag] : (MainActivity.kt:25).onCreate - AFEF

// ?뺤옣 濡쒓렇
"AFEF".logp() ->
AppName : ??PARENT] (AppCompatActivity.kt:25).onCreate
AppName : ??PARENT] (MainActivity.kt:25).onCreate - AFEF
"AFEF".logp(tag) ->
AppName[tag] : ??PARENT] (AppCompatActivity.kt:25).onCreate
AppName[tag] : ??PARENT] (MainActivity.kt:25).onCreate - AFEF
"AFEF".logt() -> AppName : [TID = 1231](MainActivity.kt:25).onCreate - AFEF
"AFEF".logt(tag) -> AppName[tag] : [TID = 1231](MainActivity.kt:25).onCreate - AFEF
"{\"key\":\"value\"}".logj() -> JSON ?щ㎎ 異쒕젰
"{\"key\":\"value\"}".logj(tag) -> JSON ?щ㎎ 異쒕젰(tag ?ы븿)
```

## ?ㅼ젙 ?쒖뒪??- setter/getter 湲곕컲 ?ㅼ젙 ?쒓났
- ?꾩옱 ?ㅼ젙 議고쉶 getter ?쒓났
- 濡쒓렇 ?꾩껜 on/off(異쒕젰/???
- ????꾪꽣(LogType: v/d/i/w/e/p/j/t)
- ?쒓렇 李⑤떒 ?꾪꽣(BLOCKLIST) (isLogTagBlockListEnabled=true???뚮쭔 ?곸슜)
- ???on/off
- ???寃쎈줈/?ㅽ넗由ъ? ????쒖뼱
- AppName 蹂寃?吏??
## ?뚯씪 ???- INTERNAL / APP_EXTERNAL / PUBLIC_EXTERNAL ???寃쎈줈 ?좏깮
- 寃쎈줈 ?좏떥 ?쒓났 諛?沅뚰븳 ?꾩슂 ?щ? 泥댄겕
- StorageType 寃쎈줈 洹쒖튃(?대?/?깆쇅遺/怨듭슜?몃?, API 遺꾧린/?대갚) 紐낆떆
- ???寃쎈줈 ?붾젆?곕━ ?먮룞 ?앹꽦, ?ㅽ뙣 ??留??쒕룄 Log.e 寃쎄퀬
- PUBLIC_EXTERNAL + API 28 이하에서 권한이 없으면 SecurityException을 던진다.
- 利됱떆 flush 以묒떖(濡쒓렇 ?좎떎 理쒖냼???곗꽑)
- 利됱떆 flush?대?濡?蹂꾨룄 ?몃━嫄?遺덊븘??(異뷀썑 踰꾪띁留??꾪솚 ???곸슜)
- ?뚯씪 ??μ? Application initialize(context) ?댄썑?먮쭔 ?덉슜
- 硫?곕씪??濡쒓렇 湲곕줉 ?뺤콉
  - JSON: 泥?以꾩뿉留?timestamp/prefix 湲곕줉, 蹂몃Ц/End???먮Ц ?쇱씤 洹몃?濡?湲곕줉
  - PARENT: 紐⑤뱺 以꾩뿉 timestamp/prefix 湲곕줉
- ?뚯씪 ?곌린 諛섎났 ?ㅽ뙣 ??留ㅻ쾲 ?먮윭 濡쒓렇瑜?肄섏넄??異쒕젰

## ?쒖빟/?꾩젣
- ?꾩튂 ?뺣낫???ㅽ깮 ?몃젅?댁뒪 湲곕컲 異붿텧
- skipPackages??packageName prefix 湲곗??쇰줈 留ㅼ묶
- ?뚯씪 ??μ? 利됱떆 flush 以묒떖
- 濡쒓렇 硫붿떆吏 湲몄씠/硫?곕씪??泥섎━??Log.*? ?숈씪
- ?뚯씪 ???湲곕뒫? initialize(context) ?댄썑?먮쭔 ?덉슜?섎ŉ, 誘명샇異??곹깭?먯꽌 setSaveEnabled(true) ???덉쇅媛 諛쒖깮?쒕떎.

## 由ъ뒪??- ?ㅽ깮 ?몃젅?댁뒪 源딆씠 蹂?붾줈 ?꾩튂 異붿텧 ?ㅽ뙣 媛?μ꽦
- ?몃씪??肄붾（??由ы뵆?됱뀡 ?꾨젅???곹뼢?쇰줈 ?꾩튂 異붿텧 ?붾뱾由?媛?μ꽦
- ?뚯씪 I/O 鍮덈쾲 ???깅뒫 ???媛?μ꽦
- Context ?녿뒗 ?섍꼍?먯꽌 ?뚯씪 寃쎈줈 ?대갚???ㅽ슚???쒗븳
- Logcat ?⑥씪 異쒕젰 湲몄씠 ?쒗븳?쇰줈 湲?JSON? 肄섏넄?먯꽌 ?섎┫ ???덉쓬(?뚯씪?먮뒗 ?꾩껜 ???

## ?ㅽ뵂 ?댁뒋
- 濡쒓렇 ?뚯쟾(?ш린/湲곌컙 ?쒗븳) ?뺤콉 遺??- 由대━利?鍮뚮뱶?먯꽌 ?쇱씤 ?뺣낫 異뺤빟 媛?μ꽦



