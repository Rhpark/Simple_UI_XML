---
name: CheckQuality
description: ktlint, lint, apiDump 정적 분석 일괄 실행 스킬. "품질 체크", "ktlint", "lint 체크", "api dump", "정적 분석" 요청 시 사용.
disable-model-invocation: true
argument-hint: "[all | ktlint | lint | api] (생략 시 all)"
---

# CheckQuality Skill

SimpleUI_XML 프로젝트의 정적 분석을 일괄 실행한다.

## 실행 전 확인

아래 명령으로 프로젝트 루트를 확인한다.

```
d:\Android Project\SimpleUI_XML
```

모든 Gradle 명령은 PowerShell로 실행한다.

```bash
powershell -Command "cd 'd:\Android Project\SimpleUI_XML'; .\gradlew.bat {task}"
```

## $ARGUMENTS 해석

| 인자 | 실행 항목 |
|---|---|
| 없음 / `all` | 아래 전체 순서대로 실행 |
| `ktlint` | ktlintCheck만 실행 |
| `lint` | lintDebug만 실행 |
| `api` | apiDump 3개만 실행 |

## 전체 실행 순서 (all)

### STEP 1 — ktlintCheck

```bash
powershell -Command "cd 'd:\Android Project\SimpleUI_XML'; .\gradlew.bat ktlintCheck"
```

### STEP 2 — lintDebug

```bash
powershell -Command "cd 'd:\Android Project\SimpleUI_XML'; .\gradlew.bat lintDebug"
```

### STEP 3 — apiDump

```bash
powershell -Command "cd 'd:\Android Project\SimpleUI_XML'; .\gradlew.bat :simple_core:apiDump :simple_xml:apiDump :simple_system_manager:apiDump"
```

## 결과 출력 형식

각 STEP 완료 후 아래 형식으로 출력한다.

```
✔ STEP{N} {태스크명} — 성공 / 실패
```

실패 시 Gradle 출력의 오류 메시지를 그대로 표시한다.

모든 STEP 완료 후 최종 요약을 출력한다.

```
============================
✅ CheckQuality 완료
- ktlintCheck : 성공 / 실패
- lintDebug   : 성공 / 실패
- apiDump     : 성공 / 실패
============================
```

## 주의사항

- 각 STEP은 이전 STEP 실패와 무관하게 순서대로 모두 실행한다.
- apiDump는 3개 모듈을 한 명령으로 실행한다.
- 결과 분석은 하지 않는다. 출력을 그대로 표시한다.
