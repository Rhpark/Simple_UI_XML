# Simple UI Compose Guide

## Module Information (모듈 정보)

- **Module**: `simple_compose`
- **Maven Central**: `io.github.rhpark:dash-droid-compose:0.5.0`
- **Role**: Reduces boilerplate for permissions, ViewModel event/effect Flow collection, system bars, and LazyList scroll state in Compose screens.

> - **모듈**: `simple_compose`
> - **Maven Central**: `io.github.rhpark:dash-droid-compose:0.5.0`
> - **역할**: Compose 화면에서 권한, ViewModel 이벤트/effect Flow 수집, 시스템 바, LazyList 스크롤 상태 처리의 보일러플레이트를 줄입니다.

`simple_core` is provided as a transitive dependency. Add it directly only when app source code imports Core APIs or Core model types.

> `simple_core`는 전이 의존성으로 제공됩니다. 앱 코드에서 Core API나 Core 모델 타입을 직접 import하는 경우에만 별도로 추가하세요.

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
    implementation("io.github.rhpark:dash-droid-compose:0.5.0")
}
```

## Permission State (권한 상태)

`rememberPermissionRequestState` handles runtime permissions, special permissions, and Role permissions in one request flow.

> `rememberPermissionRequestState`는 런타임 권한, 특수 권한, Role 권한을 하나의 요청 흐름에서 처리합니다.

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
                // Empty when every requested permission is granted.
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

### Request Phase (요청 단계)

`phase` exposes the current permission request step.

> `phase`는 현재 권한 요청 단계를 나타냅니다.

- `IDLE`: Initial state before the first request or a new State instance.
- `REQUESTING`: Waiting for a system permission UI result or settings result.
- `RATIONALE_REQUIRED`: Waiting for the app's rationale UI decision.
- `SETTINGS_NAVIGATION_REQUIRED`: Waiting for the user's settings navigation decision.
- `COMPLETED`: The last request has completed. This phase remains until the next request starts.

> - `IDLE`: 첫 요청 전 또는 새 State 인스턴스의 초기 상태입니다.
> - `REQUESTING`: 시스템 권한 UI 또는 설정 결과를 기다리는 상태입니다.
> - `RATIONALE_REQUIRED`: 앱의 rationale UI 결정을 기다리는 상태입니다.
> - `SETTINGS_NAVIGATION_REQUIRED`: 사용자의 설정 이동 동의 여부를 기다리는 상태입니다.
> - `COMPLETED`: 마지막 요청이 완료된 상태입니다. 다음 요청이 시작될 때까지 유지됩니다.

`isRequesting` is `true` during `REQUESTING`, `RATIONALE_REQUIRED`, and `SETTINGS_NAVIGATION_REQUIRED`.

> `isRequesting`은 `REQUESTING`, `RATIONALE_REQUIRED`, `SETTINGS_NAVIGATION_REQUIRED` 단계에서 `true`입니다.

### State Restoration And External Changes (상태 복원과 외부 변경)

- Request history, pending queue, progress result, pending user decisions, and the latest completion result (`deniedItems`, `phase`) are saved with `rememberSaveable`.
- Even when a result callback is lost after a configuration change, the latest completed result can be read from `deniedItems` and the `COMPLETED` phase.
- Use the `request` callback for the currently active request. Use `deniedItems` as the latest restored UI state. `deniedItems` is not an event queue that is removed after consumption.
- When the host resumes, only `allGranted` is recalculated to reflect external settings changes.
- Call `refresh()` when immediate re-checking is needed. It recalculates only `allGranted`; it does not change the latest request result stored in `deniedItems` and `phase`.

> - 요청 이력, 대기 큐, 진행 결과, 사용자 동의 대기 상태와 최신 완료 결과(`deniedItems`, `phase`)는 `rememberSaveable`로 저장됩니다.
> - 구성 변경으로 결과 콜백이 소실되어도 최신 완료 결과는 `deniedItems`와 `COMPLETED` phase에서 확인할 수 있습니다.
> - `request` 콜백은 현재 살아 있는 요청의 즉시 처리에 사용하고, `deniedItems`는 복원 가능한 최신 UI 상태로 사용합니다. `deniedItems`는 소비 후 제거되는 이벤트 큐가 아닙니다.
> - 호스트가 Resume되면 외부 설정 변경을 반영하기 위해 `allGranted`만 자동으로 다시 계산합니다.
> - 즉시 재확인이 필요하면 `refresh()`를 호출합니다. 이 함수는 `allGranted`만 다시 계산하며, 마지막 요청 결과인 `deniedItems`와 `phase`는 변경하지 않습니다.

### XML And Compose Parity (XML과 Compose 기능 대응)

| simple_xml | simple_compose |
| --- | --- |
| `onDeniedResult` | `request` callback + `deniedItems` |
| `saveState` / `restoreState` | Compose State persistence through `rememberSaveable` |
| Orphaned result list by requestId after callback loss | Latest `deniedItems` + `COMPLETED` phase per State instance |
| `onRationaleNeeded` | `rationaleRequired` + continue/cancel |
| `onNavigateToSettings` | `settingsNavigationRequired` + continue/cancel |

Both XML and Compose share result types, pre-check policy, and denied-item creation through `simple_core`. XML can consume multiple orphaned results by requestId, while Compose keeps the latest result per State instance. The goal is the same, but the storage model and consumption API are different.

> 결과 타입, 사전 판정과 거부 목록 생성 정책은 `simple_core`를 통해 공유합니다. XML은 여러 요청의 orphaned 결과를 requestId별로 소비할 수 있고, Compose는 State 인스턴스마다 최신 결과 한 건을 유지합니다. 목적은 같지만 저장 구조와 소비 API는 다릅니다.

## Lifecycle-Aware Effects (라이프사이클 인식 효과)

`CollectVmEvent` makes it explicit that the collected Flow is a one-time UI event from `BaseViewModelEvent`.

> `CollectVmEvent`는 `BaseViewModelEvent`에서 전달되는 값이 단발 UI 이벤트라는 의도를 호출부에서 명확하게 표현합니다.

```kotlin
viewModel.CollectVmEvent { event ->
    when (event) {
        is ScreenEvent.Navigate -> navigator.navigate(event.route)
        is ScreenEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
    }
}
```

Use `CollectAsEffect` for arbitrary `Flow` values that should be handled as lifecycle-aware effects.

> 임의의 `Flow`를 라이프사이클 인식 effect로 처리해야 할 때는 `CollectAsEffect`를 사용합니다.

```kotlin
viewModel.effectFlow.CollectAsEffect { effect ->
    handleEffect(effect)
}
```

`CollectAsEffect` recollects the Flow when the lifecycle becomes active again. For effects that must run only once, such as navigation, Toast, or Snackbar, use an event Flow that does not re-emit the same value on recollection. Cold Flow or `SharedFlow` with replay can run the same effect again. Use only `CREATED`, `STARTED`, or `RESUMED` for `minActiveState`.

> `CollectAsEffect`는 라이프사이클이 다시 활성화될 때 Flow를 재수집합니다. 네비게이션, 토스트, 스낵바처럼 한 번만 실행해야 하는 효과에는 재수집 시 같은 값을 다시 방출하지 않는 이벤트 Flow를 사용하세요. Cold Flow나 replay가 있는 `SharedFlow`는 같은 효과를 다시 실행할 수 있습니다. `minActiveState`에는 `CREATED`, `STARTED`, `RESUMED`만 사용합니다.

Use AndroidX `collectAsStateWithLifecycle` for persistent screen state. `simple_compose` does not duplicate that API.

> 지속적으로 보존하고 관찰해야 하는 화면 상태에는 AndroidX의 `collectAsStateWithLifecycle`을 사용하세요. `simple_compose`는 동일 기능을 다시 제공하지 않습니다.

## System Bars And Edge-To-Edge (시스템 바와 Edge-to-edge)

Enable edge-to-edge in the Activity first when consistent behavior is needed before Android 15.

> Android 15 이전 버전까지 일관된 edge-to-edge 동작이 필요하면 Activity에서 먼저 활성화합니다.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}
```

