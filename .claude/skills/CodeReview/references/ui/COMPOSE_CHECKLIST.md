# UI 체크리스트 — Jetpack Compose

Compose 기반 코드 리뷰 시 references/ 5단계와 병행한다.
STEP2(기능) 및 STEP4(품질/성능) 시 해당 항목을 추가 적용한다.

---

## 1단계: Recomposition 과도 발생

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| RC1 | `List<T>`, `Map<K,V>` 등 unstable 타입을 Composable 파라미터로 직접 전달 | HIGH | 상위 recomposition마다 불필요한 재구성 |
| RC2 | data class에 `@Stable` / `@Immutable` 어노테이션 누락 | MEDIUM | 컴파일러 최적화 불가 → 불필요한 recomposition |
| RC3 | 복수 State 결합 시 `derivedStateOf` 미사용 | MEDIUM | 입력 State 변경마다 매번 재계산 |
| RC4 | Composable에 일반 함수(non-inline lambda) 전달로 캡처 재생성 | LOW | 불필요한 recomposition |

---

## 2단계: remember / State 관리

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| RM1 | `remember(key)` 키 누락 — 의존 값 변경 시 stale closure 유지 | HIGH | 구식 상태 참조 |
| RM2 | 프로세스 재시작 후 복원 필요한 상태에 `rememberSaveable` 대신 `remember` 사용 | HIGH | 앱 재시작/프로세스 종료 시 상태 유실 |
| RM3 | `mutableStateOf(list.toMutableList())` — `mutableStateListOf` 대신 사용 | HIGH | 리스트 항목 변경이 recomposition 트리거 안 됨 |
| RM4 | `rememberCoroutineScope` 밖에서 coroutine `launch` — Composable 생명주기 무관 | HIGH | 메모리 누수 |

---

## 3단계: Effect 핸들러

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| EF1 | 특정 값 변경 시 재실행 의도인데 `LaunchedEffect(Unit)` 로 key 고정 | HIGH | 값 변경 시 재실행 안 됨 |
| EF2 | `LaunchedEffect` key 불필요하게 많아 매 recomposition 재실행 | HIGH | 의도치 않은 반복 실행 |
| EF3 | `DisposableEffect` `onDispose` 블록 누락 | HIGH | 리스너·센서·구독 등 리소스 미해제 |
| EF4 | `SideEffect` 오남용 — recomposition마다 실행됨을 모르고 상태 읽기 용도로 사용 | MEDIUM | 의도치 않은 타이밍, 부수효과 과다 |

---

## 4단계: State Hoisting

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| SH1 | Leaf Composable 내부에서 `hiltViewModel()` / `viewModel()` 직접 호출 | MEDIUM | Preview 불가, 재사용 불가 |
| SH2 | Stateful Composable이 화면 하단까지 내려가 있음 — hoisting 미적용 | MEDIUM | 재사용·테스트 불가 |
| SH3 | Composable이 직접 상태 변경 — 단방향 데이터 흐름(UDF) 미준수 | HIGH | 예측 불가 상태, 루프 위험 |

---

## 5단계: Modifier 순서 & 기타

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| MD1 | `clickable` 이후에 `padding` 적용 — 터치 영역이 padding 안으로 축소 | HIGH | UX 버그 (터치 영역 의도와 다름) |
| MD2 | `clip(RoundedCornerShape)` 이전에 `background` 적용 | MEDIUM | 모서리 클리핑 전 배경으로 시각 버그 |
| MD3 | `LazyColumn`/`LazyRow` 아이템에 `key {}` 미사용 | HIGH | 아이템 재사용 오류, 전환 애니메이션 깨짐 |
| MD4 | Stateless Composable에 `@Preview` 미작성 | LOW | 시각 검증 불가 |
| MD5 | `LocalContext.current` Composable 내 직접 접근 남용 | MEDIUM | 암묵적 의존성, 테스트 어려움 |
| MD6 | 명시적 파라미터 대신 `CompositionLocalProvider` 과도한 사용 | MEDIUM | 암묵적 의존성, 흐름 추적 어려움 |

---

## 심각도 기준

- HIGH: Recomposition 폭발·상태 유실·UX 버그·리소스 누수·UDF 위반
- MEDIUM: 재사용·테스트 어려움·성능 미세 저하·암묵적 의존성
- LOW: Preview 부재·코드 가독성
