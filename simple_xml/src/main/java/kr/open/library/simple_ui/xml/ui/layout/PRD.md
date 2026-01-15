# PRD: Layout 베이스 설계

## 문서 목적
- layout 패키지의 신규 구조와 사용 원칙을 정의합니다.
- 커스텀 레이아웃 베이스의 일관된 사용 방식을 문서화합니다.

## 기본 목적
- 사용자는 LinearLayout, RelativeLayout, ConstraintLayout, FrameLayout 등을 커스텀해서 사용합니다.
- 커스텀 사용 방식은 DataBinding, ViewBinding, 일반(Non-binding) 세 가지로 크게 나뉩니다.
- 매번 처음부터 다시 구현하지 않고, 미리 커스텀된 베이스를 제공하여 생산성을 높입니다.

## 설계 원칙
- 일관성: DataBinding/ViewBinding/일반 방식의 사용 흐름을 통일합니다.
- 최소 중복: 반복 로직은 공통 구조로 흡수합니다.
- 안전성: attach/detach 구간에서만 binding 접근을 허용합니다.
- 단순성: 레이아웃 구현자는 최소 호출만으로 동작하도록 합니다.

## 참고 패턴(components.fragment)
- 참고 수준으로만 활용합니다(정책 강제 아님).
- Root/ParentsBinding/Base 계열 구조를 참고합니다.
- 바인딩 생명주기 정리(onDestroyView 시점) 패턴을 참고합니다.
- 이벤트 수집 단일 실행(Helper 사용) 패턴을 참고합니다.
- super 호출 강제(CallSuper) 패턴을 참고합니다.

## 관련 문서
- 구현 계획: ui/layout/IMPLEMENTATION_PLAN.md

## 구조 초안(역할 요약)
- Root 계열: 라이프사이클 기반 공통 동작의 기반
- ParentsBinding 계열: binding 생성/보관 + 이벤트 수집 시작
- BaseDataBinding 계열: DataBinding inflate + LifecycleOwner 연결
- BaseViewBinding 계열: ViewBinding inflate 공통화
- Normal 계열: binding 없는 기본 레이아웃

## 사용 흐름(예시)
1) 사용자는 필요 타입(Frame/Linear/Relative/Constraint)과 방식(DataBinding/ViewBinding/일반)을 선택합니다.
2) 베이스 클래스를 상속하고 최소 구현(예: createBinding)만 제공합니다.
3) attach 시 binding 초기화 및 이벤트 수집이 시작됩니다.
4) detach 시 정리가 수행되어 재부착에 대비합니다.

## 범위
- 대상 패키지:
  - kr.open.library.simple_ui.xml.ui.layout.frame
  - kr.open.library.simple_ui.xml.ui.layout.linear
  - kr.open.library.simple_ui.xml.ui.layout.relative
  - kr.open.library.simple_ui.xml.ui.layout.constraint
- 공통 베이스/유틸 위치:
  - kr.open.library.simple_ui.xml.ui.layout.base
  - (하위) kr.open.library.simple_ui.xml.ui.layout.base.bind
  - (하위) kr.open.library.simple_ui.xml.ui.layout.base.lifecycle

## 비범위
- PRD 범위 외 패키지에 대한 직접 수정
- UI 동작/디자인 변경
- 대규모 API 변경(클래스명/패키지명 변경)

## 결정/정책 메모
- binding 접근 시점은 attach~detach 구간으로 제한합니다.
- event collect는 attach 주기당 1회만 시작합니다.
- onInitBind는 createBinding으로 새 binding이 연결될 때만 호출합니다.
  - attach 시 새 binding 연결: onInitBind 호출
  - attach 시 기존 binding 재사용: onInitBind 미호출
- clearBindingOnDetach 기본값은 true를 유지합니다.
- clearBindingOnDetach=false 사용 시 재부착 동작 차이를 문서로 명시합니다.
- ParentBindingHelperForLayout는 일단 유지하고, 필요 시 이후 변경을 검토합니다.
- 코디네이터는 구체 클래스 + 콜백 인터페이스 조합을 기본으로 합니다.

## 결정 필요 항목
- onInitBind를 재부착마다 호출할 별도 훅을 추가할지 여부
- clearBindingOnDetach=false 사용 시 기대 동작의 범위
- 코디네이터 실제 파일명/위치 명명 규칙
- 재시도 정책(횟수/간격) 상세값
