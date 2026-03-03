# Step 3. 구현

coding_rule을 준수하며 작업 유형에 따라 구현한다.

> 코드 작성 규칙 기준: docs/rules/coding_rule/
> 코드 패턴 기준: docs/rules/coding_rule/CODE_PATTERNS_INDEX.md

## 체크리스트

- [ ] 한 번에 하나의 변경만 수행하는가? (atomic 변경)
- [ ] 각 변경 후 빌드가 성공하는가?
- [ ] coding_rule의 패턴을 준수하는가?
- [ ] 리팩토링과 기능 구현을 동시에 수행하지 않는가?

## 유형별 구현 원칙

### 추가 (add)
- 새 클래스/함수는 coding_rule 명명 규칙을 따른다
- 공개 API에는 KDoc 필수
- 테스트 코드를 함께 작성한다

### 개선 (improve)
- 기존 API 시그니처 변경 시 하위 호환 유지 방법을 먼저 검토
- 하위 호환 불가 시 사용자에게 사전 확인
- 변경 전/후 동작 차이를 명확히 기록

### 제거 (remove)
1. @Deprecated 애노테이션 추가 (대체 API 안내 포함)
2. 빌드/테스트 확인
3. 실제 제거는 다음 Major 버전에서 진행 (즉시 제거 금지)

## coding_rule 우선 적용

> **범위**: 구현 대상 코드에 아래 위반이 있는 경우에만 수정한다.

- 로깅 → `Logx` 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_LOGGING.md)
- 예외 처리 → `safeCatch` 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_EXCEPTION.md)
- SDK 분기 → `checkSdkVersion` 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_SDK.md)
- Adapter → Simple UI Adapter 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_RECYCLERVIEW.md)
- 확장 함수 → 라이브러리 확장 함수 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_EXTENSIONS.md)

## 심각도 기준

- HIGH: 기능 구현과 리팩토링을 동시에 수행
- HIGH: 빌드 미확인 상태에서 다음 변경 진행
- HIGH: 제거 시 @Deprecated 없이 즉시 삭제
- MEDIUM: coding_rule 패턴 미적용
