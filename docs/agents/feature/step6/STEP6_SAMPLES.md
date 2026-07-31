# STEP6. 출력 예시

STEP6_DOCUMENT.md의 산출물 형식을 실제 상황별로 채운 예시 모음이다.

→ 산출물 형식·하네스 기준은 [STEP6_DOCUMENT.md](STEP6_DOCUMENT.md) 참조

---

## 예시 A — Tier A / review 권고

```text
[feature workflow — STEP6 산출물]
단계     : STEP 6 (문서화 & 보고)
진행도   : (6/6) - 산출물 출력 (완료)

REPORT.md 경로  : docs/agents/output/feature/20260526_093000/REPORT.md
변경 파일 목록  : PermissionDelegate.kt:87 / PermissionViewModel.kt:45 / PermissionDelegateTest.kt:12
문서 동기화     : PermissionDelegate.kt KDoc 갱신 (requestPermission() 파라미터·반환값 설명 추가)
다음 단계       : review 권고

이슈 : 없음
```

**생성된 REPORT.md 내용:**
```markdown
# REPORT — 권한 위임 API 추가

## 구현 요약
PermissionDelegate에 requestPermission() 공개 API를 추가했다. 외부 모듈이 Activity 참조 없이
Context만으로 런타임 권한을 요청할 수 있다. PermissionViewModel이 이 API를 통해 권한 상태를
StateFlow로 노출한다.

## 변경 파일 목록
- PermissionDelegate.kt:87
- PermissionViewModel.kt:45
- PermissionDelegateTest.kt:12

## 마일스톤 완료 목록
1. PermissionDelegate 공개 API 추가
2. PermissionViewModel 연동
3. 테스트 작성

## 검증 결과
- 빌드: 통과
- 전체 테스트: 통과 156건, 신규 실패 없음
- apiCheck: Breaking Change 감지(의도된 변경 — apiDump 갱신 완료)
- ktlintCheck: 위반 항목 자동 수정 후 통과

## 문서 동기화
- PermissionDelegate.kt KDoc 갱신

## 이슈
없음

## 다음 단계
review 권고
```

---

## 예시 B — Tier B / 완료

```text
[feature workflow — STEP6 산출물]
단계     : STEP 6 (문서화 & 보고)
진행도   : (6/6) - 산출물 출력 (완료)

REPORT.md 경로  : docs/agents/output/feature/20260526_094500/REPORT.md
변경 파일 목록  : SimpleXmlExtensions.kt:34
문서 동기화     : 없음
다음 단계       : 완료

이슈 : 없음
```

**생성된 REPORT.md 내용:**
```markdown
# REPORT — 확장 함수 반환 타입 수정

## 구현 요약
SimpleXmlExtensions.kt의 toSafeString() 확장 함수 반환 타입을 String?에서 String으로 수정했다.
null 반환이 없는 구현임에도 타입이 nullable로 선언되어 호출 측에서 불필요한 null 처리가 발생하던 문제를 해소했다.

## 변경 파일 목록
- SimpleXmlExtensions.kt:34

## 마일스톤 완료 목록
해당 없음 — Tier B

## 검증 결과
- 빌드: 통과
- 전체 테스트: 통과 98건, 신규 실패 없음
- apiCheck: 미실행 — STEP3 스냅샷 없음
- ktlintCheck: 미실행 — 모듈 미설정

## 문서 동기화
없음

## 이슈
없음

## 다음 단계
완료
```
