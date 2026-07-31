# Simple XML 공개 API 감사

## 문서 정보
- 문서명: Simple XML 공개 API 감사
- 작성일: 2026-07-30
- 대상 모듈: `simple_xml`
- 기준 버전: 0.5.1
- 기준 파일: `simple_xml/api/simple_xml.api`
- 상태: 1단계 전체 공개 API 감사 및 테스트 기준선 보강 완료

## 목적
- XML UI 소비자가 직접 사용하는 공개 계약과 모듈 내부 구현 성격의 노출을 구분한다.
- 이미 배포된 0.5.1 ABI를 유지하면서 다음 메이저 정리 후보를 기록한다.
- Activity, Fragment, Layout, Adapter, View 확장, 권한 요청 API의 보호 범위를 고정한다.
- 공개 동작의 테스트 공백과 문서 불일치를 코드 사실에 따라 관리한다.

## 범위

### 포함 범위
- `simple_xml/src/main/java`의 Kotlin 소스 파일 101개
- `simple_xml/src/main/res`의 XML 리소스 파일 5개
- `simple_xml/api/simple_xml.api`의 공개 ABI
- 예제 앱의 `simple_xml` 사용처
- Adapter, Permission, RecyclerView, Extension 관련 기능 문서와 README
- Unit, Robolectric, Kover, lint, API 검증 기준선

### 제외 범위
- 공개 API의 즉시 삭제, 이름 변경 또는 `internal` 전환
- Deprecated API 추가
- `simple_core`, `simple_compose`, `simple_system_manager` 자체 공개 API 감사
- 외부 저장소 소비자의 실제 사용 통계
- 연결 문제로 연기된 실기기 검증 실행

## 기준 및 원칙

### 호환성 기준
- API 기준선은 1,238줄이며 공개 ABI 클래스 레코드는 131개다.
- 기준선은 0.5.1 태그와 동일하므로 기록된 타입과 시그니처는 이미 배포된 계약으로 취급한다.
- 공개 타입의 삭제나 `internal` 전환은 Breaking Change이며 다음 메이저에서만 수행한다.
- API 기준선만 수정하여 실제 AAR의 노출과 불일치시키지 않는다.

### 판정 기준
- **유지**: README에 소비자 API로 문서화되거나 공개 상속·매개변수·반환형 계약에 포함된 API
- **내부 구현 성격 / 다음 메이저 후보**: 앱과 소비자 문서에서 사용하지 않고 라이브러리 내부 조율에만 사용되는 API
- **생성 API / 다음 메이저 후보**: 테스트 또는 빌드 설정 때문에 AAR에 포함되지만 소비자 기능이 아닌 API
- **후속 테스트**: 공개 동작이지만 주요 상태 전이나 플랫폼 분기의 직접 검증이 부족한 API

### 금지 사항
- 저장소 내부 사용처가 없다는 이유만으로 0.5.1 공개 API를 즉시 제거하지 않는다.
- 공개 기반 클래스의 protected 멤버를 단순 내부 구현으로 판단하지 않는다.
- `AdapterDropReason`처럼 공개 결과가 참조하는 타입을 독립적으로 내부화하지 않는다.
- 단순히 클래스 수를 줄이기 위해 Activity, Fragment, Layout 기반 계층을 합치지 않는다.

## 감사 결과 요약
- API 기준선은 0.5.1 태그와 동일하며 현재 작업 전후 ABI 변경은 없다.
- Activity, Fragment, Dialog, Layout 기반 클래스는 소비자가 상속하는 확장 지점이므로 유지한다.
- Adapter는 즉시 반영형 `BaseRcvAdapter`와 큐 기반 `BaseRcvListAdapter`의 두 계층이 현행 계약이다.
- Header/Content/Footer 전용 Adapter는 존재하지 않으며 `BaseRcvAdapter`와 sealed interface item 패턴을 사용한다.
- `PermissionRequester`와 `PermissionRequestInterface`는 XML 권한 요청의 공개 진입점이다.
- 생성 API와 내부 조율 API 중 다음 메이저 정리 후보 6개 묶음을 확인했다.
- 다음 메이저 후보는 현재 AAR과 0.5.1 API 기준선에 포함되어 있으므로 이번 단계에서 제거하지 않는다.

## 공개 API 인벤토리

