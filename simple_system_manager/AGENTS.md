# simple_system_manager 모듈 개요

- 전역 규칙과 코딩/문서 규칙은 루트 AGENTS.md를 우선 참조합니다.
- 이 모듈은 `simple_core`에만 의존하며, `system_manager` 기능을 독립적으로 제공합니다.

## 모듈 정의

- `kr.open.library.simple_ui.system_manager.core.*`
- `kr.open.library.simple_ui.system_manager.xml.*`
- 위 두 패키지를 같은 모듈에서 함께 소유합니다.

## 핵심 원칙

- `system_manager` 관련 source package와 테스트 패키지는 모두 `simple_system_manager` 기준으로 정리합니다.
- `simple_xml`은 이 모듈을 의존하지 않습니다.
- system_manager 기능이 필요한 앱/소비자는 `simple_system_manager`를 직접 의존합니다.
- `simple_core`에는 공통 유틸만 남기고 `system_manager` main/test 소스는 이 모듈이 소유합니다.

## 주요 패키지 구조

### core 패키지 (`system_manager.core.*`)

- **base**: 시스템 서비스 공통 기반 (`BaseSystemService`, `SystemResult`)
- **controller/alarm**: 알람 제어 (`AlarmController`, `AlarmConstants`, `BaseAlarmReceiver`)
- **controller/notification**: 알림 제어 (`SimpleNotificationController`)
- **controller/vibrator**: 진동 제어 (`VibratorController`)
- **controller/wifi**: Wi-Fi 제어 (`WifiController`)
- **info/battery**: 배터리 상태 조회 (`BatteryStateInfo`, `BatteryStateEvent`)
- **info/location**: 위치 상태 조회 (`LocationStateInfo`, `LocationStateEvent`)
- **info/network/connectivity**: 네트워크 연결 상태 조회 (`NetworkConnectivityInfo`)
- **info/network/telephony**: 전화망 상태 조회 (`TelephonyInfo`)
- **info/network/sim**: SIM 정보 조회 (`SimInfo`)
- **extensions**: 시스템 서비스 확장 함수 (`SystemServiceExtensions`)

### xml 패키지 (`system_manager.xml.*`)

- **controller/systembar**: 상태바/내비게이션바 제어 (`SystemBarController`)
- **controller/softkeyboard**: 소프트 키보드 제어 (`SoftKeyboardController`)
- **controller/window**: 플로팅 뷰 제어 (`FloatingViewController`, `FloatingDragView`, `FloatingFixedView`)
- **display**: 화면 크기/인셋 정보 (`DisplayInfo`, `DisplayInfoSize`, `DisplayInfoBarInsets`)
- **extensions**: XML 전용 시스템 서비스 확장 함수 (`SystemServiceExtensionsXml`)

## 기능별 전용 규칙

- 기능 문서 위치 패턴: `simple_system_manager/docs/feature/system_manager/<controller|info>/<기능명>/`
  - 각 기능별 PRD.md / SPEC.md / IMPLEMENTATION_PLAN.md 포함
- systembar (행동 규칙 포함): simple_system_manager/docs/feature/system_manager/controller/systembar/AGENTS.md

## 금지 패턴

- simple_xml / simple_core 에 system_manager 기능 코드를 두지 않는다
- View / Activity / Fragment 직접 의존성을 core 패키지(`system_manager.core.*`)에 두지 않는다
- 전역 싱글턴을 허용하지 않는다 (Window / Context는 생성자 주입)
- `simple_xml`이 이 모듈을 의존하는 방향을 허용하지 않는다

## 판단 기준

- 시스템 서비스 제어 / 상태 조회 → `simple_system_manager`
- View / Window 의존 시스템 제어 → `system_manager.xml.*`
- View / Window 독립 시스템 제어 → `system_manager.core.*`
- UI 없는 공통 유틸 → `simple_core`
- XML UI 레이어 컴포넌트 → `simple_xml`

## 경계 조건

- 책임지는 범위: 시스템 서비스 제어 / 상태 조회 / Window 기반 기능 / 플로팅 뷰 / 소프트키보드 / 시스템바
- 책임지지 않는 범위:
  - UI 렌더링 및 사용자 인터랙션
  - DataBinding / ViewBinding 자동화
  - RecyclerView / Adapter / ViewHolder
  - 권한 요청 흐름 (ActivityResult 기반)

## 우선 참조 문서

- 계획 문서: docs/planning/module_separation/PLAN.md
- 실행 체크리스트: docs/planning/module_separation/IMPLEMENTATION_CHECKLIST.md
- README 인덱스: README.md
- system_manager 문서 인덱스:
  - docs/readme/system_manager/README_SYSTEM_MANAGER_EXTENSIONS.md
  - docs/readme/system_manager/controller/README_SERVICE_MANAGER_CONTROL.md
  - docs/readme/system_manager/info/README_SERVICE_MANAGER_INFO.md

## 검증 원칙

- 작은 그룹으로 수정하고 즉시 검증합니다.
- 검증 실패 상태에서 다음 단계로 진행하지 않습니다.
- 기본 검증 순서:
  - `./gradlew :simple_system_manager:assembleDebug`
  - `./gradlew :simple_system_manager:compileDebugUnitTestKotlin`
  - `./gradlew :simple_system_manager:testAll`
