# Logx Implementation Plan

## 문서 정보
- 문서명: Logx Implementation Plan
- 작성일: 2026-01-23
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.logcat
- 상태: Draft

## 목표
- SPEC 기반 구현 순서와 범위를 명확히 한다.
- 단위 테스트 + Robolectric 테스트를 포함한다.
- isLogging/isSave/Context 부재 등 예외 흐름을 안전하게 처리한다.
- 실제 코드 구현은 `kr.open.library.simple_ui.core.logcat` 패키지 내에서만 진행한다.

## 구현 범위
- core(Android 기본 의존 포함: Log, 파일 I/O)
  - LogType/StorageType 등 모델/상수 정의
  - Config 모델 + setter/getter + 동시성 정책
  - LogType ALLOWLIST 필터, tag BLOCKLIST 필터
  - StackTrace 추출기 (skipPackages prefix 매칭)
  - Formatter: 기본/Parent/Thread/JSON
  - Pipeline: LogxPipeline (오케스트레이션)
  - FileLineBuilder: 파일 라인 포맷 전담
  - Writer: 콘솔/파일
  - 경로 계산 및 파일명 정책
  - 초기화/Context 처리 + 파일 저장 제어
  - Kotlin 확장 함수

## 援ы쁽 ?쒖꽌
1. 紐⑤뜽/?곸닔 ?뺤쓽
   - LogType(VERBOSE/DEBUG/INFO/WARN/ERROR/PARENT/JSON/THREAD)
   - StorageType(INTERNAL/APP_EXTERNAL/PUBLIC_EXTERNAL)
   - LogType 매핑표(v/d/i/w/e/p/j/t) 및 {type} 1글자 출력 규칙
2. Config 설계/관리
   - 기본값(기본 허용 타입, isLogging/isSave 등)
  - setter/getter (unmodifiable Set 반환)
  - 동시성: @Volatile + Copy-on-Write (컬렉션은 unmodifiable Set 반환)
   - 유효성 검사: logTagBlockList 빈 값 제거 + Log.e 1회 출력
   - setAppName 우선, initialize는 appName을 덮어쓰지 않음
   - initialize(context) 보관 + setSaveDirectory 우선순위
   - initialize 이전 setSaveDirectory 호출 시 경로 값만 보관하고, 디렉터리 생성/파일 저장은 initialize 이후에만 수행
  - initialize 미호출 상태에서 setSaveEnabled(true) 호출 시 디버그 예외/릴리즈 Log.e 처리
3. 필터 모듈
   - isLogging 검사
   - logTypes ALLOWLIST 검사 (PARENT/JSON/THREAD는 DEBUG와 독립)
   - isLogTagBlockListEnabled + logTagBlockList 적용
   - isLogTagBlockListEnabled=false이면 logTagBlockList 무시
4. StackTrace 추출기
   - Thread.currentThread().stackTrace 기반
  - logcat 내부 prefix 마지막 프레임 다음 인덱스부터 순차 탐색(리스트 생성 없음)
   - 내부 prefix가 없거나 범위를 벗어나면 fallbackStartIndex = 4부터 탐색
   - skipPackages prefix 매칭으로 내부 프레임 제거
   - 현재/부모 프레임 계산
   - 기본 skipPackages 목록:
   - kr.open.library.simple_ui.core.logcat
   - java. / kotlin. / kotlinx.coroutines. / kotlin.coroutines
   - android.util. / android.os. / dalvik.system.
   - addSkipPackages로 확장 가능
   - access$ 메서드는 스킵, 람다 프레임은 file/line 유효하면 허용
5. Formatter
   - 기본 포맷: prefix + meta + msg
   - p(): 두 줄 포맷(부모/현재)
   - t(): TID 포맷
  - j(): JSON 포맷(수동 pretty-print, indent 4, 실패 시 원문)
6. Pipeline / FileLineBuilder
   - LogxPipeline이 필터/스택/포맷/출력/파일저장을 오케스트레이션
   - LogxFileLineBuilder가 파일 라인 포맷 생성 담당
7. Writer
   - 콘솔: LogType -> android.util.Log 매핑 (p/j/t는 Log.d() 사용)
   - 콘솔 JSON은 한 번의 Log 호출로 멀티라인 메시지를 출력
   - 파일: 생성/쓰기 시점에만 synchronized 적용, 즉시 flush
   - 파일 쓰기는 코루틴(Dispatchers.IO) 단일 소비자 큐로 비동기 처리
   - 멀티라인 파일 기록: p()는 모든 줄에 timestamp, j()는 첫 줄만 timestamp
  - 파일명: `{appName}_yyyy_MM_dd__HH-mm-ss-SSS.txt`
  - 로테이션: 파일 크기 ≥ 10MB이면 `{appName}_yyyy_MM_dd__HH-mm-ss-SSS_{count}.txt`로 분리
   - 파일 재사용: 저장 경로/storageType/appName 변경이 없을 때만 동일 파일 유지
   - 저장 경로/storageType/appName 변경 시 다음 로그에서 새 파일 생성
