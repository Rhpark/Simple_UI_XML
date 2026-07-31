# 실기기 검증 체계 IMPLEMENTATION PLAN

## 문서 정보

- 문서명: 실기기 검증 체계 IMPLEMENTATION PLAN
- 대상 프로젝트: Simple UI
- 대상 모듈: `simple_core`, `simple_system_manager`, `simple_compose`, `simple_xml`, `app`
- 상태: Draft
- 상위 요구사항: [PRD.md](./PRD.md)
- 구현 규격: [SPEC.md](./SPEC.md)

## 문서 목적

이 문서는 SPEC의 1단계 실기기 검증 체계를 구현하기 위한 작업 순서, 변경 파일, 단계별 검증 명령, 중단 조건 및 릴리스 게이트 전환 절차를 정의합니다.

실기기 테스트는 로컬 PC에 연결된 `PHY-01`에서 수행합니다. GitHub Actions는 실기기 테스트를 실행하지 않으며, 로컬 검증 결과가 `APPROVED`인 대상 커밋만 배포하도록 진입 조건을 확인합니다.

## 확정 결정 사항

- 검증 도입 순서는 `simple_core → simple_system_manager → simple_compose → simple_xml`입니다.
- 모든 통합 테스트는 실제 소비자 앱인 `app/src/androidTest`에 둡니다.
- 자동 통합 테스트는 로컬 `PHY-01`에서 `:app:connectedDebugAndroidTest`로 실행합니다.
- 수동 검증은 로컬에서 빌드하고 설치한 verification APK를 기준으로 수행합니다.
- 결과와 예외 승인은 릴리스별 Markdown 문서로 기록합니다.
- SIM 없음 fallback은 P0, 실제 SIM은 P1, 멀티 SIM은 P2로 처리합니다.
- 실제 SIM P1 예외는 릴리스마다 다시 승인합니다.
- 네 모듈이 모두 준비되기 전에는 기존 배포 흐름을 유지합니다.
- 차단 정책 활성화 후에는 자동 CD 시작을 중단하고 승인된 결과를 입력으로 받는 수동 배포 진입점을 사용합니다.
- GitHub Release와 Maven Central 게시 작업 사이의 기존 순서는 변경하지 않습니다.
- 에뮬레이터 매트릭스와 CI 계측 테스트 실행은 2단계 후속 계획으로 분리합니다.

## 구현 범위

### 포함

- `app` 계측 테스트 의존성과 공통 실기기 테스트 하네스
- SPEC의 33개 최소 시나리오에 대한 자동·수동·혼합 검증 수단
- 테스트에 필요한 샘플 앱의 안정적인 진입점과 UI 식별자
- 단말 사전 점검 및 테스트 후 상태 복구 절차
- 수동 체크리스트, 결과 템플릿 및 릴리스별 증빙 구조
- APK SHA-256과 검증 대상 소스 커밋 연결
- 로컬 전체 실기기 검증과 최종 승인 절차
- 승인된 결과와 CI 실행을 확인하는 수동 CD 진입 게이트

### 제외

- API 28·33·35 에뮬레이터 생성
- GitHub Actions 또는 다른 원격 환경에서의 실기기·에뮬레이터 테스트 실행
- Firebase Test Lab 및 유료 기기 팜
- 실제 SIM 또는 멀티 SIM 단말 구매·대여
- 네 모듈 완료 전의 부분 릴리스 차단
- 실기기 검증 과정에서 발견된 라이브러리 기능 결함의 동시 수정

기능 결함이 발견되면 해당 시나리오를 `FAIL`로 기록하고 별도 기능 수정 작업으로 분리합니다. 테스트를 통과시키기 위해 공개 동작을 임의로 변경하지 않습니다.

## 현재 기준 상태

- 프로젝트 환경: compileSdk 35, minSdk 28, Kotlin 2.0.21
- 기준 단말: `PHY-01`, Samsung `SM-G977N`, Android 12/API 31, `arm64-v8a`, SIM 없음
- `app`은 네 라이브러리 모듈을 모두 직접 의존합니다.
- `app/src/androidTest`에는 패키지명만 확인하는 `ExampleInstrumentedTest.kt`만 존재합니다.
- `app`에는 AndroidJUnitRunner, AndroidX JUnit 및 Espresso Core 의존성이 있습니다.
- Compose UI 계측 테스트 의존성 및 공통 실기기 하네스는 없습니다.
- 현재 CD는 CI 성공 뒤 자동으로 GitHub Release와 Maven Central 게시를 시작합니다.
- 현재 `workflow_dispatch`에는 승인 결과와 CI 실행을 지정하는 입력값이 없습니다.

## 목표 실행 흐름

```text
릴리스 후보 소스 커밋
        ↓
기존 JVM·Robolectric·정적 분석·빌드 CI 통과
        ↓
로컬에서 debug/verification/test APK 생성
        ↓
PHY-01에서 connectedDebugAndroidTest 실행
        ↓
PHY-01에서 수동·혼합 체크리스트 실행
        ↓
Markdown 결과 및 예외 승인 기록
        ↓
APPROVED 결과와 대상 CI 실행 ID로 CD 수동 실행
        ↓
GitHub가 결과·소스 SHA·CI 성공 여부만 검증
        ↓
기존 GitHub Release → Maven Central 게시 작업 실행
```

