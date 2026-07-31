# Simple Compose 공개 API 감사

## 문서 정보
- 문서명: Simple Compose 공개 API 감사
- 작성일: 2026-07-30
- 대상 모듈: `simple_compose`
- 기준 버전: 0.5.1
- 기준 파일: `simple_compose/api/simple_compose.api`
- 상태: 전체 공개 API 감사 및 테스트 기준선 보강 완료

## 목적
- Compose 소비자가 직접 사용하는 공개 계약을 기능별로 분류한다.
- 공개 API 크기와 모듈 의존 경계를 코드 사실에 따라 평가한다.
- 공개 동작의 테스트 공백과 실기기 검증 범위를 기록한다.
- 기존 호환성을 유지하면서 다음 감사의 기준선을 고정한다.

## 범위

### 포함 범위
- `simple_compose/src/main/java`의 Kotlin 소스 파일 6개
- `simple_compose/api/simple_compose.api`의 공개 ABI
- 권한, 이벤트/effect 수집, 시스템 바, LazyList 스크롤 상태 API
- 모듈 테스트와 앱 예제·실기기 검증 코드
- `README_COMPOSE.md`, `README_PERMISSION.md`의 소비자 계약

### 제외 범위
- 공개 API의 삭제, 이름 변경 또는 동작 변경
- `simple_core`, `simple_xml`, `simple_system_manager` 자체 공개 API 감사
- 외부 저장소 소비자의 실제 사용 통계
- 연결 문제로 연기된 실기기 테스트 실행 결과

## 감사 기준
- API 기준선에 포함된 시그니처는 저장소 내부 사용 여부와 관계없이 배포된 공개 계약으로 취급한다.
- AndroidX가 이미 제공하는 `collectAsStateWithLifecycle`, inset Modifier 등은 중복 제공하지 않는다.
- Compose 효과는 상태 계산과 부수효과를 분리하고 컴포지션·Lifecycle 수명에 맞춰 관리한다.
- 공개 API가 `simple_core` 타입을 노출하면 `simple_core`를 `api` 의존성으로 유지한다.
- 단순히 API 개수를 줄이기 위해 기능별 진입점이나 공개 결과 타입을 내부화하지 않는다.

## 감사 결과 요약
- API 기준선은 67줄이며 공개 ABI 클래스 레코드는 8개다.
- 공개 소스는 네 기능 묶음과 이를 구성하는 공개 타입·함수로 한정되어 있다.
- 생성 `BuildConfig`나 `internal` 패키지의 의도하지 않은 공개 ABI는 없다.
- 즉시 제거, Deprecated 또는 패키지 이동이 필요한 공개 API는 발견되지 않았다.
- `simple_core`는 공개 시그니처에 포함되는 타입을 제공하므로 `api(project(":simple_core"))`가 필요하다.
- `activity-compose` 타입은 공개 시그니처에 노출되지 않으므로 `implementation` 의존성이 적절하다.
- 앱 예제는 권한, `CollectVmEvent`, 시스템 바, 스크롤 API를 직접 사용한다.
- `CollectAsEffect`는 앱 예제의 직접 사용처는 없지만 임의의 effect/event `Flow`를 위한 문서화된 독립 진입점이다.

## 공개 API 인벤토리

### permissions
| API | 판정 | 근거 |
| --- | --- | --- |
| `PermissionRequestPhase` | 유지 | 권한 흐름의 IDLE·요청·설명·설정 이동·완료 단계를 공개 State로 표현한다. |
| `PermissionRequestState` | 유지 | 런타임·특수·Role 권한의 상태, 결과와 사용자 결정을 묶는 주 진입점이다. |
| `rememberPermissionRequestState()` | 유지 | Activity Result launcher, `rememberSaveable`, Lifecycle 재확인을 컴포지션에 연결한다. |

`PermissionRequestState`의 public 생성자는 노출하지 않고 `rememberPermissionRequestState()`로만 생성하게 한다.
거부 결과 모델과 판정 정책은 `simple_core`를 사용하며 Compose에서 별도로 복제하지 않는다.
XML의 requestId별 orphaned 결과와 달리 Compose는 State 인스턴스마다 최신 완료 결과 한 건을 복원한다.

### state
| API | 판정 | 근거 |
| --- | --- | --- |
| `CollectVmEvent()` | 유지 | `BaseViewModelEvent`의 단일 소비자 이벤트를 Lifecycle 인식 방식으로 수집한다. |
| `CollectAsEffect()` | 유지 | 임의의 effect/event `Flow`를 `repeatOnLifecycle`로 수집하는 독립 진입점이다. |

두 함수는 상태 바인딩 API가 아니다. 지속 상태는 AndroidX `collectAsStateWithLifecycle`을 사용한다.
재구성 시 `rememberUpdatedState`로 최신 콜백을 사용하며 Lifecycle이 비활성화되면 수집을 중단한다.

