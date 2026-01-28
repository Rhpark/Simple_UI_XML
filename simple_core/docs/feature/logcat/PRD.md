# Logx PRD

## 문서 정보
- 문서명: Logx PRD
- 작성일: 2026-01-23
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.logcat
- 상태: 초안

## 배경/문제 정의
- Android 기본 Log는 tag/message 반복 입력이 많고, 파일/라인/메서드/스레드 같은 디버깅 메타 정보를 매번 수동으로 확인해야 한다.
- JSON 로그를 보기 좋게 출력하거나 호출 경로를 추적하는 기능이 흩어져 있어 일관된 운영 로그 관리가 어렵다.

## 목표
- 기본 Log 사용성을 유지하면서 호출 위치/스레드/JSON 등 추가 정보를 자동 제공한다.
- 설정/필터/파일 저장을 단일 진입점으로 통합한다.
- 운영 로그(파일 저장)까지 확장 가능한 구조로 설계한다.

## 비목표
- 로그 업로드/원격 수집/분석 시스템 제공.
- UI 컴포넌트/뷰 의존 로직 도입.
- 로그 기간 제한 정책 정의.

## 범위
### 기본 규칙
- 기본 개발 환경/규칙은 루트 AGENTS.md에서 연결되는 *_RULE.md를 따른다.

### 지원 언어
- Kotlin, Java

### 빌드 타입 정책
- Release 빌드에서도 로그 출력 허용

## 설계 원칙
- SRP 기반 패키지 분리로 유지보수성과 확장성을 확보한다.
- 위치 정보는 스택 트레이스 기반으로 계산한다.
- 성능 저하를 줄이기 위해 로그 비활성 시 포맷팅을 최소화한다.
- 파일 저장은 즉시 flush 중심으로 로그 유실을 최소화한다.
- 파일 저장은 코루틴 기반 백그라운드 writer로 처리해 호출 스레드 차단을 최소화한다.
- 앱 라이프사이클(ProcessLifecycleOwner) 기반으로 백그라운드 진입 시 writer를 정리한다.

## 모듈/패키지 구조(초안)
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

## 핵심 기능
### 표준 로그
- v/d/i/w/e 호출로 기본 로그 출력.
- 호출 위치 자동 부착: (파일명:라인).메서드
- Throwable 오버로드는 개발 범위에서 제외

### 확장 로그
- p(): 부모 호출 경로 2단계 출력 (Logcat에서 두 줄로 출력)
- j(): JSON 포맷 출력(항상 pretty, 시작/종료 마커 포함)
- t(): 스레드 ID 출력
- JSON Logcat 출력은 한 번의 Log 호출로 멀티라인 메시지 형태로 출력한다.

### 자동 메타 정보
- (파일명:라인).메서드 자동 부착

## 상세 설계 참고
- API 시그니처, 출력 포맷, 스택 추출 규칙, JSON 처리, Context 처리, 테스트 전략은 `SPEC.md`를 따른다.

## 사용법(예시)
```
Logx.d() -> AppName : (MainActivity.kt:25).onCreate
Logx.d(msg) -> AppName : (MainActivity.kt:25).onCreate - msg
Logx.d(tag, msg) -> AppName[tag] : (MainActivity.kt:25).onCreate - msg

Logx.p()
AppName : ┌[PARENT] (AppCompatActivity.kt:25).onCreate
AppName : └[PARENT] (MainActivity.kt:25).onCreate

Logx.p(msg)
AppName : ┌[PARENT] (AppCompatActivity.kt:25).onCreate
AppName : └[PARENT] (MainActivity.kt:25).onCreate - msg

Logx.p(tag, msg)
AppName[tag] : ┌[PARENT] (AppCompatActivity.kt:25).onCreate
AppName[tag] : └[PARENT] (MainActivity.kt:25).onCreate - msg

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

### Kotlin 확장 함수(예시)
```
// 표준 로그
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

// 확장 로그
"AFEF".logp() ->
AppName : ┌[PARENT] (AppCompatActivity.kt:25).onCreate
AppName : └[PARENT] (MainActivity.kt:25).onCreate - AFEF
"AFEF".logp(tag) ->
AppName[tag] : ┌[PARENT] (AppCompatActivity.kt:25).onCreate
AppName[tag] : └[PARENT] (MainActivity.kt:25).onCreate - AFEF
"AFEF".logt() -> AppName : [TID = 1231](MainActivity.kt:25).onCreate - AFEF
"AFEF".logt(tag) -> AppName[tag] : [TID = 1231](MainActivity.kt:25).onCreate - AFEF
"{\"key\":\"value\"}".logj() -> JSON 포맷 출력
"{\"key\":\"value\"}".logj(tag) -> JSON 포맷 출력(tag 포함)
```

## 설정 시스템
- setter/getter 기반 설정 제공
- 현재 설정 조회 getter 제공
- 로그 전체 on/off(출력/저장)
- 타입 필터(LogType: v/d/i/w/e/p/j/t)
- 태그 차단 필터(BLOCKLIST) (isLogTagBlockListEnabled=true일 때만 적용)
- 저장 on/off
- 저장 경로/스토리지 타입 제어
- AppName 변경 지원

## 파일 저장
- INTERNAL / APP_EXTERNAL / PUBLIC_EXTERNAL 저장 경로 선택
- 경로 유틸 제공 및 권한 필요 여부 체크
- StorageType 경로 규칙(내부/앱외부/공용외부, API 분기/폴백) 명시
- 저장 경로 디렉터리 자동 생성, 실패 시 매 시도 Log.e 경고
- PUBLIC_EXTERNAL + API 28 이하에서 권한이 없으면 디버그는 예외, 릴리즈는 Log.e 경고 후 저장 중단
- 즉시 flush 중심(로그 유실 최소화 우선)
- 즉시 flush이므로 별도 트리거 불필요 (추후 버퍼링 전환 시 적용)
- 파일 로테이션 정책
  - 파일 크기 10MB 초과 시 `_1`, `_2` 카운트 파일로 분리
  - 파일 개수 제한 없음
- 파일 저장은 Application initialize(context) 이후에만 허용
- 멀티라인 로그 기록 정책
  - JSON: 첫 줄에만 timestamp/prefix 기록, 본문/End는 원문 라인 그대로 기록
  - PARENT: 모든 줄에 timestamp/prefix 기록
- 파일 쓰기 반복 실패 시 매번 에러 로그를 콘솔에 출력

## 제약/전제
- 위치 정보는 스택 트레이스 기반 추출
- skipPackages는 packageName prefix 기준으로 매칭
- 파일 저장은 즉시 flush 중심
- 로그 메시지 길이/멀티라인 처리는 Log.*와 동일
- 파일 저장 기능은 initialize(context) 이후에만 허용하며, 미호출 상태에서 setSaveEnabled(true) 시 디버그는 예외, 릴리즈는 Log.e로 처리한다.

## 리스크
- 스택 트레이스 깊이 변화로 위치 추출 실패 가능성
- 인라인/코루틴/리플렉션 프레임 영향으로 위치 추출 흔들림 가능성
- 파일 I/O 빈번 시 성능 저하 가능성
- Context 없는 환경에서 파일 경로 폴백의 실효성 제한
- Logcat 단일 출력 길이 제한으로 긴 JSON은 콘솔에서 잘릴 수 있음(파일에는 전체 저장)

## 오픈 이슈
- 로그 기간 제한 정책 부재
- 릴리즈 빌드에서 라인 정보 축약 가능성
