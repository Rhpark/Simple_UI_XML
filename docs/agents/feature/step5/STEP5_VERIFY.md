# STEP5. 전체 검증

목적: 구현 완료 후 전체 빌드·테스트·API·스타일을 검사해 STEP3 기준선 대비 회귀가 없는지 확인한다.

## 진입 조건

- STEP4 하네스를 통과했다
- STEP4 산출물에서 아래 항목이 전달됐다
  - [ ] `구현 파일` — 전체 수정된 파일 목록이 기록됐다
  - [ ] `완료 마일스톤 목록` — 1개 이상 기록됐다 (Tier A만)
- STEP4를 통해 전달된 STEP3 기준선
  - [ ] `빌드 기준선` — 기록됐다
  - [ ] `테스트 기준선` — 기록됐다
  - [ ] `API 스냅샷` — 기록됐거나 "미실행 — 이유"로 명시됐다

## 행동 원칙

- STEP3 기준선을 유일한 비교 기준으로 사용한다. 기준선에 없는 항목은 신규 변경으로 판정한다.
- STEP3에서 "기존 실패"로 기록된 빌드·테스트 항목은 이 STEP에서 회귀로 판정하지 않는다.
- Breaking Change가 발견되면 사용자에게 보고하고 진행 여부를 결정받는다.
- ktlintCheck·apiCheck 실행 불가 시 이유를 기록하고 진행한다.

## 공통 규칙

→ [STEP_EXECUTION_RULE.md](../../common/STEP_EXECUTION_RULE.md) 참조
→ [STEP_FORCE_STOP.md](../../common/STEP_FORCE_STOP.md) 참조
→ [STEP_HARNESS.md](../../common/STEP_HARNESS.md) 참조
→ [GLOSSARY.md](../../common/GLOSSARY.md) 참조
→ [feature/common/GLOSSARY.md](../common/GLOSSARY.md) 참조

## 실행 체크리스트

- [ ] 1. STEP3 빌드 기준선·테스트 기준선·API 스냅샷과 STEP4 구현 파일 목록을 확인한다.
  - 구현 파일에서 대상 모듈명을 추출한다.

- [ ] 2. 전체 빌드를 실행하고 STEP3 빌드 기준선과 비교한다.
  - 실행 명령 (PowerShell):
    ```
    powershell -Command "cd 'D:\Android Project\SimpleUI_XML'; .\gradlew.bat assembleDebug"
    ```
  - 기준선이 "통과"였는데 실패이면 → 원인 파악 후 강제 중단 조건 적용.
  - 기준선이 "기존 실패"였던 항목의 동일 실패는 무시한다.

- [ ] 3. 전체 테스트를 실행하고 STEP3 테스트 기준선과 비교한다.
  - 실행 명령 (PowerShell):
    ```
    powershell -Command "cd 'D:\Android Project\SimpleUI_XML'; .\gradlew.bat testRobolectric testUnit"
    ```
  - STEP3 기준선의 실패 목록에 없던 신규 실패를 식별한다.
  - 신규 실패가 있으면 구현 파일과 연관성을 확인하고 수정한다.
  - 수정 후에도 해소되지 않으면 → 강제 중단 조건 적용.

- [ ] 4. apiCheck를 실행해 STEP3 API 스냅샷 대비 Breaking Change를 판정한다.
  - 실행 명령 (PowerShell):
    ```
    powershell -Command "cd 'D:\Android Project\SimpleUI_XML'; .\gradlew.bat apiCheck"
    ```
  - STEP3 API 스냅샷이 "미실행"이었으면 이 항목도 "미실행 — STEP3 스냅샷 없음"으로 기록하고 자동 통과.
  - Breaking Change 감지 시: 사용자에게 보고하고 의도된 변경인지 확인한다.
    → 의도된 변경: apiDump를 재실행해 스냅샷을 갱신하고 계속 진행한다.
    → 의도되지 않은 변경: 강제 중단 조건 적용.

- [ ] 5. ktlintCheck를 실행해 코드 스타일을 검사한다.
  - 실행 명령 (PowerShell):
    ```
    powershell -Command "cd 'D:\Android Project\SimpleUI_XML'; .\gradlew.bat ktlintCheck"
    ```
  - 위반 항목이 있으면 `ktlintFormat`으로 자동 수정을 시도한다.
    ```
    powershell -Command "cd 'D:\Android Project\SimpleUI_XML'; .\gradlew.bat ktlintFormat"
    ```
  - 자동 수정 후 ktlintCheck를 재실행해 통과를 확인한다.
  - 실행 불가 시: 이유를 기록하고 "미실행 — [이유]"로 명시한 뒤 자동 통과.

- [ ] 6. 산출물 형식에 맞춰 출력한다.

## 하네스

