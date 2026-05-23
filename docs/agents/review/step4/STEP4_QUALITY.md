# STEP4. 코드 품질 & 컨벤션

목적: 미사용 코드·중복 로직·deprecated API·hallucination·컨벤션 위반을 점검하고, 같은 이슈가 런타임 오류나 사용자 기대 결과에 영향을 주면 STEP1/STEP2를 대표 STEP으로 두고 이 단계는 보조 근거로만 기록한다.

## 진입 조건

- STEP3 하네스를 통과했다
- STEP3 산출물 전체(`[분석 입력]` + `[기능 검증]` + `[로직 & 안정성]` + `[아키텍처]`)가 전달됐다
  - [ ] `[기능 검증] 발견 사항` — STEP1 기능 finding 목록이 기록돼 있다 (없으면 "없음" 확인)
  - [ ] `[로직 & 안정성] 발견 사항` — STEP2 로직 finding 목록이 기록돼 있다 (없으면 "없음" 확인)
  - [ ] `[아키텍처] 발견 사항` — STEP3 아키텍처 finding 목록이 기록돼 있다 (없으면 "없음" 확인)
  - [ ] `[아키텍처] 확인 범위` — STEP3 확인 범위와 미확인 아키텍처 범위가 기록돼 있다
  - [ ] `[아키텍처] 이관 항목` — STEP4 소관으로 넘긴 항목이 기록돼 있다 (없으면 "없음" 확인)
  - [ ] `[분석 입력] 직접 파일` — 직접 관련 파일 목록이 파일명:라인번호로 기록돼 있다

## 행동 원칙

- 코드를 직접 열어 확인한 결과만 finding으로 기록한다.
- 파일명:라인번호 없는 finding은 기록하지 않는다.
- hallucination 판정은 반드시 선언부 또는 공식 문서에서 존재 여부를 확인한 뒤 기록한다.
- 컨벤션 위반은 프로젝트 고유 기준 문서가 있을 때만 단정한다. 없으면 "컨벤션 문서 없음 — 확인 불가"로 명시한다.
- 같은 finding이 런타임 오류·기능 영향을 동시에 일으키면 STEP1/STEP2를 대표 STEP으로 두고 STEP4는 보조 근거로만 기록한다.
- 모호하거나 불확실한 항목은 추측으로 채우지 않고 사용자에게 질문한다.

## 공통 규칙

→ [STEP_EXECUTION_RULE.md](../../common/STEP_EXECUTION_RULE.md) 참조
→ [STEP_FORCE_STOP.md](../../common/STEP_FORCE_STOP.md) 참조
→ [STEP_HARNESS.md](../../common/STEP_HARNESS.md) 참조
→ [GLOSSARY.md](../common/GLOSSARY.md) 참조

## 실행 체크리스트

- [ ] 1. 보고서 첫 줄에 `현재 단계: 리뷰 - STEP4 코드 품질 & 컨벤션`을 명시한다.
- [ ] 2. `[아키텍처] 이관 항목`의 STEP4 소관 항목을 먼저 확인한다.
  → 이관 항목 없음: 직접 파일 전수 검토로 진행한다.
  → 이관 항목 있음: 해당 항목을 우선 검증한 뒤 직접 파일 전수 검토로 확장한다.
- [ ] 3. `[아키텍처] 확인 범위`를 확인해 품질/컨벤션 검토에서 제외할 범위와 미확인 범위를 기록한다.
- [ ] 4. 사용 가능한 도구 검증(정적 분석, 린트, 빌드, 테스트 도구)이 있으면 실행 결과 또는 미실행 이유를 `[코드 품질] 도구 실행`에 기록한다.
  → 사용 가능 도구 없음: "사용 가능 도구 없음"으로 기록한다.
  → 사용자 권한 필요: 사용자 허용 시에만 실행하고 결과를 기록한다. 미허용이면 미실행 이유로 기록한다.
  → [REVIEW_STEP4_CHECKLIST.md](../../platforms/android/review/step4/REVIEW_STEP4_CHECKLIST.md)  <!-- ★ -->
