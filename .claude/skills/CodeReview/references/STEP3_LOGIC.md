# Step 3. 로직 & 안정성(정확성)

로직에 이상이 없는가? 런타임 안정성은 확보되었는가?

> 규칙 기준: docs/rules/CODING_RULE_INDEX.md

## 로직 체크리스트

- [ ] 로직 오류, 무한루프, 잘못된 조건 분기가 없는가?
- [ ] null 처리가 적절한가?
- [ ] 경계값 처리가 적절한가?
- [ ] SDK 버전 분기가 필요한 구간에 처리되었는가?
- [ ] equals()를 오버라이드하면 hashCode()도 함께 오버라이드했는가? (계약 위반 시 HashMap/HashSet에서 데이터 유실)
- [ ] 부동소수(Double/Float)를 == 로 직접 비교하지 않는가? (연산 오차 — 허용오차(epsilon) 비교 필요)

## 안정성 체크리스트 (라이프사이클 규칙)

- [ ] 메모리 누수 없는가?
- [ ] GlobalScope 사용 없는가?
- [ ] Fragment에서 viewLifecycleOwner 사용하는가?
- [ ] 코루틴 누수/미취소 없는가?
- [ ] CancellationException 오처리 없는가?

## 동시성 체크리스트 (스레드 안전)

- [ ] 여러 스레드가 접근하는 공유 가변 상태가 적절히 동기화되었는가? (check-then-act 검사가 임계 구역 밖에 있지 않은가 — 경쟁 조건)
- [ ] 지연 초기화(Double-Checked Locking) 싱글톤이 스레드 안전한가? (@Volatile 또는 by lazy 누락 시 부분 초기화 객체 노출)

## 예외 처리 체크리스트 (패턴 규칙)

- [ ] safeCatch / tryCatchSystemManager 사용하는가?
- [ ] 빈 catch 블록 없는가?

## 테스트 영향 체크리스트

- [ ] 변경으로 깨질 가능성이 있는 기존 테스트를 확인했는가?
- [ ] 추가가 필요한 테스트 케이스(정상/예외/경계)를 명시했는가?

## 심각도 기준

- CRITICAL: 메모리 누수, GlobalScope 사용, 코루틴 누수
- CRITICAL: 스레드 안전하지 않은 지연 초기화 싱글톤 (@Volatile/lazy 누락 — 부분 초기화 객체 노출)
- HIGH: 로직 오류, null 미처리, SDK 분기 누락, 빈 catch 블록
- HIGH: 동기화되지 않은 공유 가변 상태 / check-then-act 경쟁 조건
- HIGH: 테스트 영향 분석 누락
- MEDIUM: equals 오버라이드 시 hashCode 누락
- MEDIUM: 부동소수(Double/Float) == 직접 비교
- MEDIUM: 추가 테스트 케이스(정상/예외/경계) 명시 누락
