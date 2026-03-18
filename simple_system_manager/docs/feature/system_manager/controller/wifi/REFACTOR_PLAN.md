# Wifi Refactor Plan (Phase 1)

## 문서 정보
- 문서명: Wifi Refactor Plan (Phase 1)
- 작성일: 2026-03-09
- 수정일: 2026-03-09
- 대상 모듈: simple_core
- 현재 패키지: `kr.open.library.simple_ui.core.system_manager.controller.wifi`
- 1차 목표 패키지: `kr.open.library.simple_ui.core.system_manager.info.wifi`
- 임시 작업 패키지: `kr.open.library.simple_ui.core.system_manager.controller.dump.wifiController`
- 상태: 계획(draft)

## 문서 목적
- 현재 `WifiController`의 읽기/조회 책임을 별도 정보 객체로 분리하기 위한 1차 리팩토링 계획을 고정합니다.
- 실제 구현은 먼저 `dump` 패키지에서 작성하고, 검토와 검증이 끝난 뒤 정식 패키지로 반영합니다.
- 본 문서는 `WifiController` 즉시 제거가 아니라, `WifiStatusInfo` 추가와 이행 준비를 1차 목표로 합니다.

## 개발 환경 기준
- 기준 문서: `docs/rules/project/DEV_ENV_RULE.md`
- 모듈 실제 값: `simple_core/build.gradle.kts`
- 확인 결과
  - `compileSdk = 35`
  - `minSdk = 28`
- SDK 판단과 API 사용 가능 범위는 위 값을 기준으로 검토합니다.

## 현재 상태 요약
- `WifiController`는 이름과 달리 읽기/조회 기능 비중이 높고, 현대 Android에서 강한 제어 객체로 보기 어렵습니다.
- 상태 조회, 연결 정보, capability 확인, 스캔, legacy 제어가 한 클래스에 혼재되어 있습니다.
- 클래스 전체 권한 묶음 때문에 읽기 메서드도 불필요하게 강한 권한 제약을 받습니다.
- `NetworkConnectivityInfo`와 역할이 일부 겹쳐 사용자 입장에서 진입점이 모호합니다.

## 최종 방향
- 읽기/조회 기능은 `WifiStatusInfo`로 분리합니다.
- 스캔과 legacy control 성격 기능은 1차 작업에서 이동하지 않습니다.
- `WifiController`는 1차 작업 동안 유지합니다.
- `WifiStatusInfo` 구현, 호출부 이행, 테스트 검증이 끝난 뒤 기존 `WifiController` 대체 또는 제거를 별도 단계에서 진행합니다.
- 장기적으로는 아래 구조를 후보로 둡니다.
  - `WifiStatusInfo`
  - `WifiScanner`
  - `WifiCapabilities`
  - `WifiLegacyController` 또는 `WifiAccessManager`

## 1차 작업 목표
- `WifiStatusInfo`를 신규 추가합니다.
- 읽기/조회 API만 분리합니다.
- `WifiController` 제거 없이 병행 사용 가능한 상태를 만듭니다.
- `dump` 구현으로 설계를 검토한 뒤 정식 패키지로 이동합니다.

## 1차 작업 범위

### 포함
- 신규 클래스 추가
  - `kr.open.library.simple_ui.core.system_manager.info.wifi.WifiStatusInfo`
- 신규 확장 함수 추가
  - `Context.getWifiStatusInfo()`
- 읽기/조회 메서드 분리 대상
  - `isWifiEnabled()`
  - `getWifiState()`
  - `isConnectedWifi()`
  - `getConnectionInfo()`
  - `getDhcpInfo()`
  - `getCurrentSsid()`
  - `getCurrentBssid()`
  - `getCurrentRssi()`
  - `getCurrentLinkSpeed()`
  - `getModernNetworkDetails()`
  - `calculateSignalLevel()`
  - `compareSignalLevel()`
  - `is5GHzBandSupported()`
  - `is6GHzBandSupported()`
  - `isWpa3SaeSupported()`
  - `isEnhancedOpenSupported()`
- 내부 helper 재사용 검토
  - `WifiConnectionInfoProvider`
  - `WifiCapabilityChecker`
  - `WifiOperationGuard`
- 관련 호출부 검토
  - `NetworkConnectivityInfo`
  - 확장 함수 모음
  - 샘플 앱 Wi-Fi 조회 예제
- 관련 문서와 테스트 추가 또는 수정

### 제외
- `startScan()`
- `getScanResults()`
- `setWifiEnabled()`
- `getConfiguredNetworks()`
- `disconnect()`
- `reconnect()`
- `reassociate()`
- modern control API 신규 추가
- 기존 `WifiController` 제거

## 임시 구현 전략

### Step 1. dump 패키지에서 설계 검증
- 현재 임시 파일
  - `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/dump/wifiController/DumpWifiController.kt`
- 이 공간에서 `WifiStatusInfo` 초안과 내부 위임 구조를 먼저 검토합니다.
- `dump` 패키지는 임시 검증용이며 최종 공개 API 경로로 사용하지 않습니다.

### Step 2. 정식 패키지로 이동
- 검토가 끝난 구조만 `info.wifi` 패키지에 반영합니다.
- 정식 반영 시 패키지명과 공개 API 이름은 실제 배포 구조 기준으로 정리합니다.

