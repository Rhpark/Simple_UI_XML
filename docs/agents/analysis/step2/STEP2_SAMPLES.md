# STEP2. 출력 예시

STEP2_READ.md의 산출물 형식을 실제 분석 상황별로 채운 예시 모음이다.

---

출력 예시 A — code-flow:

```text
[입력 수신]
타입                   : code-flow — 저장 버튼 클릭 후 DB 저장까지 런타임 흐름 분석
복원 매핑              : 없음
대상 기능/모듈         : 체크인 저장 기능
핵심 파일              : (주) CheckInViewModel.kt:142 — 저장 요청 진입점
제외 범위/시나리오     : CalendarScreen.kt — 저장 로직과 호출 관계 없음
STEP3 적용 여부        : 필수 — code-flow 타입
입력 fixture/재현 조건 : 없음(해당 없음)
분석 환경              : 없음
이슈                   : 없음

[STEP2 근거 읽기]
읽기 방식   : code-flow
근거 파일   : CheckInViewModel.kt:142 — 저장 버튼 클릭 진입점 / 코드
              RecordRepository.kt:38 — saveRecord suspend 함수 / 코드
              RecordDao.kt:21 — Room insert 선언 / 코드
호출 흐름   : onClick → onSaveClick(CheckInViewModel.kt:142)
              → saveRecord(RecordRepository.kt:38)
              → Room.insert(RecordDao.kt:21)
              → _uiState.emit(CheckInViewModel.kt:158) → UI 렌더
문서 주장   : CheckInViewModel.kt:142 — 저장 요청은 onSaveClick에서 시작
              RecordRepository.kt:38 — try-catch 없이 Room insert 직접 호출
분기 분석   : 정상 — emotionTag != null && intensity > 0 → saveRecord 호출 (CheckInViewModel.kt:148)
              예외 — emotionTag == null → 저장 차단, 오류 상태 emit (CheckInViewModel.kt:145)
              경계 — causeTags 빈 리스트 → 저장 허용 (RecordRepository.kt:40)
충돌 항목   : 없음 (확인 완료)
미정 항목   : 없음
리스크      : HIGH — RecordRepository.kt:38 try-catch 없음. 실패 시 예외 ViewModel까지 미전파
              MEDIUM — RecordRepository.kt:38 suspend 함수. viewModelScope 취소 시 insert 중단 가능
인계 항목   : 없음 (확인 완료)

[다음 STEP 진입 조건 충족 확인]
- [x] 타입           — 채워짐 (STEP3·STEP4 진입 조건)
- [x] 근거 파일      — 채워짐 (STEP3·STEP4 진입 조건)
- [x] 분기 분석      — 채워짐 (STEP3 진입 조건)
- [x] 타입 선정 이유  — 포함됨 (STEP4 진입 조건)
```

---

출력 예시 B — doc-consistency:

