# Alarm Controller SPEC

## 문서 정보
- 문서명: Alarm Controller SPEC
- 작성일: 2026-01-30
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.system_manager.controller.alarm
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조
- 기본 규칙/환경은 루트 AGENTS.md에서 연결되는 *_RULE.md를 따른다.
- 상세 요구와 범위는 `PRD.md`를 따른다.
- 실제 구현은 `kr.open.library.simple_ui.core.system_manager.controller.alarm` 패키지 및 하위 패키지에서 완료되어 있다.

## 모듈 구조 및 책임
- `controller/alarm`
  - `AlarmController`: AlarmManager 래핑, 등록/갱신/제거/존재 확인, 권한 체크
  - `AlarmConstants`: 알람 공통 상수
  - `receiver/BaseAlarmReceiver`: 브로드캐스트 처리, 재등록, 알림 표시
  - `vo/AlarmVO` 및 하위 DTO: 스케줄/알림 데이터 모델

## 데이터 모델
### AlarmDateVO
- 목적: 특정 날짜 알람 스케줄 DTO
- 필드
  - `year: Int` (>= 1970)
  - `month: Int` (1~12)
  - `day: Int` (1~31)
- 검증
  - `require(year >= 1970)`
  - `require(month in 1..12)`
  - `require(day in 1..31)`
  - `Calendar.isLenient = false`로 실제 날짜 유효성 검증(예: 2026-02-31은 예외)

### AlarmScheduleVO
- 목적: 시간 기반 알람 스케줄 DTO
- 필드
  - `hour: Int` (0~23)
  - `minute: Int` (0~59)
  - `second: Int` (0~59, 기본 0)
  - `idleMode: AlarmIdleMode` (기본 NONE)
  - `date: AlarmDateVO?` (없으면 매일 기준, 있으면 특정 날짜)
- 검증
  - `require(hour in 0..23)`
  - `require(minute in 0..59)`
  - `require(second in 0..59)`

### AlarmIdleMode
- 목적: 유휴 모드 대응 전략 결정
- 값
  - `NONE` : 알람 시계(AlarmClock) 사용 **(API 31+ `SCHEDULE_EXACT_ALARM` 권한 필요)**
  - `INEXACT` : `setAndAllowWhileIdle` 사용(부정확, 권한 불필요)
  - `EXACT` : `setExactAndAllowWhileIdle` 사용(정확, **API 31+ `SCHEDULE_EXACT_ALARM` 권한 필요**)

### AlarmNotificationVO
- 목적: 알림 표시용 데이터 DTO
- 필드
  - `title: String` (공백 불가)
  - `message: String` (공백 불가)
  - `vibrationPattern: List<Long>?` (값은 0 이상, 비어 있으면 예외)
  - `soundUri: Uri?`

### AlarmVO
- 목적: 알람 도메인 객체(스케줄 + 알림 분리)
- 필드
  - `key: Int` (양수 필수)
  - `schedule: AlarmScheduleVO`
  - `notification: AlarmNotificationVO`
  - `isActive: Boolean` (기본 true)
  - `acquireTime: Long` (기본 `AlarmConstants.DEFAULT_ACQUIRE_TIME_MS`)
- 유틸
  - `withActiveState(active)` : 활성 상태만 변경
  - `withTime(hour, minute, second)` : 시간만 변경(기타 schedule 유지)
  - `getFormattedTime()` : HH:MM:SS
  - `getTotalSeconds()` : 자정 기준 누적 초
  - `getDescription()` : 로그용 요약 문자열
- 검증
  - `require(key > 0)`
  - `require(acquireTime > 0)`

### AlarmConstants
- 상수
  - `ALARM_KEY = "AlarmKey"`
  - `ALARM_KEY_DEFAULT_VALUE = -1`
  - `WAKELOCK_TAG = "SystemManager:AlarmReceiver"`
  - `WAKELOCK_TIMEOUT_MS = 10 * 60 * 1000L` (10분)
  - `DEFAULT_ACQUIRE_TIME_MS = 3000L` (3초)
  - `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED = "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"`