### 생성 API
| API | 판정 | 근거 |
| --- | --- | --- |
| `BuildConfig` | 사전 검증 완료 / 다음 메이저 제거 후보 | 본문 사용처가 없고 임시 사본에서 생성 비활성화 후 테스트·Release AAR·예제 앱 빌드가 통과했지만 0.5.1 API 기준선에 포함됨 |
| `TestDatabindingActivityBinding` 계열 | 사전 검증 완료 / 다음 메이저 제거 후보 | 비배포 테스트 지원 모듈로 이동한 임시 검증이 통과했지만 0.5.1 API 기준선과 AAR 리소스에 포함됨 |
| `TestDatabindingFragmentBinding` 계열 | 사전 검증 완료 / 다음 메이저 제거 후보 | 비배포 테스트 지원 모듈로 이동한 임시 검증이 통과했지만 0.5.1 API 기준선과 AAR 리소스에 포함됨 |
| Data Binding 생성 mapper·trigger·BR | 유지 | DataBinding 기능 활성화로 생성되는 구현 계약 |

테스트 전용 레이아웃 두 개는 현재 Debug·Release AAR의 `res/layout`과 `classes.jar`에 포함된다.
직접 test 소스 세트로 옮기는 방식은 현재 도구 체인의 DataBinding 컴파일에서 실패하며, 비배포 테스트 지원 모듈로 분리하는 방식은 사전 검증을 통과했다.

#### BuildConfig 다음 메이저 사전 검증
- 검증일: 2026-07-30
- 현재 작업 트리가 아닌 임시 프로젝트 사본에서만 `simple_xml`의 `buildConfig` 생성을 비활성화했다.
- `ViewExtensionsRobolectricTest`의 `BuildConfig.DEBUG` 분기를 debug 테스트 계약에 맞는 직접 예외 검증으로 대체했다.
- Unit 111개와 Robolectric 526개가 모두 통과했다.
- ktlint, lint, `simple_xml` Release AAR와 예제 앱 Debug APK 빌드가 성공했다.
- Release AAR의 `classes.jar`에 `BuildConfig.class`가 포함되지 않음을 확인했다.
- 0.5.1 API 기준선에 대한 `apiCheck`는 예상대로 실패했으며 차이는 `BuildConfig` 클래스와 `BUILD_TYPE`, `DEBUG`, `LIBRARY_PACKAGE_NAME` 필드 및 생성자 제거뿐이었다.
- 기술적으로 제거할 수 있지만 바이너리 API 호환성을 깨므로 현재 버전에서는 유지하고 다음 메이저 승인 후 적용한다.

#### 테스트 전용 DataBinding 리소스 다음 메이저 사전 검증
- 검증일: 2026-07-30
- 현재 작업 트리가 아닌 임시 프로젝트 사본에서만 두 XML과 테스트 참조 경로를 변경했다.
- `src/test/res`는 `kr.open.library.simple_ui.xml.test.R`과 테스트용 Binding 소스를 생성했지만, DataBinding 구현 클래스의 기반 Binding 클래스를 Java 컴파일에 연결하지 못해 실패했다.
- Android 리소스를 활성화한 test fixtures도 AGP 8.8.2 DataBinding 컴파일에서 생성 클래스 목록 출력 경로 오류로 실패했다.
- 별도 `simple_xml_test_support` Android Library 모듈에 두 XML을 두고, `simple_xml`이 `testImplementation`으로만 의존하는 방식은 성공했다.
- 테스트 지원 모듈은 `kr.open.library.simple_ui.xml.testsupport` 고유 namespace를 사용하고 API 검증 대상에서 제외했다.
- Unit 111개와 Robolectric 526개, ktlint, lint, `simple_xml` Release AAR, 테스트 지원 Debug AAR와 예제 앱 Debug APK 빌드가 모두 성공했다.
- `simple_xml` Release AAR에서 두 XML과 `TestDatabinding*Binding` 클래스가 제거되고 기존 `DataBinderMapperImpl`은 유지됨을 확인했다.
- 0.5.1 API 기준선에 대한 `apiCheck` 차이는 두 테스트 Binding의 추상 클래스와 구현 클래스 제거뿐이었다.
- 기술적으로 분리할 수 있지만 생성 Binding과 리소스 ID 제거가 호환성을 깨므로 현재 버전에서는 유지하고 다음 메이저 승인 후 적용한다.

### extensions
| API 묶음 | 판정 | 근거 |
| --- | --- | --- |
| Fragment의 `withContext*`, `withView*` | 유지 | attach/view 생명주기 경계를 안전하게 처리하는 소비자 확장 API |
| Resource 확장 | 유지 | drawable, color, dimension, string 접근과 안전 조회를 제공 |
| View·TextView·EditText·ImageView 확장 | 유지 | README와 앱 예제에서 사용하는 XML UI 편의 API |
| Toast·SnackBar 확장 | 유지 | 앱 예제에서 직접 사용되는 사용자 피드백 API |
| View animation 확장 | 유지 | XML View 애니메이션 진입점 |
| View lifecycle·layout 확장 | 유지 | README에 공개 사용법이 있으며 Layout 기반 클래스도 사용 |