- [ ] 5. 아래 코드 품질 항목을 `[분석 입력] 직접 파일`에서 직접 확인한다.
  - 미사용 import / 변수 / 함수 (IDE 경고 대상)
  - 중복 로직 (같은 코드 블록이 2곳 이상에 존재)
  - deprecated API를 신규 코드에서 사용
  - 존재하지 않는 API·함수·파라미터를 사용 (hallucination)
  - 주석 처리된 코드 (TODO 제외)
- [ ] 6. 아래 컨벤션 항목을 확인한다.
  - 네이밍 규칙 위반 (camelCase·PascalCase·snake_case 혼용 등)
  - 프로젝트 고유 로깅 패턴 위반 (컨벤션 문서가 있으면 먼저 읽고 `[코드 품질] 컨벤션 기준`에 기록한다 — 없으면 "컨벤션 문서 없음 — 확인 불가"로 기록)
  - 프로젝트 고유 버전 분기 패턴 위반 (컨벤션 문서가 있으면 먼저 읽고 `[코드 품질] 컨벤션 기준`에 기록한다 — 없으면 "컨벤션 문서 없음 — 확인 불가"로 기록)
- [ ] 7. 발견한 문제가 런타임 오류나 사용자 기대 결과에 영향을 주는지 판단한다.
  → 영향 있음: STEP1/STEP2 대표로 표시하고 STEP4는 보조 근거로만 기록한다. `상위 이관`에 기록한다.
  → 영향 없음: STEP4 소관 finding으로 기록한다.
- [ ] 8. 테스트 코드를 검토한다.
  - 검토 대상 파일과 연관된 테스트 파일을 탐색한다.
  → 테스트 파일 없음: CRITICAL finding으로 기록한다 (`[코드 품질] 테스트 코드`에 "없음" 기록).
  → 있음: 아래 항목을 하나씩 확인하고 finding 등급을 판정한다.
    - 주요 경로(happy path) 테스트 존재 여부 — 없으면 HIGH finding
    - 엣지 케이스 테스트 존재 여부 — 없으면 MEDIUM finding
    - 의미 있는 단언(assertion) 여부 — 형식적 테스트만 있으면 MEDIUM finding
    - 과도한 모킹으로 실제 동작 미검증 여부 — 해당하면 LOW finding
- [ ] 9. 개발자 편의성을 검토한다.
  - 검토 대상 파일을 직접 열어 아래 항목을 확인한다.
  → API 명확성: 함수명·파라미터가 의도를 전달하지 못하면 HIGH finding, 일부 불명확이면 MEDIUM finding.
  → 에러 메시지: 개발자가 원인을 파악할 수 없으면 HIGH finding, 일부 불명확이면 MEDIUM finding.
  → 로그 적절성: 핵심 경로에 로그가 전혀 없거나 과도하면 MEDIUM finding.
  → 문서화: WHY가 필요한 곳에 설명이 없으면 LOW finding.

## 하네스 (통과 기준)

- [ ] 1번 통과 — `[아키텍처] 이관 항목`의 STEP4 소관 항목이 모두 확인됐다.
     **HIGH** / 미통과 시: 1번 재수행. STEP3 이관 항목을 `[코드 품질]` 확인 항목과 대조해 미처리 항목 재확인.
- [ ] 2번 통과 — `[코드 품질] 도구 실행`에 실행한 도구와 결과 또는 미실행 이유가 기록됐다.
     **HIGH** / 미통과 시: 2번 재수행. 사용 가능한 도구를 확인하거나 미실행 이유를 명시해 `[코드 품질] 도구 실행`에 기록.
     → [REVIEW_STEP4_HARNESS.md](../../platforms/android/review/step4/REVIEW_STEP4_HARNESS.md)  <!-- ★ -->
- [ ] 3번 통과 — 코드 품질 5종 (미사용·중복·deprecated·hallucination·주석 코드)이 모두 확인됐다.
     **HIGH** / 미통과 시: 3번 재수행. 미확인 항목을 `[분석 입력] 직접 파일`에서 직접 확인. hallucination 선언부 미확인 시 해당 파일에서 직접 선언 여부 확인.
- [ ] 4번 통과 — 컨벤션 3종 (네이밍·로깅·버전 분기)이 모두 확인됐다.
     **MEDIUM** / 미통과 시: 4번 재수행. 컨벤션 기준 문서가 있으면 먼저 읽고 확인, 없으면 "컨벤션 문서 없음 — 확인 불가"로 기록.
