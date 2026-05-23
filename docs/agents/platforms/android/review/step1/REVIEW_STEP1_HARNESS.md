<!-- 파일 목적: REVIEW_STEP1_CHECKLIST.md(Android 보강) 항목이 모두 수행됐는지 검증하는 통과 기준 -->

## 하네스 (통과 기준)

- [ ] A1번 통과 — Android 프레임워크 API 카테고리 분류가 수행됐고 `[Android 호환성] 감지 카테고리`에 카테고리 목록 또는 "해당 없음 — Android 프레임워크 API 미사용"이 기록됐다.
     기록 값: [감지 카테고리 또는 "해당 없음"을 여기 기재 — 비어있으면 미통과]
     미통과 시: A1 재수행. `[분석 입력] 직접 파일`의 모든 import 구문을 다시 훑어 카테고리 매핑.
     심각도: `REVIEW_STEP1_SEVERITY.md` 적용(통상 MEDIUM).

- [ ] A2번 통과 — 감지된 모든 API에 대해 SDK 요건과 프로젝트 `minSdk`가 대조됐고 `[Android 호환성] minSdk` · `SDK 대조`에 결과가 기록됐다.
     기록 값: [minSdk 값과 대조 결과 라인 수 — 비어있으면 미통과]
     미통과 시: A2 재수행. `app/build.gradle(.kts)`에서 `minSdk` 확인 + 감지 API별 공식 문서 또는 SDK 레벨 표 재확인.
     심각도: `REVIEW_STEP1_SEVERITY.md` 적용(SDK 미충족은 통상 HIGH).

- [ ] A3번 통과 — 런타임 권한이 필요한 API가 감지된 경우 매니페스트 선언과 런타임 체크 여부가 `[Android 호환성] 권한 확인`에 기록됐다. 권한 필요 API가 없으면 "해당 없음"이 기록됐다.
     기록 값: [권한별 매니페스트 OK / 런타임 체크 OK 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: A3 재수행. 매니페스트 직접 열람 + 호출 직전 분기 직접 확인.
     심각도: 권한 누락은 통상 HIGH, 매니페스트 누락은 CRITICAL(권한 없는 호출 = 크래시 또는 SecurityException).

- [ ] A4번 통과 — Foreground Service 사용 여부와 `foregroundServiceType` 정합성이 `[Android 호환성] Foreground Service`에 기록됐다.
     기록 값: [감지 / 타입 / 매니페스트 정합성 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: A4 재수행. `startForeground` 호출부와 매니페스트 `<service>` 선언 동시 확인.
     심각도: `REVIEW_STEP1_SEVERITY.md` 적용(타입 누락은 API 34+에서 HIGH).

- [ ] A5번 통과 — 저장소 접근 코드의 Scoped Storage 정합성이 기록됐다.
     기록 값: [감지된 저장소 API와 정합성 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: A5 재수행. `MediaStore` / `Environment` / `FileProvider` 사용 위치 재확인.
     심각도: `REVIEW_STEP1_SEVERITY.md` 적용.

- [ ] A6번 통과 — PendingIntent 사용 시 `FLAG_IMMUTABLE` / `FLAG_MUTABLE` 명시 여부가 기록됐다.
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: A6 재수행. `PendingIntent.getActivity/getBroadcast/getService` 호출부 전수 확인.
     심각도: 누락은 API 31+에서 HIGH(크래시).

- [ ] A7번 통과 — EdgeToEdge / WindowInsets 처리 결과가 기록됐다.
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: A7 재수행. `enableEdgeToEdge` / `setDecorFitsSystemWindows` 호출과 insets 보정 코드 동시 확인.
     심각도: targetSdk 35+ 미적용은 통상 MEDIUM(시각적 깨짐).

- [ ] A8번 통과 — `[Android 호환성]` 블록이 `REVIEW_STEP1_OUTPUT.md` 형식대로 작성됐고 STEP1 본 산출물 `호환성` 줄에 1줄 요약이 들어갔다.
     기록 값: [요약 1줄 — 비어있으면 미통과]
     미통과 시: A8 재수행. `REVIEW_STEP1_OUTPUT.md` 예시를 참조해 형식을 맞춰 재출력.
     심각도: 형식 불일치는 MEDIUM(가독성/추적성 저하).
