# Delegate 패턴 리팩토링 계획

## 1. 현재 문제점

### 1.1 중복 코드 현황
`RootListAdapterCore`와 `RootRcvAdapterCore`에 거의 동일한 연산 로직이 반복됨.

| 중복 함수 | RootListAdapterCore | RootRcvAdapterCore |
|-----------|---------------------|---------------------|
| addItem() | O | O |
| addItemAt() | O | O |
| addItems() | O | O |
| addItemsAt() | O | O |
| removeItem() | O | O |
| removeAt() | O | O |
| removeAll() | O | O |
| replaceItemAt() | O | O |
| moveItem() | O | O |
| setItems() | O | O |
| setItemsLatest() | O | O |
| updateItems() | O | O |
| getItems() | O | O |
| clearQueue() | O | O |

### 1.2 문제점
- **유지보수 어려움**: 버그 수정 시 2곳 모두 수정 필요
- **일관성 위험**: 한쪽만 수정하면 동작 불일치 발생
- **테스트 부담**: 동일 로직을 2번 테스트해야 함
- **코드 양 증가**: 약 400줄 x 2 = 800줄 중복


## 2. 해결 방안: Delegate 패턴

### 2.1 핵심 아이디어
```
┌─────────────────────────────────────────────────────────┐
│           AdapterOperationDelegate<ITEM, META>          │
│  ─────────────────────────────────────────────────────  │
│  - 검증 로직 (인덱스 범위, null 체크 등)                 │
│  - 리스트 변환 로직 (add, remove, replace, move 등)      │
│  - 에러 메시지 생성                                      │
└─────────────────────────────────────────────────────────┘
                    ▲                    ▲
                    │                    │
     ┌──────────────┴───┐       ┌───────┴──────────────┐
     │ RootListAdapter  │       │ RootRcvAdapterCore   │
     │ Core             │       │                      │
     │ ───────────────  │       │ ───────────────────  │
     │ META = Unit      │       │ META = UpdateOp      │
     │ (DiffUtil 처리)  │       │ (notify* 메타데이터) │
     └──────────────────┘       └──────────────────────┘
```

### 2.2 차이점 처리
| 항목 | RootListAdapterCore | RootRcvAdapterCore |
|------|---------------------|---------------------|
| 메타데이터 | 없음 (Unit) | UpdateOp (Insert/Remove/Change/Move) |
| 리스트 변경 | submitList() 호출 | items 직접 변경 + notify*() |
| DiffUtil | 항상 사용 | 선택적 사용 |

→ `META` 제네릭 타입으로 메타데이터 차이를 추상화


## 3. 구현 계획

### 3.1 새로 생성할 파일

#### 파일 1: `AdapterListOperations.kt`
- **위치**: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/temp/base/`
- **역할**: 리스트 연산 공통 로직 (검증 + 변환)
- **내용**:
  ```kotlin
  object AdapterListOperations {
      // 순수 함수로 리스트 변환만 담당
      fun <ITEM> addItem(current: List<ITEM>, item: ITEM): List<ITEM>
      fun <ITEM> addItemAt(current: List<ITEM>, position: Int, item: ITEM): Result<List<ITEM>>
      fun <ITEM> removeItem(current: List<ITEM>, item: ITEM): Result<List<ITEM>>
      fun <ITEM> removeAt(current: List<ITEM>, position: Int): Result<List<ITEM>>
      fun <ITEM> replaceItemAt(current: List<ITEM>, position: Int, item: ITEM): Result<List<ITEM>>
      fun <ITEM> moveItem(current: List<ITEM>, from: Int, to: Int): Result<List<ITEM>>
      // ...
  }
  ```

#### 파일 2: `AdapterOperationValidator.kt`
- **위치**: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/temp/base/`
- **역할**: 인덱스/파라미터 검증 로직
- **내용**:
  ```kotlin
  object AdapterOperationValidator {
      fun validateInsertPosition(position: Int, listSize: Int): ValidationResult
      fun validateAccessPosition(position: Int, listSize: Int): ValidationResult
      fun validateMovePositions(from: Int, to: Int, listSize: Int): ValidationResult
  }
  ```