GitHub 검증 단계는 물리 단말에 연결하거나 `connectedDebugAndroidTest`를 실행하지 않습니다.

## 산출물 구조

### 계측 테스트

```text
app/src/androidTest/java/kr/open/library/simpleui_xml/deviceverification/
├── harness/
│   ├── DeviceEnvironment.kt
│   ├── PhysicalDeviceRule.kt
│   └── DeviceStateCleanup.kt
├── core/
├── systemmanager/
├── compose/
└── xml/
```

- `DeviceEnvironment`: 모델, API, ABI, SIM 및 필수 기능을 읽고 진단 정보를 제공합니다.
- `PhysicalDeviceRule`: `PHY-01` 실행 조건과 테스트 전후 공통 처리를 담당합니다.
- `DeviceStateCleanup`: 알림, 알람, 진동, 콜백 및 테스트 앱 내부 상태의 복구를 담당합니다.
- 테스트 메서드명 또는 표시 이름에는 SPEC 시나리오 ID를 포함합니다.
- 사람의 관찰이 필요한 결과를 계측 테스트에서 자동 `PASS`로 대체하지 않습니다.

### verification 전용 진입점

- `app/src/verification/AndroidManifest.xml`에서 체크리스트의 ADB 직접 실행 대상 Activity만 `exported=true`로 재정의합니다.
- main manifest의 `exported=false`는 유지하여 debug·release APK의 외부 진입 표면을 늘리지 않습니다.
- 패키징된 verification manifest와 체크리스트의 `adb shell am start` 대상이 모두 일치하는지 빌드 후 확인합니다.

### 검증 문서

```text
docs/verification/device/
├── DEVICE_TEST_CHECKLIST.md
├── DEVICE_TEST_RESULT_TEMPLATE.md
└── releases/
    └── {version}/
        ├── DEVICE_TEST_RESULT.md
        └── evidence/
```

### 배포 게이트

```text
.github/workflows/android-ci.yml
.github/workflows/android-cd.yml
.github/scripts/validate_device_verification_result.py
.github/scripts/tests/test_validate_device_verification_result.py
```

검증 스크립트 파일명은 구현 시 기존 스크립트 명명 규칙과 충돌하지 않는지 확인한 뒤 확정합니다.

## 단계 요약

| PHASE | 대상 | 핵심 산출물 | 완료 기준 |
| --- | --- | --- | --- |
| 0 | 기준선 | 기존 테스트·빌드·단말 상태 기록 | 회귀 기준 확보 |
| 1 | 공통 하네스·문서 | 테스트 기반, 체크리스트, 결과 템플릿 | 최소 테스트 실행 및 문서 작성 가능 |
| 2 | `simple_core` | CORE 5개 시나리오 | core P0 통과, P1 판정 가능 |
| 3 | `simple_system_manager` | SYS 14개 시나리오 | system_manager P0 통과, SIM 예외 판정 가능 |
| 4 | `simple_compose` | COMPOSE 6개 시나리오 | Compose P0 통과, P1 판정 가능 |
| 5 | `simple_xml` | XML 8개 시나리오 | XML P0 통과, P1 판정 가능 |
| 6 | 전체 실기기 검증 | 최초 릴리스 결과 문서 | 33개 시나리오 최종 판정 |
| 7 | 배포 진입 게이트 | 승인 확인형 수동 CD | 미승인 배포 차단 |
| 8 | 최종 검증 | 전체 회귀 및 운영 리허설 | 1단계 완료 조건 충족 |

## 단계별 구현 계획

### PHASE 0. 기준선 확보

#### STEP 0-1. 작업 전 상태 보존

- 기존 작업 트리 변경을 확인하고 실기기 검증 작업과 무관한 변경을 수정하지 않습니다.
- 현재 PRD, SPEC 및 IMPLEMENTATION_PLAN의 대상 커밋을 기록합니다.
- 기존 `ExampleInstrumentedTest.kt`의 실행 결과를 기록한 뒤 PHASE 1에서 대체 여부를 결정합니다.

#### STEP 0-2. 기존 JVM 검증

```powershell
.\gradlew.bat :simple_core:testAll
.\gradlew.bat :simple_system_manager:testAll
.\gradlew.bat :simple_compose:testAll
.\gradlew.bat :simple_xml:testAll
.\gradlew.bat :app:assembleDebug :app:assembleVerification
```

- 실패가 있으면 실기기 검증 작업으로 발생한 회귀와 구분할 수 있도록 기준선에 기록합니다.
- 기준선 실패를 숨기거나 이번 작업에서 관련 없는 기능 수정으로 확장하지 않습니다.

#### STEP 0-3. 단말 기준선

```powershell
adb devices -l
adb -s R3CM50A575V shell getprop ro.product.model
adb -s R3CM50A575V shell getprop ro.build.version.sdk
adb -s R3CM50A575V shell getprop ro.product.cpu.abi
```

- 등록된 단말이 `device` 상태인지 확인합니다.
- SIM 없음, Wi-Fi, 위치, 배터리 및 절전 모드 상태를 기록합니다.
- 다른 단말은 분리하고 `PHY-01`만 연결합니다.

