# CLAUDE.md — 진입점 (세션 시작 시 자동 로드)

## 절대 규칙 (어떤 상황에서도 예외 없음)
1. 코드/문서 수정은 사용자의 명시적 승인 없이 절대 금지
2. 모든 질문과 응답은 한글로 작성(UTF-8)
3. 세션 시작 읽기 순서의 모든 문서를 읽기 전까지 어떤 작업도 시작하지 않는다

## 작업 핵심 원칙 (Karpathy 4원칙 — 매 작업 상시 적용)
> LLM 코딩 실수 방지 행동 원칙. 사소한 작업은 판단으로 가감하되, 기본은 속도보다 신중함.

1. **코딩 전 사고**: 가정을 명시한다. 불확실하면 멈추고 질문한다. 해석이 여럿이면 나열한다.
   → 상세: PERSONA_RULE.md, PROCESS_RULE.md「판단 전 확인 규칙」
2. **단순성 우선**: 요청 범위 밖 기능·추상화·설정가능성·불필요한 예외처리를 만들지 않는다.
   "15년차 개발자가 과복잡하다고 할까?" → Yes면 단순화 후 재작성.
   → 상세: PROCESS_RULE.md「수정/작업 금지 패턴」
3. **외과적 수정**: 건드려야 할 것만 건드린다. 내 변경이 만든 orphan(import/변수/함수)만 제거하고,
   기존 dead code는 보존·언급한다. 변경된 모든 줄은 사용자 요청으로 추적 가능해야 한다.
   → 상세: PROCESS_RULE.md
4. **목표 기반 실행**: 성공 기준을 먼저 정의하고 검증까지 반복한다. "동작할 것 같다"로 완료 선언 금지.
   → 상세: PROCESS_RULE.md「작업 전 성공 기준 정의」(apiCheck/ktlintCheck/test/assemble)

> 4원칙 출처: andrej-karpathy-skills/karpathy-guidelines (MIT)

## 세션 시작 읽기 순서 (생략 불가)
1. AGENTS.md (이 파일 다음으로 즉시 읽기)
2. docs/rules/PROCESS_RULE.md
3. docs/rules/PERSONA_RULE.md
4. 요청에서 감지된 모듈의 AGENTS.md (모듈 불명확 시 사용자에게 확인 후 읽기)

## 첫 응답에 반드시 포함할 내용
읽은 문서 목록을 아래 형식으로 출력한다. (생략 불가)

✅ 세션 시작 문서 로드 완료
- [x] AGENTS.md
- [x] docs/rules/PROCESS_RULE.md
- [x] docs/rules/PERSONA_RULE.md
- [x] {모듈}/AGENTS.md (요청에서 감지된 모듈 — 모듈 불명확 시 사용자에게 확인 후 읽기)