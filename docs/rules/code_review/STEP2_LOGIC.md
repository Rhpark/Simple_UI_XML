# Step 2. 로직 & 안정성

로직에 이상이 없는가? 런타임 안정성은 확보되었는가?

> 규칙 기준: docs/rules/CODING_RULE_INDEX.md

## 로직 체크리스트

- [ ] 로직 오류, 무한루프, 잘못된 조건 분기가 없는가?
- [ ] null 처리가 적절한가?
- [ ] 경계값 처리가 적절한가?
- [ ] SDK 버전 분기가 필요한 구간에 처리되었는가?

## 안정성 체크리스트 (라이프사이클 규칙)

- [ ] 메모리 누수 없는가?
- [ ] GlobalScope 사용 없는가?
- [ ] Fragment에서 viewLifecycleOwner 사용하는가?
- [ ] 코루틴 누수/미취소 없는가?
- [ ] CancellationException 오처리 없는가?

## 예외 처리 체크리스트 (패턴 규칙)

- [ ] safeCatch / tryCatchSystemManager 사용하는가?
- [ ] 빈 catch 블록 없는가?

## 테스트 영향 체크리스트

- [ ] 변경으로 깨질 가능성이 있는 기존 테스트를 확인했는가?
- [ ] 추가가 필요한 테스트 케이스(정상/예외/경계)를 명시했는가?

## 심각도 기준

- CRITICAL: 메모리 누수, GlobalScope 사용, 코루틴 누수
- HIGH: 로직 오류, null 미처리, SDK 분기 누락, 빈 catch 블록
- HIGH: 테스트 영향 분석 누락
- MEDIUM: 추가 테스트 케이스(정상/예외/경계) 명시 누락
