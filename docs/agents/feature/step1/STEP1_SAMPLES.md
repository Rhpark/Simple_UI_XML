# STEP1. 출력 예시

STEP1_ENTRY.md의 산출물 형식을 실제 상황별로 채운 예시 모음이다.

→ 산출물 형식·하네스 기준은 [STEP1_ENTRY.md](STEP1_ENTRY.md) 참조

---

## 예시 A — Tier A / 분기 A (review 산출물 있음)

```text
[feature workflow — STEP1 산출물]
단계     : STEP 1 (진입 판정)
진행도   : (7/7) - 산출물 출력 (완료)

Tier            : A — 복수 레이어 걸침 (ViewModel + Repository), 공개 API 변경 포함
분기            : A — docs/agents/output/review/20260520_143022.md 사용
직접 파일       : PermissionDelegate.kt:87 / PermissionViewModel.kt:45
간접 파일       : PermissionDelegateTest.kt:12
STEP2 적용 여부 : 필수
출력 디렉토리   : docs/agents/output/feature/20260526_093000/

이슈 : 없음
```

---

## 예시 B — Tier B (단순 수정)

```text
[feature workflow — STEP1 산출물]
단계     : STEP 1 (진입 판정)
진행도   : (7/7) - 산출물 출력 (완료)

Tier            : B — 단일 파일 내 수정, 기존 패턴 그대로, 공개 API 변경 없음, 단일 레이어
분기            : 해당 없음
직접 파일       : SimpleXmlExtensions.kt:34
간접 파일       : 없음
STEP2 적용 여부 : 생략
출력 디렉토리   : docs/agents/output/feature/20260526_094500/

이슈 : 없음
```

---

## 예시 C — Tier A / 분기 B (analysis만 있음)

```text
[feature workflow — STEP1 산출물]
단계     : STEP 1 (진입 판정)
진행도   : (7/7) - 산출물 출력 (완료)

Tier            : A — 새 파일 생성 필요, 공개 API 추가
분기            : B — docs/agents/output/analysis/20260518_110045.md 사용
                       → review 실행 권고를 사용자에게 전달함. 사용자가 계속 진행 선택.
직접 파일       : NotificationHelper.kt:1 (신규 생성)
간접 파일       : AndroidManifest.xml:24
STEP2 적용 여부 : 필수
출력 디렉토리   : docs/agents/output/feature/20260526_100000/

이슈 : 없음
```

---

## 예시 D — 하네스 실패 (직접 파일 식별 불가)

```text
[feature workflow — STEP1 산출물]
단계     : STEP 1 (진입 판정)
진행도   : (2/7) - 직접 파일 식별 (진행중)

Tier            : 미완성
분기            : 미완성
직접 파일       : 미완성
간접 파일       : 미완성
STEP2 적용 여부 : 미완성
출력 디렉토리   : 미완성

이슈 : [파일] "알림 버튼 수정"으로 진입점 파일을 식별할 수 없음 — 기능명·클래스명 추가 확인 필요

실패 STEP   : STEP1
실패 항목   : 2번 통과 — 직접 파일 식별
실패 원인   : 요청이 "알림 버튼 수정"만 기술됐고 파일명·클래스명 단서 없음
복귀 대상   : 체크리스트 2번
전달 데이터 : Tier / 분기 / 직접 파일 / 간접 파일 / STEP2 적용 여부 / 출력 디렉토리 / 이슈 — 모두 미완성
```
