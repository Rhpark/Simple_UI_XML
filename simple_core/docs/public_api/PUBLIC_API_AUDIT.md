# Simple Core 공개 API 감사

## 문서 정보
- 문서명: Simple Core 공개 API 감사
- 작성일: 2026-07-29
- 대상 모듈: simple_core
- 기준 버전: 0.5.1
- 기준 파일: `simple_core/api/simple_core.api`
- 상태: 1단계 전체 감사 완료 / 2단계 테스트 기준선 보강 완료 / 4단계 멤버 상세 감사 완료(Deprecated 처리 제외)

## 목적
- 소비자가 직접 사용하는 공개 계약과 Simple UI 모듈끼리 공유하는 구현 계약을 구분한다.
- 이미 배포된 API의 호환성을 유지하면서 공개 API 표면을 단계적으로 정리한다.
- Deprecated 적용, 다음 메이저 제거, 테스트 보강의 판단 기준을 고정한다.
- 저장소 내부 사용 빈도가 높은 Core API를 우선 보호하고 회귀 검증 범위를 명확히 한다.

## 범위

### 포함 범위
- `simple_core`의 Kotlin/JVM 공개 ABI 기준선
- `simple_core/src/main/java`의 공개 소스 API
- 파일 퍼사드, 중첩 타입, 생성 `BuildConfig`를 포함한 ABI 클래스 레코드
- `internal` 패키지 경로에 존재하는 공개 API
- 저장소 내부 main 소스 사용처와 기능 PRD/SPEC, README에 명시된 공개 계약
- 단위 테스트, Robolectric 테스트, Kover 보고서의 검증 공백
- 버전 호환성을 고려한 Deprecated 및 제거 시점

### 제외 범위
- 공개 타입이나 메서드의 즉시 삭제 또는 `internal` 전환
- API 동작과 반환값 변경
- 외부 저장소 소비자의 실제 사용 통계
- 다음 메이저 버전 번호 및 출시 일정 확정
- `simple_xml`, `simple_compose`, `simple_system_manager` 자체 공개 API 감사

## 기준 및 원칙

### 호환성 기준
- 기준 버전 `0.5.1`의 API 파일은 509줄이며, 신규 공개 경로를 추가한 현재 API 파일은 510줄이다.
- 기준 파일에 포함된 타입과 시그니처는 저장소 내부 사용 여부와 관계없이 이미 배포된 공개 API로 간주한다.
- 공개 타입을 즉시 `internal`로 변경하거나 삭제하는 작업은 Breaking Change로 간주한다.
- 프로젝트 버전 규칙에 따라 Breaking Change는 Major 버전에서만 수행한다.
- `DeprecationLevel.HIDDEN`은 마이그레이션 기간 없이 적용하지 않는다.

### 판정 기준
- **유지**: README·PRD·SPEC에 소비자 계약으로 명시되거나 다른 공개 API의 매개변수·반환형·상속 계약에 포함된 API
- **모듈 간 공유 계약**: 소비자 주 진입점은 아니지만 별도 Gradle 모듈인 XML·Compose·System Manager가 공통 구현을 위해 참조하는 API
- **패키지 이동 후보**: 기능은 공개 계약이지만 현재 패키지 경로가 공개 의도와 맞지 않는 API
- **Deprecated 후보**: 내부 구현을 위해서만 사용하지만 이미 공개된 API로, 현 버전에서 즉시 제거할 수 없는 API
- **후속 설계**: 동작 계약이나 대체 경로를 먼저 확정해야 공개 범위를 판단할 수 있는 API

### 금지 사항
- 저장소 내부 직접 사용처가 없다는 이유만으로 공개 API를 제거하지 않는다.
- 다른 공개 API가 참조하는 모델 타입을 단독으로 내부화하지 않는다.
- Kotlin 파일 퍼사드를 일반 클래스처럼 판단해 제거하지 않는다.
- API 기준선에서 항목만 지워 실제 바이너리 노출과 기준선을 불일치시키지 않는다.
- 여러 패키지의 API 축소와 동작 변경을 한 변경에서 동시에 수행하지 않는다.

