<!-- 파일 목적: REVIEW_STEP3_CHECKLIST.md(Android 보강) 항목이 모두 수행됐는지 검증하는 통과 기준 -->

## 하네스 (통과 기준)

- [ ] C1번 통과 — presentation 계층의 data import 검사 결과가 `[Android 아키텍처] 레이어 건너뜀`에 기록됐다.
     기록 값: [위반 위치 목록 또는 "없음 (확인 완료)" — 비어있으면 미통과]
     미통과 시: C1 재수행. presentation 파일 전수에서 `androidx.room` / `retrofit2` / `okhttp3` / `androidx.datastore` import 재검색.
     심각도: 위반은 HIGH(레이어 건너뜀).

- [ ] C2번 통과 — ViewModel의 DAO/Retrofit Service 직접 호출 검사 결과가 기록됐다.
     기록 값: [확인 결과 또는 "ViewModel 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: C2 재수행. ViewModel 생성자 인자 타입과 본문 호출 재확인.
     심각도: 위반은 HIGH.

- [ ] C3번 통과 — Repository의 UI 상태 직접 변경 검사 결과가 기록됐다.
     기록 값: [확인 결과 또는 "Repository 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: C3 재수행. Repository/DataSource/UseCase 필드 타입 재확인.
     심각도: 위반은 HIGH.

- [ ] C4번 통과 — 프로젝트 DI 패턴이 식별돼 `[Android 아키텍처] DI 패턴`에 기록됐다.
     기록 값: [DI 명칭(Hilt/Koin/수동/없음) — 비어있으면 미통과]
     미통과 시: C4 재수행. `build.gradle(.kts)` 의존성 / `@HiltAndroidApp` / `startKoin` 호출 재확인.
     심각도: 미식별은 MEDIUM(DIP 판정의 전제 부족).

- [ ] C5번 통과 — DIP 검사 결과가 `[Android 아키텍처] DIP`에 기록됐다.
     기록 값: [위반 위치 또는 "DI 컨테이너 주입 확인 — 위반 없음" — 비어있으면 미통과]
     미통과 시: C5 재수행. DI 모듈의 `@Binds`/`@Provides` 정의와 ViewModel 생성자 타입 재확인.
     심각도: 위반은 HIGH 또는 LOW(STEP3 본 체크리스트 8번 따름).

- [ ] C6번 통과 — Context 주입 적절성 검사 결과가 기록됐다.
     기록 값: [확인 결과 또는 "Context 주입 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: C6 재수행. ViewModel 생성자에서 Context 관련 타입 재확인.
     심각도: 비-Application Context 주입은 HIGH.

- [ ] C7번 통과 — Fragment/Activity SRP 검사 결과가 기록됐다.
     기록 값: [확인 결과 또는 "Fragment/Activity 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: C7 재수행. 클래스 라인 수와 책임 분리 재확인.
     심각도: 위반은 MEDIUM.

- [ ] C8번 통과 — ViewModel 가변 상태 외부 노출 검사 결과가 기록됐다.
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: C8 재수행. ViewModel 필드 가시성과 backing property 패턴 재확인.
     심각도: 위반은 HIGH.

- [ ] C9번 통과 — Composable/View/Adapter의 ViewModel 내부 직접 변경 검사 결과가 기록됐다.
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: C9 재수행. UI 진입점에서 ViewModel 호출부 재확인.
     심각도: 위반은 HIGH.

- [ ] C10번 통과 — 멀티 모듈 경계 검사 결과가 기록됐다.
     기록 값: [위반 위치 또는 "단일 모듈 — 해당 없음" / "위반 없음 (확인 완료)" — 비어있으면 미통과]
     미통과 시: C10 재수행. `settings.gradle(.kts)`에서 모듈 구조 확인 후 모듈 간 import 재검사.
     심각도: 위반은 HIGH.

- [ ] C11번 통과 — Manifest와 실제 클래스 매칭 결과가 기록됐다.
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: C11 재수행. `AndroidManifest.xml` 각 컴포넌트 선언의 `android:name` 속성과 실제 클래스 경로 재확인.
     심각도: 불일치는 CRITICAL(앱 크래시).

- [ ] C12번 통과 — `[Android 아키텍처]` 블록이 `REVIEW_STEP3_OUTPUT.md` 형식대로 출력됐고 STEP3 본 산출물에 요약이 들어갔다.
     기록 값: [요약 — 비어있으면 미통과]
     미통과 시: C12 재수행. 출력 파일 형식대로 재작성.
     심각도: 형식 불일치는 MEDIUM.