### permissions
| API | 판정 | 근거 |
| --- | --- | --- |
| `PermissionRequester` | 유지 / 우선 테스트 보강 | Activity Result 기반 요청·복원·orphaned 결과 회수의 주 진입점 |
| `PermissionRequestInterface` | 유지 | 공개 Root Activity·Fragment·Dialog 계층이 구현하는 요청 계약 |
| `RuntimePermissionHandler` | 현재 유지 / 다음 메이저 내부화 후보 | `PermissionRequester`와 내부 테스트만 사용하고 소비자 README·앱 사용처가 없음 |
| `PermissionHostAdapter` 계열 | 현재 유지 / 다음 메이저 내부화 후보 | Activity/Fragment 차이를 내부 요청 흐름에 맞추는 조율 타입 |

`PermissionRequester`의 공개 콜백과 결과는 `simple_core`의 권한 모델을 사용한다. 현재 의존성 정책은 유지하며,
소비자가 Core 모델을 직접 사용할 때 `simple_core`도 명시적으로 추가하는 설치 안내를 따른다.

### adapter
| API | 판정 | 근거 |
| --- | --- | --- |
| `AdapterReadApi`, `AdapterWriteApi`, `AdapterClickable` | 유지 | normal/list 계층의 공통 소비자 계약 |
| `BaseRcvAdapter`, `RootRcvAdapter` | 유지 | 즉시 notify 기반 목록과 상속 확장 지점 |
| `BaseRcvListAdapter` | 유지 | DiffUtil과 큐 기반 연속 변경의 공개 진입점 |
| Simple Adapter 6종 | 유지 | View, DataBinding, ViewBinding의 normal/list 간편 진입점 |
| `NormalAdapterResult`, `ListAdapterResult` | 유지 | 두 계층의 서로 다른 종료 의미를 공개 |
| `AdapterDropReason` | 유지 | `ListAdapterResult.Failed.Dropped`가 노출하는 공개 실패 사유 |
| `QueueOverflowPolicy` | 유지 | `BaseRcvListAdapter.setQueuePolicy()`의 공개 매개변수 |
| `QueueDropReason` | 현재 유지 / 다음 메이저 내부화 후보 | 내부 큐 종료 사유이며 공개 결과는 `AdapterDropReason`으로 변환됨 |

### components·layout
| API 묶음 | 판정 | 근거 |
| --- | --- | --- |
| Root/Base Activity 계층 | 유지 | 소비자가 상속하는 lifecycle·permission 기반 클래스 |
| Root/Base Fragment 계층 | 유지 | 일반, DataBinding, ViewBinding Fragment 확장 지점 |
| Root/Base DialogFragment 계층과 `DialogConfig` | 유지 / 테스트 보강 | Dialog lifecycle과 window 크기 설정의 공개 진입점 |
| Frame/Linear/Relative/Constraint Layout 계층 | 유지 | 일반, DataBinding, ViewBinding 커스텀 Layout 확장 지점 |
| ParentBindingInterface 계층 | 유지 | 공개 기반 클래스의 binding lifecycle 상속 계약 |

반복되는 네 Layout 계층은 클래스 수가 많지만 Android XML의 구체 View 상속 타입이 서로 다르므로 단순 통합 대상이 아니다.

### RecyclerView scroll state
| API | 판정 | 근거 |
| --- | --- | --- |
| `RecyclerScrollStateView` | 유지 / 우선 테스트 보강 | 스크롤 방향·엣지를 listener와 SharedFlow로 제공 |
| `ScrollDirection`, `ScrollEdge` | 유지 | 공개 listener와 Flow의 결과 타입 |
| 두 listener interface | 유지 | Java/Kotlin 호출부의 공개 콜백 계약 |
| `safeEmit` | 사전 검증 완료 / 다음 메이저 내부화 후보 | `@JvmSynthetic`과 `internal`을 함께 적용한 임시 검증이 통과했으며 소비자는 `MutableSharedFlow.tryEmit()`으로 대체 가능 |