## 감사 결과 요약
- 공개 API 기준선은 총 510줄이다.
- ABI에는 파일 퍼사드, 중첩 타입, Companion, `DefaultImpls`를 포함한 공개 클래스 레코드 43개가 기록되어 있다.
- 그룹별 클래스 레코드는 extensions 9개, local 2개, logcat 4개, permissions 24개, thread 1개, viewmodel 2개, 생성 `BuildConfig` 1개다.
- `simple_core/src/main/java`의 Kotlin 소스 파일은 내부 `PermissionPolicy` 추가 후 총 52개다.
- `internal` 패키지 경로에서 공개 ABI에 포함된 항목은 `ManifestPermissionReaderKt` 1개다.
- 공개 reified inline 함수 `Bundle.getValue()`는 소스 API지만 API 기준선에는 기록되지 않는다.
- 저장소 main 소스 import 기준 핵심 사용 API는 `Logx` 57개 파일, `safeCatch` 36개 파일, `checkSdkVersion` 32개 파일이다.
- 외부 저장소 소비자의 실제 호출 통계는 없으므로 저장소 사용 횟수만으로 Deprecated 여부를 결정하지 않는다.

## 공개 API 인벤토리

### 생성 API
| API | 판정 | 근거 |
| --- | --- | --- |
| `BuildConfig` | 현재 유지 / 계약 명시 | `MainThreadGuard`, `LogxPipeline`이 `DEBUG`를 사용하므로 대체 전략 없이 생성을 비활성화할 수 없음 |

`simple_core`는 release 컴포넌트를 배포한다. 따라서 배포 AAR의 `BuildConfig.DEBUG`는 소비자 앱의 debug 여부를 나타내지 않는다. 현 버전에서는 `assertMainThreadDebug` 구현과 공개 시그니처를 유지하며, `@MainThread`를 기본 계약으로 사용한다. 런타임 검증은 `simple_core` 자체가 Debug 변형으로 빌드된 경우에만 동작하고, 배포된 release AAR에서는 소비자 앱이 Debug 빌드여도 비활성화된다.

### extensions
| 패키지·API | 판정 | 근거 |
| --- | --- | --- |
| `bundle.getValue` | 유지 / 별도 소스 API 보호 | README에 타입 안전 Bundle 접근 API로 명시됨 |
| `conditional.CollectionExtensionsKt` | 유지 | 조건부 컬렉션 공개 확장 함수 |
| `conditional.IfInlineKt` | 유지 / 멤버 단위 감사 완료 | README 공개 계약이며 Line 117/117, Branch 80/80 직접 검증 완료 |
| `conditional.SdkVersionInlineKt` | 유지 | 저장소 main 소스 28개 파일에서 사용 |
| `date.DateExtensionsKt` | 유지 | README 공개 확장 패키지 |
| `display.DisplayUnitExtensionsKt` | 유지 | README 공개 확장 패키지 |
| `round_to.RoundToInlineKt` | 유지 / 반올림 계약 통일 완료 | 모든 숫자 타입의 `roundTo`와 `roundHalfUp`이 exact half를 0에서 멀어지는 방향으로 처리 |
| `string.StringExtensionsKt` | 유지 | README 공개 확장 패키지 |
| `time.MeasureTimeExtensionsKt` | 유지 | 실행 결과와 경과 시간을 함께 반환하는 독립 유틸리티 |
| `trycatch.TryCatchExtensionsKt` | 유지 / 전체 공개 계약 검증 완료 | `safeCatch`가 저장소 main 소스 36개 파일에서 사용되며 경계·SDK guard 직접 검증 완료 |

#### Bundle 소스 API 보호
- `Bundle.getValue()`는 `public inline fun <reified T>`이며 API 기준선에 나타나지 않는다.
- 기존 `BundleInlineTest`가 함수 호출을 컴파일하고 지원 타입 동작을 검증하므로 테스트를 유지한다.
- `apiCheck` 통과만으로 이 함수의 소스 호환성을 보장했다고 판단하지 않는다.