#### PHASE 0 완료 조건

- 기존 JVM 검증 결과와 빌드 결과가 기록되어 있습니다.
- `PHY-01`의 실제 속성이 SPEC과 일치합니다.
- 실패 기준선과 신규 회귀를 구분할 수 있습니다.

### PHASE 1. 공통 하네스와 결과 문서 기반 구축

#### STEP 1-1. 계측 테스트 의존성 정리

수정 대상:

```text
gradle/libs.versions.toml
app/build.gradle.kts
```

- AndroidX Test Core와 필요한 테스트 규칙 의존성을 version catalog로 관리합니다.
- Compose UI 계측 테스트용 BOM과 `ui-test-junit4`를 `androidTest` 구성에 추가합니다.
- Compose 테스트 호스트에 필요한 `ui-test-manifest`는 debug 전용으로 추가합니다.
- 시스템 설정 화면을 자동 조작하는 의존성은 1단계 기본값으로 추가하지 않습니다. 수동·혼합 시나리오로 처리하기 어려운 경우에만 별도 검토합니다.

#### STEP 1-2. 공통 하네스 추가

신규 대상:

```text
app/src/androidTest/java/kr/open/library/simpleui_xml/deviceverification/harness/DeviceEnvironment.kt
app/src/androidTest/java/kr/open/library/simpleui_xml/deviceverification/harness/PhysicalDeviceRule.kt
app/src/androidTest/java/kr/open/library/simpleui_xml/deviceverification/harness/DeviceStateCleanup.kt
```

- 모델명만으로 테스트를 무조건 실패시키지 않고 실제 환경 정보를 진단 결과로 제공합니다.
- 릴리스 전체 실행은 SPEC에 등록된 `PHY-01`과 일치하는지 수동 사전 점검에서 강제합니다.
- 테스트가 생성한 리소스에는 전용 ID·태그·채널명을 사용하여 사용자 데이터와 구분합니다.
- 정리 코드는 성공과 실패 모두에서 실행되도록 JUnit rule 또는 `try/finally` 경계에 둡니다.
- 권한과 시스템 설정처럼 원상 복구를 자동 보장할 수 없는 항목은 체크리스트로 넘깁니다.

#### STEP 1-3. 기본 예제 테스트 대체

- `ExampleInstrumentedTest.kt`의 패키지명 확인을 공통 smoke test에 통합합니다.
- 예제 파일을 유지할 실익이 없으면 제거하고 `deviceverification` 패키지를 단일 진입점으로 사용합니다.
- 삭제 전 동일한 패키지·Context 검증이 `CORE-P0-001`에 포함되는지 확인합니다.

#### STEP 1-4. 체크리스트와 결과 템플릿 추가

신규 대상:

```text
docs/verification/device/DEVICE_TEST_CHECKLIST.md
docs/verification/device/DEVICE_TEST_RESULT_TEMPLATE.md
docs/verification/device/releases/.gitkeep
```

- 체크리스트는 모듈 도입 순서와 SPEC 시나리오 ID 순서로 구성합니다.
- 각 항목에 사전 조건, 실행 절차, 기대 결과, 복구, 증빙 및 결과 상태를 포함합니다.
- 결과 템플릿에는 SPEC의 메타데이터, 시나리오 결과 표, 예외 승인 및 최종 판정을 포함합니다.
- CD 검증 스크립트가 읽을 필드명은 고정하고 사람이 읽는 설명과 분리합니다.

#### STEP 1-5. 공통 하네스 검증

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

#### PHASE 1 완료 조건

- `PHY-01`에서 최소 smoke test가 실행됩니다.
- 실패 시에도 공통 정리 절차가 실행됩니다.
- 빈 릴리스 결과 문서를 템플릿으로 작성할 수 있습니다.
- 수동 항목이 자동 테스트 성공으로 잘못 계산되지 않습니다.

### PHASE 2. `simple_core` 검증 구축

#### STEP 2-1. 자동 통합 테스트

권장 신규 파일:

```text
deviceverification/core/CoreContextIntegrationTest.kt
deviceverification/core/CoreLogxIntegrationTest.kt
deviceverification/core/CorePermissionIntegrationTest.kt
deviceverification/core/CoreStorageIntegrationTest.kt
```

매핑:

- `CORE-P0-001`: 앱 Context 기반 공개 API 소비자 통합
- `CORE-P0-002`: 일반 권한 허용·거부 상태 판정
- `CORE-P0-004`: SharedPreferences 저장·조회·삭제
- `CORE-P1-001`: Logx 실제 파일 생성, 파일명·포맷, 타입·태그 필터 및 테스트 파일 정리

#### STEP 2-2. 수동·혼합 시나리오

- `CORE-P0-003`: 특수 권한 설정 이동, 앱 복귀, 최신 상태 재판정
- `CORE-P1-001`: verification 앱에서 생성한 사용자 지정 경로의 Logx 파일이 앱 재실행 후에도 유지되는지 확인
- 기존 `PermissionsActivity.kt`와 `LogxActivity.kt`를 진입 화면으로 우선 사용합니다.
- 테스트 안정성을 위해 필요한 경우에만 View ID 또는 테스트 전용 상태 표시를 보완합니다.