Apply per-screen system bar icon contrast with `SystemBarsStyle`. It captures the current style when the composable enters composition and restores it when the composable leaves composition. Keep one active call that represents the current screen for a Window.

> 화면별 시스템 바 아이콘 명암은 `SystemBarsStyle`로 적용합니다. 컴포저블이 컴포지션에 진입할 때 현재 스타일을 캡처하고, 이탈할 때 복원합니다. 한 Window에서는 현재 화면을 대표하는 호출 한 곳만 활성화하세요.

```kotlin
@Composable
fun LightSurfaceScreen() {
    SystemBarsStyle(statusBarDarkIcons = true)

    Box(modifier = Modifier.systemBarsPadding()) {
        ScreenContent()
    }
}
```

Use AndroidX inset modifiers such as `statusBarsPadding`, `navigationBarsPadding`, and `systemBarsPadding` directly.

> Inset 패딩은 AndroidX의 `statusBarsPadding`, `navigationBarsPadding`, `systemBarsPadding`을 직접 사용합니다.

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

- The default direction threshold is 20px and must be 0 or greater.
- The default edge threshold is 10px and must be 0 or greater. It applies only to `TOP` and `LEFT`; `BOTTOM` and `RIGHT` are determined with `canScrollForward`.
- Vertical lists use `UP` and `DOWN`; horizontal lists use `LEFT` and `RIGHT`.
- Direction returns to `IDLE` when the active scroll session ends.
- Index changes while `isScrollInProgress == false` do not change direction. Programmatic animations where this value is `true` are treated as scroll motion.
- Edges that do not match the list axis always return `false`.

