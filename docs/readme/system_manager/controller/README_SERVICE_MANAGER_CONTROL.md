# System Manager Controller vs Plain Android - Complete Comparison Guide
> **System Manager Controller vs 순수 Android - 비교 가이드**

`simple_system_manager`는 기존 `simple_core`, `simple_xml`에 흩어져 있던 system manager controller와 확장 진입점을 한 모듈로 정리합니다.  
이 문서는 빠른 개요용 요약 문서이며, 상세 동작은 하위 README와 feature 문서를 참조합니다.

## Module Information (모듈 정보)
- **Module**: `simple_system_manager`

<br></br>

## At a Glance (한눈 비교)

### Core-origin Controller
| Controller | Summary | Docs |
|---|---|---|
| `AlarmController` | Alarm register/remove/exists<br>알람 등록/삭제/존재 확인 | [core/README_ALARM_CONTROLLER.md](core/README_ALARM_CONTROLLER.md) |
| `NotificationController` | Notifications: show/progress/channel<br>알림 표시/진행률/채널 관리 | [core/README_NOTIFICATION_CONTROLLER.md](core/README_NOTIFICATION_CONTROLLER.md) |
| `VibratorController` | Vibration pattern/preset/SDK branching<br>진동 패턴/프리셋/SDK 분기 | [core/README_VIBRATOR_CONTROLLER.md](core/README_VIBRATOR_CONTROLLER.md) |
| `WifiController` | Wi-Fi info/state/scan<br>Wi-Fi 정보/상태/스캔 | [core/README_WIFI_CONTROLLER.md](core/README_WIFI_CONTROLLER.md) |

### XML-origin Controller
| Controller | Summary | Docs |
|---|---|---|
| `SystemBarController` | Status/navigation bar color, visibility, edge-to-edge, insets state<br>상태/내비게이션 바 색상, 가시성, edge-to-edge, insets 상태 | [xml/README_SYSTEMBAR_CONTROLLER.md](xml/README_SYSTEMBAR_CONTROLLER.md) |
| `SoftKeyboardController` | Keyboard request/await contract + resize policy<br>키보드 요청/대기 계약 + resize 정책 | [xml/README_SOFTKEYBOARD_CONTROLLER.md](xml/README_SOFTKEYBOARD_CONTROLLER.md) |
| `FloatingViewController` | Floating view add/move/remove contract<br>플로팅 뷰 추가/이동/제거 계약 | [xml/README_FLOATING_VIEW_CONTROLLER.md](xml/README_FLOATING_VIEW_CONTROLLER.md) |

### Extension Entry Points
- Context/Window 확장 함수 목록: [README_SYSTEM_MANAGER_EXTENSIONS.md](../README_SYSTEM_MANAGER_EXTENSIONS.md)
- System bar 진입 경로:
  - `window.getSystemBarController()`
  - `window.destroySystemBarControllerCache()`

<br></br>

## Why It Matters (왜 중요한가)
- 반복적인 `getSystemService()` 호출과 SDK 분기 코드를 줄입니다.
- system service 제어 로직을 controller 패턴으로 통일해 사용성을 높입니다.
- `simple_core` 공통 유틸을 재사용하면서 system manager 공개 API는 `simple_system_manager`에 모읍니다.

<br></br>

## Common Notes (공통 주의사항)
- Android 13+ 알림은 `POST_NOTIFICATIONS` 권한이 필요합니다.
- `NotificationChannel`은 필수이며 `createChannel()`은 이후 생성되는 알림에만 적용됩니다.
- 진행률 알림을 사용했다면 Activity/Service 종료 시 `cleanup()` 호출을 권장합니다.
- Alarm 트리거 경로도 런타임에서 `POST_NOTIFICATIONS`를 확인하며, 권한이 없으면 알림 표시를 건너뜁니다.
- `VIBRATE`는 일반 권한이므로 매니페스트 선언만 필요하고 런타임 요청은 없습니다.
- `removeAllFloatingView()`는 `first-failure-stop` 전략이며 일반 호출에서 부분 정리 상태가 남을 수 있습니다.
- `window.getSystemBarController()` / `window.destroySystemBarControllerCache()`는 `@MainThread` 계약입니다.

<br></br>

## Document Locations (상세 문서 위치)
- Core-origin docs: `docs/readme/system_manager/controller/core/`
- XML-origin docs: `docs/readme/system_manager/controller/xml/`
- Feature docs: `simple_system_manager/docs/feature/system_manager/controller/`


