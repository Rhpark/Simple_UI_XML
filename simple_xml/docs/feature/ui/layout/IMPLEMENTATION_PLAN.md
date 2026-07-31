# Layout Feature Implementation Plan (As-Is)

## 문서 정보
- 문서명: Layout Feature Implementation Plan
- 작성일: 2026-07-30
- 수정일: 2026-07-30
- 대상 모듈: simple_xml
- 패키지: `kr.open.library.simple_ui.xml.ui.layout`
- 상태: 현행(as-is)
- 기준 PRD: `simple_xml/docs/feature/ui/layout/PRD.md`

## 목적
- 현재 Layout 구현의 attach/detach 실행 순서를 코드 기준으로 재현 가능하게 정리합니다.
- Frame, Linear, Relative, Constraint 계열이 같은 정책을 유지하는지 검토할 기준을 제공합니다.
- 바인딩 생성, 이벤트 수집, LifecycleOwner 연결과 재시도 경계를 테스트와 함께 관리합니다.

## 구현 범위
- 내부 생명주기 코디네이터와 지연 재시도
- 내부 바인딩 코디네이터와 바인딩 보관 헬퍼
- DataBinding LifecycleOwner 연결 유틸
- FrameLayout, LinearLayout, RelativeLayout, ConstraintLayout 공개 계층
- 일반, ViewBinding, DataBinding 변형
- Layout 전용 Robolectric 테스트

## 구성 요소

| 구성 요소 | 역할 | 공개 여부 |
| --- | --- | --- |
| `LayoutLifecycleCoordinator` | attach/detach 시 생명주기 연결·해제 조율 | internal |
| `LayoutLifecycleBindRetry` | layout 우선 연결과 제한된 지연 재시도 | internal |
| `LayoutBindingCoordinator` | 바인딩 생성·초기화·이벤트 수집·정리 조율 | internal |
| `DataBindingLifecycleOwnerUtil` | DataBinding LifecycleOwner 탐색과 layout 재확인 | internal |
| `Root*Layout` | 호스트 생명주기를 관찰하는 공개 기반 | public |
| `ParentsBinding*Layout` | ViewBinding 공통 생명주기와 확장 훅 제공 | public |
| `BaseViewBinding*Layout` | inflate 함수 기반 ViewBinding 생성 | public |
| `BaseDataBinding*Layout` | layout ID 기반 DataBinding 생성과 Owner 연결 | public |
| 일반 `Base*Layout` | 바인딩 없는 기본 Layout 진입점 | public |

## 단계별 실행 흐름

### 1) Layout 계층 선택
1. 호출부가 Frame, Linear, Relative, Constraint 중 기반 View를 선택합니다.
2. 바인딩이 없으면 일반 `Base*Layout`을 선택합니다.
3. ViewBinding이면 `BaseViewBinding*Layout`, DataBinding이면 `BaseDataBinding*Layout`을 선택합니다.
4. 바인딩 생성 자체를 제어해야 하면 `ParentsBinding*Layout`을 직접 상속합니다.

검증 포인트
- 같은 Layout 종류에서 일반, ViewBinding, DataBinding의 공개 계층이 모두 제공되는지 확인
- 소비자 코드가 내부 coordinator를 직접 참조하지 않는지 확인

### 2) attach와 생명주기 연결
1. Android가 `onAttachedToWindow()`를 호출합니다.
2. Root 계층이 `LayoutLifecycleCoordinator.onAttach()`를 호출합니다.
3. 코디네이터가 layout 완료 시 LifecycleOwner 연결을 먼저 시도합니다.
4. 연결되지 않으면 기본 50ms 간격으로 최대 3회 재시도합니다.
5. 한 attach 주기에서 `start()`가 중복 호출되면 추가 작업을 등록하지 않습니다.

검증 포인트
- 성공 후 추가 재시도가 예약되지 않는지 확인
- `maxRetry`가 지연 재시도 횟수의 정확한 상한인지 확인
- View가 detach되면 대기 작업이 실행되지 않는지 확인

### 3) 바인딩 생성과 초기화
1. ParentsBinding 계층이 `LayoutBindingCoordinator.onAttach()`를 호출합니다.
2. 보관된 바인딩이 없으면 `createBinding()`으로 생성합니다.
3. 새 바인딩을 생성한 경우에만 `onInitBind(binding)`을 호출합니다.
4. 해당 attach 주기에서 아직 이벤트 수집을 시작하지 않았다면 `onEventVmCollect(binding)`을 호출합니다.

검증 포인트
- 같은 attach 주기에서 바인딩과 이벤트 수집이 중복 초기화되지 않는지 확인
- 재사용 바인딩에서 `onInitBind()`가 다시 호출되지 않는지 확인
- detach 후 재부착하면 이벤트 수집 훅이 다시 호출되는지 확인