#### `IfInlineKt` 멤버 단위 감사 완료
- 수치 비교, Boolean 분기, `firstNotNull`은 README에 소비자 API로 문서화되어 있다.
- 저장소의 다른 main 소스 직접 사용처는 없지만 외부 소비자 사용 통계를 알 수 없으므로 사용 횟수만으로 축소하지 않는다.
- `IfInlineTest`가 Line 117/117, Branch 80/80을 검증하므로 현재 공개 멤버를 유지한다.

#### `roundHalfUp` 상세 감사 완료
- `roundHalfUp(Double)`은 exact half를 양수·음수 모두 0에서 멀어지는 방향으로 처리하는 공개 반올림 함수다.
- `Double.roundTo`와 `Float.roundTo`도 같은 함수를 사용하도록 통일하여 KDoc의 half-up 계약과 구현을 일치시켰다.
- `RoundToInlineTest`는 `2.5 → 3`, `-2.5 → -3`을 함수와 부동소수점 확장 API에서 직접 검증한다.
- 공개 API를 유지하며 Deprecated 대상으로 분류하지 않는다.

### local
| API | 판정 | 근거 |
| --- | --- | --- |
| `BaseSharedPreference` | 유지 / protected 표면 감사 완료 | 소비자가 상속하는 공개 확장 지점이며 System Manager와 실기기 검증 앱이 사용 |

`BaseSharedPreference`의 protected delegate·읽기·쓰기·삭제 API는 상속 계약이므로 단순 내부 구현으로 취급하지 않는다. 매개변수 없는 `saveApply()`와 타입별 삭제 함수에 정리 여지가 있지만, Deprecated 처리를 제외하면 대체 API 추가가 공개 표면만 늘리므로 현행을 유지한다.

### logcat
| API | 판정 | 근거 |
| --- | --- | --- |
| `Logx` | 유지 | Logx의 단일 공개 진입점이며 저장소 main 소스 52개 파일에서 사용 |
| `LogStorageType` | 유지 | `Logx.setStorageType()`의 공개 매개변수 |
| `LogType` | 유지 / `writeToLog` 후속 감사 | 공개 필터 설정 타입이지만 `writeToLog`는 내부 Console Writer만 사용 |
| `LogxStringExtensionsKt` | 유지 | Logx PRD/SPEC과 README에 확장 API로 명시됨 |

`Logx`의 많은 오버로드는 v/d/i/w/e/p/j/t의 일관된 Kotlin·Java 호출 계약이다. 메서드 수만으로 축소 후보로 분류하지 않는다. `LogType.writeToLog()`는 소비자 기능이 아니라 내부 출력 매핑이므로 멤버 단위 Deprecated 후보로 남긴다.

### permissions — 소비자 공개 계약
| API | 판정 | 근거 |
| --- | --- | --- |
| `PermissionExtensionsKt`의 권한 검사 함수 | 유지 | README에서 Core 주요 API로 명시됨 |
| `readDeclaredManifestPermissions` | 유지 / 공개 패키지 이동 완료 | `permissions.extensions`에 정식 경로를 제공하고 기존 `permissions.internal` ABI는 Deprecated forwarding으로 유지 |
| `PermissionDeniedItem` | 유지 | XML·Compose 소비자 결과 모델 |
| `PermissionDeniedType` | 유지 | 거부·실패 결과 공개 열거형 |
| `OrphanedDeniedRequestResult` | 유지 | XML 복원 결과 회수 API의 반환 모델 |
| `PermissionDeferredPolicy` | 유지 | rationale/settings 공개 콜백의 lifecycle 정책 |
| `PermissionRationaleRequest` | 유지 | 소비자 UI 훅 계약 |
| `PermissionSettingsRequest` | 유지 | 소비자 설정 이동 훅 계약 |

