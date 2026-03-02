# Adapter Feature Implementation Plan (As-Is)

## 문서 정보
- 문서명: Adapter Feature Implementation Plan
- 작성일: 2026-03-02
- 수정일: 2026-03-02
- 대상 모듈: simple_xml
- 패키지: `kr.open.library.simple_ui.xml.ui.adapter`
- 상태: 현행(as-is)

## 목적
- 현재 adapter 구현을 기준으로 실행 흐름을 단계별로 재현 가능하게 정리합니다.
- 유지보수 시 `normal`, `header/footer`, `list` 중 어느 계층에서 문제가 발생했는지 빠르게 좁힐 수 있게 합니다.
- 예제 앱 사용 방식까지 포함해 코드/문서/테스트 정합성을 유지합니다.

## 구현 범위
- 공통 계약/유틸
- normal 계층
- header/footer section 계층
- list queue 계층
- simple adapter 계층
- viewholder 계층
- 예제 앱 RecyclerView 사용 방식

## 단계별 실행 흐름 (코드 기준)

### 1) adapter 선택
1. 호출부가 사용 시나리오를 결정합니다.
2. content-only + 즉시 반영이면 `BaseRcvAdapter` 계열
3. header/footer가 필요하면 `HeaderFooterRcvAdapter` 계열
4. 연속 변경/대량 변경이면 `BaseRcvListAdapter` 계열

검증 포인트
- `normal`과 `list`를 같은 의미로 섞어 쓰지 않는지 확인
- 예제/README가 같은 선택 기준을 안내하는지 확인

### 2) ViewHolder 생성 및 클릭 연결
1. `normal`
   - `RootRcvAdapter.onCreateViewHolder()`
   - `createViewHolderInternal()`
   - `bindClickListeners(holder)` 1회 실행
2. `list`
   - `BaseRcvListAdapter.onCreateViewHolder()`
   - `createViewHolderInternal()`
   - 내부 `bindClickListener(holder)` 1회 실행

검증 포인트
- 클릭 리스너를 `onBind`마다 다시 달지 않는지 확인
- stale position이 아니라 클릭 시점 현재 position을 쓰는지 확인

### 3) 데이터 조회
1. `getItems()`, `getItemOrNull()`, `getItemPosition()`, `getMutableItemList()` 사용
2. `getMutableItemList()`는 스냅샷 복사본만 반환
3. 수정 후 adapter 갱신은 반드시 `setItems()` 또는 mutation API로 다시 전달

검증 포인트
- `getMutableItemList()` 변경이 adapter 상태를 직접 바꾸는 것으로 오해하지 않는지 확인

### 4) normal mutation 실행
1. `BaseRcvAdapter` 또는 `HeaderFooterRcvAdapter` 공개 mutation API 호출
2. `AdapterCommonDataLogic.validate*()`로 입력 검증
3. 실패 시 즉시 `NormalAdapterResult.Rejected`
4. 성공 시 내부 데이터 갱신 + `notify...`
5. `runResultCallback(NormalAdapterResult.Applied, onResult)`

검증 포인트
- invalid position, empty input, item not found가 즉시 결과로 내려오는지 확인
- `HeaderFooterRcvAdapter`의 section notify 순서가 계약과 맞는지 확인

### 5) list mutation 실행
1. `BaseRcvListAdapter` 공개 mutation API 호출
2. `AdapterCommonDataLogic.validate*()`로 입력 검증
3. 실패 시 즉시 `ListAdapterResult.Rejected`
4. 성공 시 `AdapterOperationQueue`에 operation 등록
5. queue 종료 시 `Applied` 또는 `Failed`를 `onResult`로 전달

검증 포인트
- 호출 시점 성공과 실제 반영 성공을 혼동하지 않는지 확인
- `onResult` null 경로에서도 queue 동작이 정상인지 확인

### 6) queue 처리
1. `AdapterOperationQueue`가 operation을 `OperationQueueProcessor`에 전달
2. processor가 overflow policy와 clear-and-enqueue 정책을 적용
3. 메인 스레드에서 operation 실행
4. `submitList(updatedList, callback)` 경로로 반영
5. terminal state를 public result로 변환

