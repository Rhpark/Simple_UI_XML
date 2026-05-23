<!-- 파일 목적: a STEP1(Android 보강) finding의 심각도 판정 기준 -->

### 심각도 기준

#### CRITICAL

- **권한 선언 누락 상태에서 권한 필요 API 호출**: 매니페스트에 `<uses-permission>`이 없는데 `Context.startActivity(...permission required intent)`, `BluetoothAdapter.startScan`, `LocationManager.requestLocationUpdates` 등을 직접 호출. 런타임 `SecurityException`으로 크래시.
- **존재하지 않는 Android API/필드 호출(hallucination)**: 예시 — `NotificationManager.IMPORTANCE_HIGH_PRIORITY`(실제 없음), `Intent.FLAG_FOREGROUND_BIND`(실제 없음). 선언부를 공식 문서/소스에서 확인할 수 없으면 CRITICAL.
- **앱 첫 실행 직후 100% 크래시로 이어지는 SDK 정책 위반**: 예 — Android 14(targetSdk 34) 빌드에서 `ForegroundServiceType` 미지정.

#### HIGH

- **minSdk 미충족 API 무방어 호출**: 분기 없이 `NotificationChannel`(26+), `POST_NOTIFICATIONS`(33+), `READ_MEDIA_IMAGES`(33+) 등을 호출하면서 minSdk가 해당 레벨보다 낮은 경우. 구형 단말에서 `NoSuchMethodError` / `ClassNotFoundException` 발생.
- **PendingIntent FLAG_IMMUTABLE/FLAG_MUTABLE 미명시(API 31+)**: 런타임 `IllegalArgumentException`.
- **런타임 권한 체크 누락**: 매니페스트 선언은 있으나 `checkSelfPermission` 분기 없이 권한 필요 API를 호출. 권한 거부 사용자에게서 기능 미동작.
- **Foreground Service 타입 누락 또는 매니페스트 타입 불일치(API 34+)**: `Missing foregroundServiceType` 크래시.
- **Scoped Storage 위반으로 인한 파일 접근 실패(API 30+ 강제)**: 사용자가 파일을 저장/조회할 수 없음.

#### MEDIUM

- **EdgeToEdge 미적용(targetSdk 35+)**: 시스템 바와 콘텐츠 겹침. 사용 가능하지만 시각적 결함.
- **`requestLegacyExternalStorage="true"` 의존(API 29~30 전환기)**: 임시 동작은 하나 향후 차단.
- **권한 그룹 누락(예: `BLUETOOTH_SCAN`은 있는데 `BLUETOOTH_CONNECT` 누락)**: 일부 기능만 동작.
- **패키지 가시성 `queries` 누락(API 30+)**: `queryIntentActivities` 결과가 비어 일부 인텐트 라우팅 실패.
- **A1 카테고리 분류 미수행**: STEP1 호환성 결론의 신뢰도 저하.

#### LOW

- **deprecated 권한/플래그를 신규 API와 함께 사용**: 동작은 하나 향후 deprecated 경고 누적.
- **`@RequiresApi` 어노테이션 누락이지만 분기 자체는 정상 동작**: 정적 분석 도구가 잡지 못함.
- **불필요한 `<uses-permission>` 선언**: 권한이 매니페스트에는 있으나 코드에서 사용하지 않음.