#### Manifest 권한 조회 API 이동 원칙
- 신규 공개 위치는 `kr.open.library.simple_ui.core.permissions.extensions`가 현재 문서와 역할에 맞다.
- 기존 `permissions.internal.readDeclaredManifestPermissions`는 바로 삭제하지 않는다.
- 신규 함수를 먼저 제공하고 기존 경로는 일반 경고 수준의 Deprecated forwarding API로 유지한다.
- 실제 기존 경로 제거는 다음 메이저에서만 수행한다.

### permissions — 고급 분류 API
| API | 판정 | 근거 |
| --- | --- | --- |
| `PermissionClassifier` | 유지 / 후속 감사 | Core 권한 분류 기능으로 문서화됐으며 XML·Compose가 사용 |
| `PermissionType` | 유지 | `PermissionClassifier.classify()` 반환형 |
| `RuntimePermissionRequestability` | 유지 | `PermissionClassifier.getRuntimeRequestability()` 반환형 |

고급 분류 API는 일반 요청 흐름의 주 진입점은 아니지만 Core 단독 소비자가 권한 정책을 조회할 수 있다. 저장소 외부 사용 통계 없이 내부화하지 않는다.

### permissions — 모듈 간 공유 계약
| API | 판정 | 근거 |
| --- | --- | --- |
| `RolePermissionHandler` | 모듈 간 공유 계약 / 후속 감사 | XML·Compose 요청 흐름이 사용 |
| `SpecialPermissionHandler` | 모듈 간 공유 계약 / 후속 감사 | XML·Compose 요청 흐름이 사용 |
| `PermissionDecisionType` | 모듈 간 공유 계약 | SPEC이 내부 처리·저장 타입으로 정의하고 XML·Compose가 사용 |
| `toDeniedTypeOrNull` | 모듈 간 공유 계약 | XML·Compose 공통 결과 변환 규칙 |
| `buildPermissionDeniedItems` | 모듈 간 공유 계약 | XML·Compose 공통 거부 목록 생성 규칙 |
| `PermissionQueue` | 모듈 간 공유 계약 | XML·Compose 요청 직렬화에 사용 |
| `RuntimePermissionDecisionTracker` | 모듈 간 공유 계약 | XML·Compose 영구 거부 판정의 단일 출처 |

위 API는 소비자 주 진입점은 아니지만 서로 다른 배포 모듈이 참조하므로 Kotlin `internal`로 즉시 전환할 수 없다. 먼저 문서와 제한 어노테이션으로 계약 범위를 표현하고, 실제 내부화는 모듈 구조 변경 또는 다음 메이저 설계와 함께 검토한다.

### permissions — 상수·열거형
| API | 판정 | 근거 |
| --- | --- | --- |
| `PermissionConstants` | 유지 / 내부 정책 분리 완료 | 공개 getter는 유지하고 라이브러리 내부 분류·설정 이동은 `PermissionPolicy`를 단일 출처로 사용 |
| `PermissionSpecialType` | 유지 | 특수 권한 전체 열거와 권한 문자열 매핑을 제공하며 현재 공개 API로 완전히 대체할 수 없음 |

`PermissionClassifier`와 `isSpecialPermission()`은 분류 결과만 제공하므로 `PermissionSpecialType.entries`의 열거 계약을 완전히 대체하지 못한다. `PermissionConstants`는 내부 정책 구현에서 분리했지만 기존 공개 getter와 값은 그대로 유지한다. 두 타입 모두 Deprecated 대상으로 분류하지 않는다.

### thread
| API | 판정 | 근거 |
| --- | --- | --- |
| `assertMainThreadDebug` | 유지 / 배포 한계 문서화 | XML·Compose·System Manager가 공유하며, 배포 AAR에서는 소비자 앱의 debug 상태와 관계없이 런타임 검증이 비활성화됨 |

`@MainThread`를 기본 정적 계약으로 유지한다. `assertMainThreadDebug`는 라이브러리 모듈의 Debug 변형에서만 동작하는 보조 진단 수단이며, 소비자 앱의 빌드 유형을 판정하기 위한 Context 기반 API나 전역 초기화 설정은 추가하지 않는다.

