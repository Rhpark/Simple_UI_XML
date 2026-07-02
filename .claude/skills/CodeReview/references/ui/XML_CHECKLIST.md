# UI 체크리스트 — XML (ViewBinding / DataBinding)

XML 기반 레이아웃이 포함된 코드 리뷰 시 references/ 5단계와 병행한다.
STEP2(기능) 및 STEP4(품질/성능) 시 해당 항목을 추가 적용한다.

---

## 1단계: Binding 방식 감지

대상 파일에서 아래 패턴으로 방식을 판단하고 결과를 출력한다.

| 감지 패턴 | 방식 |
|-----------|------|
| `ActivityXxxBinding.inflate` / `FragmentXxxBinding.inflate` | ViewBinding |
| `DataBindingUtil.setContentView` / `DataBindingUtil.inflate` / `<layout>` 태그 | DataBinding |
| 위 패턴 없음 | 일반 `findViewById` |

```
> Binding 방식: [ViewBinding / DataBinding / findViewById]
```

---

## 2단계: ViewBinding 고유 체크

ViewBinding 감지 시 아래 항목을 적용한다.

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| VB1 | Fragment `onDestroyView`에서 `_binding = null` 미처리 | HIGH | View 트리 참조 유지 → 메모리 누수 |
| VB2 | `binding!!` 강제 언팩 사용 | HIGH | NPE 위험 |
| VB3 | `onCreateView`에서 View 참조 초기화 — `onViewCreated`에서 해야 함 | MEDIUM | 생명주기 불일치 |
| VB4 | binding을 companion object / 전역 변수로 보유 | HIGH | Activity/Fragment 소멸 후에도 참조 유지 → 누수 |

---

## 3단계: DataBinding 고유 체크

DataBinding 감지 시 아래 항목을 적용한다.

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| DB1 | `binding.lifecycleOwner = viewLifecycleOwner` 미설정 | HIGH | LiveData/StateFlow 관찰 안 됨 |
| DB2 | Two-way binding `@={}` 에서 무한 업데이트 루프 가능성 | HIGH | 화면 반복 갱신 |
| DB3 | `@BindingAdapter`에 비즈니스 로직 삽입 | MEDIUM | 테스트 불가, 로직 분산 |
| DB4 | `ObservableField` + `StateFlow` 혼용 | MEDIUM | 관리 혼재, 중복 구독 |
| DB5 | `<variable>` 타입이 구체 클래스 — 인터페이스/ViewModel 대신 직접 참조 | MEDIUM | 테스트 대체 불가 |

---

## 4단계: RecyclerView / Adapter 체크

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| RV1 | `notifyDataSetChanged()` 사용 — `DiffUtil` / `ListAdapter` 미사용 | HIGH | 전체 아이템 재그리기, 애니메이션 없음 |
| RV2 | `onBindViewHolder`에서 이미지 디코딩·DB 조회 등 무거운 연산 | HIGH | 스크롤 프레임 드랍 |
| RV3 | ViewHolder 내부에서 Context·Activity 참조 직접 보유 | HIGH | 메모리 누수 |
| RV4 | `NestedScrollView` 내부에 `RecyclerView` (`wrap_content`) | HIGH | 전체 아이템 한번에 생성, 재활용 불가 |
| RV5 | 아이템 수 고정인데 `setHasFixedSize(true)` 미설정 | LOW | 불필요한 크기 재계산 |

---

## 5단계: Fragment / Activity 생명주기 체크

| # | 항목 | 심각도 | 위험 |
|---|------|--------|------|
| LC1 | Fragment에서 `viewLifecycleOwner` 대신 `this`로 Observer 등록 | HIGH | backstack 복귀 시 관찰자 중복 누적 |
| LC2 | Fragment 간 직접 참조 (`(activity as XxxActivity).fragment`) | HIGH | 강한 결합, 생명주기 불일치 |
| LC3 | ViewModel 없이 Fragment에서 직접 네트워크/DB 호출 | HIGH | 화면 회전 시 데이터 유실 |
| LC4 | `onResume`/`onPause`에서 무거운 초기화 — 매 포커스 진입마다 실행 | MEDIUM | 불필요한 재초기화 |

---

## 심각도 기준

- HIGH: 메모리 누수·프레임 드랍·데이터 유실·NPE 위험
- MEDIUM: 테스트 어려움·로직 분산·불필요한 재초기화
- LOW: 성능 미세 개선 기회