#### STEP 2-3. 검증

```powershell
.\gradlew.bat :simple_core:testAll
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.package=kr.open.library.simpleui_xml.deviceverification.core"
```

#### PHASE 2 완료 조건

- core P0 네 항목이 `PASS`입니다.
- Logx P1을 실행하거나 결과 상태와 근거를 기록할 수 있습니다.
- core 테스트가 변경한 앱 데이터와 권한 상태의 복구 여부가 기록됩니다.

### PHASE 3. `simple_system_manager` 검증 구축

#### STEP 3-1. 공통·컨트롤러 자동 통합 테스트

권장 신규 파일:

```text
deviceverification/systemmanager/SystemServiceBaseIntegrationTest.kt
deviceverification/systemmanager/NotificationIntegrationTest.kt
deviceverification/systemmanager/AlarmIntegrationTest.kt
deviceverification/systemmanager/VibratorIntegrationTest.kt
deviceverification/systemmanager/WifiIntegrationTest.kt
```

- `SYS-P0-001`부터 `SYS-P0-005`까지 자동 판정 가능한 반환값, 등록 상태 및 정리 동작을 검증합니다.
- 실제 알림 표시, 알림 클릭, 알람 발화 및 진동 감지는 혼합 체크리스트에서 최종 판정합니다.
- 알림 채널, 알림 ID, 알람 requestCode는 검증 전용 범위로 고정합니다.

#### STEP 3-2. 상태 정보 자동 통합 테스트

권장 신규 파일:

```text
deviceverification/systemmanager/LocationIntegrationTest.kt
deviceverification/systemmanager/BatteryIntegrationTest.kt
deviceverification/systemmanager/NetworkIntegrationTest.kt
deviceverification/systemmanager/NoSimFallbackIntegrationTest.kt
```

- `SYS-P0-006`부터 `SYS-P0-009`까지 실제 단말 상태와 안전한 반환을 검증합니다.
- 네트워크 연결·해제는 Wi-Fi 기준으로 수행하고 모바일 데이터가 있다고 가정하지 않습니다.
- SIM 없음 fallback은 P0 자동 테스트로 고정합니다.
- 실제 SIM P1과 멀티 SIM P2는 자동 테스트 골격과 환경 판정만 준비하되 현재 단말에서 성공으로 위장하지 않습니다.

#### STEP 3-3. Window 계열 혼합 검증

권장 신규 파일:

```text
deviceverification/systemmanager/SystemBarIntegrationTest.kt
deviceverification/systemmanager/SoftKeyboardIntegrationTest.kt
deviceverification/systemmanager/FloatingViewIntegrationTest.kt
```

- `SYS-P0-010`부터 `SYS-P0-012`까지 상태 적용·해제 가능한 부분을 자동 검증합니다.
- 실제 시스템 바 시각 상태, 키보드 표시 및 플로팅 뷰 이동은 체크리스트에서 관찰합니다.
- 기존 `ServiceManagerControllerActivity` 하위 샘플 화면을 우선 사용합니다.

#### STEP 3-4. SIM 예외 기록 검증

- `SYS-P1-001`을 `ENV_UNAVAILABLE`로 기록합니다.
- 영향 API, 위험, 현재 fallback P0 결과 및 승인자를 기록합니다.
- 만료 릴리스를 현재 릴리스로 설정하고 다음 릴리스 자동 승인을 금지합니다.
- `SYS-P2-001`은 P2 미실행 사유를 기록합니다.

#### STEP 3-5. 검증

```powershell
.\gradlew.bat :simple_system_manager:testAll
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.package=kr.open.library.simpleui_xml.deviceverification.systemmanager"
```

#### PHASE 3 완료 조건

- system_manager P0 열두 항목이 `PASS`입니다.
- 실제 SIM P1은 `PASS` 또는 유효한 릴리스별 예외로 판정됩니다.
- 알림, 알람, 진동, 위치, 콜백, 플로팅 뷰가 테스트 뒤 정리됩니다.

### PHASE 4. `simple_compose` 검증 구축

#### STEP 4-1. Compose 테스트 진입점 보완

수정 후보:

```text
app/src/main/java/kr/open/library/simpleui_xml/compose/ComposeExamplesActivity.kt
app/src/main/java/kr/open/library/simpleui_xml/compose/ComposeExamplesViewModel.kt
```

- 기존 샘플의 동작을 변경하지 않고 테스트 가능한 semantics와 안정적인 상태 노출만 보완합니다.
- 테스트 전용 공개 API를 라이브러리 모듈에 추가하지 않습니다.

#### STEP 4-2. Compose 계측 테스트

권장 신규 파일:

```text
deviceverification/compose/ComposeRenderingIntegrationTest.kt
deviceverification/compose/ComposePermissionIntegrationTest.kt
deviceverification/compose/ComposeLifecycleIntegrationTest.kt
deviceverification/compose/ComposeSystemBarsIntegrationTest.kt
deviceverification/compose/ComposeScrollIntegrationTest.kt
```

