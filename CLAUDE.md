# CLAUDE.md — 진입점 (세션 시작 시 자동 로드)

## 절대 규칙 (어떤 상황에서도 예외 없음)
1. 코드/문서 수정은 사용자의 명시적 승인 없이 절대 금지
2. 모든 질문과 응답은 한글로 작성(UTF-8)
3. 세션 시작 읽기 순서의 모든 문서를 읽기 전까지 어떤 작업도 시작하지 않는다

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