# Vibrator Controller SPEC

## 문서 정보

- 문서명: Vibrator Controller SPEC
- 작성일: 2026-01-30
- 수정일: 2026-01-31
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.controller.vibrator
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조

- 기본 규칙/환경은 루트 AGENTS.md에서 연결되는 *_RULE.md를 따른다.
- 상세 요구와 범위는 `PRD.md`를 따른다.
- 실제 구현은 `kr.open.library.simple_ui.core.system_manager.controller.vibrator` 패키지에서 완료되어 있다.

## 모듈 구조 및 책임

- `controller/vibrator`
  - `VibratorController`: Vibrator/VibratorManager 분기, 입력 검증, 진동 실행/취소
- `system_manager/extensions`
  - `getVibrator()`: 레거시 Vibrator 접근
  - `getVibratorManager()`: SDK 31+ VibratorManager 접근 (`@RequiresApi(S)`)
  - `getVibratorController()`: 컨트롤러 생성
- `system_manager/base`
  - `BaseSystemService`: 권한 상태 캐시, 매니페스트 미선언 경고, 공통 예외 처리

## API 설계

```kotlin
public open class VibratorController(
    context: Context,
) : BaseSystemService(context, listOf(VIBRATE)) {

    // === 실행 API (매니페스트 선언 필요, @RequiresPermission(VIBRATE), tryCatchSystemManager) ===
    fun createOneShot(timer: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE): Boolean
    fun vibrate(milliseconds: Long): Boolean  // createOneShot()에 위임

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createPredefined(vibrationEffectClick: Int): Boolean

    fun createWaveform(times: LongArray, amplitudes: IntArray, repeat: Int = -1): Boolean
    fun vibratePattern(pattern: LongArray, repeat: Int = -1): Boolean

    fun cancel(): Boolean

    // === 조회 API (권한 불필요, safeCatch) ===
    fun hasVibrator(): Boolean
    fun hasAmplitudeControl(): Boolean
}
```

## 공통 동작 규칙

### 실행 API 래핑 정책

- `createOneShot`, `createPredefined`, `createWaveform`, `vibratePattern`, `cancel`은 `tryCatchSystemManager(false)`로 감싼다.
- 권한 미부여 시 false 반환 + 경고 로그
- 예외 발생 시 false 반환 + 에러 로그
- 입력 검증 실패 시 false 반환 + Logx.e 로그
  - VIBRATE는 일반 권한이므로 런타임 거부 시나리오는 거의 발생하지 않으며, 매니페스트 미선언 경고가 주요 안전장치

### vibrate() 위임 구조

- `vibrate(milliseconds)`는 자체 입력 검증(`milliseconds > 0`) 후 `createOneShot(milliseconds)`에 위임한다.
- `tryCatchSystemManager`를 직접 사용하지 않으며, `createOneShot` 내부에서 간접 보호된다.

### 조회 API 래핑 정책

- `hasVibrator`, `hasAmplitudeControl`은 `safeCatch(false)`로 감싼다.
- 권한 검증 없이 예외만 처리 (하드웨어 정보 조회이므로 권한 불필요)

### 어노테이션 정책

- **`@RequiresPermission(VIBRATE)` 부착 대상**: `createOneShot`, `vibrate`, `createPredefined`, `createWaveform`, `vibratePattern`, `cancel`
- **어노테이션 없음**: `hasVibrator`, `hasAmplitudeControl`
- **`@RequiresApi(Q)` 부착 대상**: `createPredefined`

## 입력 검증 규칙

### createOneShot

- `timer > 0`
- `amplitude`는 `DEFAULT_AMPLITUDE(-1)` 또는 `1..255`

### createWaveform

- `times`/`amplitudes`는 비어 있으면 안 됨
- 길이가 동일해야 함
- `times`는 모두 0 이상
- `amplitudes`는 `DEFAULT_AMPLITUDE(-1)` 또는 `0..255`
- `repeat`는 `-1` 또는 유효 인덱스
- `repeat`가 0 이상이면 `cancel()` 호출 전까지 반복

### vibratePattern

- `pattern` 비어있음 금지
- `pattern` 값은 모두 0 이상
- `repeat`는 `-1` 또는 유효 인덱스
- `repeat`가 0 이상이면 `cancel()` 호출 전까지 반복

## SDK 분기 규칙

- SDK 31+(S): `VibratorManager` + `CombinedVibration.createParallel()` 사용
- SDK 30 이하: `Vibrator` 사용
- `createPredefined()`: `@RequiresApi(Q)` + 내부 `checkSdkVersion(Q)` 이중 보호. pre-Q에서는 한글 로그 출력 후 false 반환
- `vibratorManager` 프로퍼티: `by lazy`로 SDK S 이상에서만 초기화. SDK < S에서 접근 시 `throwMinSdkVersion` 발생 (모든 메서드가 `checkSdkVersion(S)`로 분기하므로 실제 접근되지 않음)

## 권한/매니페스트 요구사항

- 필요 권한: `android.permission.VIBRATE`
- `BaseSystemService` 초기화에서 매니페스트 미선언 권한 경고 로그 출력
- VIBRATE는 일반 권한(normal permission)이므로 런타임 사용자 승인 절차 없음
- 일반 권한 특성상 `tryCatchSystemManager`의 권한 차단은 사실상 동작하지 않으며, 매니페스트 경고가 주요 안전장치

## 오류 처리/로그 정책

- 예외: false 반환 및 Logx 자동 로그
- 입력 오류: Logx.e로 상세 로그 출력 (어떤 파라미터가 어떤 값으로 잘못되었는지 명시)
- 매니페스트 미선언: BaseSystemService에서 Logx.w 경고 로그 출력
- 권한 미부여: tryCatchSystemManager에서 Logx.w 경고 로그 출력

## 테스트

- `VibratorControllerRobolectricTest`
  - SDK P/Q/S 분기 검증 (레거시 Vibrator vs VibratorManager)
  - 입력 검증 (timer, amplitude, 배열 길이/값/repeat 범위)
  - 예외 처리 (시스템 서비스 예외 시 false 반환)
  - 권한 미부여 처리 (false 반환)
  - `hasAmplitudeControl()` SDK 분기 및 권한 무관 동작 검증
  - `hasVibrator()` SDK 분기 및 권한 무관 동작 검증
  - `createPredefined()` pre-Q 안전 반환 검증
  - Robolectric 환경에서 권한 보호 수준을 위험 권한으로 가정하여 권한 미부여 시나리오를 검증
