# Simple UI Compose Guide

## Module Information (모듈 정보)

- **Module**: `simple_compose`
- **Maven Central**: `io.github.rhpark:dash-droid-compose:0.4.16`
- **Role**: Compose 환경에서 권한, 이벤트/effect Flow, 시스템 바, LazyList 상태 처리의 보일러플레이트를 줄입니다.
- `simple_core`는 전이 의존성으로 제공되므로 앱에서 직접 API를 사용하지 않는다면 별도로 추가할 필요가 없습니다.

## Installation (설치)

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("io.github.rhpark:dash-droid-compose:0.4.16")
}
```

## Permission State (권한 상태)

`rememberPermissionRequestState`는 런타임 권한, 특수 권한, Role 권한을 한 요청 흐름에서 처리합니다.

```kotlin
@Composable
fun CameraPermissionContent() {
    val permissionState = rememberPermissionRequestState(
        permissions = listOf(Manifest.permission.CAMERA),
        gateSettingsNavigation = true,
    )

    Button(
        enabled = !permissionState.isRequesting,
        onClick = {
            permissionState.request { deniedItems ->
                // 모두 승인되면 빈 목록입니다.
            }
        },
    ) {
        Text(if (permissionState.allGranted) "Granted" else "Request")
    }

    if (permissionState.rationaleRequired.isNotEmpty()) {
        Row {
            Button(onClick = permissionState::continueRequest) { Text("Continue") }
            Button(onClick = permissionState::cancelRequest) { Text("Cancel") }
        }
    }

    if (permissionState.settingsNavigationRequired != null) {
        Row {
            Button(onClick = permissionState::continueSettingsNavigation) { Text("Open settings") }
            Button(onClick = permissionState::cancelSettingsNavigation) { Text("Cancel") }
        }
    }
}
```

### Request phase (요청 단계)

`phase`는 다음 상태를 가집니다.

- `IDLE`: 첫 요청 전 또는 새 State의 초기 상태
- `REQUESTING`: 시스템 권한 UI 또는 설정 결과 대기
- `RATIONALE_REQUIRED`: 설명 UI 결정 대기
- `SETTINGS_NAVIGATION_REQUIRED`: 설정 이동 동의 대기
- `COMPLETED`: 마지막 요청 완료. 다음 요청이 시작될 때까지 유지

`isRequesting`은 `REQUESTING`, `RATIONALE_REQUIRED`, `SETTINGS_NAVIGATION_REQUIRED`에서 `true`입니다.

### State restoration and external changes (상태 복원과 외부 변경)

- 요청 이력, 대기 큐, 진행 결과, 동의 대기 상태와 최신 완료 결과 한 건(`deniedItems`, `phase`)은
  `rememberSaveable`로 보존됩니다.
- 구성 변경으로 결과 콜백이 소실되어도 해당 State의 최신 완료 결과는 `deniedItems`와
  `COMPLETED` phase에서 확인할 수 있습니다.
- `request` 콜백은 현재 살아 있는 요청의 즉시 처리에 사용하고, `deniedItems`는 UI 표시·복원 확인용
  최신 State로 사용합니다. `deniedItems`는 소비 후 제거되는 이벤트 큐가 아닙니다.
- 호스트가 Resume되면 외부 설정 변경을 반영해 `allGranted`만 자동으로 다시 계산합니다.
- 즉시 재확인이 필요하면 `refresh()`를 호출합니다. 이 함수도 `allGranted`만 재계산하며
  마지막 요청 결과인 `deniedItems`와 `phase`는 변경하지 않습니다.

### XML and Compose parity (XML과 Compose 기능 대응)

| simple_xml | simple_compose |
| --- | --- |
| `onDeniedResult` | `request` 콜백 + `deniedItems` |
| `saveState` / `restoreState` | `rememberSaveable`을 통한 Compose State 저장·복원 |
| 콜백을 잃은 requestId별 orphaned 결과 목록 | 해당 State의 최신 `deniedItems` 한 건 + `COMPLETED` phase |
| `onRationaleNeeded` | `rationaleRequired` + continue/cancel |
| `onNavigateToSettings` | `settingsNavigationRequired` + continue/cancel |

결과 타입, 사전 판정과 거부 목록 생성 정책은 `simple_core`를 통해 공유합니다. 다만 XML은 여러 요청의
orphaned 결과를 requestId별로 소비하고, Compose는 State 인스턴스마다 최신 결과 한 건을 유지합니다.
두 방식은 콜백 소실 후 결과를 다시 확인한다는 목적은 같지만 저장 구조와 소비 API는 동일하지 않습니다.

## Lifecycle-aware Effects (라이프사이클 인식 효과)

`CollectVmEvent`는 `BaseViewModelEvent`의 단발 UI 이벤트라는 의도를 호출부에서 명확하게 표현합니다.

```kotlin
viewModel.CollectVmEvent { event ->
    when (event) {
        is ScreenEvent.Navigate -> navigator.navigate(event.route)
        is ScreenEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
    }
}
```

임의의 `Flow`는 `CollectAsEffect`로 수집할 수 있습니다.

```kotlin
viewModel.effectFlow.CollectAsEffect { effect ->
    handleEffect(effect)
}
```

`CollectAsEffect`는 라이프사이클이 다시 활성화될 때 Flow를 재수집합니다. 네비게이션·토스트·스낵바처럼
한 번만 실행해야 하는 효과에는 재수집 시 값을 다시 방출하지 않는 이벤트 Flow를 사용하세요. Cold Flow나
replay가 있는 `SharedFlow`는 같은 효과를 다시 실행할 수 있습니다. `minActiveState`에는 `CREATED`,
`STARTED`, `RESUMED`만 사용합니다.

화면 상태는 AndroidX의 `collectAsStateWithLifecycle`을 사용합니다. `simple_compose`는 동일 기능을 다시 제공하지 않습니다.

## System Bars and Edge-to-edge (시스템 바와 Edge-to-edge)

Android 15 이전 버전까지 일관된 edge-to-edge 동작이 필요하면 Activity에서 먼저 활성화합니다.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}
```