#### safeEmit 다음 메이저 사전 검증
- 검증일: 2026-07-30
- 현재 작업 트리가 아닌 임시 프로젝트 사본에서만 `safeEmit`에 `@JvmSynthetic`과 `internal`을 적용했다.
- `internal`만 적용하면 Kotlin 공개 API 기준선에서는 제외되지만 JVM 바이트코드에는 Java에서 호출 가능한 `public static` 메서드로 남았다.
- `@JvmSynthetic`을 함께 적용한 Release AAR에서는 같은 JVM 메서드가 `ACC_SYNTHETIC`으로 표시되어 Java 소스 접근도 차단됨을 확인했다.
- JVM 메서드와 descriptor는 유지되므로 공개 표면을 줄이면서 기존 바이트코드 호출 경로의 충격을 낮출 수 있다.
- Unit 111개와 Robolectric 526개, ktlint, lint, `simple_xml` Release AAR와 예제 앱 Debug APK 빌드가 모두 성공했다.
- lint 결과는 오류 0개와 기존 경고 8개이며, 0.5.1 API 기준선에 대한 `apiCheck` 차이는 `RecyclerViewScrollStateImpKt.safeEmit` 제거뿐이었다.
- 저장소 본문 사용처는 `RecyclerScrollStateView`뿐이고 예제 앱과 소비자 README 사용처는 없다.
- 소비자가 직접 사용했다면 `MutableSharedFlow.tryEmit(value)`의 반환값을 확인하고 실패 처리를 호출하는 코드로 대체할 수 있다.
- 현재 버전에서는 유지하고 다음 메이저 승인 후 `@JvmSynthetic`과 `internal`을 함께 적용한다.

## 테스트 기준선

### 실행 결과
- `./gradlew :simple_xml:testAll`
  - Unit: 111개
  - Robolectric: 526개
  - 합계: 637개
  - 실패·오류·건너뜀: 0개
- `./gradlew :simple_xml:koverXmlReportDebug`
  - Line: 2,232/2,792, 79.94%
  - Branch: 643/1,044, 61.59%
  - Method: 699/866, 80.72%
  - Class: 189/196, 96.43%
- `./gradlew :simple_xml:testAll :simple_xml:koverXmlReportDebug :simple_xml:ktlintCheck :simple_xml:lintDebug :simple_xml:apiCheck :simple_xml:assembleDebug :app:assembleDebug --continue`
  - 모두 성공
  - lint 오류 0개, 의존성 관련 기존 경고 8개
  - 0.5.1 API 기준선과 현재 API의 차이 없음
  - `simple_xml` Debug AAR와 예제 앱 Debug APK 빌드 성공

### 측정 기준
- Kover 제외 목록은 `BuildConfig`, R, Data Binding 생성 코드만 유지한다.
- 과거 `ui.activity`, `ui.fragment`, `simple_xml.system_manager`, `simple_core` 패키지 제외 규칙은 현재 소스와 일치하지 않아 제거했다.
- `PermissionRequestInterface` 패키지도 측정에서 임의로 제외하지 않는다.
- 제외 규칙 정리 전후 총계가 동일한 것은 오래된 경로가 실제 현재 클래스와 일치하지 않았기 때문이다.

### 우선 보강 결과
| 대상 | 보강 전 Line | 보강 후 Line | 판정 |
| --- | ---: | ---: | --- |
| `PermissionRequester` | 73/186, 39.25% | 120/182, 65.93% | 생명주기 미준비, manifest 미선언, 중복 권한 정규화 검증 |
| `PermissionFlowProcessor` | 169/307, 55.05% | 144/264, 54.55% | 기존 defer·복원 검증 유지, 실제 Activity Result 복귀는 실기기 범위 |
| `PermissionStateStore.kt` | 22/41, 53.66% | 41/41, 100% | API 28·33 Bundle 왕복과 null 복원 시 스냅샷 참조 유지 검증 |
| `FragmentExtensions.kt` | 10/26, 38.46% | 26/26, 100% | detached·context-only·view 생성 상태의 실행 여부와 결과 반환 검증 |
| `LayoutLifecycleBindRetry` | 15/25, 60% | 27/27, 100% | 성공·상한·0회·중복 start·cancel·detach를 검증하고 지연 재시도 off-by-one 수정 |
| `DataBindingLifecycleOwnerUtil.kt` | 3/9, 33.33% | 9/9, 100% | context owner·ViewTree owner 우선순위·layout 지연 연결·detach 경계 검증 |
| `RecyclerScrollStateView` | 33/103, 32.04% | 75/93, 80.65% | 수직·수평 방향, idle, edge의 listener·Flow 동시 발행과 속성 예외 시 TypedArray 회수 검증 |
| `DialogWindowSize` | 0/16, 0% | 15/16, 93.75% | API 30+ metrics와 API 28 decor·display fallback 검증 |
| `ListAdapterResult` 변환 함수 | 5/11, 45.45% | 11/11, 100% | 큐 drop 사유 5종의 공개 `AdapterDropReason` 변환 검증 |
| `ListAdapterResult.fold` | 0/4, 0% | 4/4, 100% | Applied·Rejected·Dropped·ExecutionError 분기 검증 |
| `NormalAdapterResult.fold` | 0/3, 0% | 3/3, 100% | Applied·Rejected 분기 검증 |
| `AdapterDropReason` | 0/5, 0% | 5/5, 100% | 공개 drop 사유 값 전체 검증 |

