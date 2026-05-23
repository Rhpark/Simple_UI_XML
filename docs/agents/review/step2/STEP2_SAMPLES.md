# STEP2 산출물 예시

→ 산출물 형식·하네스 기준은 [STEP2_LOGIC.md](STEP2_LOGIC.md) 참조

---

## 예시 A — 하네스 통과 / 런타임 finding 있음

```text
현재 단계: 리뷰 - STEP2 로직 & 안정성

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

[로직 & 안정성]
로직 확인  : 조건 분기 — emotionTag null 분기 정상 (CheckInViewModel.kt:148), 누락 없음
             무한 루프 — 없음 (확인 완료) — 반복 구조 없음
             null 처리 — saveRecord 반환값 미사용, 오류 전파 없음 (RecordRepository.kt:38)
             경계값 — causeTags 빈 리스트 허용 확인 완료
런타임 확인: 메모리 누수 — 없음 (확인 완료) — context 참조 없음
             비동기 누수 — viewModelScope 취소 시 insert 미완료 가능 (RecordRepository.kt:38) — HIGH
             생명주기 위반 — 없음 (확인 완료)
             예외 처리 — try-catch 없음, 예외 삼킴 (RecordRepository.kt:38) — HIGH
발견 사항  : 비동기 누수 — HIGH / RecordRepository.kt:38 / viewModelScope 취소 시 insert 중단 / 저장 미완료 무응답
             예외 미전파 — HIGH / RecordRepository.kt:38 / try-catch 없음 / 오류 상위 미전달
확인 범위  : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: CalendarUtils / 미확인 런타임 범위: 없음
이관 항목  : STEP3 소관 — 없음
             STEP4 소관 — 없음
```

---

## 예시 B — 하네스 실패 / 이관 항목 미처리

```text
현재 단계: 리뷰 - STEP2 로직 & 안정성

(분석 입력, 기능 검증 — STEP1 예시 A와 동일)

[로직 & 안정성]
로직 확인  : 조건 분기 — null 분기 정상 (CheckInViewModel.kt:148)
             무한 루프 — 없음 (확인 완료)
             null 처리 — 없음 (확인 완료)
             경계값 — 없음 (확인 완료)
런타임 확인: 메모리 누수 — 없음 (확인 완료)
             비동기 누수 — 없음 (확인 완료)
             생명주기 위반 — 없음 (확인 완료)
             예외 처리 — 없음 (확인 완료)
발견 사항  : 없음
확인 범위  : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: 없음 / 미확인 런타임 범위: 없음
이관 항목  : STEP3 소관 — 없음
             STEP4 소관 — 없음

실패 STEP   : STEP2
실패 항목   : [기능 검증] 이관 항목의 STEP2 소관 항목이 모두 확인됐다
실패 원인   : STEP1에서 이관된 "viewModelScope 취소 시 insert 미완료 가능성 (RecordRepository.kt:38)" 항목이 비동기 누수로 확인되지 않고 "없음 (확인 완료)"으로 처리됨
복귀 대상   : STEP2 재수행 — RecordRepository.kt:38 직접 열어 insert 호출이 viewModelScope 취소에 영향을 받는지 확인
전달 데이터 : [로직 & 안정성] 위 내용 그대로 (이관 항목 미확인)
```

---

## 예시 C — CRITICAL finding / 즉시 중단

```text
현재 단계: 리뷰 - STEP2 로직 & 안정성

(분석 입력, 기능 검증 — STEP1 예시 A와 동일)

[로직 & 안정성]
로직 확인  : 조건 분기 — 없음 (확인 완료)
             무한 루프 — 없음 (확인 완료)
             null 처리 — emotionEntity!! 강제 언박싱 (RecordRepository.kt:52) — CRITICAL
             경계값 — 없음 (확인 완료)
런타임 확인: 메모리 누수 — 없음 (확인 완료)
             비동기 누수 — 없음 (확인 완료)
             생명주기 위반 — 없음 (확인 완료)
             예외 처리 — 없음 (확인 완료)
발견 사항  : null 역참조 크래시 — CRITICAL / RecordRepository.kt:52 / emotionEntity!! 사용 — null 시 NullPointerException / DB 저장 중 앱 강제 종료
확인 범위  : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: CalendarUtils / 미확인 런타임 범위: 없음
이관 항목  : STEP3 소관 — 없음
             STEP4 소관 — 없음

---
⚠️ CRITICAL finding 발견 — 사용자 보고

[CRITICAL] null 역참조 크래시
위치  : RecordRepository.kt:52
근거  : `emotionEntity!!` — getEmotion() 반환값이 null인 경우 NullPointerException 발생
영향  : 저장 시도 시 앱 강제 종료 (데이터 손실)
조치  : RecordRepository.kt:52 에서 !! 연산자 제거 후 null 분기 처리 필요
       → feature 워크플로우로 진행 전 코드 수정 필요
```