### viewmodel
| API | 판정 | 근거 |
| --- | --- | --- |
| `BaseViewModel` | 유지 | Lifecycle observer 결합 기반 클래스이며 MVVM README에 명시됨 |
| `BaseViewModelEvent` | 유지 | 단발 UI 이벤트용 공개 기반 클래스이며 XML·Compose 사용법이 문서화됨 |

`BaseViewModelEvent.eventVmFlow`는 단일 소비자 Channel 기반이라는 KDoc 계약을 유지한다. 멀티캐스트 State/이벤트 API로 의미를 바꾸려면 별도 신규 API로 설계한다.

## 테스트 및 검증 현황

### 실행 결과
- `./gradlew :simple_core:testDebugUnitTest :simple_core:koverXmlReportDebug --rerun-tasks`
  - 테스트 스위트: 43개
  - 테스트: 517개
  - 실패: 0개
  - 오류: 0개
  - 건너뜀: 0개
  - Line: 1,224/1,271, 96.3%
  - Branch: 518/614, 84.4%
  - Method: 413/419, 98.6%
  - Class: 72/72, 100%
- `./gradlew :simple_core:testReleaseUnitTest --tests 'kr.open.library.simple_ui.core.robolectric.thread.MainThreadGuardRobolectricTest'`
  - 테스트: 2개
  - 실패: 0개
  - 오류: 0개
  - 건너뜀: 0개
  - Release 변형의 워커 스레드 비강제 정책 검증: 성공
- `./gradlew :simple_core:apiCheck`
  - API 검증: 성공
- `./gradlew :simple_core:ktlintCheck`
  - Ktlint 검증: 성공

### 해석 주의
- Kover 제외 목록은 `BuildConfig`, `R`, Data Binding 등 자동 생성 코드만 유지한다.
- `Logx` 공개 루트와 `DisplayUnitExtensions`를 포함하도록 제외 규칙을 정리한 뒤, 현재 측정 대상은 1,271줄이다.
- Line 90.0%에서 86.9%로 변경된 것은 테스트 회귀가 아니라 기존 제외 대상 129줄이 기준선에 포함된 결과다.
- 과거 `ILogx`, `logcat.extensions`, `logcat.runtime`, `simple_core.system_manager` 제외 규칙은 현재 소스에 존재하지 않아 제거했다.