- [ ] 1번 통과 — STEP3 기준선과 STEP4 구현 파일이 확인됐고 대상 모듈이 추출됐다.
     **CRITICAL** / 미통과 시: 누락된 산출물 확인 후 해당 STEP으로 복귀.
- [ ] 2번 통과 — 전체 빌드가 통과됐다. (STEP3 "기존 실패" 항목 제외)
     **CRITICAL** / 미통과 시: 원인 파악 후 STEP4 구현 문제이면 STEP4로 복귀. 환경 문제이면 사용자에게 보고. 해소 불가이면 강제 중단.
- [ ] 3번 통과 — STEP3 기준선 대비 신규 테스트 실패가 없다.
     **HIGH** / 미통과 시: 원인 파악 후 STEP4로 복귀해 수정. 해소 불가이면 강제 중단.
- [ ] 4번 통과 — apiCheck가 통과됐거나 Breaking Change가 사용자 확인으로 해소됐다. 미실행이면 자동 통과.
     **MEDIUM** / 미통과 시: 의도되지 않은 Breaking Change이면 강제 중단. 의도된 변경이면 apiDump 갱신 후 통과.
- [ ] 5번 통과 — ktlintCheck가 통과됐거나 미실행 이유가 명시됐다.
     **MEDIUM** / 미통과 시: ktlintFormat 후 재실행. 재실행 불가이면 이유 기록 후 자동 통과.
- [ ] 6번 통과 — 산출물이 형식에 맞춰 출력됐다.
     **HIGH** / 미통과 시: 6번 재수행.

미통과 항목이 있으면 실패 규격에 따라 기록 후 강제 중단 조건을 확인한다. 루프 판정 기준은 → [STEP_ROLLBACK.md](../../common/STEP_ROLLBACK.md) 참조

## 산출물(STEP6에 전달 정보)

STEP 완료 시 아래 형식으로 값을 채워 출력한다.

```text
[feature workflow — STEP5 산출물]
단계     : STEP 5 (전체 검증)
진행도   : (n/6) - 마지막으로 완료한 체크리스트 항목명 (진행중 / 완료)

apiCheck  : 통과 / Breaking Change 감지(의도된 변경 — apiDump 갱신 완료) / 미실행 — [이유]
ktlintCheck : 통과 / 위반 항목 자동 수정 후 통과 / 미실행 — [이유]
전체 테스트 : 통과 N건, 신규 실패 없음 / 통과 N건, 신규 실패 [테스트명 목록]

이슈 : [빌드/테스트/API/스타일/기타] 내용 — 이유. 없으면 "없음"
```

→ 출력 예시는 [STEP5_SAMPLES.md](STEP5_SAMPLES.md) 참조

## 사용자 검증 포인트

AI 산출물 수령 후 아래 항목을 확인한다.

- [ ] `전체 테스트`에 신규 실패가 없는가 — 있으면 원인이 납득 가능한지 확인한다
- [ ] `apiCheck`에 Breaking Change가 있으면 의도된 변경인지 직접 확인한다
- [ ] `ktlintCheck`가 미실행이면 별도로 코드 스타일을 검토한다
- [ ] `이슈`가 "없음"이 아니면 STEP6 진행 전 직접 판단한다

## 실패 규격

하네스 미통과 시 아래 형식으로 기록한다.

```text
실패 STEP   : STEP5
실패 항목   : 미통과 하네스 항목명
실패 원인   : 어떤 데이터가 없는지 / 어떤 조건이 충족되지 않는지
복귀 대상   : 해당 체크리스트 항목 또는 STEP4
전달 데이터 : apiCheck / ktlintCheck / 전체 테스트 / 이슈. 미완성 항목은 "미완성"
```

## 강제 중단 조건

복귀 없이 즉시 작업을 멈춘다.

- 전체 빌드 실패가 해소되지 않는다 → 실패 내용을 사용자에게 보고 후 중단
- 신규 테스트 실패가 해소되지 않는다 → 실패 목록을 사용자에게 보고 후 중단
- 의도되지 않은 Breaking Change가 발견됐다 → 변경 내용을 사용자에게 보고 후 중단
- 루프 판정 → [STEP_ROLLBACK.md](../../common/STEP_ROLLBACK.md) 루프 판정 기준 적용

## 다음 STEP

하네스 통과와 산출물 출력 완료 후 [STEP6_DOCUMENT.md](../step6/STEP6_DOCUMENT.md)로 아래 값을 전달한다.

- **Tier A**: Tier, 분기, 직접 파일, 간접 파일, 출력 디렉토리, PRD 경로, SPEC 경로, PLAN 경로, 완료 마일스톤 목록, 구현 파일, apiCheck, ktlintCheck, 전체 테스트, 이슈
- **Tier B**: Tier, 직접 파일, 간접 파일, 출력 디렉토리, 구현 파일, apiCheck, ktlintCheck, 전체 테스트, 이슈
