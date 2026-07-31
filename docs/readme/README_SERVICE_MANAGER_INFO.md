# System Manager Info Guide — Document Moved
> **System Manager 정보 가이드 — 문서 이동 안내**

<br>

## Current Module Ownership (현재 모듈 소유권)

System Manager information APIs are owned by `simple_system_manager`.
The previous `simple_core.system_manager` package is no longer the current public API location.

> System Manager 정보 API는 `simple_system_manager` 모듈이 소유합니다.
> 기존 `simple_core.system_manager` 패키지는 현재 공개 API 위치가 아닙니다.

<br>

## Current Guide (현재 문서)

- **Module**: `simple_system_manager`
- **Package**: `kr.open.library.simple_ui.system_manager.core.info.*`, `kr.open.library.simple_ui.system_manager.xml.*`
- **Guide**: [system_manager/info/README_SERVICE_MANAGER_INFO.md](system_manager/info/README_SERVICE_MANAGER_INFO.md)
- **Maven Central**: `io.github.rhpark:dash-droid-system-manager:0.5.1`

> - **모듈**: `simple_system_manager`
> - **패키지**: `kr.open.library.simple_ui.system_manager.core.info.*`, `kr.open.library.simple_ui.system_manager.xml.*`
> - **문서**: [system_manager/info/README_SERVICE_MANAGER_INFO.md](system_manager/info/README_SERVICE_MANAGER_INFO.md)
> - **Maven Central**: `io.github.rhpark:dash-droid-system-manager:0.5.1`

<br>

## Dependency Declaration (의존성 선언)

```kotlin
dependencies {
    implementation("io.github.rhpark:dash-droid-system-manager:0.5.1")
}
```

Add `dash-droid-core` directly when app source code also calls Core APIs, inherits Core classes,
or references Core model types.

> 앱 소스 코드에서 Core API를 호출하거나 Core 클래스를 상속하거나 Core 모델 타입을 참조하면
> `dash-droid-core`도 직접 추가하세요.

```kotlin
dependencies {
    implementation("io.github.rhpark:dash-droid-core:0.5.1")
    implementation("io.github.rhpark:dash-droid-system-manager:0.5.1")
}
```

For the complete module selection policy, see
[README.md — Select dependencies](../../README.md#2-select-dependencies-필요한-모듈-선택).

> 전체 모듈 선택 정책은
> [README.md — 필요한 모듈 선택](../../README.md#2-select-dependencies-필요한-모듈-선택)을 참조하세요.
