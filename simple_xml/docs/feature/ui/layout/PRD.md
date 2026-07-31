# Layout Feature PRD

## 문서 정보
- 문서명: Layout Feature PRD
- 작성일: 2026-07-30
- 수정일: 2026-07-30
- 대상 모듈: simple_xml
- 패키지: `kr.open.library.simple_ui.xml.ui.layout`
- 상태: 현행(as-is)
- 기준 코드:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/layout/base/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/layout/frame/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/layout/linear/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/layout/relative/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/layout/constraint/`

## 배경/문제 정의
- 커스텀 `FrameLayout`, `LinearLayout`, `RelativeLayout`, `ConstraintLayout`은 생명주기 연결과 바인딩 초기화 코드를 반복해서 구현하기 쉽습니다.
- ViewBinding과 DataBinding은 생성 방식과 LifecycleOwner 연결 여부가 다르므로 같은 정리 규칙만으로 처리할 수 없습니다.
- attach와 detach가 반복될 때 바인딩 생성, 초기화, 이벤트 수집과 라이프사이클 옵저버 등록이 중복되면 누수나 중복 수집이 발생할 수 있습니다.
- LifecycleOwner가 attach 직후 준비되지 않는 환경에서도 제한된 재시도로 연결할 수 있어야 합니다.
- 라이브러리 사용자는 일반, ViewBinding, DataBinding 중 필요한 계층을 선택하고 최소 구현만 제공할 수 있어야 합니다.

## 제품 목표
- 네 Android Layout 계열에 동일한 생명주기·바인딩 사용 흐름을 제공합니다.
- 공통 동작을 내부 코디네이터로 모아 공개 베이스 클래스 간 정책 차이를 줄입니다.
- attach 주기마다 라이프사이클 연결과 이벤트 수집을 안전하게 시작하고 detach 시 정리합니다.
- DataBinding에는 사용 가능한 LifecycleOwner를 자동으로 연결하고 detach 시 참조를 해제합니다.
- 바인딩 유지가 필요한 특수한 경우에는 `clearBindingOnDetach=false`를 명시적으로 선택할 수 있게 합니다.

## 비목표
- Compose 전용 Layout 제공
- Android View 자체의 측정·배치·그리기 정책 변경
- Fragment의 `viewLifecycleOwner` 정책 대체
- 사용자가 구현한 코루틴 수집 작업의 취소 방식 강제
- Layout 클래스명이나 패키지명을 변경하는 호환성 파괴
- EditMode 프리뷰 예외를 라이브러리 내부에서 일괄 차단

## 범위

### 포함 범위
- 공통 생명주기 조율
  - `LayoutLifecycleCoordinator`
  - `LayoutLifecycleBindRetry`
  - `LayoutLifecycleCallbacks`
  - `LayoutLifecycleBindRetryCallbacks`
- 공통 바인딩 조율
  - `LayoutBindingCoordinator`
  - `LayoutBindingCallbacks`
  - `DataBindingLifecycleOwnerUtil`
  - `ParentBindingHelperForLayout`
- Layout 계열
  - FrameLayout
  - LinearLayout
  - RelativeLayout
  - ConstraintLayout
- 각 Layout의 공개 계층
  - `Root*Layout`
  - `ParentsBinding*Layout`
  - `BaseDataBinding*Layout`
  - `BaseViewBinding*Layout`
  - 일반 `Base*Layout`

### 제외 범위
- `ui.components.activity`, `ui.components.fragment`, `ui.components.dialog`의 바인딩 생명주기
- RecyclerView Adapter와 ViewHolder 바인딩
- 앱별 커스텀 Layout 구현
- XML 디자인과 스타일 리소스

## 사용자/이해관계자
- 라이브러리 사용자: 커스텀 Layout에서 생명주기와 바인딩 보일러플레이트를 줄여야 합니다.
- 라이브러리 유지보수자: 네 Layout 계열의 attach/detach 정책을 동일하게 유지해야 합니다.
- QA/리뷰어: 재부착, 바인딩 재사용, LifecycleOwner 지연 연결 경계를 검증해야 합니다.

## 공개 계층 선택 기준

| 요구 사항 | 선택 계층 | 사용자 구현 |
| --- | --- | --- |
| 바인딩이 필요 없는 커스텀 Layout | 일반 `Base*Layout` | 필요한 View 동작만 구현 |
| ViewBinding 사용 | `BaseViewBinding*Layout` | inflate 함수 제공, 필요 시 `onInitBind()` 재정의 |
| DataBinding과 LiveData 사용 | `BaseDataBinding*Layout` | layout ID 제공, 필요 시 `onInitBind()` 재정의 |
| 바인딩 생명주기를 직접 구성 | `ParentsBinding*Layout` | `createBinding()`과 필요한 훅 구현 |
| 생명주기 관찰만 필요 | `Root*Layout` | `DefaultLifecycleObserver` 콜백 구현 |

## 핵심 시나리오
1. 사용자는 필요한 Android Layout 종류와 일반, ViewBinding, DataBinding 방식을 선택합니다.
2. View가 attach되면 Root 계층이 호스트 LifecycleOwner 연결을 시작합니다.
3. Binding 계층은 바인딩이 없을 때만 새 인스턴스를 만들고 `onInitBind()`를 호출합니다.
4. attach 주기마다 `onEventVmCollect()`를 한 번 호출합니다.
5. DataBinding 계층은 ViewTree LifecycleOwner를 우선 사용하고, 없으면 Context의 LifecycleOwner를 사용합니다.
6. View가 detach되면 라이프사이클 재시도와 옵저버 연결을 해제하고 이벤트 수집 시작 상태를 초기화합니다.
7. 기본 정책에서는 바인딩을 제거하며, 유지 정책에서는 같은 바인딩을 다음 attach에서 재사용합니다.

## 동작 계약

### 라이프사이클 연결
- `Root*Layout`은 attach 시 `LayoutLifecycleCoordinator.onAttach()`를 호출합니다.
- 연결은 레이아웃 완료 시 우선 시도하고 실패하면 기본 50ms 간격으로 최대 3회 지연 재시도합니다.
- 같은 attach 주기의 중복 시작은 무시합니다.
- detach 시 대기 중인 재시도를 취소하고 등록된 라이프사이클 옵저버를 해제합니다.
- EditMode 가드는 적용하지 않으며 프리뷰 대응은 호출자가 결정합니다.

### 바인딩 생성과 재사용
- `LayoutBindingCoordinator`는 보관된 바인딩이 없을 때만 `createBinding()`을 호출합니다.
- `onInitBind()`는 새 바인딩을 생성한 경우에만 호출합니다.
- `onEventVmCollect()`는 attach 주기당 한 번 호출되며 detach 후 다시 attach되면 다시 호출됩니다.
- `getBinding()`은 `onAttachedToWindow()`가 완료된 뒤 사용해야 합니다.

### detach 정리 정책
- `clearBindingOnDetach` 기본값은 `true`입니다.
- `true`이면 detach 시 바인딩을 제거하고 다음 attach에서 새로 생성하므로 `onInitBind()`도 다시 호출합니다.
- `false`이면 바인딩을 보관하고 다음 attach에서 재사용하므로 `onInitBind()`를 다시 호출하지 않습니다.
- `false`는 기존 View와 바인딩을 안전하게 재사용할 수 있는 경우에만 선택합니다.
- 바인딩을 보관하더라도 detach 상태에서 `getBinding()`을 사용하는 소비자 계약은 제공하지 않습니다.

### DataBinding LifecycleOwner
- ViewTree LifecycleOwner가 Context LifecycleOwner보다 우선합니다.
- 즉시 찾지 못하면 다음 layout 완료 시 한 번 더 확인합니다.
- layout 완료 시 View가 detach 상태이면 LifecycleOwner를 연결하지 않습니다.
- detach 시 `lifecycleOwner`를 `null`로 정리합니다.
- `clearBindingOnDetach=true`이면 detach 시 `unbind()`도 호출합니다.

## 테스트 기준
- 네 일반 Layout 계열의 attach 시 옵저버 연결과 detach 시 해제를 검증합니다.
- 네 Binding Layout 계열의 생성, 초기화, 이벤트 수집, 재부착과 DataBinding 정리를 검증합니다.
- 재시도 성공, 최대 횟수, 0회, 중복 시작, 취소와 detach 경계를 검증합니다.
- Context Owner, ViewTree Owner 우선순위, layout 지연 연결과 detach 경계를 검증합니다.
- 전체 모듈 테스트와 정적 분석 및 API 호환성 검사를 통과해야 합니다.

## 확정 정책
- 별도의 재부착 전용 초기화 훅을 추가하지 않습니다.
- 새 바인딩 초기화는 `onInitBind()`, attach 주기별 작업은 `onEventVmCollect()`로 구분합니다.
- `ParentBindingHelperForLayout`을 유지합니다.
- 내부 코디네이터는 구체 클래스와 콜백 인터페이스 조합을 사용합니다.
- 생명주기 연결 재시도 기본값은 최대 3회, 간격은 50ms입니다.
- 공개 클래스명과 패키지 구조를 유지합니다.

## 관련 문서
- 구현 계획: `simple_xml/docs/feature/ui/layout/IMPLEMENTATION_PLAN.md`
- 모듈 규칙: `simple_xml/AGENTS.md`
- 공개 API 감사: `simple_xml/docs/public_api/PUBLIC_API_AUDIT.md`
