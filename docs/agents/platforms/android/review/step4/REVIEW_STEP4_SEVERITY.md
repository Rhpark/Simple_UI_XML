<!-- 파일 목적: a STEP4(Android 보강) finding의 심각도 판정 기준 -->

### 심각도 기준

#### CRITICAL

- **존재하지 않는 Android/Compose/Coroutines API 호출(hallucination)**: 선언부를 공식 문서 또는 라이브러리 소스에서 확인할 수 없음. 빌드 실패 또는 런타임 `NoSuchMethodError`. 공통 SEVERITY_RULE.md CRITICAL 조건 직접 적용.
- **Gradle build 실패**(`./gradlew assembleDebug` 사용자 허용 후 실행했을 때 컴파일 오류): 모든 사용자에게 빌드 불가. 다음 STEP 진입 불가.
- **Android Lint Fatal 등급 이슈**: 예 — `MissingPermission`, `MissingTranslation`(strict), `WrongCallType`. 빌드는 통과해도 런타임 보장 깨짐.

#### HIGH

- **deprecated API 사용으로 런타임 영향이 입증된 경우**: 예 — `AsyncTask`(이미 제거됨, 컴파일 실패), `startActivityForResult` + 결과 누락 시나리오. 이때 대표 STEP은 STEP2(런타임) 또는 STEP1(기능), STEP4는 보조 근거.
- **Android Lint Error 등급 이슈가 다수**: 예 — `UnusedResources` 다수(번역 누락 포함), `IconMissingDensityFolder`.
- **로깅 패턴 위반으로 release 빌드에 디버그 정보 노출**: `Log.d` 호출이 분기 없이 release에서도 실행돼 민감 정보 유출 가능.
- **테스트 코드가 ViewModel에서 `Dispatchers.setMain` 없이 작성돼 모든 테스트가 flaky**.

#### MEDIUM

- **deprecated API 사용 — 런타임 영향 없음**: 동작은 정상이나 향후 보강 필요.
- **컨벤션 위반(네이밍·로깅·버전 분기) — 컨벤션 문서 기준**: 위반 확정.
- **컨벤션 문서 없음 — 확인 불가**: 결론 도출 자체가 어려움.
- **하드코딩 문자열**: `strings.xml` 미사용. 다국어 대응 불가.
- **로그가 핵심 경로에 부재**: 디버깅 곤란.
- **Lint Warning 다수**: 빌드 통과하나 잠재 이슈.
- **D8 테스트 환경 미식별**: 테스트 품질 평가 신뢰도 저하.
- **D10 형식 누락**.

#### LOW

- **미사용 import**: 동작 영향 없음.
- **주석 처리된 코드**: TODO 제외.
- **`BuildConfig.DEBUG` 분기 없는 로그가 release에는 자동 제거되는 ProGuard 설정이 있는 경우**: 유출 위험 없음, LOW 유지.
- **strings.xml 키 네이밍 불일치(소문자/대문자 혼용)**: 단순 정리 항목.