## API 설계
### AlarmController (공개 API)
```kotlin
public open class AlarmController(context: Context) : BaseSystemService {
    val alarmManager: AlarmManager

    @RequiresPermission(SCHEDULE_EXACT_ALARM, conditional = true)
    fun registerAlarmClock(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean

    @RequiresPermission(SCHEDULE_EXACT_ALARM, conditional = true)
    fun registerAlarmExactAndAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean

    fun registerAlarmAndAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean

    fun registerRepeating(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        intervalMillis: Long,
        namespace: String? = null,
        type: Int = AlarmManager.RTC_WAKEUP,
    ): Boolean

    fun registerBySchedule(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean

    fun updateAlarmClock(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean
    fun updateExactAndAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean
    fun updateAllowWhileIdle(receiver: Class<*>, alarmVo: AlarmVO, namespace: String? = null): Boolean
    fun updateRepeating(
        receiver: Class<*>,
        alarmVo: AlarmVO,
        intervalMillis: Long,
        namespace: String? = null,
        type: Int = AlarmManager.RTC_WAKEUP,
    ): Boolean

    fun remove(key: Int, receiver: Class<*>, namespace: String? = null): Boolean
    fun exists(key: Int, receiver: Class<*>, namespace: String? = null): Boolean

    fun canScheduleExactAlarms(): Boolean
    fun buildExactAlarmPermissionIntent(): Intent?
}
```

#### 공통 동작 규칙
- 모든 등록/갱신/제거/존재 확인 함수는 `tryCatchSystemManager`로 감싸져 있으며, 예외 발생 시 `false`를 반환한다.
- `namespace`는 requestCode 충돌 방지용이며, 같은 알람에 대해 **등록/삭제/조회 시 동일한 값**을 사용해야 한다.
- `AlarmConstants.ALARM_KEY`는 PendingIntent에 반드시 삽입된다.

#### registerAlarmClock
- API 31+에서 `canScheduleExactAlarms()`가 false면 false 반환 및 경고 로그 출력
- `getCalendar(alarmVo)`로 시간 계산
- PendingIntent 생성 실패 시 false 반환
- `AlarmManager.setAlarmClock()` 호출

#### registerAlarmExactAndAllowWhileIdle
- API 31+에서 권한 허용 여부 체크(거부 시 false)
- `AlarmManager.setExactAndAllowWhileIdle()` 호출

#### registerAlarmAndAllowWhileIdle
- 권한 체크 없음(부정확 알람)
- `AlarmManager.setAndAllowWhileIdle()` 호출

#### registerRepeating
- `intervalMillis`가 60,000ms 미만이면 보정(클램핑)
- `AlarmManager.setRepeating()` 사용(API 19+에서 inexact)

#### registerBySchedule
- `AlarmScheduleVO.idleMode` 기반으로 등록 API 선택
  - NONE -> registerAlarmClock
  - INEXACT -> registerAlarmAndAllowWhileIdle
  - EXACT -> registerAlarmExactAndAllowWhileIdle

#### update*
- 공통 동작: `exists()` 확인 후 `remove()` 수행, 이어서 재등록
- 내부적으로 `updateInternal` private inline 함수를 사용하여 remove → register 흐름을 통합

> **⚠️ 주의사항**: 재등록 실패 시 기존 알람은 이미 제거된 상태일 수 있다. 호출부에서 실패 반환값을 반드시 처리해야 한다.

#### remove/exists
- `PendingIntent.FLAG_NO_CREATE`로 기존 PendingIntent 확인
- 존재하면 `AlarmManager.cancel()` 및 `PendingIntent.cancel()` 호출

#### canScheduleExactAlarms
- API 31+에서 `AlarmManager.canScheduleExactAlarms()` 사용
- API 30 이하에서는 항상 true 반환

#### buildExactAlarmPermissionIntent
- API 31+이고 권한이 없을 때만 반환
- `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` + 패키지 URI

### 내부 알고리즘
#### requestCode 계산
- `effectiveNamespace = namespace?.takeIf { it.isNotBlank() } ?: receiver.name`
- `requestCode = (effectiveNamespace.hashCode() * 31) xor key`

#### getCalendar(alarmVo)
- `AlarmScheduleVO.date`가 **없으면**:
  - 오늘 시간이 지났으면 다음 날로 보정
- `AlarmScheduleVO.date`가 **있으면**:
  - 과거 시각인 경우 `require(!alarmTime.before(now))` 검증 실패로 `IllegalArgumentException` 발생 (메시지: `"Specified date/time is in the past: ${alarmTime.time}"`)

#### resolveRepeatingInterval(intervalMillis)
- `intervalMillis`가 `MIN_REPEATING_INTERVAL_MS`(60,000ms) 미만이면 60,000ms로 클램핑
- 클램핑 발생 시 Logx 경고 로그 출력
- `registerRepeating` 내부에서 호출