- [ ] 5번 통과 — 발견된 finding마다 파일명:라인번호, 근거, 심각도, 사용자 영향이 기록됐다.
     확인된 finding 위치: [파일명:라인번호를 여기 기재 — finding이 "없음"이면 "없음", 비어있으면 미통과]
     hallucination 선언부 검증: [확인한 선언부 파일명:라인번호 또는 공식 문서 URL 기재 — 해당 finding 없으면 "해당 없음"]
     **CRITICAL** / 미통과 시: 5번 재수행. 파일명:라인번호 없는 finding은 코드에서 직접 확인 후 위치 기록. hallucination 미확인 시 선언부 직접 확인.
- [ ] 6번 통과 — 컨벤션 기준 문서가 있으면 `[코드 품질] 컨벤션 기준`에 기록됐고, 없으면 기준 부재가 명시됐다.
     **MEDIUM** / 미통과 시: 6번 재수행. `[코드 품질] 컨벤션 기준`에 기준 문서 참조 또는 "컨벤션 문서 없음 — 확인 불가" 기록.
- [ ] 7번 통과 — 런타임/기능에 영향 주는 항목은 STEP1/STEP2 대표로 표시하고 STEP4 finding에서 제외됐다.
     **HIGH** / 미통과 시: 7번 재수행. STEP4 finding 목록에서 런타임·기능 영향 항목을 STEP1/STEP2 이관 항목으로 이동.
- [ ] 8번 통과 — 테스트 코드 확인 결과가 `[코드 품질] 테스트 코드`에 기록됐다.
     테스트 파일 존재 여부: [있음/없음과 경로를 여기 기재 — 비어있으면 미통과]
     **HIGH** / 미통과 시: 8번 재수행. 검토 대상 파일 경로 기반으로 테스트 파일 재탐색 후 결과 기록.
- [ ] 9번 통과 — 개발자 편의성 확인 결과가 `[코드 품질] 개발자 편의성`에 기록됐다.
     **MEDIUM** / 미통과 시: 9번 재수행. API 명확성·에러 메시지·로그·문서화를 직접 파일에서 재확인 후 기록.

미통과 항목이 있으면 → 실패 규격에 따라 기록 후 복귀 조건 확인.

우선순위 규칙: 체크리스트는 최소 보장선이지 상한선이 아니다. 결론을 바꿀 수 있는 사실을 발견하면 체크리스트 외 항목이라도 finding에 기록한다.

### 심각도 기준

→ [SEVERITY_RULE.md](../../common/SEVERITY_RULE.md) 참조
→ [REVIEW_STEP4_SEVERITY.md](../../platforms/android/review/step4/REVIEW_STEP4_SEVERITY.md)  <!-- ★ -->

## 산출물

STEP 완료 시 아래 형식으로 값을 채워 출력한다.
STEP1·STEP2·STEP3 산출물(`[분석 입력]`, `[기능 검증]`, `[로직 & 안정성]`, `[아키텍처]`)을 그대로 포함하고 이번 STEP 결과를 추가한다.

