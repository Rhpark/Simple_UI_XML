<!-- 파일 목적: REVIEW_STEP4_CHECKLIST.md(Android 보강) 항목이 모두 수행됐는지 검증하는 통과 기준 -->

## 하네스 (통과 기준)

- [ ] D1번 통과 — 프로젝트 도구 환경이 식별돼 `[Android 도구] 환경`에 기록됐다.
     기록 값: [도구 목록 — 비어있으면 미통과]
     미통과 시: D1 재수행. `build.gradle(.kts)` plugins 블록과 Gradle Wrapper 존재 여부 재확인.
     심각도: 미식별은 MEDIUM(이후 도구 실행 판단 근거 부족).

- [ ] D2번 통과 — Android Lint 실행 결과 또는 미실행 사유가 `[Android 도구] Lint`에 기록됐다.
     기록 값: [결과 또는 미실행 사유 — 비어있으면 미통과]
     미통과 시: D2 재수행. 사용자 허용 여부 확인 + 사유 명시.
     심각도: 미기록은 HIGH(STEP4_QUALITY.md 하네스 2번 직접 대응).

- [ ] D3번 통과 — ktlint/detekt 실행 결과 또는 미적용/미실행 사유가 기록됐다.
     기록 값: [결과 또는 사유 — 비어있으면 미통과]
     미통과 시: D3 재수행. plugins 블록 재확인 + 사용자 허용 여부 재확인.
     심각도: 미기록은 MEDIUM.

- [ ] D4번 통과 — Gradle build/test 실행 결과 또는 미실행 사유가 기록됐다.
     기록 값: [결과 또는 사유 — 비어있으면 미통과]
     미통과 시: D4 재수행. 사용자 허용 여부 확인.
     심각도: 컴파일 실패 발견은 CRITICAL. 미실행은 MEDIUM.

- [ ] D5번 통과 — deprecated Android API 검사 결과가 기록됐다.
     기록 값: [발견 위치 또는 "없음 (확인 완료)" — 비어있으면 미통과]
     미통과 시: D5 재수행. `@Deprecated` 컴파일 경고 및 알려진 deprecated 호출 재확인.
     심각도: 위반은 LOW~MEDIUM(런타임 영향 시 STEP2 대표로 이관).

- [ ] D6번 통과 — Android hallucination 검사 결과가 기록됐다.
     기록 값: [선언부 검증 결과 또는 "없음 (확인 완료)" — 비어있으면 미통과]
     미통과 시: D6 재수행. 의심 API의 공식 문서 또는 라이브러리 소스 직접 확인.
     심각도: 확인 불가 시 CRITICAL(`SEVERITY_RULE.md` CRITICAL 조건 — hallucination).

- [ ] D7번 통과 — Android 컨벤션 위반 검사 결과가 기록됐다.
     기록 값: [결과 또는 "컨벤션 문서 확인 — 해당 규칙 없음" — 비어있으면 미통과]
     미통과 시: D7 재수행. `docs/rules/coding_rule/CODE_NAMING_RULE.md` 및 `docs/rules/coding_rule/CODE_LIFE_CYCLE.md` 열람 후 재검사.
     심각도: 위반은 MEDIUM. 컨벤션 문서 확인 후 해당 규칙 없음은 정상.

- [ ] D8번 통과 — 테스트 코드 Android 보강 검사 결과가 기록됐다.
     기록 값: [테스트 환경 / 결과 또는 "없음" — 비어있으면 미통과]
     미통과 시: D8 재수행. `src/test/` / `src/androidTest/` 디렉터리 재탐색.
     심각도: 미기록만 MEDIUM.

- [ ] D9번 통과 — 개발자 편의성 Android 보강 검사 결과가 기록됐다.
     기록 값: [로그/에러/문자열 결과 — 비어있으면 미통과]
     미통과 시: D9 재수행. 직접 파일에서 Log/Toast/문자열 사용 재확인.
     심각도: 미기록은 MEDIUM.

- [ ] D10번 통과 — `[Android 품질]` 블록이 `REVIEW_STEP4_OUTPUT.md` 형식대로 출력됐고 STEP4 본 산출물에 요약이 들어갔다.
     기록 값: [요약 — 비어있으면 미통과]
     미통과 시: D10 재수행.
     심각도: 형식 불일치는 MEDIUM.