화면별 아이콘 명암은 `SystemBarsStyle`로 적용합니다. 호출 지점이 컴포지션에 진입할 때의 값을 캡처하고
이탈할 때 복원하므로, 한 Window에서는 현재 화면을 대표하는 호출 한 곳만 활성화합니다.

```kotlin
@Composable
fun LightSurfaceScreen() {
    SystemBarsStyle(statusBarDarkIcons = true)

    Box(modifier = Modifier.systemBarsPadding()) {
        ScreenContent()
    }
}
```

Inset 패딩은 AndroidX의 `statusBarsPadding`, `navigationBarsPadding`, `systemBarsPadding`을 직접 사용합니다.

## LazyList Scroll State (LazyList 스크롤 상태)

```kotlin
@Composable
fun MessageList() {
    val listState = rememberLazyListState()
    val direction by rememberScrollDirectionState(listState)
    val isBottom by rememberEdgeReachedState(listState, ScrollEdge.BOTTOM)

    LazyColumn(state = listState) {
        // items(...)
    }
}
```

- 방향 임계값 기본값은 20px이며 0 이상이어야 합니다.
- 엣지 임계값 기본값은 10px이며 0 이상이어야 합니다. 이 값은 `TOP`/`LEFT`에만 적용되고,
  `BOTTOM`/`RIGHT`는 `canScrollForward`로 판정합니다.
- 수직 리스트는 `UP`/`DOWN`, 수평 리스트는 `LEFT`/`RIGHT`를 사용합니다.
- 실제 스크롤 세션이 종료되면 방향은 `IDLE`로 복귀합니다.
- `isScrollInProgress == false`인 인덱스 변경은 방향을 변경하지 않습니다. 프로그램적 애니메이션처럼
  이 값이 `true`인 이동은 스크롤 모션으로 처리합니다.
- 리스트 축과 맞지 않는 엣지는 항상 `false`입니다.

## Verification (검증)

```bash
./gradlew :simple_compose:testAll
./gradlew :simple_compose:lintDebug
./gradlew :simple_compose:apiCheck
./gradlew :simple_compose:assemble
```