- `COMPOSE-P0-001`부터 `COMPOSE-P0-005`까지 렌더링, 상태 전이, 생명주기, 시스템 바 복원 및 스크롤 상태를 검증합니다.
- `COMPOSE-P1-001`은 Activity 재생성과 회전을 이용하여 저장 가능한 상태를 검증합니다.
- 시스템 바 시각 상태와 설정 화면 복귀는 혼합 결과로 기록합니다.

#### STEP 4-3. 검증

```powershell
.\gradlew.bat :simple_compose:testAll
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.package=kr.open.library.simpleui_xml.deviceverification.compose"
```

#### PHASE 4 완료 조건

- Compose P0 다섯 항목이 `PASS`입니다.
- 구성 변경 P1의 실행 결과가 기록됩니다.
- Robolectric Compose 테스트와 실기기 Compose 테스트의 책임이 구분됩니다.

### PHASE 5. `simple_xml` 검증 구축

#### STEP 5-1. 컴포넌트 생명주기 테스트

권장 신규 파일:

```text
deviceverification/xml/ActivityLifecycleIntegrationTest.kt
deviceverification/xml/FragmentLifecycleIntegrationTest.kt
deviceverification/xml/DialogLifecycleIntegrationTest.kt
```

- `XML-P0-001`부터 `XML-P0-003`까지 실제 Activity, Fragment 및 DialogFragment 생명주기를 검증합니다.
- 기존 `activity_fragment` 샘플을 우선 사용하고 테스트를 위한 별도 복제 화면을 만들지 않습니다.

#### STEP 5-2. 권한·RecyclerView·View 확장 테스트

권장 신규 파일:

```text
deviceverification/xml/XmlPermissionIntegrationTest.kt
deviceverification/xml/RecyclerViewIntegrationTest.kt
deviceverification/xml/ViewExtensionIntegrationTest.kt
```

- `XML-P0-004`부터 `XML-P0-006`까지 실제 권한 결과, 데이터 변경, 스크롤 및 View 상태를 검증합니다.
- 필요한 View ID는 기존 레이아웃에 추가하되 사용자 화면 동작과 디자인은 변경하지 않습니다.

#### STEP 5-3. XML 수동·P1 검증

- `XML-P1-001`: Toast, Snackbar 및 애니메이션을 사람이 관찰합니다.
- `XML-P1-002`: 구성 변경과 프로세스 수준 재생성 후 저장 대상 상태를 확인합니다.
- 프로세스 종료가 필요한 절차는 앱 강제 종료 방식과 재진입 경로를 체크리스트에 명시합니다.

#### STEP 5-4. 검증

```powershell
.\gradlew.bat :simple_xml:testAll
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.package=kr.open.library.simpleui_xml.deviceverification.xml"
```

#### PHASE 5 완료 조건

- XML P0 여섯 항목이 `PASS`입니다.
- XML P1 두 항목의 결과와 증빙이 기록됩니다.
- 앞선 core, system_manager 및 Compose 실기기 테스트가 계속 통과합니다.

### PHASE 5-A. PHASE 6 진입 전 검증 정합성 보완

리뷰에서 확인된 자동 테스트 범위와 시나리오 전체 계약의 차이를 PHASE 6 최초 실행 전에 정리합니다. 새 계획 문서를 만들지 않고 SPEC, 체크리스트 및 결과 템플릿을 단일 기준으로 갱신합니다.

#### STEP 5-A-1. 빠른 안정성 개선

- 실기기 하네스의 SDK 분기는 프로젝트 공통 `checkSdkVersion`을 사용합니다.
- 오버레이 권한 OFF와 Wi-Fi 연결 상태를 관련 시나리오의 명시적 사전 조건으로 검사합니다.
- 사전 조건 불일치는 기능 실패와 구분할 수 있도록 시나리오 ID, 기대 상태, 실제 상태 및 단말 환경을 진단 메시지에 포함합니다.
- P0 사전 조건 불일치를 JUnit skip으로 통과시키지 않습니다.
- 프로세스 수준 수동 확인이 필요한 `XML-P1-002`와 실제 Wi-Fi 연결 변경을 관찰하는 `SYS-P0-008`을 `HYBRID_DEVICE`로 정합화합니다.

#### STEP 5-A-2. 혼합 시나리오 실행 절차 보완

- `CORE-P1-001`에 자동 파일 생성·형식·필터·정리 판정과 `LogxActivity`에서 만든 사용자 지정 경로 파일의 앱 재실행 후 유지 확인 절차를 추가합니다.
- core 권한 복귀, system_manager 알림·알람·진동·Wi-Fi·위치·시스템 바·키보드·플로팅 뷰, Compose 권한·시스템 바, XML Dialog·권한·프로세스 재생성의 수동 부분을 체크리스트에 명시합니다.
- 자동 테스트가 검증하는 부분과 사람이 관찰하는 부분을 분리하고, 두 결과를 릴리스 결과 표에 각각 기록합니다.
- 기존 샘플 화면만으로 실행할 수 없는 수동 부분은 성공으로 간주하지 않고 `BLOCKED`로 남기며, 필요한 검증 호스트를 후속 구현 대상으로 기록합니다.
- `COMPOSE-P0-002`는 verification 전용 Compose 호스트에서 설정 이동, Resume 재판정, 중복 요청 무시 및 단일 콜백을 관찰할 수 있게 합니다.