### 직접 테스트 보강 현황
| 대상 | 현재 Kover Line | 상태 | 보강 이유 |
| --- | ---: | --- | --- |
| `RuntimePermissionDecisionTracker` | 100% | 완료 | XML·Compose가 공유하는 영구 거부 판정 단일 출처 |
| `PermissionModelsKt` | 100% | 완료 | 결정→거부 변환과 요청 순서 보존 규칙 |
| 권한 복원·defer 공개 모델 | 100% | 완료 | 복원 결과 데이터 보존·정책값·두 Request의 기본 defer 정책 검증 |
| `RolePermissionHandler` | 100% | 완료 | SDK·RoleManager 미제공·예외 경계 검증 |
| `SpecialPermissionHandler` | 100% | 완료 | Settings Intent·package URI·SDK 경계 검증 |
| `ManifestPermissionReaderKt` | 100% | 완료 | 신규 공개 경로의 API 33 전후 분기·중복 제거·실패 기본값과 기존 경로 forwarding 검증 |
| `MainThreadGuardKt` | 100% | 완료 | Debug 변형의 main/worker thread와 Release 변형의 worker thread 정책 검증 |
| `Logx` | 98.3% | 완료 | 전체 공개 로그 오버로드와 초기화 전후·Debug/Release·API 28 권한·사용자 지정 경로·앱 이름 설정 경계 검증 |
| `LogxFilter` | 100% | 완료 | Pipeline의 중복 판정 제거 후 전역·타입·태그 필터 단일 구현 검증 |
| `LogxStringExtensionsKt` | 100% | 완료 | 16개 공개 확장 함수의 메시지·태그 위임 계약 검증 |
| `LogxPipeline` | 94.5% | 완료 | 필터 위임·포맷·콘솔 출력·파일 저장 연결·Context 경계 검증 |
| `LogxPathResolver` | 100% | 완료 | 저장 타입별 경로·API 28/29+ 분기·사용자 경로 허용 범위와 Documents 미제공 시 앱 전용 외부 저장소, 앱 전용 외부 저장소 미제공 시 내부 저장소 폴백 검증. Branch 14/19 |
| `LogxFileWriter` | 96.0% | 완료 | 비동기 기록·즉시 flush·빈 입력·close 후 재개·큐 종료 실패·반복 IOException·경로 계산 실패 검증. Branch 10/10 |
| `LogxFilePathResolver` | 94.6% | 완료 | 기본/사용자 경로 생성·파일/차단 부모·Scoped Storage·API 28 기본/사용자 공용 경로 권한 경계 검증. 플랫폼 의존 `mkdirs()` 실패만 제외 |
| `LogxFileSession` | 81.0% | 완료 | 동일 Writer 재사용·설정 변경·close 후 이어 쓰기·10MB 로테이션 검증. 정상 흐름에서 도달하지 않는 null 방어 블록은 제외 |
| `DisplayUnitExtensionsKt` | 100% | 완료 | 6개 공개 단위 변환 함수의 density·fontScale 검증 |
| `RoundToInlineKt` | 100% | 완료 | `roundHalfUp`과 Double·Float 확장 함수의 양수·음수 exact half 계약 검증 |
| `PermissionPolicy` | 100% | 완료 | 기존 권한 상수 테스트와 분류기·특수 권한 핸들러 테스트를 통한 내부 단일 출처 검증 |
| `TryCatchExtensionsKt` | 100% | 완료 | `safeCatch` 예외 경계와 지연 메시지·SDK 최소/최대 guard 검증. Kover Branch는 4/6(66.7%)이며 inline `requireInBounds`의 양 경계는 직접 테스트로 검증 |
| `PermissionQueue` | 100% | 완료 | FIFO·중복 방지·스냅샷과 `clear`·`isEmpty` 상태 전이 검증 |

## 단계별 처리 계획

### 1단계: 현행 공개 API 감사
- 상태: 완료
- API 기준선과 `0.5.1` 태그의 동일성 확인
- ABI 클래스 레코드 43개와 `0.5.1` 기준 소스 파일 51개 인벤토리 확인
- 소비자 공개 계약, 모듈 간 공유 계약, 후속 감사 후보 분류
- 즉시 제거 또는 내부화 없음

### 2단계: 테스트 기준선 보강
- 상태: 완료
- `RuntimePermissionDecisionTracker` 직접 단위 테스트 — 완료
- `toDeniedTypeOrNull`, `buildPermissionDeniedItems` 직접 단위 테스트 — 완료
- Role·Special Handler와 Manifest Reader Robolectric 테스트 — 완료
- MainThreadGuard의 배포 동작 계약 문서화와 Debug/Release 직접 테스트 — 완료
- Logx 공개 진입점과 Kotlin 확장 함수 위임 테스트 — 완료
- Logx 초기화·파일 저장 설정 경계 직접 테스트 — 완료
- Logx 내부 Writer·PathResolver·Session 실패 및 로테이션 경계 직접 테스트 — 완료
- TryCatch 경계·SDK guard와 PermissionQueue 상태 전이 직접 테스트 — 완료
- Kover 제외 목록 정리 후 새 기준선 기록 — 완료

### 3단계: 비파괴 공개 경로 정리
- 상태: 완료
- `permissions.extensions.readDeclaredManifestPermissions` 신규 공개 경로 제공 — 완료
- 기존 `permissions.internal` 경로에 Deprecated forwarding 유지 — 완료
- `PermissionClassifier`와 직접 테스트를 신규 공개 경로로 전환 — 완료
- README·KDoc에 정식 공개 경로와 호환 정책 명시 — 완료
- `apiDump`와 `apiCheck`로 기존 ABI 유지와 신규 API 추가 확인 — 완료

