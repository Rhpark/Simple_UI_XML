# STEP1 산출물 예시

→ 산출물 형식·하네스 기준은 [STEP1_FUNC.md](STEP1_FUNC.md) 참조

---

## 예시 A — 하네스 통과 / 기능 finding 있음

```text
현재 단계: 리뷰 - STEP1 기능 검증

[분석 입력]
직접 파일    : CheckInViewModel.kt:142 / RecordRepository.kt:38
영향 계층    : presentation(ViewModel) → data(Repository) → local(RoomDB)
기준 문서    : RECORD_LOGIC_SPEC.md:2

[기능 검증]
검증 대상  : CheckInViewModel.kt:142 / onSaveClick() — 감정 기록 저장 진입점
검증 맥락  : 분석 요약 — 저장 실패 시 UI 상태가 갱신되지 않을 가능성이 있다
             기준 문서 — RECORD_LOGIC_SPEC.md:2 저장 가능 조건 기준
             확인 범위 — 읽은 범위: CheckInViewModel/RecordRepository / 제외 범위: CalendarUtils / 미확인 범위: 없음
엣지 케이스: null/empty — emotionTag null 시 조건 분기로 저장 차단 (CheckInViewModel.kt:148)
             0개 데이터 — causeTags 빈 리스트 허용, DB insert 정상 완료
             단일 항목 — intensity=1 최솟값 저장 정상 동작
             최대 개수 — 해당 없음 — causeTags 상한 제한 코드 없음
             실패 응답 — DB 오류 시 catch 없음, UI 상태 미갱신 — HIGH
발견 사항  : 저장 실패 무응답 — HIGH / CheckInViewModel.kt:155 / viewModelScope.launch 내 catch 없이 emit 없음 / 사용자가 저장 실패를 인지 불가
이관 항목  : STEP2 소관 — viewModelScope 취소 시 insert 미완료 가능성 (RecordRepository.kt:38)
             STEP3 소관 — 없음
             STEP4 소관 — 없음
```

---

## 예시 B — 하네스 실패 / finding에 파일명:라인번호 누락

```text
현재 단계: 리뷰 - STEP1 기능 검증

[분석 입력]
직접 파일    : CheckInViewModel.kt:142 / RecordRepository.kt:38
영향 계층    : presentation(ViewModel) → data(Repository) → local(RoomDB)
기준 문서    : RECORD_LOGIC_SPEC.md:2

[기능 검증]
검증 대상  : 감정 기록 저장 기능
검증 맥락  : 분석 요약 — 저장 실패 시 UI 상태가 갱신되지 않을 가능성이 있다
             기준 문서 — RECORD_LOGIC_SPEC.md:2
             확인 범위 — 읽은 범위: ViewModel/Repository / 제외 범위: Calendar / 미확인 범위: 없음
엣지 케이스: null/empty — 저장 차단 확인
             0개 데이터 — 정상 완료 확인
             단일 항목 — 정상 동작
             최대 개수 — 해당 없음
             실패 응답 — 오류 시 UI 무응답 — HIGH
발견 사항  : 저장 실패 무응답 — HIGH / (코드 미확인) / catch 없음 추정 / 사용자 인지 불가
이관 항목  : STEP2 소관 — 비동기 누수 가능성
             STEP3 소관 — 없음
             STEP4 소관 — 없음

실패 STEP   : STEP1
실패 항목   : 발견된 finding마다 파일명:라인번호가 기록됐다
실패 원인   : "저장 실패 무응답" finding 위치가 "(코드 미확인)" — 파일명:라인번호 없음, 코드 근거 추정으로 기록
복귀 대상   : STEP1 재수행 — CheckInViewModel.kt:155 직접 열어 viewModelScope.launch 블록 내 catch 유무 확인
전달 데이터 : [기능 검증] 위 내용 그대로 (발견 사항 파일명:라인번호 미완성)
```

---

## 예시 C — 하네스 통과 / finding 없음

```text
현재 단계: 리뷰 - STEP1 기능 검증

[분석 입력]
직접 파일    : ProfileViewModel.kt:88 / UserRepository.kt:22
영향 계층    : presentation(ViewModel) → data(Repository) → remote(API)
기준 문서    : 없음

[기능 검증]
검증 대상  : ProfileViewModel.kt:88 / onLoadProfile() — 프로필 조회 진입점
검증 맥락  : 분석 요약 — 프로필 조회 로직이 정상 흐름에서 의도대로 동작한다
             기준 문서 — 없음
             확인 범위 — 읽은 범위: ProfileViewModel/UserRepository / 제외 범위: 없음 / 미확인 범위: 없음
엣지 케이스: null/empty — userId null 시 early return으로 조회 차단 (ProfileViewModel.kt:91)
             0개 데이터 — 해당 없음 — 단일 사용자 조회
             단일 항목 — 해당 없음 — 리스트 미사용
             최대 개수 — 해당 없음 — 컬렉션 미사용
             실패 응답 — API 오류 시 error 상태 emit 확인 (ProfileViewModel.kt:98)
발견 사항  : 없음
이관 항목  : STEP2 소관 — 없음
             STEP3 소관 — 없음
             STEP4 소관 — 없음
```