#### STEP 5-A-3. 검증

```powershell
.\gradlew.bat ktlintCheck
.\gradlew.bat :app:lintVerification
.\gradlew.bat :simple_core:apiCheck :simple_system_manager:apiCheck :simple_compose:apiCheck :simple_xml:apiCheck
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleVerification
.\gradlew.bat :app:connectedDebugAndroidTest
```

#### PHASE 5-A 완료 조건

- 모든 `HYBRID_DEVICE` 시나리오의 자동 범위와 수동 범위가 구분되어 있습니다.
- `CORE-P1-001`을 포함한 수동 항목에 진입 경로, 기대 결과, 증빙 및 복구 절차가 있습니다.
- `COMPOSE-P0-002`의 수동 검증 호스트와 오버레이 권한 복구 절차가 있습니다.
- 자동·수동 결과 중 하나가 미완료이면 최종 `PASS`가 될 수 없습니다.
- 사전 조건 불일치가 라이브러리 기능 실패로 오인되지 않으면서도 릴리스는 계속 차단합니다.
- 전체 정적 분석, 빌드 및 실기기 자동 테스트가 통과합니다.

### PHASE 6. 전체 로컬 릴리스 후보 검증

#### STEP 6-1. 검증 대상 고정

- 제품 코드와 빌드 설정이 포함된 검증 대상 소스 커밋 SHA를 기록합니다.
- 해당 커밋의 Android CI 실행 ID와 성공 여부를 기록합니다.
- 검증 중 제품 코드가 변경되면 기존 결과를 폐기하고 새 커밋으로 다시 시작합니다.

#### STEP 6-2. 로컬 아티팩트 생성

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleVerification
```

기록 대상:

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
app/build/outputs/apk/verification/app-verification.apk
```

- verification APK가 unsigned 이름으로 생성되면 실제 파일명과 서명 상태를 기록합니다.
- 각 파일은 `Get-FileHash -Algorithm SHA256`으로 해시를 계산합니다.

#### STEP 6-3. 전체 자동 실기기 테스트

```powershell
adb devices -l
.\gradlew.bat :app:connectedDebugAndroidTest
```

- `PHY-01`만 연결된 상태에서 실행합니다.
- Gradle HTML/XML 결과 경로를 릴리스 결과에 기록합니다.
- P0 또는 P1 자동 테스트가 실패하면 수동 검증으로 덮어쓰지 않고 `REJECTED`로 판정합니다.

#### STEP 6-4. 수동·혼합 체크리스트

- verification APK를 `PHY-01`에 설치합니다.
- 체크리스트를 모듈 순서대로 실행합니다.
- 화면 캡처, 로그 또는 관찰 기록을 `evidence/`에 저장합니다.
- 테스트가 변경한 단말 상태를 복구하고 복구 결과를 기록합니다.

#### STEP 6-5. 결과와 예외 승인

- `docs/verification/device/releases/{version}/DEVICE_TEST_RESULT.md`를 작성합니다.
- P0는 모두 `PASS`인지 확인합니다.
- `SYS-P1-001`의 SIM 미보유 예외를 해당 릴리스에 한해 승인하거나 릴리스를 중단합니다.
- P1의 `FAIL`, `BLOCKED`, `NOT_RUN`은 예외로 바꾸지 않습니다.
- 최종 상태를 `APPROVED` 또는 `REJECTED`로 기록합니다.

#### PHASE 6 완료 조건

- 33개 시나리오가 SPEC 규칙에 따라 판정되어 있습니다.
- APK 해시, 소스 SHA, CI 실행 ID, 단말 정보 및 증빙이 기록되어 있습니다.
- 단말 복구가 완료되었습니다.
- `APPROVED`가 아니면 PHASE 7 배포를 실행하지 않습니다.

### PHASE 7. 승인 확인형 수동 CD 진입 게이트

이 PHASE는 PHASE 2부터 PHASE 6까지 안정적으로 운영한 뒤 별도 전환 시점에 수행합니다. 전환 전에는 기존 CD를 유지합니다.

#### STEP 7-1. CI 메타데이터에 소스 식별자 추가

수정 대상:

```text
.github/workflows/android-ci.yml
```

- `release-metadata.json`에 CI 실행 ID와 CI 대상 소스 SHA를 추가합니다.
- 기존 tag, title, body, testers 및 `should_release` 필드는 유지합니다.
- 소스 SHA는 로컬 결과 문서의 검증 대상 SHA와 비교할 단일 기준으로 사용합니다.
- 현재 release metadata의 7일 보존기간 안에 로컬 검증과 배포 승인을 완료합니다. 기간이 만료되면 동일 소스 SHA로 Android CI를 다시 실행하고 새 CI 실행 ID를 결과 문서에 기록합니다.

#### STEP 7-2. 결과 검증 스크립트 추가

신규 대상:

```text
.github/scripts/validate_device_verification_result.py
.github/scripts/tests/test_validate_device_verification_result.py
```

검증 항목:

