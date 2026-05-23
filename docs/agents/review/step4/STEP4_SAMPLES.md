# STEP4 산출물 예시

→ 산출물 형식·하네스 기준은 [STEP4_QUALITY.md](STEP4_QUALITY.md) 참조

---

## 예시 A — 하네스 통과 / 품질 finding 있음

```text
현재 단계: 리뷰 - STEP4 코드 품질 & 컨벤션

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
레이어 확인     : 레이어 건너뜀 — 없음 (확인 완료)
                  DAO 직접 호출 — 없음 (확인 완료)
                  UI 직접 변경 — 없음 (확인 완료)
경계 확인       : 모듈 경계 — 없음 (확인 완료)
                  가변 상태 노출 — _uiState internal 노출 (CheckInViewModel.kt:21) — HIGH
설계 원칙 확인  : SRP — 저장+유효성+UI 상태 혼재 (CheckInViewModel.kt:142~165) — MEDIUM
                  DIP — 인터페이스 없이 직접 의존 (CheckInViewModel.kt:18) — LOW
발견 사항       : 가변 상태 노출 — HIGH / CheckInViewModel.kt:21 / _uiState internal 노출 / 외부 직접 변경 가능
                  SRP 위반 — MEDIUM / CheckInViewModel.kt:142 / 혼재 / 영향 범위 과대
확인 범위       : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: DI 모듈 / 미확인 아키텍처 범위: 없음
이관 항목       : STEP4 소관 — 없음

[코드 품질]
도구 실행    : Android Lint 실행 — 미사용 import 1건 (CheckInViewModel.kt:5) 외 경고 없음
품질 확인    : 미사용 코드 — import android.util.Log 미사용 (CheckInViewModel.kt:5) — LOW
               중복 로직 — 없음 (확인 완료)
               deprecated API — 없음 (확인 완료)
               hallucination — 없음 (확인 완료) — 모든 API 실제 존재 확인
               주석 코드 — // TODO: 에러 처리 (CheckInViewModel.kt:156) — LOW
컨벤션 확인  : 네이밍 — 없음 (확인 완료)
               로깅 패턴 — 없음 (확인 완료)
               버전 분기 패턴 — 해당 없음 — 버전 분기 코드 없음
컨벤션 기준  : 컨벤션 문서 없음 — 확인 불가
발견 사항    : 미사용 import — LOW / CheckInViewModel.kt:5 / import android.util.Log 미사용 / 사용자 영향 없음
               TODO 주석 — LOW / CheckInViewModel.kt:156 / 에러 처리 미완성 주석 / 사용자 영향 없음
확인 범위    : 확인 범위: [분석 입력] 직접 파일 / 제외 범위: generated code / 미확인 품질 범위: 없음
상위 이관    : 없음
```

---

## 예시 B — 하네스 실패 / hallucination 근거 미확인 → STEP4 재수행

```text
현재 단계: 리뷰 - STEP4 코드 품질 & 컨벤션

(분석 입력, 기능 검증, 로직 & 안정성, 아키텍처 — 예시 A와 동일)

[코드 품질]
도구 실행    : Android Lint 미실행 — 빌드 환경 없음
품질 확인    : 미사용 코드 — 없음 (확인 완료)
               중복 로직 — 없음 (확인 완료)
               deprecated API — 없음 (확인 완료)
               hallucination — RecordRepository.kt:41 에서 `db.recordDao().insertWithResult()` 호출 — insertWithResult() 선언부 미확인 — HIGH
               주석 코드 — 없음 (확인 완료)
컨벤션 확인  : 네이밍 — 없음 (확인 완료)
               로깅 패턴 — 없음 (확인 완료)
               버전 분기 패턴 — 해당 없음
컨벤션 기준  : 컨벤션 문서 없음 — 확인 불가
발견 사항    : hallucination 근거 미확인 — HIGH / RecordRepository.kt:41 / insertWithResult() 선언부 미확인 상태 / 선언부 없으면 런타임 호출 시 크래시
확인 범위    : 확인 범위: [분석 입력] 직접 파일 / 제외 범위: generated code / 미확인 품질 범위: RecordDao.kt (insertWithResult 선언 여부)
상위 이관    : 없음

실패 STEP   : STEP4
실패 항목   : hallucination 또는 deprecated 판정에 도구/공식/선언부 근거가 없다
실패 원인   : insertWithResult() 선언부를 RecordDao.kt에서 직접 확인하지 않고 HIGH로 기록함 — 선언부 근거 없이 판정 불가
복귀 대상   : STEP4 재수행 — RecordDao.kt 직접 열어 insertWithResult() 선언 여부 확인
             선언부 없음 확인 시 → CRITICAL로 상향 후 STEP2로 복귀, 런타임 영향 재확인
             선언부 있음 확인 시 → hallucination 항목 "없음 (확인 완료)"으로 정정
전달 데이터 : [코드 품질] 위 내용 그대로 (hallucination 선언부 미확인 품질 범위에 기록)
```