> - 방향 임계값 기본값은 20px이며 0 이상이어야 합니다.
> - 엣지 임계값 기본값은 10px이며 0 이상이어야 합니다. 이 값은 `TOP`/`LEFT`에만 적용되고, `BOTTOM`/`RIGHT`는 `canScrollForward`로 판정합니다.
> - 수직 리스트는 `UP`/`DOWN`, 수평 리스트는 `LEFT`/`RIGHT`를 사용합니다.
> - 실제 스크롤 세션이 종료되면 방향은 `IDLE`로 복귀합니다.
> - `isScrollInProgress == false`인 인덱스 변경은 방향을 변경하지 않습니다. 프로그램적 애니메이션처럼 이 값이 `true`인 이동은 스크롤 모션으로 처리합니다.
> - 리스트 축과 맞지 않는 엣지는 항상 `false`입니다.

## Example App (예제 앱)

The `app` module includes a Compose example screen that runs the main `simple_compose` APIs in one place.

> `app` 모듈에는 `simple_compose`의 주요 API를 한 화면에서 실행할 수 있는 Compose 예제 화면이 포함되어 있습니다.

### How To Run (실행 방법)

1. Run the `app` module.
2. Scroll down on the main screen.
3. Select the `Go to Compose Examples` button.

> 1. `app` 모듈을 실행합니다.
> 2. 메인 화면을 아래로 스크롤합니다.
> 3. `Go to Compose Examples` 버튼을 선택합니다.

### Included Examples (제공 예제)

- `rememberPermissionRequestState`: Camera permission request, rationale continue/cancel, request phase, and result display.
- `CollectVmEvent`: One-time event sequence delivered from a ViewModel.
- `SystemBarsStyle`: Status bar and navigation bar icon contrast switching.
- `rememberScrollDirectionState`: Current LazyList scroll direction.
- `rememberEdgeReachedState`: LazyList `TOP` and `BOTTOM` edge detection.

> - `rememberPermissionRequestState`: 카메라 권한 요청, rationale 진행·취소, 요청 단계와 결과 확인
> - `CollectVmEvent`: ViewModel이 전달한 일회성 이벤트의 순번 확인
> - `SystemBarsStyle`: 상태 바와 내비게이션 바 아이콘 명암 전환
> - `rememberScrollDirectionState`: LazyList의 현재 스크롤 방향 확인
> - `rememberEdgeReachedState`: LazyList의 `TOP`/`BOTTOM` 도달 여부 확인

The number shown in the `CollectVmEvent` example is a one-time event sequence from the ViewModel, not persistent Count state. Use `StateFlow` with AndroidX `collectAsStateWithLifecycle` for state that should be retained and observed continuously.

> `CollectVmEvent` 예제에 표시되는 숫자는 영구적인 Count 상태가 아니라 ViewModel이 전달한 일회성 이벤트의 순번입니다. 지속해서 보존하고 관찰해야 하는 화면 상태에는 `StateFlow`와 AndroidX의 `collectAsStateWithLifecycle`을 사용하세요.

### Example Code Location (예제 코드 위치)

- [ComposeExamplesActivity.kt](../../app/src/main/java/kr/open/library/simpleui_xml/compose/ComposeExamplesActivity.kt)
- [ComposeExamplesViewModel.kt](../../app/src/main/java/kr/open/library/simpleui_xml/compose/ComposeExamplesViewModel.kt)

## Verification (검증)

```bash
./gradlew :simple_compose:testAll
./gradlew :simple_compose:lintDebug
./gradlew :simple_compose:apiCheck
./gradlew :simple_compose:assemble
```
