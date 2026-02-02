# System Service Manager Info vs Plain Android - Complete Comparison Guide
> **System Service Manager Info vs 순수 Android - 완벽 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_core`, `simple_xml` 
- **Package**: `kr.open.library.simple_ui.core.system_manager.info.*`, `kr.open.library.simple_ui.xml.system_manager.info.*` 

<br></br>

## Overview (개요)
Provides Flow/Callback-based system information collection with helper APIs.  
> Flow/Callback 기반 시스템 정보 수집과 헬퍼 API를 제공합니다.

<br></br>

## At a Glance (한눈 비교)
| Info (정보)                 | Module (모듈) | Doc (문서) |
|---------------------------|---|---|
| Battery Info              | `simple_core` | [core/README_BATTERY_INFO.md](core/README_BATTERY_INFO.md) |
| Location Info             | `simple_core` | [core/README_LOCATION_INFO.md](core/README_LOCATION_INFO.md) |
| SIM Info                  | `simple_core` | [core/README_SIM_INFO.md](core/README_SIM_INFO.md) |
| Telephony Info            | `simple_core` | [core/README_TELEPHONY_INFO.md](core/README_TELEPHONY_INFO.md) |
| Network Connectivity Info | `simple_core` | [core/README_NETWORK_INFO.md](core/README_NETWORK_INFO.md) |
| Display Info              | `simple_xml` | [xml/README_DISPLAY_INFO.md](xml/README_DISPLAY_INFO.md) |

<br></br>

## Why It Matters (중요한 이유)
- **Real-time Updates:** Collect system state in real time with Flow/Callback.
- **Automated Configuration:** Automate complex setup such as SDK branching and resource queries.
- **Developer-Friendly API:** Improve usability with intuitive helper methods.
> - **실시간 업데이트:** Flow/Callback 기반으로 실시간 상태를 수집합니다.
> - **설정 자동화:** SDK 분기/리소스 조회 등 복잡한 설정을 자동화합니다.
> - **개발자 친화 API:** 직관적 헬퍼 메서드로 사용성을 개선합니다.

<br></br>

## Feature Docs (기능별 문서)
### simple_core (Info)
- Battery: [README_BATTERY_INFO.md](core/README_BATTERY_INFO.md)
- Location: [README_LOCATION_INFO.md](core/README_LOCATION_INFO.md)
- SIM: [README_SIM_INFO.md](core/README_SIM_INFO.md)
- Telephony: [README_TELEPHONY_INFO.md](core/README_TELEPHONY_INFO.md)
- Network Connectivity: [README_NETWORK_INFO.md](core/README_NETWORK_INFO.md)

### simple_xml (Info)
- Display: [README_DISPLAY_INFO.md](xml/README_DISPLAY_INFO.md)

<br></br>

## Permission Guide (권한 가이드)
See the permission guide for requirements and policies.  
> 권한 요구사항과 정책은 권한 가이드를 참고하세요.

- [README_PERMISSION.md](../../README_PERMISSION.md)

<br></br>

## Example (예제)
System Service Manager Info example code:  
> System Service Manager Info 예제 코드:

- [ServiceManagerInfoActivity.kt](../../../../app/src/main/java/kr/open/library/simpleui_xml/system_service_manager/info/ServiceManagerInfoActivity.kt)

<br></br>
