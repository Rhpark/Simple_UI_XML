# STEP5. 출력 예시

STEP5_VERIFY.md의 산출물 형식을 실제 상황별로 채운 예시 모음이다.

→ 산출물 형식·하네스 기준은 [STEP5_VERIFY.md](STEP5_VERIFY.md) 참조

---

## 예시 A — Tier A / 전체 통과

```text
[feature workflow — STEP5 산출물]
단계     : STEP 5 (전체 검증)
진행도   : (6/6) - 산출물 출력 (완료)

apiCheck  : 통과
ktlintCheck : 통과
전체 테스트 : 통과 156건, 신규 실패 없음

이슈 : 없음
```

---

## 예시 B — Breaking Change 감지 / 의도된 변경

```text
[feature workflow — STEP5 산출물]
단계     : STEP 5 (전체 검증)
진행도   : (6/6) - 산출물 출력 (완료)

apiCheck  : Breaking Change 감지(의도된 변경 — apiDump 갱신 완료)
            변경 항목: PermissionDelegate.requestPermission() 추가 — 사용자 확인 완료
ktlintCheck : 위반 항목 자동 수정 후 통과 (PermissionDelegate.kt:87 import 정렬)
전체 테스트 : 통과 156건, 신규 실패 없음

이슈 : 없음
```

---

## 예시 C — Tier B / API·ktlint 미실행

```text
[feature workflow — STEP5 산출물]
단계     : STEP 5 (전체 검증)
진행도   : (6/6) - 산출물 출력 (완료)

apiCheck  : 미실행 — STEP3 스냅샷 없음 (simple_xml 모듈 apiDump 미지원)
ktlintCheck : 미실행 — simple_xml 모듈 ktlint 미설정
전체 테스트 : 통과 98건, 신규 실패 없음

이슈 : [API] apiCheck 미실행 — STEP3부터 미지원 확인된 항목
       [스타일] ktlintCheck 미실행 — 모듈 미설정
```

---

## 예시 D — 하네스 실패 (신규 테스트 실패)

```text
[feature workflow — STEP5 산출물]
단계     : STEP 5 (전체 검증)
진행도   : (3/6) - 전체 테스트 실행 (진행중)

apiCheck  : 미완성
ktlintCheck : 미완성
전체 테스트 : 통과 154건, 신규 실패 PermissionDelegateTest.testRequestPermission_denied:38

이슈 : [테스트] 신규 실패 — PermissionDelegateTest.testRequestPermission_denied:38
       STEP3 기준선에 없던 실패. 구현 파일(PermissionDelegate.kt:87)과 연관.

실패 STEP   : STEP5
실패 항목   : 3번 통과 — 신규 테스트 실패
실패 원인   : PermissionDelegateTest.testRequestPermission_denied:38 신규 실패 — requestPermission() 거부 시 콜백 미호출
복귀 대상   : STEP4 L2번 (코드 수정)
전달 데이터 : apiCheck 미완성 / ktlintCheck 미완성 / 전체 테스트 신규 실패 위 참조 / 이슈 위 참조
```
