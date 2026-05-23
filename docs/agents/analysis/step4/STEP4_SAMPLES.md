# STEP4. 출력 예시

STEP4_REPORT.md의 산출물 형식을 실제 분석 상황별로 채운 예시 모음이다.

---

출력 예시 A — code-flow 케이스:

```text
[분석 요약]
진입 경로 : STEP3 통과 — code-flow 타입으로 영향 범위 산출 완료
타입      : code-flow — 저장 버튼 클릭 후 DB 저장까지의 런타임 흐름 분석
핵심 파일 : CheckInViewModel.kt:142 / onSaveClick()
근거 파일 : CheckInViewModel.kt:142, RecordRepository.kt:38, RecordDao.kt:21
직접 파일 : CheckInViewModel.kt:142 — 저장 로직 진입점
             RecordRepository.kt:38 — Room insert 호출부
간접 파일 : RecordDao.kt:21 — Room 쿼리 변경 시 영향

[사용자 요약]
한 줄 결론 : saveRecord 실패 시 예외가 삼켜지고 UI 상태가 갱신되지 않아 사용자가 저장 실패를 알 수 없다.
점수       : Agent 수행성 90/100, 사용자 이해성 87/100
감점 이유  : Agent 수행성 감점 — RecordDao 구현체를 확인하지 못해 insert 옵션 분석 미완 / 사용자 이해성 감점 — UI 동작 시나리오 설명이 누락됨
범위       : 읽은 범위: CheckInViewModel, RecordRepository, RecordDao / 제외 범위: CalendarScreen / 미확인 범위: Room insert conflict 전략
우선 행동  : 1순위 — saveRecord catch 블록 추가 및 에러 상태 emit 구현
             2순위 — viewModelScope 취소 시 insert 처리 검토

[STEP4 보고]
사실      : CheckInViewModel.kt:142 — onSaveClick에서 viewModelScope.launch로 saveRecord 호출
             RecordRepository.kt:38 — saveRecord는 suspend 함수이나 try-catch 없음
             RecordDao.kt:21 — Room insert 선언 확인, 예외 타입 미확인
추정      : Room insert 실패 시 SQLException이 발생한다고 가정 — 예외 타입 공식 문서 미확인
원인      : 1순위 — RecordRepository.saveRecord에 예외 처리 없어 ViewModel까지 전파 불가
             2순위 — viewModelScope 취소 시 insert 중단 가능성 미처리
심각도    : 예외 미전파 — HIGH / RecordRepository.kt:38 / try-catch 없음 / catch 후 에러 상태 emit 구현
             비동기 누수 — MEDIUM / RecordRepository.kt:38 / viewModelScope 취소 시 미완료 / NonCancellable 컨텍스트 검토
다음 단계 : feature (주) + review (보조) — HIGH finding인 예외 처리 누락이 우선, review로 전체 저장 흐름 점검 병행 가능
```

---

출력 예시 B — doc-consistency 케이스:

```text
[분석 요약]
진입 경로 : STEP3 통과 — doc-consistency 타입으로 영향 범위 산출 완료
타입      : doc-consistency — 태그 저장 기준과 UI 기준의 충돌 여부 분석
핵심 파일 : CHECKIN_UI_SPEC.md:22 / 태그 선택 규칙
근거 파일 : RECORD_DATA_SPEC.md:18 — causeTags 데이터 기준
             CHECKIN_UI_SPEC.md:22 — 체크인 화면 태그 기준
직접 파일 : CHECKIN_UI_SPEC.md:22 — 화면 구현 기준
간접 파일 : 없음

[사용자 요약]
한 줄 결론 : 태그 선택은 단일 causeTags 기준으로 정리돼 있으나, UI 문서가 기준 문서 연결을 더 명확히 해야 한다.
점수       : Agent 수행성 88/100, 사용자 이해성 86/100
감점 이유  : Agent 수행성 감점 — COMMON_DATA_RULES.md를 직접 읽지 않아 태그 카탈로그 충돌 여부 미확인 / 사용자 이해성 감점 — 없음
범위       : 읽은 범위: RECORD_DATA_SPEC, CHECKIN_UI_SPEC / 제외 범위: Phase 2 AI 문서 / 미확인 범위: COMMON_DATA_RULES
우선 행동  : 1순위 — CHECKIN_UI_SPEC에 COMMON_DATA_RULES 태그 목록 참조 추가

[STEP4 보고]
사실      : RECORD_DATA_SPEC.md:18은 causeTags 단일 필드와 최대 3개 제한을 기준으로 둔다.
             CHECKIN_UI_SPEC.md:22는 준비된 선택지 안에서만 태그를 선택한다고 쓴다.
추정      : 없음
원인      : 1순위 — UI 문서가 태그 목록 기준 문서를 직접 연결하지 않아 구현자가 별도 추적해야 한다.
심각도    : 태그 목록 기준 연결 부족 — MEDIUM / CHECKIN_UI_SPEC.md:22 / 기준 문서 참조 추가
다음 단계 : feature — 문서 기준 연결을 보강하면 구현 기준이 명확해진다.
```

---

출력 예시 C — ui-spec, STEP3 생략 직행 케이스:

```text
[분석 요약]
진입 경로 : STEP3 생략 직행 — ui-spec 단독 타입이며 STEP2 직행 보강 완료
타입      : ui-spec — 체크인 화면의 저장 차단 상태와 오류 문구 이해 가능성 분석
핵심 파일 : CHECKIN_UI_SPEC.md:22 / 저장 버튼 상태 기준
근거 파일 : CHECKIN_UI_SPEC.md:22 — 저장 버튼 상태 기준
             CHECKIN_UI_SPEC.md:31 — 오류 문구 표시 기준
직접 파일 : CHECKIN_UI_SPEC.md:22 — 저장 버튼 상태 기준
             CHECKIN_UI_SPEC.md:31 — 오류 문구 표시 기준
간접 파일 : 없음

[사용자 요약]
한 줄 결론 : 저장 차단 기준은 명확하지만 긴 오류 문구 줄바꿈 기준이 없어 모바일에서 이해성이 떨어질 수 있다.
점수       : Agent 수행성 87/100, 사용자 이해성 84/100
감점 이유  : Agent 수행성 감점 — 실제 화면 렌더링 검증 미실행 / 사용자 이해성 감점 — 긴 문구 표시 기준 미정의
범위       : 읽은 범위: CHECKIN_UI_SPEC / 제외 범위: RecordRepository / 미확인 범위: 실제 모바일 렌더링
우선 행동  : 1순위 — CHECKIN_UI_SPEC에 긴 오류 문구 줄바꿈 및 최대 길이 기준 추가

[STEP4 보고]
사실      : CHECKIN_UI_SPEC.md:22 — 필수 감정 값이 없으면 저장 버튼 비활성
             CHECKIN_UI_SPEC.md:31 — 저장 차단 사유는 버튼 주변에 표시
추정      : 긴 오류 문구가 모바일 화면에서 버튼과 겹칠 수 있음 — 실제 렌더링 미확인
원인      : 1순위 — 오류 문구 길이와 줄바꿈 기준이 UI 문서에 없다.
심각도    : 오류 문구 표시 기준 부족 — MEDIUM / CHECKIN_UI_SPEC.md:31 / 줄바꿈 및 최대 길이 기준 추가
다음 단계 : feature — UI 문서 기준을 보강하면 구현 기준이 명확해진다.
```