`PermissionRequester`, `PermissionFlowProcessor`, `RecyclerScrollStateView`는 보강 과정의 코드 변경으로 실행 가능 Line 총계가 달라졌다.
따라서 보강 전 수치는 당시 기준선이고, 보강 후 수치는 최신 리포트 기준이며 두 비율을 테스트 추가 효과만으로 직접 비교하지 않는다.

감사 과정에서 총 33개 Robolectric 테스트와 7개 Unit 테스트를 추가했다. 단순 생성자 호출이나 data object의 생성 메서드만 채우기보다
소비자가 관찰하는 상태 전이와 결과를 우선 검증했다. 권한 다이얼로그, 설정 화면, 역할 요청 후 Activity Result 복귀는
Robolectric에서 플랫폼 UI를 흉내 내어 수치만 높이지 않고 연결 가능한 실기기에서 검증한다.

## 문서 감사
- Adapter PRD·SPEC·IMPLEMENTATION_PLAN·AGENTS를 전용 Header/Footer 계층이 없는 현재 코드에 맞췄다.
- `README_RECYCLERVIEW.md`의 남아 있던 `SimpleHeaderFooterViewBindingRcvAdapter` 사용 안내를 제거했다.
- `simple_xml/AGENTS.md`의 오래된 Activity/Fragment 경로와 Adapter 설명을 현재 패키지 구조에 맞췄다.
- Layout PRD·IMPLEMENTATION_PLAN을 `docs/feature/ui/layout`으로 이동하고 현행 코드와 테스트 기준으로 정리했다.

## 단계별 처리 계획

### 1단계: 현행 공개 API 감사
- 상태: 완료
- API 기준선과 0.5.1 태그 동일성 확인
- 소비자 계약, 생성 노출, 내부 구현 성격의 API 분류
- 즉시 제거·내부화·Deprecated 처리 없음

### 2단계: 테스트·측정 기준선 정리
- 상태: 완료
- 오래된 Kover 제외 패키지 경로 제거 — 완료
- 공개 권한 요청과 RecyclerView 스크롤 상태의 핵심 경로 테스트 보강 — 완료
- Dialog window 크기 플랫폼 분기 테스트 추가 — 완료
- 권한 상태 Bundle 저장·복원과 Adapter 결과 계약 테스트 추가 — 완료
- Fragment context·view 경계 확장 함수 테스트 추가 — 완료
- Layout lifecycle 재시도와 DataBinding LifecycleOwner 연결 테스트 추가 및 재시도 상한 수정 — 완료

### 3단계: 다음 메이저 사전 검증
- 테스트 전용 DataBinding 리소스 분리 가능성 검증 — 완료, 비배포 테스트 지원 모듈 필요
- `BuildConfig` 생성 비활성화 가능성 검증 — 완료, 다음 메이저 적용 후보
- `safeEmit` 소비자 대체 경로와 내부화 가능성 검증 — 완료, `@JvmSynthetic`과 `internal` 병행 필요
- `QueueDropReason`, `RuntimePermissionHandler`, `PermissionHostAdapter`의 소비자 대체 경로와 마이그레이션 필요 여부 확정
- Breaking Change 승인 후에만 API 기준선 갱신

## 현 단계 결론
- 현 버전에서 공개 API를 삭제하거나 `internal`로 전환하지 않는다.
- Activity, Fragment, Dialog, Layout, Adapter, View 확장, 권한 요청의 소비자 진입점은 유지한다.
- 생성·내부 조율 API의 정리는 다음 메이저 후보로만 기록한다.
- 문서 정합성, Kover 기준선 정리, 권한·Fragment 확장·Layout lifecycle·스크롤·Dialog·Adapter 결과 계약 테스트 보강을 완료했다.
- 다음 우선순위는 연결 가능한 실기기에서 권한 Activity Result 복귀를 검증하고, 다음 메이저 후보의 호환성 계획을 승인받는 것이다.