```text
[분석 입력]
직접 파일    : (STEP1에서 그대로)
영향 계층    : (STEP1에서 그대로)
기준 문서    : (STEP1에서 그대로)

[기능 검증]
검증 대상  : (STEP1에서 그대로)
검증 맥락  : (STEP1에서 그대로)
엣지 케이스: (STEP1에서 그대로)
발견 사항  : (STEP1에서 그대로)
이관 항목  : (STEP1에서 그대로)

[로직 & 안정성]
로직 확인  : (STEP2에서 그대로)
런타임 확인: (STEP2에서 그대로)
발견 사항  : (STEP2에서 그대로)
확인 범위  : (STEP2에서 그대로)
이관 항목  : (STEP2에서 그대로)

[아키텍처]
기준 문서       : (STEP3에서 그대로)
레이어 확인     : (STEP3에서 그대로)
경계 확인       : (STEP3에서 그대로)
설계 원칙 확인  : (STEP3에서 그대로)
발견 사항       : (STEP3에서 그대로)
확인 범위       : (STEP3에서 그대로)
이관 항목       : (STEP3에서 그대로)

[코드 품질]
도구 실행    : 실행한 도구와 결과 또는 미실행 이유
               → [REVIEW_STEP4_OUTPUT.md](../../platforms/android/review/step4/REVIEW_STEP4_OUTPUT.md)  <!-- ★ -->
품질 확인    : 미사용 코드 — 확인 결과 (파일명:라인번호)
               중복 로직 — 확인 결과
               deprecated API — 확인 결과
               hallucination — 확인 결과
               주석 코드 — 확인 결과
컨벤션 확인  : 네이밍 — 확인 결과 (파일명:라인번호)
               로깅 패턴 — 확인 결과
               버전 분기 패턴 — 확인 결과
컨벤션 기준  : 기준 문서 또는 "컨벤션 문서 없음 — 확인 불가"
발견 사항    : finding명 — 등급 / 파일명:라인번호 / 근거 / 사용자 영향
               (finding이 여러 개면 줄 단위로 나열, 없으면 "없음")
확인 범위    : 확인 범위 / 제외 범위 / 미확인 품질 범위
테스트 코드  : 테스트 파일 — 있음 (경로) 또는 "없음"
               주요 경로 — 확인 결과
               엣지 케이스 — 확인 결과
               단언 품질 — 확인 결과
               모킹 수준 — 확인 결과
개발자 편의성: API 명확성 — 확인 결과 (파일명:라인번호)
               에러 메시지 — 확인 결과
               로그 적절성 — 확인 결과
               문서화 — 확인 결과
상위 이관    : STEP1/STEP2 대표로 이관한 항목 — 이관 대상 STEP / 파일명:라인번호 / 이유
               (없으면 "없음")
```

→ 추가 예시 및 실패 규격 예시는 [STEP4_SAMPLES.md](STEP4_SAMPLES.md) 참조

예시:

```text
[분석 입력]
직접 파일    : CheckInViewModel.kt:142 / RecordRepository.kt:38
영향 계층    : presentation(ViewModel) → data(Repository) → local(RoomDB)
기준 문서    : RECORD_LOGIC_SPEC.md:2

[기능 검증]
검증 대상  : 감정 기록 저장 기능
검증 맥락  : 분석 요약 — 저장 실패 시 UI 상태가 갱신되지 않을 가능성이 있다
             기준 문서 — RECORD_LOGIC_SPEC.md:2 저장 가능 조건 기준
             확인 범위 — 읽은 범위: ViewModel/Repository / 제외 범위: Calendar / 미확인 범위: 없음
엣지 케이스: null/empty — emotionTag null 시 저장 차단 확인 (CheckInViewModel.kt:148)
             0개 데이터 — causeTags 빈 리스트 허용, 저장 정상 완료 확인
             단일 항목 — intensity=1 최솟값 저장 정상 동작
             최대 개수 — 해당 없음 — causeTags 상한 제한 없음
             실패 응답 — DB 오류 시 UI 무응답 (롤백 없음) — HIGH
발견 사항  : 저장 실패 무응답 — HIGH / CheckInViewModel.kt:155 / catch 없이 emit 없음 / 사용자가 저장 실패를 인지 불가
이관 항목  : STEP2 소관 — viewModelScope 취소 시 insert 중단 가능성 (RecordRepository.kt:38)
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
             예외 처리 — catch 블록 없음, 예외 삼킴 (RecordRepository.kt:38) — HIGH
발견 사항  : 비동기 누수 — HIGH / RecordRepository.kt:38 / viewModelScope 취소 시 insert 중단 / 저장 미완료 무응답
             예외 미전파 — HIGH / RecordRepository.kt:38 / try-catch 없음 / 오류 상위 미전달
확인 범위  : 확인 범위: CheckInViewModel, RecordRepository / 제외 범위: Calendar / 미확인 런타임 범위: 없음
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
확인 범위       : 확인 범위: ViewModel, Repository / 제외 범위: DI 모듈 / 미확인 아키텍처 범위: 없음
이관 항목       : STEP4 소관 — 없음

[코드 품질]
도구 실행    : Android Lint 미실행 — 문서 예시, 실제 리뷰 시 실행 결과 필요
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
테스트 코드  : 테스트 파일 — 없음
               주요 경로 — 해당 없음 (테스트 파일 없음)
               엣지 케이스 — 해당 없음 (테스트 파일 없음)
               단언 품질 — 해당 없음 (테스트 파일 없음)
               모킹 수준 — 해당 없음 (테스트 파일 없음)
개발자 편의성: API 명확성 — 양호 (함수명·파라미터 의도 명확)
               에러 메시지 — 불명확 (RecordRepository.kt:38 — 예외 삼킴으로 원인 전달 없음) — MEDIUM
               로그 적절성 — 없음 (핵심 저장 경로 로그 없음) — MEDIUM
               문서화 — 누락 없음
상위 이관    : 없음
```

