<!-- 파일 목적: a STEP4(Android 보강) 산출물 형식 -->

## 산출물

STEP4 본 산출물 `[코드 품질]` 블록 바로 아래에 `[Android 품질]` 블록을 평행 블록으로 추가한다.
본 블록은 review STEP5(보고) 입력으로 그대로 전달된다.

STEP4 본 산출물 `도구 실행` 줄 형식:
- `도구 실행 : Android Lint — [실행 결과 또는 미실행 사유] / ktlint·detekt — [결과] / Gradle build — [결과] (상세: [Android 품질] 블록 참조)`

`[Android 품질]` 블록 형식:

```text
[Android 품질]
환경           : [Gradle Wrapper 유무 / AGP 버전 / 정적 분석 플러그인 목록]
Lint           : [실행 결과 요약 또는 미실행 사유]
ktlint/detekt  : [실행 결과 요약 또는 미적용/미실행 사유]
Gradle build   : [성공/실패 또는 미실행 사유]
deprecated API : [없음 (확인 완료) 또는 발견 위치 목록]
hallucination  : [없음 (확인 완료) 또는 의심 위치 — 선언부 확인 결과]
컨벤션         : [위반 없음 또는 위반 위치 또는 "컨벤션 문서 없음 — 확인 불가"]
테스트 환경    : [JVM/Instrumented 존재 여부 / 사용 Mock 라이브러리]
로그 적절성    : [BuildConfig.DEBUG 분기 여부 또는 Timber 사용 여부]
에러 메시지    : [Toast/Snackbar 명확성 확인 결과]
문자열 하드코딩: [없음 (확인 완료) 또는 발견 위치]
위반 요약      : [위반 건수] 건 (없으면 "없음")
```
