# 제품 요구사항: RecyclerView 어댑터 temp 설계

## 문서 목적
- ui.temp 패키지의 Adapter 베이스 구조와 사용 원칙을 정의합니다.
- RecyclerView.Adapter / ListAdapter 기반의 공통 정책과 사용 흐름을 문서화합니다.

## 기본 목적
- DataBinding / ViewBinding / 일반(비바인딩) 방식의 반복 구현을 줄입니다.
- 팀 내 표준 구조로 유지보수 비용과 구현 편차를 낮춥니다.
- 큐 기반 연산과 스레드 계약으로 안정성을 확보합니다.

## 배경/문제 정의
- ViewHolder 생성, 바인딩, DiffUtil, 이벤트 처리 등이 반복됩니다.
- 구현 방식 차이로 재사용성과 일관성이 떨어집니다.
- 대량 변경/연속 변경 시 동시성 문제가 발생하기 쉽습니다.

## 설계 원칙
- 일관성: 바인딩 방식과 어댑터 유형에 관계없이 동일한 흐름을 제공합니다.
- 안전성: 메인 스레드 계약, 입력 검증, 큐 기반 연산을 기본으로 합니다.
- 확장성: 단일/다중 ViewType과 모든 바인딩 방식을 지원합니다.
- 테스트성: Diff 계산 Executor 주입과 아이템 변환 Executor 주입으로 테스트가 가능해야 합니다.

## 사용자 시나리오
- 개발자는 신규 리스트 화면에서 어댑터를 빠르게 생성합니다.
- 단일 ViewType을 기본으로 사용하고 필요 시 다중 ViewType으로 확장합니다.
- 아이템 조작은 큐 API로 처리하고 완료 콜백으로 결과를 받습니다.

## 예제 제공 범위(앱)
- 예제 코드는 app 모듈의 simpleui_xml.temp 패키지에서 제공합니다.
- 바인딩 선택 메뉴 1개 + 바인딩별 예제 메뉴 3개로 구성합니다(총 4개 메뉴).
- 각 예제 메뉴는 예제 화면 4개로 구성합니다.
- 예제 화면은 ListAdapter 단일/다중, RecyclerView.Adapter 단일/다중 흐름을 제공합니다.
- 다중 타입 예제 코드는 temp/multi 하위로 분리합니다.
- diff 옵션(diffExecutor/DiffUtil/커스텀 DiffCallback)을 기본 노출합니다.

## 구조(실제 구현 기준)
- base: 공통 정책/헬퍼/추상 계층(AdapterOperationQueueCoordinator/AdapterClickBinder 포함, internal)
  - base.internal: internal 헬퍼 패키지
  - base.list: ListAdapter 공통 코어
  - base.normal: RecyclerView.Adapter 공통 코어
- list: ListAdapter 계열
  - list.binding.databind: DataBinding 기반
  - list.binding.viewbind: ViewBinding 기반
  - list.normal: 일반(비바인딩) 기반
- normal: RecyclerView.Adapter 계열
  - normal.binding.databind: DataBinding 기반
  - normal.binding.viewbind: ViewBinding 기반
  - normal.normal: 일반(비바인딩) 기반
- viewholder: ViewHolder 계열
  - viewholder.binding
  - viewholder.normal
  - viewholder.root

## 핵심 정책
- 큐 기반 연산: add/remove/move/replace/setItems는 큐에 적재되어 순차 처리됩니다.
- 배치 업데이트: updateItems로 여러 변경을 단일 큐 연산에서 처리합니다.
- 큐 처리 공통화: Root*는 결과 적용만 담당하고 큐 실행/드롭/오류 처리는 공통 코디네이터에서 처리합니다.
- 큐 병합: 동일 이름의 연속 연산은 머지 키로 병합되어 최신 요청만 유지됩니다.
- commitCallback: 모든 큐 연산은 성공 여부(Boolean)를 메인 스레드에서 전달합니다.
- AdapterOperationFailureListener: 실패 상세(AdapterOperationFailure)를 메인 스레드에서 전달합니다.
- 큐 폭주 대응: maxPending/overflowPolicy 설정으로 드롭 정책을 제어합니다.
- 큐 정리/최신화: clearQueue로 대기 큐를 비우고 setItemsLatest로 최신 요청만 유지합니다.
- submitList 직접 호출 제한: ListAdapter 계열은 submitList 직접 호출을 경고하고 큐 API 사용을 권장합니다.
- Diff 정책:
  - ListAdapter는 DiffUtil이 기본이며 DefaultDiffCallback은 동등성 비교를 사용합니다.
  - ListAdapter의 diffExecutor 주입은 AsyncDifferConfig 지원 버전에서만 적용되며, 미지원 시 기본 경로로 동작합니다.
  - RecyclerView.Adapter는 DiffUtil 기본 OFF이며 작은 변경에만 ON을 권장합니다.
  - Diff 설정(diffCallback/diffExecutor/diffUtilEnabled)은 생성 시점에 결정합니다.
  - RecyclerView.Adapter에서 Diff OFF 시 큐 연산 타입에 맞춰 notifyItem*를 사용하며, 범위가 불명확하면 전체 갱신으로 폴백합니다.
  - Diff OFF + 메인 스레드 경로에서는 내부 리스트 재사용으로 할당을 줄입니다.
  - 대량 변경은 DiffUtil OFF 또는 비교 로직 단순화를 권장합니다.
