# STEP3. 출력 예시

STEP3_SAFETY.md의 산출물 형식을 실제 상황별로 채운 예시 모음이다.

→ 산출물 형식·하네스 기준은 [STEP3_SAFETY.md](STEP3_SAFETY.md) 참조

---

## 예시 A — Tier A / 빌드·테스트·API 모두 정상

```text
[feature workflow — STEP3 산출물]
단계     : STEP 3 (안전망 확보)
진행도   : (5/5) - 산출물 출력 (완료)

빌드 기준선  : simple_xml assembleDebug — 통과
테스트 기준선 : 통과 142건 / 실패 없음
API 스냅샷   : PermissionDelegate.requestPermission() 없음 (신규 추가 예정)
               PermissionViewModel.state: StateFlow<PermissionState> 노출 중

이슈 : 없음
```

---

## 예시 B — Tier B / 기존 실패 포함 / API 미실행

```text
[feature workflow — STEP3 산출물]
단계     : STEP 3 (안전망 확보)
진행도   : (5/5) - 산출물 출력 (완료)

빌드 기준선  : simple_xml assembleDebug — 기존 실패 — SimpleXmlExtensions.kt:12 미사용 import 경고 (기존부터 존재)
테스트 기준선 : 통과 98건 / 실패 없음
API 스냅샷   : 미실행 — simple_xml 모듈 apiDump 미지원 (api 디렉토리 없음)

이슈 : [API] apiDump 미지원 모듈 — STEP5 apiCheck 건너뜀 예정
```

---

## 예시 C — 하네스 실패 (빌드 실패 / 직접 파일 연관)

```text
[feature workflow — STEP3 산출물]
단계     : STEP 3 (안전망 확보)
진행도   : (2/5) - 빌드 기준선 실행 (진행중)

빌드 기준선  : simple_xml assembleDebug — 실패
               오류: PermissionDelegate.kt:45 — Unresolved reference: ActivityResultLauncher
테스트 기준선 : 미실행
API 스냅샷   : 미실행

이슈 : [빌드] 빌드 실패 원인이 직접 파일(PermissionDelegate.kt:45)과 연관됨 — 사용자에게 보고, 진행 여부 결정 대기

실패 STEP   : STEP3
실패 항목   : 2번 통과 — 빌드 기준선
실패 원인   : 빌드 실패 오류가 직접 수정 대상 파일과 연관되어 있어 사용자 확인 필요
복귀 대상   : 사용자 확인 후 결정
전달 데이터 : 빌드 기준선 실패(오류 위 참조) / 테스트 기준선 미완성 / API 스냅샷 미완성 / 이슈 위 참조
```