```text
[입력 수신]
타입                   : doc-consistency — 태그 저장 모델과 UI 문서 기준 일치 여부 분석
복원 매핑              : 없음
대상 기능/모듈         : 체크인 원인 태그 저장 및 UI 선택
핵심 파일              : (주) RECORD_DATA_SPEC.md:12 — causeTags 데이터 기준
                         (주) CHECKIN_UI_SPEC.md:30 — 원인 태그 UI 선택 기준
제외 범위/시나리오     : AI_REPORT_DATA_SPEC.md — 1단계 태그 저장 모델과 직접 관련 없음
STEP3 적용 여부        : 필수 — doc-consistency 타입
입력 fixture/재현 조건 : 없음(해당 없음)
분석 환경              : 없음
이슈                   : 없음

[STEP2 근거 읽기]
읽기 방식   : doc-consistency
근거 파일   : RECORD_DATA_SPEC.md:12 — causeTags 데이터 기준 / 문서
              CHECKIN_UI_SPEC.md:30 — 태그 칩 UI 기준 / 문서
호출 흐름   : 해당 없음
문서 주장   : RECORD_DATA_SPEC.md:12 — causeTags 단일 필드, 최대 3개 제한
              CHECKIN_UI_SPEC.md:30 — 준비된 선택지 안에서만 태그 선택 허용
분기 분석   : 정상 — 두 문서 모두 선택형 태그만 허용 (기준 일치)
              예외 — customCauseTags가 남아 있으면 데이터 모델 충돌 가능
              경계 — 태그 0개 저장 허용 여부: CHECKIN_UI_SPEC.md에 미명시
충돌 항목   : 없음 (확인 완료)
미정 항목   : CHECKIN_UI_SPEC.md:30 — 태그 0개 저장 허용 여부 미정의
리스크      : MEDIUM — 태그 카탈로그 출처 문서 미확인. source of truth 미확인
인계 항목   : 없음 (확인 완료)

[다음 STEP 진입 조건 충족 확인]
- [x] 타입           — 채워짐 (STEP3·STEP4 진입 조건)
- [x] 근거 파일      — 채워짐 (STEP3·STEP4 진입 조건)
- [x] 분기 분석      — 채워짐 (STEP3 진입 조건)
- [x] 타입 선정 이유  — 포함됨 (STEP4 진입 조건)
```

---

출력 예시 C — ui-spec, STEP3 생략:

```text
[입력 수신]
타입                   : ui-spec — 체크인 화면의 저장 차단 상태와 오류 문구 이해 가능성 분석
복원 매핑              : 없음
대상 기능/모듈         : 체크인 저장 화면
핵심 파일              : (주) CHECKIN_UI_SPEC.md:22 — 저장 버튼 활성/비활성 기준
제외 범위/시나리오     : RecordRepository.kt — 런타임 저장 로직은 이번 UI 문구 분석 범위 밖
STEP3 적용 여부        : 생략 — 주/보조 타입이 ui-spec만 해당
입력 fixture/재현 조건 : 없음(해당 없음)
분석 환경              : 없음
이슈                   : 없음

[STEP2 근거 읽기]
읽기 방식   : ui-spec
근거 파일   : CHECKIN_UI_SPEC.md:22 — 저장 버튼 상태 기준 / 문서
              CHECKIN_UI_SPEC.md:31 — 오류 문구 표시 기준 / 문서
호출 흐름   : 해당 없음
문서 주장   : CHECKIN_UI_SPEC.md:22 — 필수 감정 값이 없으면 저장 버튼 비활성
              CHECKIN_UI_SPEC.md:31 — 저장 차단 사유는 버튼 주변에 표시
분기 분석   : 정상 — 필수 감정 값 선택 시 저장 버튼 활성
              예외 — 필수 감정 값 없음 → 저장 버튼 비활성
              경계 — 오류 문구가 길 때 줄바꿈 기준 미명시
충돌 항목   : 없음 (확인 완료)
미정 항목   : CHECKIN_UI_SPEC.md:31 — 긴 오류 문구 줄바꿈 기준 미정의
리스크      : MEDIUM — 긴 오류 문구가 모바일 화면에서 버튼과 겹칠 가능성
인계 항목   : 없음 (확인 완료)

[STEP4 직행 보강]
직접 파일   : CHECKIN_UI_SPEC.md:22 — 저장 버튼 상태 기준
              CHECKIN_UI_SPEC.md:31 — 오류 문구 표시 기준
간접 파일   : 없음 (확인 완료)
사용자 영향 : 저장 차단 사유가 길면 사용자가 저장 불가 이유를 읽기 어려울 수 있음

[다음 STEP 진입 조건 충족 확인]
- [x] 타입             — 채워짐 (STEP4 진입 조건)
- [x] 근거 파일        — 채워짐 (STEP4 진입 조건)
- [x] 분기 분석        — STEP3 생략으로 STEP3 진입 조건에서 제외
- [x] 타입 선정 이유   — 포함됨 (STEP4 진입 조건)
- [x] STEP4 직행 보강  — 채워짐 (STEP4 진입 조건)
```