- 스레드 계약:
  - 공개 API는 메인 스레드 호출이 기본이며 AdapterThreadCheckMode(LOG/CRASH/OFF)로 위반 처리 정책을 선택합니다.
  - 기본값: Debug=CRASH, Release=LOG.
  - Diff 계산은 백그라운드 Executor에서 수행됩니다(주입 가능).
  - 아이템 변환은 기본적으로 백그라운드에서 수행되며, 필요 시 메인 스레드로 전환할 수 있습니다.
- 운영 디버깅:
  - 큐 이벤트(추가/시작/완료/드롭)를 리스너로 전달할 수 있습니다.
- 클릭/롱클릭:
  - 리스너는 onCreateViewHolder에서 1회 바인딩합니다.
  - 하위 클래스는 createViewHolderInternal만 구현하며 attachClickListeners는 코어에서 처리합니다.
  - 클릭 시점의 bindingAdapterPosition으로 아이템을 조회합니다.
  - 롱클릭 리스너가 없으면 false를 반환하여 이벤트 소비를 방지합니다.
- 다중 ViewBinding:
  - viewType별 다른 ViewBinding 혼합을 지원합니다.
  - viewType 매핑 누락 시 예외/로그로 실패를 명확히 합니다.

## 범위
- simple/normal 분류는 클래스명으로 구분하며, 패키지로 분리하지 않습니다.
- 대상 패키지:
  - kr.open.library.simple_ui.xml.ui.temp.base
  - kr.open.library.simple_ui.xml.ui.temp.base.internal
  - kr.open.library.simple_ui.xml.ui.temp.base.list
  - kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback
  - kr.open.library.simple_ui.xml.ui.temp.base.normal
  - kr.open.library.simple_ui.xml.ui.temp.list
  - kr.open.library.simple_ui.xml.ui.temp.list.binding
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.databind
  - kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind
  - kr.open.library.simple_ui.xml.ui.temp.list.normal
  - kr.open.library.simple_ui.xml.ui.temp.normal
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind
  - kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind
  - kr.open.library.simple_ui.xml.ui.temp.normal.normal
  - kr.open.library.simple_ui.xml.ui.temp.viewholder
  - kr.open.library.simple_ui.xml.ui.temp.viewholder.binding
  - kr.open.library.simple_ui.xml.ui.temp.viewholder.normal
  - kr.open.library.simple_ui.xml.ui.temp.viewholder.root

## 비범위
- 기존 adapter 패키지의 직접 수정
- UI 디자인/레이아웃 변경
- 대규모 API 변경(클래스명/패키지명 변경)

## 리스크 및 대응
- 추상화 복잡도 증가
  - 대응: 베이스는 최소 책임만 갖고, 예제/가이드로 사용 흐름을 보완합니다.
- Diff 정책 오사용
  - 대응: 기본 정책 가이드와 커스텀 콜백 예시를 제공합니다.
- 큐 기반 API 미사용
  - 대응: submitList 경고와 문서 가이드로 사용을 유도합니다.

## 테스트/검증(추후)
- 어댑터 생성/바인딩 기본 동작 테스트
- 큐 연산 순서 및 commitCallback 호출 타이밍 확인
- Diff ON/OFF 동작 및 대량 변경 시 동작 확인
- 클릭/롱클릭 이벤트 소비 여부 확인
- 다중 ViewType 시나리오 동작 확인

## 관련 문서
- 구현 계획: ui/temp/IMPLEMENTATION_PLAN.md

## 문서화
- README_* 반영은 추후 진행