8. 파일 저장 제어
   - initialize 이후에만 저장 허용
  - Context 미주입 상태에서 setSaveEnabled(true) 호출 시 디버그 예외/릴리즈 Log.e
   - setSaveDirectory 우선, 없으면 storageType 경로
   - 저장 경로 디렉터리 자동 생성, 실패 시 Log.e 매 시도 경고 후 저장 중단
   - PUBLIC_EXTERNAL + API 28 이하에서 권한이 없으면 저장 중단 + Log.e 매 시도 경고
   - isSave false -> true 전환 시 파일 생성/재사용 규칙 적용
   - isLogging=false이면 저장 중단
   - isSave=false이면 저장 중단
   - 플러시 트리거: BufferedWriter 유지 + 즉시 flush (추후 버퍼링 전환 시 적용)
   - ProcessLifecycleOwner로 백그라운드 전환 시 writer close (다음 로그 시 재오픈)
9. Logx 공개 API
   - v/d/i/w/e: (무인자, msg, tag+msg)
   - p/j/t: (무인자, msg, tag+msg)
   - tag null/빈 값 처리 (길이 제한 없이 원문 그대로 출력)
   - msg null이면 문자열 "null"로 처리
   - Throwable 오버로드는 1차 개발 범위에서 제외(추후 추가 예정)
10. Kotlin 확장 함수
   - Any.logv/logd/logi/logw/loge/logp/logt 패턴 구현
   - String.logj 패턴 구현(JSON 전용)
11. 테스트
   - 단위 테스트 + Robolectric 테스트 작성

## 실패 처리 정책
- 잘못된 tag 입력: Log.e 1회 출력 후 tag 무시
- 내부 경고/에러 Log.e 태그: 입력 tag가 있으면 해당 tag, 없으면 ERROR
- Context 미주입 + 저장 활성화: 디버그는 예외, 릴리즈는 콘솔 경고 1회 후 저장 중단
- JSON 파싱 실패: 원문 출력
- 파일 쓰기 실패: 예외 삼키고 콘솔 에러 로그 출력(매번)
- PUBLIC_EXTERNAL + API 28 이하: 권한 미보유 시 디버그는 예외, 릴리즈는 저장 중단 + Log.e 매 시도 경고

## 테스트 범위
### 단위 테스트
- LogType 매핑/출력 규칙
- Config 기본값/Setter/Getter (unmodifiable Set 반환)
- logTagBlockList 빈 값 제거 및 Log.e 처리
- LogType 필터 규칙(ALLOWLIST)
- StackTrace 추출(현재/부모)
- p() 부모 프레임 없는 경우 포맷 검증
- Formatter(기본/Parent/Thread/JSON)
- msg null일 때 "null" 문자열 처리 검증
- skipPackages 확장 반영
- isLogging=false에서 콘솔/파일 차단

### Robolectric 테스트
- storageType 경로 계산
- 파일 생성/재사용 정책
- setSaveDirectory 우선순위
- initialize 이전 setSaveDirectory 동작
- Context 미주입 저장 차단
- isSave false -> true 전환 시 파일 생성/재사용
- 파일 쓰기 반복 실패 시 매번 에러 로그 출력 검증
- PUBLIC_EXTERNAL 권한 미보유 시 저장 중단/경고 동작

## 테스트 파일 위치 가이드
- 단위 테스트:
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/unit/logcat/...`
- Robolectric 테스트:
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/logcat/...`

## 테스트 방법(수동 시나리오)
- isLogging=false에서 콘솔/파일 모두 출력되지 않는지 확인
- isSave=false에서 파일 미생성 확인
- setSaveDirectory 지정 후 storageType 변경 시에도 지정 경로 사용 확인
- p/j/t LogType 필터 개별 on/off 확인
- 빈 tag 입력 시 Log.e 출력 + tag 무시 확인

## 리스크/체크리스트
- skipPackages 기본 목록 적절성 및 확장성
- 스택 프레임 추출 실패 시 포맷 안정성
- 파일 I/O 잦은 호출로 인한 성능 저하
- Application initialize 타이밍 누락 위험
- Logcat 단일 출력 길이 제한으로 긴 JSON 일부가 잘릴 수 있음