## 사용자 검증 포인트

AI 산출물 수령 후 아래 항목을 확인한다.

- [ ] `[코드 품질] 품질 확인`의 hallucination 항목에서 "없음 (확인 완료)"이라면, 실제 사용된 API가 공식 문서 또는 프로젝트 내 선언부에 존재하는가
- [ ] `[코드 품질] 도구 실행`에 기록된 도구 실행 결과가 실제 로컬 실행 결과와 일치하는가
- [ ] `[코드 품질] 컨벤션 확인`에서 "컨벤션 문서 없음 — 확인 불가"로 기록된 항목이 있다면, 팀 컨벤션 문서를 추가하고 재확인이 필요한가
- [ ] `[코드 품질] 컨벤션 기준`이 실제 컨벤션 기준 문서와 일치하는가
- [ ] `[코드 품질] 확인 범위`의 제외/미확인 범위가 품질 결론을 흔들지 않는가
- [ ] `[코드 품질] 상위 이관`으로 이관된 항목이 실제로 STEP1/STEP2에서 올바르게 반영됐는가
- [ ] `[코드 품질] 테스트 코드`에서 테스트 파일이 "없음"으로 기록된 경우 실제로 테스트 파일이 존재하지 않는가. finding 등급이 실제 커버리지 상태와 일치하는가
- [ ] `[코드 품질] 개발자 편의성`의 finding이 실제 코드를 열었을 때 확인되는 내용과 일치하는가

## 실패 규격

하네스 미통과 시 아래 형식으로 기록한다.

```text
실패 STEP   : STEP4
실패 항목   : 미통과 하네스 항목명(번호 포함)
실패 원인   : 어떤 데이터가 없는지 / 어떤 조건이 충족되지 않는지
복귀 대상   : 하네스 N번 참조 또는 STEP3/STEP2 이관 항목 재확인 사유
전달 데이터 : 완성된 [코드 품질] 섹션 값 (미완성 항목은 "미완성")
```

## 복귀 조건

- STEP3 `[아키텍처] 이관 항목`이 불완전해 STEP4 소관 항목을 특정할 수 없다 → [STEP3_ARCH.md](../step3/STEP3_ARCH.md)로 복귀, `[아키텍처] 이관 항목` 재확인
- hallucination 항목이 실제 런타임 오류로 이어진다 → [STEP2_LOGIC.md](../step2/STEP2_LOGIC.md)로 복귀, 런타임 영향 재확인

## 강제 중단 조건

복귀 없이 즉시 작업을 멈춘다.

- STEP3 산출물이 누락되거나 진입 조건 6개 항목이 절반 이상 비어 있다 → 사용자에게 STEP3 재실행을 요청한다.
- 직접 파일이 존재하지 않거나 열 수 없어 품질 판정 자체가 불가능하다 → 사용자에게 핵심 파일 재확인을 요청한다.
- hallucination 의심 항목의 선언부를 끝까지 확인할 수 없다 → 사용자에게 선언부 위치 확인을 요청한다.
- CRITICAL finding이 발견됐고 사용자 응답 없이 다음 STEP으로 진행해야 하는 상황이다 → 사용자 응답 대기.
- 루프 판정 → [STEP_ROLLBACK.md](../../common/STEP_ROLLBACK.md) 루프 판정 기준 적용.

## 다음 STEP

하네스 통과 + 산출물 출력 완료 → [STEP5_REPORT.md](../step5/STEP5_REPORT.md)로 `[분석 입력]` + `[기능 검증]` + `[로직 & 안정성]` + `[아키텍처]` + `[코드 품질]` 값을 전달한다.


