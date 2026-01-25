# 구현 계획: RecyclerView 어댑터 temp

## 목적
- ui.temp 패키지의 Adapter 베이스 설계를 안전하게 적용하기 위한 구현 순서와 검증 기준을 정리합니다.

## 전제
- `PRD.md` 기준으로 진행합니다.
- 큐 기반 연산과 commitCallback을 기본 정책으로 적용합니다.
- ListAdapter의 submitList 직접 호출은 경고하고 큐 API 사용을 권장합니다.
- DiffUtil 정책과 Executor 주입을 제공하여 성능/테스트성을 확보합니다.
- 아이템 변환은 기본적으로 백그라운드에서 처리하며 필요 시 메인 스레드로 전환합니다.
- Diff 설정(diffCallback/diffExecutor/diffUtilEnabled)은 생성 시점에 결정합니다.
- Simple은 단일 ViewType 전용, Normal은 단일/다중 ViewType을 모두 지원합니다.

## 진행 상태 표기
- 완료: 코드 반영이 끝난 상태(검증/운영 확인은 별도)
- 부분 완료: 일부 반영됨, 추가 확인 필요
- 미진행: 아직 착수하지 않음

## 단계별 구현 순서
1) 공통 코어 정리 (상태: 완료)
   - `RootRcvAdapterCore`, `RootListAdapterCore`
   - 큐 처리 공통화(OperationQueueCoordinator), commitCallback, 스레드 계약, 클릭/롱클릭 정책, Diff 실행 경로 정리
   - 큐 병합 키(setQueueMergeKeys) 지원
2) ListAdapter 계열 정리 (상태: 완료)
   - list.normal
   - list.binding.databind
   - list.binding.viewbind
3) RecyclerView.Adapter 계열 정리 (상태: 완료)
   - normal.normal
   - normal.binding.databind
   - normal.binding.viewbind
4) ViewHolder 계열 정리 (상태: 완료)
   - viewholder.root
   - viewholder.normal
   - viewholder.binding
5) Diff 기본 정책 가이드 정리 (상태: 완료)
   - `DefaultDiffCallback` 및 대량 변경 대응 전략 문서화
6) 사용 흐름/예제 점검 (상태: 부분 완료)
   - 단일/다중 ViewType 흐름 확인
   - 바인딩 선택 메뉴 + 바인딩별 예제 메뉴 + 예제 화면 4개 구성 반영
   - 큐 API 사용 흐름 확인
   - 큐 정책/디버그 리스너 적용 예제 확인
   - diff 옵션(diffExecutor/DiffUtil/커스텀 DiffCallback) 노출 확인
7) 테스트 추가 (상태: 부분 완료)
   - Robolectric 테스트: 큐 드롭 정책/검증 실패 시나리오
   - Robolectric 테스트: 클릭/롱클릭 동작 검증 보강
   - Robolectric 테스트: 큐 병합 동작 검증
   - 큐 순서/commitCallback/클릭 이벤트 검증
8) 예제 패키지 구조 정리 (상태: 부분 완료)
   - databinding/viewbinding/normal 기준으로 분리 완료
   - adapter/activity 하위 패키지로 역할 분리 완료
   - 다중 타입 예제는 temp/multi 하위로 분리 완료
   - 공통 UI/옵션 로직의 base 이동은 보류(화면 인라인 유지)

## 구현 체크리스트
- RecyclerView.Adapter / ListAdapter 모두 제공
- DataBinding / ViewBinding / 일반(비바인딩) 방식 모두 제공
- Simple/Normal 분류는 클래스명 규칙으로 구분
- 큐 기반 아이템 조작 API 제공
- updateItems API 제공
- addItemsAt API 제공
- commitCallback은 메인 스레드 호출 보장
- submitList 직접 호출 경고 및 대체 경로 제공
- ListAdapter diffExecutor 주입은 지원 버전에서만 적용되며, 미지원/리플렉션 실패 시 기본 경로 동작
- diffExecutor 주입 가능
- 아이템 변환 Executor 주입 가능
- 큐 폭주 대응 정책 제공(maxPending/overflowPolicy)
- 큐 병합 키(setQueueMergeKeys) 제공
- QueueMergeKeys 상수 제공
- 실패 원인 리스너(OperationFailureInfo) 제공
- 큐 디버그 리스너 제공
- clearQueue API 제공
- setItemsLatest API 제공
- ThreadCheckMode(LOG/CRASH/OFF) 정책 제공
- Diff OFF 시 notifyItem* 분배 및 폴백 처리
- createViewHolderInternal 구현, attachClickListeners는 코어 처리
- 다중 ViewBinding 혼합 가능

## 검증 기준
- :simple_xml 모듈 컴파일 성공
- 큐 연산 순서 및 성공 여부 콜백 확인
- DiffUtil ON/OFF 동작 확인
- 클릭/롱클릭 이벤트 소비 동작 확인
- 다중 ViewType 시나리오 동작 확인

## 문서화
- README_* 반영은 추후 진행

