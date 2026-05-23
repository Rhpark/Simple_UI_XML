# STEP3. 출력 예시

STEP3_IMPACT.md의 산출물 형식을 실제 분석 상황별로 채운 예시 모음이다.

---

출력 예시 A — code-flow:

```text
[입력 수신]
타입      : code-flow — 저장 버튼 클릭 후 DB 저장까지의 런타임 흐름 분석
핵심 파일 : (주) CheckInViewModel.kt:142 — 저장 요청 진입점
근거 파일 : CheckInViewModel.kt:142, RecordRepository.kt:38, RecordDao.kt:21

[STEP3 영향 범위]
영향 계층   : presentation — onSaveClick(CheckInViewModel.kt:142)
               data — saveRecord(RecordRepository.kt:38) Room insert 호출
기준 문서   : RECORD_LOGIC_SPEC.md:2 — 저장 가능 조건 기준
직접 파일   : CheckInViewModel.kt:142 — 저장 로직 진입점
               RecordRepository.kt:38 — Room insert 호출부
간접 파일   : RecordDao.kt:21 — Room 쿼리 변경 시 영향
               CheckInViewModelTest.kt — 저장 흐름 단위 테스트 영향
공통 파일   : 없음 (확인 완료)
사용자 영향 : 저장 실패 시 사용자는 기록 완료 여부를 알 수 없음
테스트      : CheckInViewModelTest.kt — 저장 실패 상태 검증 필요
리스크      : HIGH — saveRecord 실패 시 UI 상태 롤백 처리 미확인
               LOW — causeTags 빈 리스트 처리 경로 미분석

[다음 STEP 진입 조건 충족 확인]
- [x] 타입        — 채워짐 (STEP4 진입 조건)
- [x] 근거 파일   — 채워짐 (STEP4 진입 조건)
- [x] 직접 파일   — 채워짐 (STEP4 진입 조건)
- [x] 간접 파일   — 채워짐 (STEP4 진입 조건)
- [x] 사용자 영향 — 채워짐 (STEP4 진입 조건)
```

---

출력 예시 B — doc-consistency:

```text
[입력 수신]
타입      : doc-consistency — 태그 저장 기준과 UI 기준의 충돌 여부 분석
핵심 파일 : (주) RECORD_DATA_SPEC.md:12 — causeTags 데이터 기준
근거 파일 : RECORD_DATA_SPEC.md:12, CHECKIN_UI_SPEC.md:30

[STEP3 영향 범위]
영향 계층   : source of truth — COMMON_DATA_RULES.md:3 원인 태그 카탈로그
               dependent doc — CHECKIN_UI_SPEC.md:2 태그 칩 표시
               test doc — TEST_CASE.md 태그 선택 검증
기준 문서   : COMMON_DATA_RULES.md:3 — 태그 내부 값과 표시 순서 기준
직접 파일   : COMMON_DATA_RULES.md:3 — 태그 카탈로그 기준
               CHECKIN_UI_SPEC.md:2 — 화면 표시 기준
간접 파일   : CALENDAR_LOGIC_SPEC.md — 태그 빈도 집계 기준 영향
공통 파일   : DOCUMENT_AUTHORING_SOP.md — source of truth 표 영향
사용자 영향 : 태그 칩과 필터 이름이 다르면 사용자가 같은 태그로 인식하지 못함
테스트      : TEST_CASE.md — 태그 칩 표시 순서와 필터 기준 검증 필요
리스크      : MEDIUM — 태그 카탈로그 변경 시 UI/필터/요약 동기화 필요

[다음 STEP 진입 조건 충족 확인]
- [x] 타입        — 채워짐 (STEP4 진입 조건)
- [x] 근거 파일   — 채워짐 (STEP4 진입 조건)
- [x] 직접 파일   — 채워짐 (STEP4 진입 조건)
- [x] 간접 파일   — 채워짐 (STEP4 진입 조건)
- [x] 사용자 영향 — 채워짐 (STEP4 진입 조건)
```
