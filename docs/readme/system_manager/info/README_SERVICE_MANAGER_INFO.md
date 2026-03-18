# System Service Manager Info vs Plain Android - Complete Comparison Guide
> **System Service Manager Info vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_system_manager`
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.*`, `kr.open.library.simple_ui.xml.system_manager.info.*`

<br></br>

## Overview (개요)
Provides Flow/Callback-based system information collection helpers owned by `simple_system_manager`.  
> `simple_system_manager`가 제공하는 Flow/Callback 기반 시스템 정보 수집 helper를 정리합니다.

<br></br>

## At a Glance (한눈 비교)
| Info | Origin | Doc |
|---|---|---|
| Battery Info | core-origin | [core/README_BATTERY_INFO.md](core/README_BATTERY_INFO.md) |
| Location Info | core-origin | [core/README_LOCATION_INFO.md](core/README_LOCATION_INFO.md) |
| SIM Info | core-origin | [core/README_SIM_INFO.md](core/README_SIM_INFO.md) |
| Telephony Info | core-origin | [core/README_TELEPHONY_INFO.md](core/README_TELEPHONY_INFO.md) |
| Network Connectivity Info | core-origin | [core/README_NETWORK_INFO.md](core/README_NETWORK_INFO.md) |
| Display Info | xml-origin | [xml/README_DISPLAY_INFO.md](xml/README_DISPLAY_INFO.md) |

<br></br>

## Why It Matters (중요한 이유)
- Flow/Callback 기반으로 시스템 상태를 실시간 수집할 수 있습니다.
- SDK 분기, 리소스 조회, receiver/emitter 구성 같은 반복 설정을 줄입니다.
- 위치 정보처럼 라이프사이클과 권한 계약이 까다로운 기능을 모듈 내부에서 정리합니다.

<br></br>

## Feature Docs (기능별 문서)
### Core-origin Info
- Battery: [README_BATTERY_INFO.md](core/README_BATTERY_INFO.md)
- Location: [README_LOCATION_INFO.md](core/README_LOCATION_INFO.md)
- SIM: [README_SIM_INFO.md](core/README_SIM_INFO.md)
- Telephony: [README_TELEPHONY_INFO.md](core/README_TELEPHONY_INFO.md)
- Network Connectivity: [README_NETWORK_INFO.md](core/README_NETWORK_INFO.md)

### XML-origin Info
- Display: [README_DISPLAY_INFO.md](xml/README_DISPLAY_INFO.md)
- Display PRD: [PRD.md](../../../../simple_system_manager/docs/feature/system_manager/info/display/PRD.md)
- Display SPEC: [SPEC.md](../../../../simple_system_manager/docs/feature/system_manager/info/display/SPEC.md)
- Display Plan: [IMPLEMENTATION_PLAN.md](../../../../simple_system_manager/docs/feature/system_manager/info/display/IMPLEMENTATION_PLAN.md)

<br></br>

## Permission Guide (권한 가이드)
- 권한 요구사항과 정책은 [README_PERMISSION.md](../../README_PERMISSION.md)를 참조하십시오.

<br></br>

## Example (예제)
- [ServiceManagerInfoActivity.kt](../../../../app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/info/ServiceManagerInfoActivity.kt)

<br></br>

## Related Docs (관련 문서)
- Info docs: `docs/readme/system_manager/info/`
- Feature docs: `simple_system_manager/docs/feature/system_manager/info/`