### BaseAlarmReceiver (공개/확장 API)
```kotlin
abstract class BaseAlarmReceiver : BroadcastReceiver() {
    protected lateinit var notificationController: SimpleNotificationController
    protected abstract val classType: Class<*>

    protected open fun resolveRegisterType(alarmVo: AlarmVO): RegisterType
    protected open fun resolveAlarmNamespace(alarmVo: AlarmVO): String?
    protected abstract fun createNotificationChannel(context: Context, notification: AlarmNotificationVO)
    protected abstract fun buildNotificationOption(context: Context, alarmVo: AlarmVO): SimpleNotificationOptionBase
    protected open fun resolveNotificationShowType(alarmVo: AlarmVO): SimpleNotificationType

    protected abstract fun loadAllAlarmVoList(context: Context): List<AlarmVO>
    protected abstract fun loadAlarmVoList(context: Context, intent: Intent, key: Int): AlarmVO?
    protected abstract val powerManagerAcquireTime: Long

    protected open fun onExactAlarmPermissionDenied(context: Context)
}
```

#### onReceive 처리 흐름
1. `context`/`intent` null 검사
2. WakeLock 획득 시도 (`minOf(powerManagerAcquireTime, WAKELOCK_TIMEOUT_MS)`)
3. `processAlarmIntent()` 수행
4. finally에서 WakeLock 해제(예외 발생 시에도 안전 처리)

#### processAlarmIntent 분기
- `ACTION_BOOT_COMPLETED`, `ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED` -> `handleReschedule()`
- `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` -> `handleExactAlarmPermissionChanged()`
- 그 외 -> `handleAlarmTrigger()`

#### handleReschedule
- `loadAllAlarmVoList()` 호출
- `isActive == true`인 알람만 재등록
- 재등록은 `resolveRegisterType()` 결과로 분기

#### handleExactAlarmPermissionChanged
- API 31+에서만 동작
- `canScheduleExactAlarms()`가 true면 재등록, false면 `onExactAlarmPermissionDenied()` 호출

#### handleAlarmTrigger
- intent에서 `ALARM_KEY` 추출
- 기본값(-1)이면 오류 로그 후 종료
- `loadAlarmVoList()`로 알람 로딩
- `createNotificationChannel()` 호출 후 `notificationController` 초기화 여부 확인
- `buildNotificationOption()`으로 옵션 구성
- `SimpleNotificationController.showNotification()` 호출

#### RegisterType
- `ALARM_CLOCK`
- `ALARM_AND_ALLOW_WHILE_IDLE`
- `ALARM_EXACT_AND_ALLOW_WHILE_IDLE`

### 권한/매니페스트 요구사항
- 정확 알람
  - API 31+에서 `SCHEDULE_EXACT_ALARM` 권한이 필요
  - 권한이 없으면 exact 계열 등록은 실패(false 반환)
- WakeLock
  - `android.permission.WAKE_LOCK` 권한이 없으면 SecurityException이 발생할 수 있으나, 코드에서 안전 처리됨
- 부팅/시간 변경 수신
  - `RECEIVE_BOOT_COMPLETED` 권한 필요
  - Receiver intent-filter 등록 필요

### 필수 브로드캐스트 액션
- `Intent.ACTION_BOOT_COMPLETED`
- `Intent.ACTION_TIME_CHANGED`
- `Intent.ACTION_TIMEZONE_CHANGED`
- `AlarmConstants.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`

### 알람 저장소 구현 요구
- `loadAllAlarmVoList()`는 **영속 저장소**(DB/파일/Preference) 기반으로 구현해야 한다.
- `loadAlarmVoList()`는 key 기반 단건 로드를 수행해야 한다.

## 오류 처리/로그 정책
- AlarmController: 예외 발생 시 false 반환, Logx로 경고/디버그 로그 출력
- BaseAlarmReceiver: 예외는 로그 후 안전 종료, WakeLock은 finally에서 해제
- 잘못된 key 수신 시 오류 로그 후 종료
- 과거 날짜 알람은 `require(!alarmTime.before(now))`로 즉시 실패 처리

## 테스트
- 단위 테스트
  - `AlarmVoUnitTest`: VO 유효성/팩토리/기본값 검증
- Robolectric 테스트
  - `AlarmControllerRobolectricTest`: 알람 등록/해제 동작 검증