검증 포인트
- queue drop reason이 public `AdapterDropReason`과 일치하는지 확인
- snapshot 기반 operation이 원본 list 변경에 흔들리지 않는지 확인

### 7) bind / payload 처리
1. `normal`
   - `onBindViewHolder(holder, position)` -> `item` 조회 -> `onBindViewHolder(holder, item, position)`
   - payload가 있으면 payload 훅으로 위임
2. `header/footer`
   - adapter position을 section position으로 해석
   - header/content/footer 훅으로 분기
3. `list`
   - `ListAdapter` currentList 기준으로 바인딩

검증 포인트
- payload가 있을 때 full bind로 잘못 떨어지지 않는지 확인
- header/footer section position 계산이 일관적인지 확인

### 8) 예제 앱 사용 흐름
1. `RecyclerViewActivity`에서 3가지 adapter 전환
2. `Add` -> `addItem`
3. `Clear` -> `removeAll`
4. `Change` -> `setItems(getItems().shuffled())`
5. 클릭 삭제는 `setOnItemClickListener` 경로 사용

검증 포인트
- shuffle 후 클릭 삭제가 현재 클릭 아이템 기준으로 동작하는지 확인
- 바인딩 시점 `position` 캡처 패턴이 다시 들어오지 않는지 확인

## 운영 체크리스트
- 공개 API 이름 변경 시 `apiDump` / `apiCheck` 동시 확인
- README와 feature 문서에서 `normal` / `list` 의미를 같은 용어로 유지
- 예제 앱 코드가 권장 패턴을 어기지 않는지 함께 점검
- stale 진단 문자열이나 KDoc 잔재가 없는지 확인

## 로컬 검증 명령
- `./gradlew :simple_xml:compileReleaseKotlin`
- `./gradlew :simple_xml:testDebugUnitTest`
- `./gradlew :simple_xml:apiCheck`
- 예제 앱 확인이 필요하면
  - `./gradlew :app:compileDebugKotlin`

## 검증 항목
- 구조 정합성
  - `common / normal / list / viewholder` 패키지 경계 유지
- 기능 정합성
  - 즉시 반영 vs queue 반영 결과 모델 구분
  - header/footer section CRUD
  - queue drop / failed 의미
- 사용성 정합성
  - 예제 앱 stale position 재발 방지
- 문서 정합성
  - PRD/SPEC/PLAN/README 용어 일치

## 오류 처리/로그 정책
- 입력 오류는 `AdapterCommonDataLogic`에서 검증하고 결과 모델로 반환
- 바인딩 실패나 방어적 불일치 경로는 `Logx`로 기록
- 결과 콜백은 `safeCatch` 또는 queue callback safety 경계로 보호

## 문서 동기화 규칙
- 코드 계약 변경 시 다음 문서를 같은 작업 범위에서 동시 갱신
  - `simple_xml/docs/feature/ui/adapter/PRD.md`
  - `simple_xml/docs/feature/ui/adapter/SPEC.md`
  - `simple_xml/docs/feature/ui/adapter/IMPLEMENTATION_PLAN.md`
  - `docs/readme/README_RECYCLERVIEW.md`
  - 필요 시 `simple_xml/AGENTS.md`

## 개선 백로그 (선택)
- `BaseRcvListAdapter` mutation 블록 delegate 분리 여부 검토
- `BaseRcvAdapterItemApi` 유지 필요성 재검토
- queue policy 문서화와 결과 모델 설명 강화 여부 검토
- `HeaderFooterRcvAdapter` / `BaseRcvListAdapter` 파일 분해 필요성 재평가

## 산출물 기준
- 문서만 보고 현재 adapter 구조를 재현 가능해야 합니다.
- 요구사항 -> 명세 -> 실행 흐름 -> 예제 사용 방식 -> 검증 경로가 연결되어야 합니다.
- 예제 앱이 권장 패턴의 실제 증거 역할을 해야 합니다.
