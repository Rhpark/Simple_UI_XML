<!-- 파일 목적: a STEP1(기능 검증)에서 Android 플랫폼 추가 체크 항목 -->

## 실행 체크리스트

- [ ] A1. `[분석 입력] 직접 파일`의 import 구문을 위에서부터 훑어 **Android 프레임워크 API 사용 여부**를 분류한다. 아래 카테고리 중 하나라도 해당하면 그 카테고리명을 `[Android 호환성] 감지 카테고리`에 기록한다.
  - 알림: `NotificationManager`, `NotificationChannel`, `NotificationCompat`
  - 저장소/파일: `MediaStore`, `Storage Access Framework`, `Environment.getExternalStorage*`, `FileProvider`, `ContentResolver`
  - 권한: `ContextCompat.checkSelfPermission`, `ActivityCompat.requestPermissions`, `registerForActivityResult(...RequestPermission)`
  - 백그라운드 실행: `Service`, `ForegroundService`, `WorkManager`, `JobScheduler`, `AlarmManager`, `BroadcastReceiver`
  - UI/창: `WindowInsets`, `WindowInsetsController`, `EdgeToEdge`, `DisplayCutout`, `WindowCompat`
  - 위치/센서: `LocationManager`, `FusedLocationProviderClient`, `SensorManager`, `BluetoothAdapter`(BLE 권한 동반)
  - 카메라/미디어: `CameraX`, `Camera2`, `MediaRecorder`, `MediaProjection`
  - 보안/암호화: `KeyStore`, `BiometricPrompt`, `EncryptedSharedPreferences`
  - 시스템 인텐트: `Intent.ACTION_*`(특히 `OPEN_DOCUMENT`, `CREATE_DOCUMENT`, `VIEW`, `SEND`)
  - 패키지 가시성: `queries` 매니페스트 필요 API(`PackageManager.queryIntentActivities` 등)
  → 어떤 카테고리에도 해당하는 import가 없으면 `[Android 호환성] 감지 카테고리`에 "해당 없음 — Android 프레임워크 API 미사용"으로 기록하고 A2~A8을 건너뛴다.

- [ ] A2. 감지된 각 API별 **공식 최소 SDK 요건**과 `[분석 입력] 영향 계층`에 기록된 프로젝트 `minSdk`를 대조한다.
  - 프로젝트 `minSdk`가 산출물에 없으면 `app/build.gradle(.kts)` 또는 `build.gradle.kts` 모듈 파일을 직접 열어 `minSdk` / `minSdkVersion` 값을 확인하고 `[Android 호환성] minSdk`에 기록한다.
  - 대표 기준선: Notification Channel(API 26+), `requestPermissions` 런타임 권한(API 23+), `POST_NOTIFICATIONS`(API 33+), Scoped Storage(API 29+, 30 강제), `ForegroundServiceType`(API 29+, 34부터 타입 필수), `READ_MEDIA_*`(API 33+), `BluetoothScan/Connect`(API 31+), `queries` 패키지 가시성(API 30+), `BackgroundLocation`(API 29+), `EdgeToEdge` 의무화(API 35+).
  - `minSdk`보다 높은 API 레벨이 필요한 호출이 `Build.VERSION.SDK_INT >= ...` 분기 없이 사용되면 finding으로 기록한다.
  → 정상: `[Android 호환성] SDK 대조`에 "감지 API — 최소 요건 — minSdk 충족 여부"를 한 줄씩 기록.
  → 위반: 동일 위치에 "API명 / 요건 / 실제 minSdk / 위반 라인" 기록 + 등급은 `REVIEW_STEP1_SEVERITY.md` 적용.

- [ ] A3. **런타임 권한이 필요한 API**가 사용된 경우, 호출 직전에 권한 확인이 있는지 확인한다.
  - `ContextCompat.checkSelfPermission(...)` 또는 `shouldShowRequestPermissionRationale(...)` 호출이 같은 흐름에 존재하는가.
  - `AndroidManifest.xml`에 해당 `<uses-permission>` 선언이 있는가.
  - `POST_NOTIFICATIONS`(33+), `READ_MEDIA_IMAGES/VIDEO/AUDIO`(33+), `BLUETOOTH_SCAN/CONNECT/ADVERTISE`(31+), `ACCESS_BACKGROUND_LOCATION`(29+), `FOREGROUND_SERVICE_*`(34+) 등 신규 권한군이 누락되지 않았는지 본다.
  → 권한 누락/매니페스트 누락: 해당 라인을 finding으로 기록한다(`REVIEW_STEP1_SEVERITY.md`).
  → 정상: `[Android 호환성] 권한 확인`에 "감지 권한 — 매니페스트 OK / 런타임 체크 OK"로 기록.

- [ ] A4. **Foreground Service** 사용 시 `foregroundServiceType` 매니페스트 속성과 `startForeground(notificationId, notification, type)` 매개변수가 minSdk 정책에 부합하는지 확인한다(API 29+ 타입 권장, API 34+ 타입 필수).
  → 누락: finding 등록.
  → 미사용: `[Android 호환성] Foreground Service`에 "해당 없음".

- [ ] A5. **저장소 접근**(파일 read/write, 미디어 read)에서 Scoped Storage 규칙 준수 여부를 확인한다.
  - API 29+에서 `Environment.getExternalStoragePublicDirectory()` 등 레거시 경로 직접 사용 여부.
  - API 30+에서 `requestLegacyExternalStorage` 의존 여부.
  - 미디어 접근 시 `MediaStore` 또는 `READ_MEDIA_*` 권한 사용 여부.
  → 규칙 위반: finding 등록.

- [ ] A6. **Pending Intent** 생성 시 `FLAG_IMMUTABLE` 또는 `FLAG_MUTABLE` 명시 여부 확인(API 31+ 필수).
  → 누락: finding 등록.

- [ ] A7. **WindowInsets / EdgeToEdge** 사용 여부 확인.
  - `enableEdgeToEdge()` 또는 `WindowCompat.setDecorFitsSystemWindows(window, false)` 호출 시, 시스템 바와 겹치는 영역에 `WindowInsetsCompat`을 통한 padding 보정이 있는가.
  - `targetSdk` 35 이상이면서 EdgeToEdge 미적용이면 향후 시각적 깨짐 가능 — finding 등록.

- [ ] A8. A1~A7의 감지/대조 결과를 `[Android 호환성]` 블록으로 묶어 STEP1 본 산출물 `호환성` 줄에 "Android: (요약) — 상세는 `[Android 호환성]` 참조" 형식으로 1줄 요약을 남긴다. 상세는 `REVIEW_STEP1_OUTPUT.md` 형식대로 출력한다.