## 권한 전략
- `WifiStatusInfo`는 읽기/조회 전용 객체로 설계합니다.
- 기존 `WifiController`처럼 클래스 전체에 제어 권한까지 묶지 않습니다.
- 메서드별 실제 요구 권한과 `@RequiresPermission`이 최대한 일치하도록 정리합니다.
- 단순 조회 메서드가 위치 권한 때문에 모두 실패하는 구조는 1차 작업에서 해소 대상으로 봅니다.

## 구현 전략

### M1. 구조 초안 정리
- 상태: 미시작
- `dump` 패키지에서 `WifiStatusInfo` 초안을 작성합니다.
- 기존 `WifiController`에서 어떤 메서드를 분리할지 최종 고정합니다.
- 산출물
  - `dump` 초안 구현
  - 분리 대상 메서드 목록 확정

### M2. 정식 API 추가
- 상태: 미시작
- `info.wifi` 패키지에 `WifiStatusInfo`를 추가합니다.
- `SystemServiceExtensions`에 `getWifiStatusInfo()`를 추가합니다.
- 산출물
  - `WifiStatusInfo`
  - `Context.getWifiStatusInfo()`

### M3. 내부 의존 정리
- 상태: 미시작
- `WifiConnectionInfoProvider`, `WifiCapabilityChecker`, `WifiOperationGuard` 재사용 여부를 확정합니다.
- 필요 시 읽기 전용 책임에 맞게 helper 의존 경계를 다듬습니다.
- 산출물
  - helper 재사용 또는 조정 결정
  - 읽기 전용 책임 기준 메모

### M4. 호출부 이행
- 상태: 미시작
- `NetworkConnectivityInfo`의 Wi-Fi 읽기 의존 지점을 검토합니다.
- 샘플 앱의 정보 조회 예제를 `WifiStatusInfo` 기반으로 이행합니다.
- 1차에서는 `WifiController`를 유지하고, 병행 사용 상태를 허용합니다.
- 산출물
  - 내부 호출부 이행 내역
  - 샘플 갱신 내역

### M5. 테스트 보강
- 상태: 미시작
- `WifiStatusInfo` 전용 Robolectric 테스트를 추가합니다.
- 기존 `WifiController` 테스트는 유지하되, 조회 책임과 제어 책임 경계를 검토합니다.
- 산출물
  - `WifiStatusInfo` 관련 테스트
  - 기존 테스트 영향 범위 정리

### M6. 문서 갱신
- 상태: 미시작
- `README_WIFI_CONTROLLER.md`의 역할 설명을 조정합니다.
- 필요 시 `WifiStatusInfo`용 README 또는 확장 함수 README 반영을 추가합니다.
- 산출물
  - README 갱신
  - 역할 분리 설명 반영

### M7. 제거 준비 상태 점검
- 상태: 미시작
- `WifiStatusInfo`가 기존 조회 API를 충분히 대체하는지 확인합니다.
- 호출부 이행과 테스트가 완료되면, 별도 단계에서 `WifiController` 제거 또는 축소를 진행합니다.
- 산출물
  - 대체 가능 여부 판단
  - 다음 단계 진입 여부 결정

## 완료 기준

### 1차 완료 기준
- `WifiStatusInfo`가 `info.wifi` 패키지에 추가됩니다.
- 읽기/조회 메서드가 `WifiStatusInfo`에서 동작합니다.
- `Context.getWifiStatusInfo()`가 추가됩니다.
- 관련 테스트가 추가되거나 갱신됩니다.
- 문서에서 `WifiController`와 `WifiStatusInfo`의 역할 차이가 설명됩니다.
- `WifiController`는 아직 제거되지 않습니다.

### 최종 대체 단계 진입 조건
- `WifiStatusInfo`가 기존 조회 API를 실사용 기준으로 대체 가능합니다.
- 샘플 및 내부 호출부가 `WifiStatusInfo` 기반으로 검증됩니다.
- 관련 컴파일과 선택 테스트가 모두 통과합니다.
- 위 조건이 충족된 뒤에만 기존 `WifiController` 제거 또는 축소 작업으로 넘어갑니다.

## 검증 계획
- 컴파일
  - `.\gradlew.bat :simple_core:compileDebugKotlin`
  - `.\gradlew.bat :simple_core:compileDebugUnitTestKotlin`
- 테스트
  - `WifiStatusInfo` 관련 Robolectric 테스트 선택 실행
  - 기존 `WifiController` 관련 테스트 중 조회 흐름 영향 범위 선택 실행
- 문서 검증
  - UTF-8 저장 확인
  - 한글 깨짐 문자 여부 확인

## 관련 파일
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/WifiController.kt`
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/WifiNetworkDetails.kt`
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/internal/WifiConnectionInfoProvider.kt`
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/internal/WifiCapabilityChecker.kt`
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/internal/WifiOperationGuard.kt`
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/info/network/connectivity/NetworkConnectivityInfo.kt`
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/extensions/SystemServiceExtensions.kt`
- `simple_system_manager/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/dump/wifiController/DumpWifiController.kt`
- `docs/readme/system_manager/controller/core/README_WIFI_CONTROLLER.md`

## 메모
- 본 문서는 1차 리팩토링 계획서입니다.
- `WifiScanner`, `WifiLegacyController`, `WifiAccessManager` 같은 후속 구조 분리는 본 문서 범위에 포함하지 않습니다.
- 후속 단계에서 최종 상위 facade 또는 controller 구조는 다시 결정합니다.
