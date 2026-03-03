# Step 3. 리팩토링 실행

coding_rule을 준수하며 한 번에 하나씩 변경한다.

> 코드 작성 규칙 기준: docs/rules/coding_rule/
> 코드 패턴 기준: docs/rules/coding_rule/CODE_PATTERNS_INDEX.md

## 체크리스트

- [ ] 한 번에 하나의 변경만 수행하는가? (atomic 변경)
- [ ] 각 변경 후 빌드가 성공하는가?
- [ ] coding_rule의 패턴을 준수하는가?
- [ ] 기능 변경 없이 구조만 개선하는가? (리팩토링 원칙)
- [ ] 변경 이유를 명확히 기록하는가?

## 실행 원칙

### 한 번에 하나씩
- 이름 변경 → 빌드 확인 → 구조 변경 → 빌드 확인 순으로 진행
- 여러 변경을 동시에 하면 문제 발생 시 원인 파악 불가

### coding_rule 우선 적용
> **범위**: 리팩토링 대상 코드에서 아래 위반이 발견된 경우에만 수정한다. 주변 코드 전면 교체는 별도 작업으로 분리한다.

- 로깅 → `Logx` 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_LOGGING.md)
- 예외 처리 → `safeCatch` 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_EXCEPTION.md)
- SDK 분기 → `checkSdkVersion` 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_SDK.md)
- Adapter → Simple UI Adapter 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_RECYCLERVIEW.md)
- 확장 함수 → 라이브러리 확장 함수 사용 (docs/rules/coding_rule/patterns/CODE_PATTERNS_EXTENSIONS.md)

### 기능 보존
- 리팩토링은 외부 동작을 바꾸지 않는다
- 기능 개선이 필요하면 리팩토링과 분리하여 별도 작업으로 진행

## 심각도 기준

- HIGH: 리팩토링과 기능 변경을 동시에 수행
- HIGH: 빌드 미확인 상태에서 다음 변경 진행
- MEDIUM: coding_rule 패턴 미적용
