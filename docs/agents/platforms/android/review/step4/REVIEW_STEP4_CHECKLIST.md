<!-- 파일 목적: review STEP4(코드 품질 & 컨벤션)에서 Android 플랫폼 추가 체크 항목 -->

## 실행 체크리스트

- [ ] D1. **프로젝트 도구 환경 식별**을 수행한다.
  - `[분석 입력] 직접 파일`이 속한 프로젝트의 루트에서 다음을 확인하고 `[Android 도구] 환경`에 기록한다.
    - Gradle Wrapper 존재: `gradlew` / `gradlew.bat` 파일.
    - 빌드 시스템: `build.gradle` 또는 `build.gradle.kts`.
    - Android Gradle Plugin 버전: 모듈 또는 settings 파일에서 추출(예: `com.android.application` 버전).
    - 정적 분석 플러그인: `ktlint` / `detekt` / `spotless` 등 plugins 블록.
    - Lint baseline: `lint-baseline.xml` 또는 `lintOptions { baseline file(...) }` 설정.
  - 결과: 사용 가능 도구 목록을 1줄로 정리.

- [ ] D2. **Android Lint 실행 가능 여부와 실행**을 결정한다.
  - 사용자가 `./gradlew lint` 또는 `./gradlew :app:lint` 실행을 허용했는가.
  - 허용 + Wrapper 존재 → 실행하고 `app/build/reports/lint-results-*.xml` 또는 `lint-results-*.html`을 결과로 기록.
  - 미허용 → `[Android 도구] Lint`에 "미실행 — 사용자 미허용" 기록.
  - Wrapper 미존재 → "미실행 — Gradle Wrapper 없음" 기록.
  - 실행 결과 중 Error/Fatal은 finding으로 격상(등급은 `REVIEW_STEP4_SEVERITY.md` 참조).

- [ ] D3. **ktlint / detekt 실행 가능 여부와 실행**을 결정한다.
  - plugins 블록에서 적용 여부 확인. 미적용이면 "미적용" 기록.
  - 적용 + 사용자 허용 → `./gradlew ktlintCheck` / `./gradlew detekt` 실행 후 결과 기록.
  - 위반 발견 시 STEP4 본 체크리스트 6번(컨벤션) finding으로 등록.

- [ ] D4. **Gradle build / test 실행 가능 여부**를 확인한다.
  - 사용자가 `./gradlew assembleDebug` 또는 `./gradlew test` 실행을 허용했는가.
  - 실행 시 컴파일 오류·테스트 실패는 finding으로 등록(컴파일 오류는 CRITICAL — 빌드 불가).
  - 미실행은 사유 기록.

- [ ] D5. **deprecated Android API 사용**을 확인한다.
  - `[분석 입력] 직접 파일`에서 `@Deprecated` 어노테이션이 붙은 호출을 검사한다.
  - Android 공식 deprecation 예: `Fragment.setUserVisibleHint`, `AsyncTask`(제거됨), `startActivityForResult`(권장 대안: `registerForActivityResult`), `Handler()` 기본 생성자(권장: `Handler(Looper.getMainLooper())`), `WebView.setWebContentsDebuggingEnabled`(보안 경고 동반), `LiveData.observeForever`(누수 위험), `getDrawable(int)`(권장: `ContextCompat.getDrawable`).
  - 위반 시 finding 등록(등급: `REVIEW_STEP4_SEVERITY.md` 참조).

- [ ] D6. **Android hallucination 검출**을 수행한다.
  - 다음 의심 패턴을 찾는다.
    - `androidx.compose.*`에서 실제 존재하지 않는 함수(예: `LaunchedEffectOnce` — 실제 없음).
    - `androidx.lifecycle.*`에서 실제 존재하지 않는 멤버(예: `Lifecycle.coroutineScope` — 의심 시 import 확인).
    - `androidx.activity.compose.*`에서 실제 존재하지 않는 함수.
    - `kotlinx.coroutines.*`에서 실제 존재하지 않는 함수(예: `Flow.collectLatestAsState` — 실제 없음).
  - 의심 항목은 해당 라이브러리 공식 문서 또는 Android SDK 소스에서 선언 여부를 확인한다.
  - 확인 불가 시 CRITICAL finding으로 등록.

- [ ] D7. **Android 컨벤션 위반**을 확인한다.
  - 컨벤션 기준 문서를 먼저 열어 확인한다: `docs/rules/coding_rule/CODE_NAMING_RULE.md`, `docs/rules/coding_rule/CODE_LIFE_CYCLE.md`
  - 리소스 ID 네이밍: `R.id.btn_save` / `R.string.save_button` 등 `CODE_NAMING_RULE.md` 기준으로 대조.
  - 로깅 패턴: `android.util.Log` 직접 사용 vs Timber 등 래퍼 사용 — `CODE_NAMING_RULE.md` / `CODE_LIFE_CYCLE.md` 기준.
  - 버전 분기 패턴: `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)` vs 매직 넘버 `if (Build.VERSION.SDK_INT >= 26)` — `CODE_NAMING_RULE.md` 기준.
  - 문서 열람 후 해당 항목이 없으면 "컨벤션 문서 확인 — 해당 규칙 없음"으로 명시한다.

- [ ] D8. **테스트 코드 — Android 보강**을 수행한다.
  - 검토 대상 파일에 대응하는 테스트 파일 위치: `src/test/...`(JVM Unit Test), `src/androidTest/...`(Instrumented Test).
  - ViewModel 테스트의 `Dispatchers.setMain` / `MainDispatcherRule` 사용 여부.
  - Fragment/Compose 테스트에서 `ComposeTestRule` / `FragmentScenario` 사용 여부.
  - Mock 라이브러리(Mockito vs MockK vs Robolectric) 식별.
  - STEP4 본 체크리스트 8번 항목(주요 경로/엣지 케이스/단언/모킹)을 Android 환경에서 재평가하고 등급 적용.

- [ ] D9. **개발자 편의성 — Android 보강**을 확인한다.
  - 로그 적절성: `Log.d` 호출이 release 빌드에서 제거되도록 `BuildConfig.DEBUG` 분기가 있는가 또는 Timber tree 패턴 사용.
  - 에러 메시지: 사용자에게 노출되는 `Toast` / `Snackbar` / 에러 화면이 원인을 명확히 전달하는가.
  - 리소스 문자열 사용: 하드코딩된 한국어 문자열이 코드에 직접 박혀있는가(`strings.xml` 사용 여부).

- [ ] D10. D1~D9 결과를 `[Android 품질]` 블록으로 묶어 STEP4 본 산출물 `도구 실행` / `품질 확인` / `컨벤션 확인` / `테스트 코드` / `개발자 편의성`에 요약을 추가하고 상세는 `REVIEW_STEP4_OUTPUT.md` 형식대로 출력한다.
