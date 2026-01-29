# System Service Manager Controller vs Plain Android - Complete Comparison Guide
> **System Service Manager Controller vs 순수 Android - 완벽 비교 가이드**
> **System Service Manager Controller**는 `simple_core`의 컨트롤러와 `simple_xml`의 Context 확장 함수로 구성됩니다.
> 이 문서는 **핵심만 빠르게 확인**할 수 있도록 요약했고, 상세 내용은 별도 문서로 분리했습니다.

## 📦 Module Information (모듈 정보)
- **Module**: `simple_core`, `simple_xml`

<br></br>

## 🔎 At a Glance (한눈 비교)

### simple_core (Controller)
| 컨트롤러 | 역할 요약 | 상세 문서 |
|---|---|---|
| AlarmController | 알람 등록/삭제/존재 확인 | [core/README_ALARM_CONTROLLER.md](core/README_ALARM_CONTROLLER.md) |
| NotificationController | 알림 표시/진행률/채널 관리 | [core/README_NOTIFICATION_CONTROLLER.md](core/README_NOTIFICATION_CONTROLLER.md) |
| VibratorController | 진동 패턴/프리셋/SDK 분기 처리 | [core/README_VIBRATOR_CONTROLLER.md](core/README_VIBRATOR_CONTROLLER.md) |
| WifiController | WiFi 정보/상태/스캔 | [core/README_WIFI_CONTROLLER.md](core/README_WIFI_CONTROLLER.md) |

### simple_xml (Controller)
| 컨트롤러 | 역할 요약 | 상세 문서 |
|---|---|---|
| SoftKeyboardController | 키보드 표시/숨김/지연, SDK 분기 처리 | [xml/README_SOFTKEYBOARD_CONTROLLER.md](xml/README_SOFTKEYBOARD_CONTROLLER.md) |
| FloatingViewController | 플로팅 뷰 추가/이동/제거 | [xml/README_FLOATING_VIEW_CONTROLLER.md](xml/README_FLOATING_VIEW_CONTROLLER.md) |

**Context 확장 함수 목록:**  
[xml/README_SYSTEM_MANAGER_EXTENSIONS.md](xml/README_SYSTEM_MANAGER_EXTENSIONS.md)

<br></br>

## 💡 Why It Matters (왜 중요한가)

### Eliminate Repetitive Code (반복 코드 제거)
- **System Service Acquisition:** Simplify `getSystemService()` calls with Extension functions
- **Auto SDK Version Handling:** Automatically handle Vibrator/VibratorManager version branching internally
- **Hide Complex Configuration:** Encapsulate Alarm Calendar calculations, Floating View Touch handling, etc.
> - **시스템 서비스 획득**: `getSystemService()` 호출과 Extension 함수로 간단하게
> - **SDK 버전 처리 자동화**: Vibrator/VibratorManager 버전 분기를 내부에서 자동 처리
> - **복잡한 설정 숨김**: Alarm Calendar 계산, Floating View Touch 처리 등을 캡슐화

<br></br>

### Safe Error Handling (안전한 에러 처리)
- **Automatic Exception Handling:** Controller automatically handles exceptions and returns Runtime results
- **Return Result Values:** Return safe Boolean via `tryCatchSystemManager()`
- **Lifecycle Integration:** Automatically cleanup all resources on `onDestroy()`
> - **자동 예외 처리**: Controller 내부에서 자동 예외 처리 후 Runtime 결과 반환
> - **결과 값 리턴**: `tryCatchSystemManager()` 통해 안전한 Boolean 반환
> - **Lifecycle 연동**: `onDestroy()` 시 모든 리소스 자동 정리

<br></br>

### Developer-Friendly Interface (개발자 친화적 인터페이스)
- **Unified API:** Intuitive methods like `show()`, `vibrate()`, `registerAlarmClock()`
- **Consistent Code Style:** Unify all services with Controller pattern
- **Type Safety:** Compile-time error checking support
> - **통합 API 제공**: `show()`, `vibrate()`, `registerAlarmClock()` 등 직관적 메서드
> - **일관된 코드 스타일**: Controller 패턴으로 모든 서비스 통일
> - **타입 안전성**: Compile-time 오류 체크 지원

<br></br>

## 공통 주의사항
- Android 13+ 알림은 `POST_NOTIFICATIONS` 권한이 필요합니다.
- `NotificationChannel` 전달은 필수이며, `createChannel()`은 **이후 생성되는 알림**에만 적용됩니다.
- 진행률 알림을 사용했다면 Activity/Service 종료 시 `cleanup()` 호출을 권장합니다.

<br></br>

## 상세 문서 위치
- core 상세 문서: `docs/readme/system_manager_controller/core/`
- xml 상세 문서: `docs/readme/system_manager_controller/xml/`
