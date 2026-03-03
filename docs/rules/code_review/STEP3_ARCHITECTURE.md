# Step 3. 아키텍처

목적에 맞게 객체가 분리되어 있는가?

> 아키텍처 규칙 기준: docs/rules/coding_rule/CODE_ARCHITECTURE.md

## 체크리스트

- [ ] 레이어 책임 분리가 올바른가? (ViewModel/UseCase/Repository/UI)
- [ ] 모듈 경계를 위반하지 않는가? (simple_core UI 의존, 역의존)
- [ ] 공개 API 접근 제어가 적절한가?
- [ ] @RequiresPermission, @RequiresApi 어노테이션이 표기되었는가?
- [ ] 하위 호환성이 고려되었는가?
- [ ] 가변 상태가 외부에 노출되지 않는가?
- [ ] SRP: 클래스/함수가 단일 책임을 가지는가? (설명에 "그리고"가 들어가면 위반 의심)
- [ ] DIP: 구체 구현이 아닌 인터페이스/추상화에 의존하는가?

## 심각도 기준

- CRITICAL: 모듈 경계 위반 (simple_core UI 의존, 역의존)
- HIGH: 비즈니스 로직 레이어 오배치
- HIGH: 가변 상태 외부 노출
- HIGH: 공개 API 접근 제어 부적절
- HIGH: SRP 위반 (하나의 클래스/함수가 여러 책임 보유)
- MEDIUM: DIP 위반 (구체 구현에 직접 의존)
