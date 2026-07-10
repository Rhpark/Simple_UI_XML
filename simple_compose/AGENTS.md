# simple_compose 모듈 개요
 - **전역 규칙은 루트 AGENTS.md 참조**
 - 주석 스타일, 코딩 컨벤션, 대화 규칙 등은 루트 AGENTS.md를 따름
 - **Maven 좌표**: JitPack `com.github.Rhpark:Simple_UI_Compose` / Maven Central `io.github.rhpark:dash-droid-compose` (appVersion 공유)


 ## 기능 설계 문서
  - 모듈 신설 PRD/SPEC/PLAN: docs/agents/output/plan/260611_135832/ (260611_135832_PRD.md / SPEC.md / PLAN.md)


 ## 모듈 정의
  - **Jetpack Compose 전용 UI 레이어**
  - Compose 고유 패러다임(State, remember, Composable, Modifier) 기준으로 설계된 헬퍼 제공
  - simple_xml 기능의 1:1 치환이 아님 — Compose 방식의 신규 설계
  - simple_core 모듈을 기반으로 동작


 ## 주요 패키지 구조

  ### permissions
   - **PermissionRequestState**: State 기반 권한 요청 API (simple_compose/src/main/java/kr/open/library/simple_ui/compose/permissions/PermissionRequestState.kt)
   - `rememberPermissionRequestState(permissions, gateSettingsNavigation = false)` — 런타임/특수/Role 권한 통합 처리
   - `phase` / `isRequesting` — IDLE/요청/설명/설정 이동/완료 단계를 State로 노출. `isRequesting`은 시스템 UI 대기뿐 아니라 설명·설정 이동 결정을 기다리는 동안에도 true
   - `refresh()` + 호스트 Resume 자동 재확인 — 외부 설정 변경을 반영해 `allGranted`만 재계산하고 `deniedItems`/`phase`는 유지
   - rationale은 콜백이 아닌 **상태 노출**(`rationaleRequired` + `continueRequest()`/`cancelRequest()`)
   - 특수/Role 설정 이동은 기본 즉시 이동, `gateSettingsNavigation = true`면 동의 게이트(`settingsNavigationRequired` + `continueSettingsNavigation()`/`cancelSettingsNavigation()`) — xml `onNavigateToSettings`와 동일 의미
   - 결과 매핑(GRANTED/DENIED/PERMANENTLY_DENIED)은 simple_core `RuntimePermissionDecisionTracker` 단일 출처 사용 (자체 구현 금지)
   - 미판정 권한은 무음 스킵하지 않고 MANIFEST_UNDECLARED 기본값으로 기록 (xml과 동일)
   - 요청 이력·큐·진행 중 결과·최신 완료 `deniedItems` 한 건·`phase`·rationale/설정 게이트 대기 상태는 rememberSaveable로 구성 변경에 보존
   - XML의 requestId별 orphaned 결과 목록과 Compose의 최신 `deniedItems`는 저장 구조가 다르며, 동일 API로 취급하지 않음

  ### state
   - **VmEventCollect**: BaseViewModelEvent 이벤트 채널·임의 Flow의 라이프사이클 인식 수집 (simple_compose/src/main/java/kr/open/library/simple_ui/compose/state/VmEventCollect.kt)
   - `CollectVmEvent` / `CollectAsEffect` — LaunchedEffect + repeatOnLifecycle 기반 effect 수집
   - `CollectAsEffect`를 단발 효과에 사용할 때는 재수집 시 값을 다시 내보내지 않는 이벤트 Flow(Channel 또는 replay 없는 hot Flow 등)를 전달
   - `collectAsStateWithLifecycle`은 androidx 제공이므로 재제공하지 않음

  ### systembars
   - **SystemBarsStyle**: 시스템 바 아이콘 명암(appearance) 제어 + 이탈 시 복원 (simple_compose/src/main/java/kr/open/library/simple_ui/compose/systembars/SystemBarsStyle.kt)
   - compileSdk 35 enforced edge-to-edge 대응: 색상 설정 API 미사용

  ### scroll
   - **ScrollStateHelpers**: LazyList 스크롤 방향·엣지 감지 (simple_compose/src/main/java/kr/open/library/simple_ui/compose/scroll/ScrollStateHelpers.kt)
   - `rememberScrollDirectionState`(기본 임계값 20px) / `rememberEdgeReachedState`(기본 10px) — simple_xml RecyclerScrollStateView와 동일 값 정책
   - 축은 `LazyListState.layoutInfo.orientation`으로 자동 감지, 축 불일치 엣지는 항상 false
   - 엣지 임계값은 시작 엣지(TOP/LEFT)에만 적용하고, 끝 엣지(BOTTOM/RIGHT)는 `canScrollForward`로 판정
   - XML RecyclerScrollStateView와 동일하게 실제 스크롤 세션 종료 시 방향은 IDLE로 복귀
   - 방향은 **스크롤 모션만** 반영 — `isScrollInProgress == false`인 즉시 위치 재설정·데이터 변경은 방향 미발행, `true`인 프로그램적 애니메이션은 모션으로 처리


 ## 핵심 설계 원칙 (Compose 효과 규칙)
  - `derivedStateOf`/`snapshotFlow` 계산 블록 안에서 상태를 쓰지 않는다 (부수효과 금지)
  - 나중에 호출되는 람다 파라미터는 `rememberUpdatedState`로 최신화한다
  - 파라미터 변경에 반응해야 하는 적용 로직은 효과(`LaunchedEffect` 등)의 키에 파라미터를 포함한다
  - 원본 저장/복원이 필요한 설정은 `DisposableEffect`로 컴포지션 수명과 일치시킨다


 ## 금지 패턴
  - simple_xml / simple_system_manager 모듈 의존 금지
  - Material3 및 accompanist 등 외부 Compose 라이브러리 의존 금지 (ui/foundation/runtime/activity-compose/lifecycle-runtime-compose만 사용)
  - 시스템 바 색상 설정 API(window.statusBarColor 등 deprecated) 사용 금지
  - androidx가 이미 제공하는 API(collectAsStateWithLifecycle, statusBarsPadding 등) 중복 제공 금지
  - 권한 결과 매핑 규칙 자체 구현 금지 — simple_core RuntimePermissionDecisionTracker 사용


 ## 모듈 의존성 규칙

  ### 허용되는 의존성
   - simple_core 모듈 (모든 기능 사용 가능)
   - Compose BOM 2024.12.01 고정 (Kotlin 2.0.21 호환): ui, foundation, runtime
   - androidx.activity:activity-compose, androidx.lifecycle:lifecycle-runtime-compose
   - 공개 API에 노출되는 simple_core/ui/foundation/runtime/lifecycle은 `api`, 내부 launcher용 activity-compose는 `implementation`


 ## simple_core와의 관계

  ### simple_compose가 사용하는 simple_core 기능
   - Logx: 모든 로깅
   - permissions 레이어 전체: PermissionClassifier / SpecialPermissionHandler / RolePermissionHandler / PermissionQueue / RuntimePermissionDecisionTracker / PermissionDeniedItem·Type / hasPermission
   - BaseViewModelEvent: 이벤트 채널 수집 대상
   - safeCatch, checkSdkVersion 등 공통 확장


 ## 테스트 작성 규칙
  - 디렉터리: src/test/java/.../unit/ (순수 로직), src/test/java/.../robolectric/ (createComposeRule 기반 Compose 테스트)
  - Robolectric + compose-ui-test-junit4 조합 사용, 테스트 호스트 Activity는 src/test/AndroidManifest.xml에 등록
  - 실행: `./gradlew :simple_compose:testUnit` / `:simple_compose:testRobolectric` / `:simple_compose:testAll`