- 결과 경로가 `docs/verification/device/releases/{version}/DEVICE_TEST_RESULT.md` 형식인지
- 최종 상태가 `APPROVED`인지
- 결과의 검증 대상 소스 SHA가 CI 메타데이터의 소스 SHA와 일치하는지
- 릴리스 버전이 CI release metadata와 일치하는지
- 모든 P0가 `PASS`인지
- 적용 대상 P1이 `PASS` 또는 유효한 예외 승인인지
- P0·P1에 `FAIL`, `BLOCKED`, `NOT_RUN`이 없는지
- 승인자와 승인 일시가 존재하는지

- 파서는 Markdown의 고정 필드만 읽고 임의 코드를 실행하지 않습니다.
- 정상·누락·SHA 불일치·미승인·만료 예외 케이스를 스크립트 단위 테스트로 검증합니다.

#### STEP 7-3. CD를 수동 승인 진입점으로 전환

수정 대상:

```text
.github/workflows/android-cd.yml
```

- 자동 `workflow_run` 게시 시작을 비활성화합니다.
- `workflow_dispatch`에 최소 다음 입력을 추가합니다.
  - 승인 대상 Android CI 실행 ID
  - 릴리스 결과 Markdown 경로
- `check-ci-success`는 지정한 CI 실행의 성공 여부와 `release-metadata`·coverage artifact를 확인합니다.
- 결과 검증 스크립트가 실패하면 `should_release=false`로 처리하고 모든 게시 job을 차단합니다.
- 승인된 소스 SHA를 job output으로 전달합니다.
- `release`, `maven-central-publish`, `assemble-apk`, `firebase-distribution`의 checkout은 승인된 소스 SHA를 사용합니다.
- 기존 `release → maven-central-publish → assemble-apk → firebase-distribution` 의존 순서는 유지합니다.
- Firebase 배포는 게시 후 샘플 APK 배포로 유지하며 실기기 승인 입력으로 사용하지 않습니다.

#### STEP 7-4. 미승인 차단 검증

다음 경우 실제 게시 job이 시작되지 않아야 합니다.

- 결과 문서 없음
- `REJECTED`, `DRAFT`, `TESTING` 또는 `EXCEPTION_REVIEW`
- 소스 SHA 불일치
- 실패한 CI 실행 ID
- 릴리스 버전 불일치
- P0 미실행 또는 실패
- P1 만료 예외

실제 Maven Central 게시 없이 workflow syntax, 스크립트 단위 테스트 및 비게시 dry-run 경로로 먼저 검증합니다.

#### STEP 7-5. 승인 성공 경로 검증

- 테스트용 승인 문서와 CI 실행 ID로 check job까지만 실행하는 dry-run을 제공합니다.
- check job이 승인된 소스 SHA를 출력하는지 확인합니다.
- 실제 게시 전 유지보수자가 출력 SHA와 결과 문서 SHA를 다시 비교합니다.
- 첫 실제 활성화 릴리스는 수동 감시하에 진행합니다.

#### PHASE 7 완료 조건

- GitHub에서 실기기 테스트를 실행하는 단계가 없습니다.
- 미승인·불일치 결과는 게시 전에 차단됩니다.
- 승인된 소스 SHA만 태그와 Maven Central 게시 대상으로 사용됩니다.
- 기존 게시 job 내부 순서는 유지됩니다.

### PHASE 8. 최종 회귀와 운영 인수

#### STEP 8-1. JVM 전체 검증

```powershell
.\gradlew.bat :simple_core:testAll
.\gradlew.bat :simple_system_manager:testAll
.\gradlew.bat :simple_compose:testAll
.\gradlew.bat :simple_xml:testAll
```

#### STEP 8-2. 빌드·API·정적 분석

프로젝트 정적 분석 규칙에 따라 다음 순서로 실행합니다.

```powershell
.\gradlew.bat ktlintCheck
.\gradlew.bat :app:lintDebug :simple_core:lintDebug :simple_system_manager:lintDebug :simple_compose:lintDebug :simple_xml:lintDebug
.\gradlew.bat :simple_core:apiCheck :simple_system_manager:apiCheck :simple_compose:apiCheck :simple_xml:apiCheck
.\gradlew.bat :app:assembleDebug :app:assembleVerification
```

이번 작업은 라이브러리 공개 API 변경을 목표로 하지 않으므로 API 기준 파일을 갱신하는 `apiDump`가 아니라 기존 기준과 비교하는 `apiCheck`를 실행합니다.

#### STEP 8-3. 전체 실기기 재검증

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

- 수동·혼합 체크리스트를 다시 실행합니다.
- 최초 운영 결과 문서를 완성합니다.
- SIM P1 예외가 현재 릴리스에만 유효한지 확인합니다.

#### STEP 8-4. 운영 문서 갱신

- 프로젝트 README 또는 릴리스 문서 인덱스에 실기기 검증 절차를 연결합니다.
- 테스트 단말 교체, 결과 작성, 예외 재승인 및 배포 실행 절차를 유지보수자 관점에서 정리합니다.
- 2단계 에뮬레이터·CI 자동화 항목을 별도 계획 후보로 남깁니다.

#### PHASE 8 완료 조건