### 4) ViewBinding 생성
1. `BaseViewBinding*Layout`이 전달받은 inflate 함수를 호출합니다.
2. `LayoutInflater`, 현재 Layout parent, `attachToParent` 값을 그대로 전달합니다.
3. 생성한 ViewBinding을 공통 coordinator가 보관합니다.

검증 포인트
- `attachToParent` 설정이 inflate 함수에 그대로 전달되는지 확인
- Frame, Linear, Relative, Constraint에서 같은 생성 계약을 유지하는지 확인

### 5) DataBinding 생성과 LifecycleOwner 연결
1. `BaseDataBinding*Layout`이 `DataBindingUtil.inflate()`로 바인딩을 생성합니다.
2. attach 공통 처리가 끝나면 `bindLifecycleOwnerOnce(getBinding())`를 호출합니다.
3. ViewTree LifecycleOwner를 먼저 찾고, 없으면 Context LifecycleOwner를 확인합니다.
4. Owner가 없으면 layout 완료 시 한 번 더 확인합니다.
5. layout 완료 시 View가 detach 상태이면 연결하지 않습니다.

검증 포인트
- ViewTree Owner가 Context Owner보다 우선하는지 확인
- Owner가 늦게 준비되면 layout 시 연결되는지 확인
- detached View에 Owner를 연결하지 않는지 확인

### 6) detach 정리
1. DataBinding 계층은 부모가 바인딩을 제거하기 전에 현재 바인딩에 접근합니다.
2. `clearBindingOnDetach=true`이면 `unbind()`를 호출합니다.
3. 유지 여부와 관계없이 DataBinding `lifecycleOwner`를 `null`로 설정합니다.
4. Root 계층이 재시도를 취소하고 라이프사이클 옵저버를 해제합니다.
5. `LayoutBindingCoordinator`가 이벤트 수집 시작 상태를 초기화합니다.
6. 기본 정책이면 바인딩도 제거하고, 유지 정책이면 보관합니다.

검증 포인트
- DataBinding 정리가 바인딩 제거보다 먼저 실행되는지 확인
- 기본 정책의 재부착에서 새 바인딩과 `onInitBind()`가 생성되는지 확인
- 유지 정책의 재부착에서 기존 바인딩을 재사용하는지 확인

## 계열별 적용 상태

| 계열 | Root | ParentsBinding | ViewBinding | DataBinding | 일반 | 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| FrameLayout | 적용 | 적용 | 적용 | 적용 | 적용 | 완료 |
| LinearLayout | 적용 | 적용 | 적용 | 적용 | 적용 | 완료 |
| RelativeLayout | 적용 | 적용 | 적용 | 적용 | 적용 | 완료 |
| ConstraintLayout | 적용 | 적용 | 적용 | 적용 | 적용 | 완료 |

## 테스트 구성
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/layout/frame/binding/BindingFrameLayoutsRobolectricTest.kt`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/layout/linear/binding/BindingLinearLayoutsRobolectricTest.kt`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/layout/relative/binding/BindingRelativeLayoutsRobolectricTest.kt`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/layout/constraint/binding/BindingConstraintLayoutsRobolectricTest.kt`
- 각 Layout 계열의 `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/layout/*/normal/Base*LayoutRobolectricTest.kt`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/layout/base/bind/retry/LayoutLifecycleBindRetryRobolectricTest.kt`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/layout/base/bind/DataBindingLifecycleOwnerUtilRobolectricTest.kt`

## 검증 명령
- `./gradlew :simple_xml:testAll`
- `./gradlew :simple_xml:koverXmlReportDebug`
- `./gradlew :simple_xml:ktlintCheck`
- `./gradlew :simple_xml:lintDebug`
- `./gradlew :simple_xml:apiCheck`
- `./gradlew :simple_xml:assembleDebug :app:assembleDebug`

## 완료 기준
- 네 Layout 계열이 같은 attach/detach 계약을 유지합니다.
- 일반, ViewBinding, DataBinding 테스트가 모두 통과합니다.
- 지연 재시도와 DataBinding LifecycleOwner 경계 테스트가 통과합니다.
- 정적 분석과 API 호환성 검사가 통과합니다.
- 공개 API 삭제나 패키지 이동이 없습니다.
- PRD, 구현 계획, 모듈 인덱스가 현재 코드 경로와 일치합니다.

## 유지보수 원칙
- 생명주기·바인딩 공통 정책은 먼저 내부 coordinator에서 변경합니다.
- 네 Layout 계열 중 하나를 변경하면 나머지 세 계열의 동일 지점도 함께 확인합니다.
- `onInitBind()`와 `onEventVmCollect()`의 호출 의미를 합치지 않습니다.
- `clearBindingOnDetach=false`를 기본값으로 변경하지 않습니다.
- 재시도 횟수나 간격을 변경하면 상한·취소·detach 테스트를 함께 갱신합니다.
- 공개 계층 변경은 API 호환성 승인 후에만 수행합니다.
