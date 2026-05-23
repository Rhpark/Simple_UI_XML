# STEP3 산출물 예시

→ 산출물 형식·하네스 기준은 [STEP3_ARCH.md](STEP3_ARCH.md) 참조

---

## 예시 A — 하네스 통과 / 아키텍처 finding 있음

```text
현재 단계: 리뷰 - STEP3 아키텍처

[분석 입력]
직접 파일    : CheckInViewModel.kt:142 / RecordRepository.kt:38
영향 계층    : presentation(ViewModel) → data(Repository) → local(RoomDB)
기준 문서    : RECORD_LOGIC_SPEC.md:2

[기능 검증]
검증 대상  : CheckInViewModel.kt:142 / onSaveClick() — 감정 기록 저장 진입점
검증 맥락  : 분석 요약 — 저장 실패 시 UI 상태가 갱신되지 않을 가능성이 있다
             기준 문서 — RECORD_LOGIC_SPEC.md:2 저장 가능 조건 기준
             확인 범위 — 읽은 범위: CheckInViewModel/RecordRepository / 제외 범위: CalendarUtils / 미확인 범위: 없음
엣지 케이스: null/empty — emotionTag null 시 저장 차단 (CheckInViewModel.kt:148)
             0개 데이터 — causeTags 빈 리스트 허용
             단일 항목 — intensity=1 정상 동작
             최대 개수 — 해당 없음
             실패 응답 — DB 오류 시 무응답 — HIGH
발견 사항  : 저장 실패 무응답 — HIGH / CheckInViewModel.kt:155 / catch 없이 emit 없음 / 저장 실패 인지 불가
이관 항목  : STEP2 소관 — viewModelScope 취소 시 insert 미완료 (RecordRepository.kt:38)
             STEP3 소관 — 없음
             STEP4 소관 — 없음

[로직 & 안정성]
로직 확인  : 조건 분기 — emotionTag null 분기 정상 (CheckInViewModel.kt:148)
             무한 루프 — 없음 (확인 완료)
             null 처리 — saveRecord 반환값 미사용 (RecordRepository.kt:38)
             경계값 — causeTags 빈 리스트 허용 확인 완료
런타임 확인: 메모리 누수 — 없음 (확인 완료)
             비동기 누수 — viewModelScope 취소 시 insert 미완료 가능 (RecordRepository.kt:38) — HIGH
             생명주기 위반 — 없음 (확인 완료)
             예외 처리 — try-catch 없음 (RecordRepository.kt:38) — HIGH
발견 사항  : 비동기 누수 — HIGH / RecordRepository.kt:38 / viewModelScope 취소 시 insert 중단 / 저장 미완료 무응답
             예외 미전파 — HIGH / RecordRepository.kt:38 / try-catch 없음 / 오류 상위 미전달
확인 범위  : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: CalendarUtils / 미확인 런타임 범위: 없음
이관 항목  : STEP3 소관 — 없음
             STEP4 소관 — 없음

[아키텍처]
기준 문서       : APP_ARCHITECTURE.md — 레이어 책임 기준
레이어 확인     : 레이어 건너뜀 — 없음 (확인 완료) — ViewModel → Repository 경유 확인
                  DAO 직접 호출 — 없음 (확인 완료) — ViewModel에서 DAO 미사용
                  UI 직접 변경 — 없음 (확인 완료) — Repository는 StateFlow 미보유
경계 확인       : 모듈 경계 — 없음 (확인 완료)
                  가변 상태 노출 — _uiState MutableStateFlow가 internal로 노출됨 (CheckInViewModel.kt:21) — HIGH
설계 원칙 확인  : SRP — CheckInViewModel이 저장·유효성 검사·UI 상태를 모두 처리 (CheckInViewModel.kt:142~165) — MEDIUM
                  DIP — RecordRepository가 인터페이스 없이 직접 의존됨 (CheckInViewModel.kt:18) — LOW
발견 사항       : 가변 상태 노출 — HIGH / CheckInViewModel.kt:21 / _uiState internal 노출 / 외부 모듈이 상태 직접 변경 가능
                  SRP 위반 — MEDIUM / CheckInViewModel.kt:142 / 저장+유효성+UI 상태 혼재 / 변경 시 영향 범위 과대
확인 범위       : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: DI 모듈 / 미확인 아키텍처 범위: 없음
이관 항목       : STEP4 소관 — 없음
```

---

## 예시 B — 하네스 실패 / 아키텍처 기준 문서 미기록

```text
현재 단계: 리뷰 - STEP3 아키텍처

(분석 입력, 기능 검증, 로직 & 안정성 — 예시 A와 동일)

[아키텍처]
기준 문서       : (미기록)
레이어 확인     : 레이어 건너뜀 — 없음 (확인 완료)
                  DAO 직접 호출 — 없음 (확인 완료)
                  UI 직접 변경 — 없음 (확인 완료)
경계 확인       : 모듈 경계 — 없음 (확인 완료)
                  가변 상태 노출 — _uiState internal 노출 (CheckInViewModel.kt:21) — HIGH
설계 원칙 확인  : SRP — 저장+유효성+UI 상태 혼재 (CheckInViewModel.kt:142) — MEDIUM
                  DIP — 없음 (확인 완료)
발견 사항       : 가변 상태 노출 — HIGH / CheckInViewModel.kt:21 / _uiState internal 노출 / 외부 직접 변경 가능
                  SRP 위반 — MEDIUM / CheckInViewModel.kt:142 / 혼재 / 영향 범위 과대
확인 범위       : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: DI 모듈 / 미확인 범위: 없음
이관 항목       : STEP4 소관 — 없음

실패 STEP   : STEP3
실패 항목   : [아키텍처] 기준 문서에 기준 문서 또는 "없음/미확인"이 기록됐다
실패 원인   : [분석 입력] 기준 문서에 RECORD_LOGIC_SPEC.md:2가 있으나 아키텍처 기준 문서 확인 여부가 [아키텍처] 기준 문서에 미기록됨
복귀 대상   : STEP3 재수행 — [분석 입력] 기준 문서 또는 프로젝트 내 아키텍처 문서를 확인해 [아키텍처] 기준 문서에 기록
전달 데이터 : [아키텍처] 위 내용 그대로 (기준 문서 항목 미완성)
```