### systembars
| API | 판정 | 근거 |
| --- | --- | --- |
| `SystemBarsStyle()` | 유지 | compileSdk 35의 edge-to-edge 환경에서 deprecated 색상 API 없이 아이콘 명암만 제어한다. |

원래 상태·내비게이션 바 아이콘 명암은 컴포지션 진입 시 각각 저장하고 이탈 시 복원한다.
한 Window에서 여러 호출을 동시에 활성화할 때의 복원 순서 영향은 KDoc과 README에 제한 사항으로 명시되어 있다.

### scroll
| API | 판정 | 근거 |
| --- | --- | --- |
| `ScrollDirection` | 유지 | 수직·수평 방향과 유휴 상태를 표현하는 공개 결과 타입이다. |
| `ScrollEdge` | 유지 | 시작·끝 엣지 감시 대상을 표현하는 공개 매개변수 타입이다. |
| `rememberScrollDirectionState()` | 유지 | LazyList의 스크롤 모션과 방향 임계값을 State로 제공한다. |
| `rememberEdgeReachedState()` | 유지 | LazyList 축과 시작·끝 엣지 도달 여부를 State로 제공한다. |

스크롤 축은 `LazyListState.layoutInfo.orientation`에서 판정한다. 프로그램적 즉시 점프는 방향을 발행하지 않고,
스크롤 모션 중 아이템 경계 통과와 애니메이션은 방향에 반영한다. 임계값은 0 이상만 허용한다.

## 테스트 기준선

### 실행 결과
- `./gradlew :simple_compose:testAll`
  - Unit: 27개
  - Robolectric: 56개
  - 합계: 83개
  - 실패·오류·건너뜀: 0개
- `./gradlew :simple_compose:koverXmlReportDebug`
  - Line: 439/457, 96.1%
  - Branch: 216/289, 74.7%
  - Method: 59/59, 100%
  - Class: 17/17, 100%

### 패키지별 Line 기준선
| 패키지 | Line | 판정 |
| --- | ---: | --- |
| `permissions` | 300/318, 94.3% | 공개 흐름·복원·실패 경계를 직접 검증한다. |
| `state` | 15/15, 100% | Lifecycle 중단·재개와 최신 콜백 교체를 검증한다. |
| `systembars` | 22/22, 100% | 독립 설정·복원과 Window 부재 경계를 검증한다. |
| `scroll` | 102/102, 100% | 방향·엣지·임계값·아이템 경계 이동을 검증한다. |

### 이번 보강 항목
- 진행 중 권한 중복 요청이 첫 콜백과 진행 상태를 유지하는지 검증했다.
- 대기 상태가 없는 권한 결정 메서드와 늦은 결과가 완료 상태를 오염시키지 않는지 검증했다.
- 플랫폼 결과 맵에서 권한이 누락돼도 보유 상태를 재확인하여 완료하는지 검증했다.
- 설정 이동 동의 후 launcher가 없으면 `FAILED_TO_LAUNCH_SETTINGS`로 완료하는지 검증했다.
- `CollectAsEffect`가 STARTED 아래에서 중단되고 재진입 시 재개되는지 검증했다.
- 재구성으로 콜백이 바뀌어도 Flow 수집을 재시작하지 않고 최신 콜백을 사용하는지 검증했다.
- 시스템 바 두 아이콘 명암의 독립 복원과 Activity Window 부재 동작을 검증했다.
- 애니메이션의 정·역방향 아이템 경계 통과와 0px 임계값 경계를 검증했다.

## 문서 감사
- `README_COMPOSE.md`는 네 기능 묶음의 설치·사용·제약을 현재 코드와 일치하게 설명한다.
- `README_PERMISSION.md`는 XML의 requestId별 orphaned 결과와 Compose의 최신 State 결과 복원을 구분한다.
- `simple_compose/AGENTS.md`의 존재하지 않는 초기 설계 문서 경로를 현재 감사·소비자 문서 경로로 교체했다.

## 실기기 검증 상태
- 앱에는 렌더링, 권한, Lifecycle, 시스템 바, 스크롤 Compose 실기기 시나리오가 존재한다.
- 연결된 테스트 단말에서 실행하는 단계는 장치 연결 문제로 연기되어 있으며 통과로 기록하지 않는다.
- 다음 실기기 검증에서는 자동 시나리오와 특수 권한 설정 복귀 수동 시나리오를 다시 승인받아 실행한다.

## 현 단계 결론
- `simple_compose`의 공개 표면은 기능 대비 과도하지 않으며 8개 ABI 클래스 레코드를 모두 유지한다.
- 공개 API 삭제·내부화·Deprecated 추가는 필요하지 않다.
- 이번 단계는 라이브러리 본문 동작을 변경하지 않고 테스트와 문서 기준선만 보강했다.
- 후속 우선순위는 연기된 실기기 검증이며, 새로운 기능 요구가 생기기 전까지 공개 API 확장은 보류한다.