#### 파일 3: `AdapterOperationMessages.kt`
- **위치**: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/temp/base/`
- **역할**: 에러 메시지 상수/생성 함수
- **내용**:
  ```kotlin
  object AdapterOperationMessages {
      fun invalidInsertPosition(position: Int, listSize: Int): String
      fun invalidAccessPosition(position: Int, listSize: Int): String
      fun itemNotFound(): String
      // ...
  }
  ```

### 3.2 수정할 파일

#### RootListAdapterCore.kt
- 기존 연산 함수 본문을 `AdapterListOperations` 호출로 대체
- 예시:
  ```kotlin
  // 변경 전 (약 15줄)
  fun addItemAt(position: Int, item: ITEM, commitCallback: ((Boolean) -> Unit)? = null) {
      enqueueOperation("addItemAt", commitCallback) { current ->
          if (position < 0 || position > current.size) {
              val message = "Cannot add item at position $position. Valid range: 0..${current.size}"
              Logx.e(message)
              return@enqueueOperation OperationResult(current, false, failure = ...)
          }
          OperationResult(current.toMutableList().apply { add(position, item) }, true)
      }
  }

  // 변경 후 (약 5줄)
  fun addItemAt(position: Int, item: ITEM, commitCallback: ((Boolean) -> Unit)? = null) {
      enqueueOperation("addItemAt", commitCallback) { current ->
          AdapterListOperations.addItemAt(current, position, item)
              .toOperationResult()
      }
  }
  ```

#### RootRcvAdapterCore.kt
- 동일하게 `AdapterListOperations` 호출로 대체
- `UpdateOp` 메타데이터는 호출 측에서 추가
  ```kotlin
  fun addItemAt(position: Int, item: ITEM, commitCallback: ((Boolean) -> Unit)? = null) {
      enqueueOperation("addItemAt", commitCallback) { current ->
          AdapterListOperations.addItemAt(current, position, item)
              .toOperationResult { UpdateOp.Insert(position, 1) }
      }
  }
  ```


## 4. 작업 순서

### Phase 1: 기반 클래스 생성
1. [ ] `AdapterOperationMessages.kt` 생성
2. [ ] `AdapterOperationValidator.kt` 생성
3. [ ] `AdapterListOperations.kt` 생성
4. [ ] 단위 테스트 작성

### Phase 2: RootListAdapterCore 리팩토링
5. [ ] 기존 연산 함수를 새 구조로 변경
6. [ ] 기존 테스트 통과 확인

### Phase 3: RootRcvAdapterCore 리팩토링
7. [ ] 기존 연산 함수를 새 구조로 변경
8. [ ] 기존 테스트 통과 확인

### Phase 4: 검증 및 정리
9. [ ] 전체 빌드 확인
10. [ ] 중복 코드 제거 확인
11. [ ] 문서 업데이트


## 5. 예상 효과

### 5.1 코드 라인 수 변화
| 구분 | 변경 전 | 변경 후 |
|------|---------|---------|
| RootListAdapterCore 연산 부분 | ~300줄 | ~100줄 |
| RootRcvAdapterCore 연산 부분 | ~350줄 | ~120줄 |
| 새 공통 파일 | 0줄 | ~150줄 |
| **총계** | ~650줄 | ~370줄 |
| **절감** | - | **약 43% 감소** |

### 5.2 유지보수 개선
- 버그 수정: 2곳 → 1곳
- 새 연산 추가: 2곳 구현 → 1곳 구현 + 2곳 호출
- 테스트: 중복 테스트 제거


## 6. 리스크 및 대응

### 6.1 API 호환성
- **리스크**: 외부 API 시그니처 변경 시 사용처 영향
- **대응**: 외부 API 시그니처는 변경하지 않음 (내부 구현만 변경)

### 6.2 성능
- **리스크**: 함수 호출 오버헤드 증가
- **대응**: inline 함수 활용, 실제 측정 시 무시할 수준 예상

### 6.3 테스트 커버리지
- **리스크**: 리팩토링 중 기존 동작 변경
- **대응**: 기존 테스트 먼저 확보 후 리팩토링 진행


## 7. 승인 요청 사항

위 계획대로 진행해도 될까요?

- [ ] Phase 1부터 순차 진행
- [ ] 다른 방식 제안 필요
- [ ] 추가 논의 필요
