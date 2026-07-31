# STEP4. 출력 예시

STEP4_IMPLEMENT.md의 산출물 형식을 실제 상황별로 채운 예시 모음이다.

→ 산출물 형식·하네스 기준은 [STEP4_IMPLEMENT.md](STEP4_IMPLEMENT.md) 참조

---

## 예시 A — Tier A / 마일스톤 1 완료 보고

```text
[feature workflow — 마일스톤 1 완료]
마일스톤    : 1. PermissionDelegate 공개 API 추가

구현 파일   : PermissionDelegate.kt:87 (requestPermission() 추가, 기존 내부 메서드 리팩토링)
빌드        : 통과
테스트      : 신규 실패 없음
하네스 준수 : PRD 금지 패턴 준수 — Activity 직접 참조 없음
             PRD 경계 조건 준수 — 권한 요청 흐름만 구현, UI 안내 미포함
             SPEC 판단 기준 준수 — Context 기반 설계 선택 적용

다음 마일스톤 : 2. PermissionViewModel 연동
```

---

## 예시 B — Tier B / 단일 사이클 완료 보고

```text
[feature workflow — 마일스톤 1 완료]
마일스톤    : 1. (Tier B 단일 사이클)

구현 파일   : SimpleXmlExtensions.kt:34 (확장 함수 반환 타입 수정)
빌드        : 통과
테스트      : 신규 실패 없음
하네스 준수 : 해당 없음 — Tier B

다음 마일스톤 : 없음(마지막 마일스톤)
```

---

## 예시 C — 전체 완료 산출물 (STEP5에 전달)

```text
[feature workflow — STEP4 산출물]
단계     : STEP 4 (구현)
진행도   : 완료

완료 마일스톤 : 1. PermissionDelegate 공개 API 추가 / 2. PermissionViewModel 연동 / 3. 테스트 작성
구현 파일     : PermissionDelegate.kt:87 / PermissionViewModel.kt:45 / PermissionDelegateTest.kt:12

이슈 : 없음
```

---

## 예시 D — 하네스 실패 (L3 빌드 실패)

```text
[feature workflow — 마일스톤 2 완료]
마일스톤    : 2. PermissionViewModel 연동

구현 파일   : PermissionViewModel.kt:45
빌드        : 실패 — PermissionViewModel.kt:52 Type mismatch: inferred type is Unit but StateFlow<PermissionState> was expected
테스트      : 미실행
하네스 준수 : 미완성

다음 마일스톤 : 미완성

실패 STEP      : STEP4
실패 마일스톤  : 2. PermissionViewModel 연동
실패 항목      : L3번 통과 — 빌드
실패 원인      : StateFlow 반환 타입 불일치 — PermissionViewModel.kt:52
복귀 대상      : L2번 (코드 수정)
전달 데이터    : 완료 마일스톤 1. PermissionDelegate 공개 API 추가 / 구현 파일 PermissionViewModel.kt:45 / 이슈 빌드 실패
```
