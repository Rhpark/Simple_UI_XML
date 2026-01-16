# PRD: RecyclerView Adapter 베이스 설계

## 문서 목적
- ui.temp 패키지의 Adapter 베이스 구조와 사용 원칙을 정의합니다.
- RecyclerView.Adapter, ListAdapter 기반 커스텀 베이스의 일관된 사용 방식을 문서화합니다.

## 기본 목적
- RecyclerView에서 사용하는 Adapter를 DataBinding, ViewBinding, 일반(Non-binding) 방식으로 자주 커스텀합니다.
- 매번 처음부터 다시 구현하지 않고, 미리 커스텀된 베이스를 제공하여 생산성을 높입니다.
- 팀 내 표준 구조를 제공하여 유지보수 비용과 구현 편차를 줄입니다.

## 배경/문제 정의
- Adapter 구현은 바인딩 방식에 따라 중복 코드가 반복됩니다.
- ViewHolder 생성/바인딩, 아이템 비교, DiffUtil 구성 등에서 반복이 잦습니다.
- 프로젝트별로 구현 방식이 달라 재사용성과 일관성이 떨어집니다.

## 설계 원칙
- 일관성: DataBinding/ViewBinding/일반 방식의 사용 흐름을 통일합니다.
- 최소 중복: 공통 로직은 base 계층으로 흡수합니다.
- 안전성: 바인딩 접근 시점과 생명주기 범위를 명확히 합니다.
- 확장성: 다양한 Adapter 유형(RecyclerView.Adapter, ListAdapter)을 유연하게 지원합니다.

## 사용자 시나리오
- 개발자는 신규 리스트 화면에서 Adapter를 빠르게 생성해야 합니다.
- DataBinding 또는 ViewBinding 방식 중 하나를 선택하고, 최소 구현만 작성합니다.
- 베이스가 공통 흐름을 제공하여 구현자는 데이터/뷰 로직에 집중합니다.

## 구조 초안(역할 요약)
- base: 공통 정책/헬퍼/추상 계층
- list: ListAdapter 계열 베이스(간단형/일반형 포함)
  - list.binding: 바인딩 기반 ListAdapter
    - list.binding.simple: 간단형(람다 기반)
      - list.binding.simple.databind: DataBinding 간단형
      - list.binding.simple.viewbind: ViewBinding 간단형
    - list.binding.normal: 일반형(추상 메서드 기반)
      - list.binding.normal.databind: DataBinding 일반형
      - list.binding.normal.viewbind: ViewBinding 일반형
  - list.normal: 바인딩 없는 ListAdapter
    - list.normal.simple: 간단형(람다 기반)
    - list.normal.normal: 일반형(추상 메서드 기반)
- normal: RecyclerView.Adapter 기반(간단형/일반형 포함)
  - normal.binding: 바인딩 기반 RecyclerView.Adapter
    - normal.binding.simple: 간단형(람다 기반)
      - normal.binding.simple.databind: DataBinding 간단형
      - normal.binding.simple.viewbind: ViewBinding 간단형
    - normal.binding.normal: 일반형(추상 메서드 기반)
      - normal.binding.normal.databind: DataBinding 일반형
      - normal.binding.normal.viewbind: ViewBinding 일반형
  - normal.normal: 바인딩 없는 RecyclerView.Adapter
    - normal.normal.simple: 간단형(람다 기반)
    - normal.normal.normal: 일반형(추상 메서드 기반)
- viewholder: ViewHolder 계열 베이스
  - viewholder.binding: 바인딩 기반 ViewHolder
  - viewholder.normal: 바인딩 없는 ViewHolder

## 사용 흐름(예시)
1) 개발자는 ListAdapter 또는 RecyclerView.Adapter를 선택합니다.
2) DataBinding/ViewBinding/일반 중 필요한 방식을 선택합니다.
3) 간단형(simple) 또는 일반형(normal) 베이스를 선택합니다.
4) 베이스를 상속하고 최소 구현(아이템 타입, 바인딩 로직)만 작성합니다.
5) 공통 정책(예: DiffUtil, 이벤트 처리, 바인딩 접근 규칙)을 베이스에서 보장합니다.

## 범위
- simple/normal 분류는 아래 패키지 구조에 포함됩니다.
- 대상 패키지:
  - kr.open.library.simple_ui.xml.ui.temp.base
  - kr.open.library.simple_ui.xml.ui.temp.list
  - kr.open.library.simple_ui.xml.ui.temp.list.binding
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.simple
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.simple.databind
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.simple.viewbind
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.normal
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.normal.databind
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.normal.viewbind
  - kr.open.library.simple_ui.xml.ui.temp.list.normal
  - kr.open.library.simple_ui.xml.ui.temp.list.normal.simple
  - kr.open.library.simple_ui.xml.ui.temp.list.normal.normal
  - kr.open.library.simple_ui.xml.ui.temp.viewholder
  - kr.open.library.simple_ui.xml.ui.temp.viewholder.binding
  - kr.open.library.simple_ui.xml.ui.temp.viewholder.normal
  - kr.open.library.simple_ui.xml.ui.temp.normal
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.simple
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.simple.databind
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.simple.viewbind
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.normal
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.normal.databind
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.normal.viewbind
  - kr.open.library.simple_ui.xml.ui.temp.normal.normal
  - kr.open.library.simple_ui.xml.ui.temp.normal.normal.simple
  - kr.open.library.simple_ui.xml.ui.temp.normal.normal.normal

## 비범위
- 기존 adapter 패키지의 직접 수정
- UI 디자인/레이아웃 변경
- 대규모 API 변경(클래스명/패키지명 변경)

## 결정/정책 메모
- DiffUtil은 공통 베이스 + 헬퍼/옵션으로 제공합니다.
- 단일 ViewType 사용이 가장 쉽도록 기본 흐름을 설계합니다.
- 간단형(simple)은 단일 ViewType 전용입니다.
- Normal 계열은 단일(BaseSingle)과 다중(BaseMulti)을 모두 포함합니다.
- BaseSingle 계열은 단일 ViewType 전용이며 Simple이 이를 상속합니다.
- BaseMulti 계열은 다중 ViewType 전용입니다.
- 간단형은 람다 기반 사용을 허용하며, 일반형은 추상 메서드 기반을 기본으로 합니다.
- 간단형/일반형은 공통 기능을 공유하되, ViewType 범위는 분리합니다.
- Adapter 유형(RecyclerView.Adapter, ListAdapter)별 베이스를 분리합니다.
- 바인딩 방식(DataBinding/ViewBinding/일반)에 따라 베이스 클래스를 분리합니다.
- ViewHolder 생성/바인딩 흐름은 베이스에서 통일된 패턴으로 제공합니다.
- 공통 유틸/헬퍼는 base 계층에 배치합니다.

## 리스크 및 대응
- 중복 추상화로 인해 복잡도가 증가할 수 있습니다.
  - 대응: 베이스는 최소 책임만 갖고, 과도한 공통화를 지양합니다.
- 베이스 정책과 실제 사용 방식의 불일치가 발생할 수 있습니다.
  - 대응: 예제/가이드를 통해 사용 흐름을 명확히 합니다.

## 테스트/검증(추후)
- Adapter 생성/바인딩 기본 동작 테스트
- ListAdapter DiffUtil 정책 검증
- ViewHolder 재사용 시 바인딩 안정성 확인

## 관련 문서
- 구현 계획: ui/temp/IMPLEMENTATION_PLAN.md

## 문서화
- README_*.md 반영은 추후 추가 예정