- JVM, 빌드, 정적 분석 및 실기기 검증이 모두 완료됩니다.
- 최초 릴리스 결과가 추적 가능한 형태로 보존됩니다.
- 승인 없는 배포가 차단되는 운영 리허설을 통과합니다.
- 문서와 실제 명령·경로가 일치합니다.

## 단계별 중단 조건

- 각 PHASE의 P0 실패 상태에서는 다음 PHASE로 진행하지 않습니다.
- 공통 하네스가 사용자 데이터나 단말 설정을 복구하지 못하면 기능별 테스트를 확대하지 않습니다.
- 테스트가 불안정하게 성공·실패하면 `PASS`로 고정하지 않고 `BLOCKED`로 기록합니다.
- 실기기 결과와 Robolectric 결과가 다르면 실제 단말 결과를 결함 후보로 기록하고 원인을 별도 분석합니다.
- CD dry-run이 미승인 입력을 차단하지 못하면 자동 `workflow_run`을 비활성화하지 않습니다.
- 승인된 소스 SHA를 정확히 checkout한다고 증명하기 전에는 실제 Maven Central 게시에 적용하지 않습니다.

## 롤백 기준

### 테스트 하네스

- 모듈별 테스트는 패키지 단위로 독립되어야 합니다.
- 신규 테스트가 다른 모듈 테스트를 오염시키면 해당 모듈 PHASE의 하네스와 샘플 앱 식별자 변경만 되돌릴 수 있어야 합니다.
- 테스트가 생성한 단말 리소스는 롤백 전 수동 체크리스트로 정리합니다.

### 문서와 결과

- 릴리스 결과는 덮어쓰지 않고 정정 이력을 남깁니다.
- 잘못 승인한 결과는 삭제하지 않고 최종 상태를 `REJECTED`로 정정한 뒤 변경 사유와 정정 일시를 기록합니다.

### 배포 게이트

- 수동 CD 전환 실패 시 자동 게시를 즉시 재활성화하지 않습니다.
- 원인이 해결될 때까지 배포를 수동 중단하고 기존 워크플로 파일로 되돌릴지 별도 승인합니다.
- 이미 생성된 Git tag나 Maven Central 게시물은 단순 롤백할 수 없으므로 dry-run에서 차단 경로를 먼저 검증합니다.

## 리스크와 대응

| 리스크 | 대응 |
| --- | --- |
| 단일 Samsung/API 31 결과를 전체 Android 호환성으로 오인 | 결과 문서에 환경 한계를 고정하고 2단계 에뮬레이터 범위를 유지 |
| 수동 항목을 자동 테스트 성공으로 간주 | 자동·수동·혼합 결과 열을 분리하고 최종 Markdown에서 각각 판정 |
| SIM 없음으로 전화망 오류 은폐 | no-SIM fallback P0 강제, 실제 SIM P1 릴리스별 예외, 멀티 SIM P2 유지 |
| 테스트가 알림·알람·콜백·Window 상태를 남김 | 공통 cleanup과 수동 복구 체크리스트를 모두 적용 |
| 샘플 앱 수정이 라이브러리 동작을 바꿈 | 테스트 식별자와 상태 노출만 허용하고 공개 라이브러리 API 변경 금지 |
| 로컬 APK와 게시 소스 SHA 불일치 | APK 해시·소스 SHA·CI 실행 ID를 기록하고 CD에서 일치 확인 |
| Markdown을 임의로 작성해 승인 우회 | 고정 필드 검증 스크립트와 실패 케이스 단위 테스트 추가 |
| GitHub에서 실기기 검증을 수행한다고 오해 | PLAN과 운영 문서에 로컬 실행, GitHub 승인 확인 책임을 반복 명시 |
| 수동 CD 전환으로 기존 artifact 전달이 끊김 | 입력한 CI 실행 ID에서 release metadata와 coverage artifact를 재사용 |
| 로컬 검증 중 release metadata의 7일 보존기간 만료 | 동일 소스 SHA로 CI를 다시 실행하고 새 CI 실행 ID와 성공 결과를 연결 |

## 최종 완료 기준

- SPEC의 33개 최소 시나리오가 테스트 코드 또는 체크리스트에 매핑됩니다.
- `PHY-01`에서 전체 `:app:connectedDebugAndroidTest`가 통과합니다.
- 모든 P0가 `PASS`이고 P1은 `PASS` 또는 유효한 릴리스별 예외입니다.
- 수동 검증 결과와 증빙이 릴리스별 Markdown으로 보존됩니다.
- debug, test 및 verification APK가 검증 대상 소스 SHA와 연결됩니다.
- 테스트가 변경한 단말 상태가 복구됩니다.
- 네 모듈 완료 전에는 기존 배포 흐름이 유지됩니다.
- 활성화 후에는 승인 결과가 없는 CD 실행이 게시 job에 진입하지 못합니다.
- GitHub Actions에는 실기기 또는 에뮬레이터 계측 테스트 실행 단계가 없습니다.
- 기존 GitHub Release와 Maven Central 게시 작업의 내부 순서는 유지됩니다.
- PRD, SPEC, IMPLEMENTATION_PLAN 및 실제 구현이 서로 일치합니다.
