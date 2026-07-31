# 실기기 검증 결과

## 배포 게이트 필드

- 스키마 버전: `1`
- 릴리스 버전: `미입력`
- 검증 대상 소스 SHA: `미입력`
- Android CI 실행 ID: `미입력`
- 최종 상태: `DRAFT`
- 승인자: `미입력`
- 승인 일시: `미입력`

위 필드명과 한 줄 형식은 배포 게이트가 읽으므로 변경하지 않습니다.

## 아티팩트

| 종류 | 파일명 | SHA-256 | 빌드 유형 | 서명 상태 |
| --- | --- | --- | --- | --- |
| debug 앱 APK | `미입력` | `미입력` | debug | `미입력` |
| 계측 테스트 APK | `미입력` | `미입력` | debugAndroidTest | `미입력` |
| verification APK | `미입력` | `미입력` | verification | `미입력` |

## 실행 정보

- 결과 문서 Git 커밋 SHA: `미입력`
- 검증 일시·시간대: `미입력`
- 테스터: `미입력`
- Gradle 명령: `미입력`
- Gradle 종료 상태: `미입력`
- HTML/XML 결과 경로: `미입력`

## 단말 환경

| 항목 | 값 |
| --- | --- |
| 슬롯 | `PHY-01` |
| 제조사·모델 | Samsung `SM-G977N` |
| 일련번호 | `R3CM50A575V` |
| Android/API | Android 12 / API 31 |
| ABI | `arm64-v8a` |
| 보안 패치 | `미입력` |
| SIM | 없음 |
| Wi-Fi | `미입력` |
| 위치 | `미입력` |
| 배터리 | `미입력` |
| 절전 모드 | `미입력` |

## 시나리오 결과

- `AUTO_DEVICE`의 수동 결과와 `MANUAL_DEVICE`의 자동 결과는 `N/A`로 기록합니다.
- `HYBRID_DEVICE`는 자동 결과와 수동 결과가 모두 `PASS`일 때만 최종 결과를 `PASS`로 기록합니다.
- 자동·수동 결과 중 하나라도 `FAIL`, `BLOCKED`, `NOT_RUN`이면 최종 결과를 `PASS`로 기록하지 않습니다.

| 시나리오 ID | 우선순위 | 방식 | 자동 결과 | 수동 결과 | 최종 결과 | 증빙 | 이슈 | 예외 승인 ID | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `CORE-P0-001` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `CORE-P0-002` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `CORE-P0-003` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `CORE-P0-004` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `CORE-P1-001` | P1 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-001` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-002` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-003` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-004` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-005` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-006` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-007` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-008` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-009` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-010` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-011` | P0 | `MANUAL_DEVICE` | `N/A` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P0-012` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `SYS-P1-001` | P1 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `SYS-P2-001` | P2 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `COMPOSE-P0-001` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `COMPOSE-P0-002` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `COMPOSE-P0-003` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `COMPOSE-P0-004` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `COMPOSE-P0-005` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `COMPOSE-P1-001` | P1 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `XML-P0-001` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `XML-P0-002` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `XML-P0-003` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `XML-P0-004` | P0 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `XML-P0-005` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `XML-P0-006` | P0 | `AUTO_DEVICE` | `NOT_RUN` | `N/A` | `NOT_RUN` |  |  |  |  |
| `XML-P1-001` | P1 | `MANUAL_DEVICE` | `N/A` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |
| `XML-P1-002` | P1 | `HYBRID_DEVICE` | `NOT_RUN` | `NOT_RUN` | `NOT_RUN` |  |  |  |  |

## 환경 미보유 예외

| 예외 승인 ID | 시나리오 ID | 필요한 환경 | 미보유 사유 | 영향 API·위험 | 완화 조치 | 승인자·일시 | 만료 릴리스 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `미입력` | `SYS-P1-001` | 실제 SIM | 테스트 단말에 SIM 없음 | `미입력` | `SYS-P0-009` 결과 확인 | `미입력` | `현재 릴리스` |

## 집계

- P0 전체·PASS: `27 / 0`
- P1 전체·PASS·예외: `5 / 0 / 0`
- P2 전체·실행: `1 / 0`
- `FAIL`: `0`
- `BLOCKED`: `0`
- `NOT_RUN`: `33`
- 단말 상태 복구: `미확인`
- 대상 커밋·아티팩트 연결: `미확인`

## 최종 판정 근거

- 판정 내용: `미입력`
- 남은 위험: `미입력`
- 다음 릴리스 재승인 항목: `SYS-P1-001`
- 정정 이력: `없음`