### 4단계: 멤버 단위 상세 감사
- 상태: 완료(Deprecated 처리 제외)
1. `LogType.writeToLog` 내부 출력 구현 노출 — 검토 완료 / Deprecated 적용 제외
2. `roundHalfUp` 독립 공개 필요성 및 반올림 의미 — 유지 / 숫자 타입별 half-up 통일 완료
3. `PermissionConstants`, `PermissionSpecialType` 소비자 대체 경로 — 공개 API 유지 / 내부 `PermissionPolicy` 분리 완료
4. `BaseSharedPreference` protected 상속 표면 — 감사 완료 / 공개 표면 추가 없이 현행 유지
5. 확정된 `assertMainThreadDebug` 계약의 직접 테스트 — 완료

Deprecated가 필요한 정리는 이번 범위에서 제외하고, 공개 계약을 유지하면서 가능한 구현·테스트·문서 정합성만 반영했다.

### 5단계: 다음 메이저 정리
- Deprecated 마이그레이션 기간을 거친 API만 제거하거나 내부화한다.
- `BuildConfig` 대체 전략이 구현된 경우 `buildFeatures.buildConfig = false`를 검토한다.
- API 기준선 차이를 Breaking Change로 승인한다.
- 릴리스 노트에 제거 API와 대체 경로를 기록한다.

## 검증 기준

### 문서 단계
- 현재 API 기준선 510줄과 클래스 레코드 43개를 유지해야 한다.
- `internal` 경로의 공개 API를 누락하지 않아야 한다.
- 파일 퍼사드와 공개 inline 함수를 누락하지 않아야 한다.
- 기능 PRD/SPEC·README에 명시된 API를 근거 없이 제거 후보로 분류하지 않아야 한다.

### 코드 변경 단계
1. `./gradlew :simple_core:compileDebugKotlin`
2. `./gradlew :simple_core:compileDebugUnitTestKotlin`
3. `./gradlew :simple_core:testDebugUnitTest`
4. `./gradlew :simple_core:koverXmlReportDebug`
5. `./gradlew :simple_core:apiDump`
6. `./gradlew :simple_core:apiCheck`

### 다음 메이저 제거 단계
- 소비자 대체 경로가 문서화되어 있어야 한다.
- 일반 경고 수준의 Deprecated 마이그레이션 기간을 거쳐야 한다.
- API 기준선 차이를 Breaking Change로 승인해야 한다.
- 릴리스 노트에 제거 타입·메서드와 대체 방법을 기록해야 한다.

## 현 단계 결론
- `simple_core`는 다른 라이브러리 모듈과 앱이 공통으로 사용하는 우선 보호 대상이다.
- 현 버전에서 즉시 제거하거나 `internal`로 전환할 공개 API는 없다.
- `Logx`, 공용 확장 함수, `BaseSharedPreference`, ViewModel 기반 클래스, 권한 검사·소비자 결과 모델은 유지한다.
- 권한 큐·결정·Tracker·Handler는 별도 Gradle 모듈이 공유하는 계약이므로 즉시 내부화하지 않는다.
- `PermissionConstants`와 `PermissionSpecialType`은 유지하고, 라이브러리 내부 권한 매핑만 `PermissionPolicy`로 분리한다.
- `roundHalfUp`과 모든 숫자 타입의 `roundTo`는 exact half를 0에서 멀어지는 방향으로 일관되게 처리한다.
- `BaseSharedPreference`는 Deprecated 없이 대체 API를 추가하면 표면만 늘어나므로 현행 상속 계약을 유지한다.
- `readDeclaredManifestPermissions`는 `permissions.extensions` 정식 경로를 제공하고 기존 `permissions.internal` 경로를 Deprecated forwarding으로 유지한다.
- 공개 표면 축소보다 직접 테스트가 없는 계약의 검증을 먼저 수행한다.
- `MainThreadGuard`는 현 구현과 배포 한계를 문서화하고 Debug/Release 변형별 직접 테스트를 완료했다.
