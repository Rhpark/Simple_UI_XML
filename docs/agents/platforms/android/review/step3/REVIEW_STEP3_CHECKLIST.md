<!-- 파일 목적: review STEP3(아키텍처)에서 Android 플랫폼 추가 체크 항목 -->

## 기준 문서 참조

아키텍처·패턴 판단 시 아래 문서를 우선 확인한다. 문서가 없거나 해당 규칙이 없으면 일반 원칙을 따른다.
- 코딩 규칙 인덱스: `docs/rules/CODING_RULE_INDEX.md`
- 아키텍처 기준: `docs/rules/coding_rule/CODE_ARCHITECTURE.md`
- 코드 패턴 기준: `docs/rules/coding_rule/CODE_PATTERNS_INDEX.md`

## 실행 체크리스트

- [ ] C1. **레이어 건너뜀 — Android 구체 분류**를 확인한다.
  - presentation 계층(Activity / Fragment / Composable / ViewModel)에서 다음 import가 있는지 검사한다.
    - `androidx.room.*` 직접 import (Room DAO/Entity/Database 직접 참조)
    - `retrofit2.*` 직접 import (Retrofit Service 직접 참조)
    - `okhttp3.*` 직접 import
    - `androidx.sqlite.*` / `android.database.sqlite.*` 직접 import
    - `androidx.datastore.*` 직접 import (Repository 우회)
  - 위 import가 ViewModel 또는 그 상위 presentation 클래스에 있으면 레이어 건너뜀 위반 — finding 등록(HIGH).
  - 정상이면 `[Android 아키텍처] 레이어 건너뜀`에 "ViewModel/Composable의 data import 없음 (확인 완료)" 기록.

- [ ] C2. **ViewModel의 DAO/Retrofit Service 직접 호출** 여부를 확인한다(C1보다 좁은 검사).
  - ViewModel 생성자 인자 타입 중 `@Dao` 어노테이션이 붙은 인터페이스 또는 Retrofit `@Get/@Post` 메서드를 가진 인터페이스가 직접 주입되는가.
  - ViewModel 메서드 본문에서 `dao.xxx()` 또는 `service.xxx()` 호출이 직접 발생하는가.
  - 위반 시: finding 등록(등급: HIGH).
  - 정상이면 `[Android 아키텍처] DAO 직접 호출`에 "없음 (확인 완료) — Repository 경유 확인" 기록.

- [ ] C3. **Repository의 UI 상태 직접 변경** 여부를 확인한다.
  - Repository / DataSource / UseCase 계층에 `MutableStateFlow<UiState>` / `MutableLiveData<UiState>` / `Channel<UiEffect>` 필드가 있는가.
  - 위 필드가 있다면 외부에 노출되는 타입이 UI 모델(`UiState` / `UiEffect` 등)인가.
  - 위반 시(UI 상태가 data 계층에 정의되거나 노출됨): finding 등록(등급: HIGH).
  - 정상이면 `[Android 아키텍처] UI 직접 변경`에 "없음 (확인 완료)" 기록.

- [ ] C4. **DI 컨테이너 사용 패턴**을 식별한다(C5의 전제).
  - 프로젝트가 Hilt(`@HiltAndroidApp` / `@AndroidEntryPoint` / `@Inject` / `@Module`) / Koin / 수동 DI 중 무엇을 사용하는지 식별한다.
  - 사용 DI를 `[Android 아키텍처] DI 패턴`에 기록한다(예: "Hilt").
  - DI 미사용 + ViewModel에서 직접 `new Repository(...)` / Kotlin `Repository(...)` 호출이 있으면 — finding 등록(등급: HIGH).

- [ ] C5. **DIP 위반(Android 특화)**을 확인한다.
  - **DI 컨테이너 주입은 위반으로 보지 않는다**(STEP3 본 체크리스트 8번 명시). 그러나 다음은 위반으로 기록한다.
    - DI를 통해 주입받지 않고 **생성자/필드에서 구현체를 직접 생성**(`val repo = RecordRepositoryImpl()`)
    - 구현체를 인터페이스 없이 직접 주입(추상화 미적용)이지만 프로젝트 규칙이 인터페이스 분리를 요구하는 경우
    - DI 모듈에서 `@Binds` / `@Provides`로 구현체를 인터페이스에 바인딩하지 않고 구현체 자체를 직접 주입하는 경우
  - 정상이면 `[Android 아키텍처] DIP`에 "DI 컨테이너 주입 확인 — 위반 없음" 기록.

- [ ] C6. **Context 주입 위치**가 적절한지 확인한다.
  - ViewModel에 `Activity`/`FragmentActivity`/`Fragment`/`Context`(특히 `@ApplicationContext` 없는 경우) 주입이 있는가.
  - Android Architecture 권장: ViewModel에는 `@ApplicationContext`만 허용. Activity Context 주입은 누수 위험.
  - 위반 시: finding 등록(등급: HIGH — STEP2 Context 보유와 겹치면 대표 STEP은 STEP2, STEP3는 보조 근거).

- [ ] C7. **Fragment / Activity 책임 분리(SRP — Android 구체)**를 확인한다.
  - Fragment / Activity 클래스 라인 수가 200줄 이상이면서 UI 렌더링·비즈니스 로직·데이터 변환을 모두 가지면 — finding 등록(등급: MEDIUM).
  - `setOnClickListener` 안에 데이터 가공/네트워크 호출이 직접 있으면 — finding 등록(등급: MEDIUM).

- [ ] C8. **가변 상태 외부 노출(Android 구체)**을 확인한다.
  - ViewModel에 `val uiState: MutableStateFlow<...>`(public)가 있는가 → 위반.
  - ViewModel에 `val uiState: MutableLiveData<...>`(public)가 있는가 → 위반.
  - backing property 패턴(`private val _uiState: MutableStateFlow<...>` / `val uiState: StateFlow<...> get() = _uiState`)이 깨졌는가.
  - `asStateFlow()` / `asLiveData()` 변환 없이 노출되면 위반.
  - 위반 시: finding 등록(등급: HIGH).

- [ ] C9. **Composable / View / Adapter의 ViewModel 직접 변경** 여부를 확인한다.
  - Composable 함수 본문 또는 Adapter에서 `viewModel._uiState.value = ...` 같이 private 필드 우회 호출이 있는가(언어상 가능하지 않으나 internal 모듈 공유 시 발생 가능).
  - 위반 시: finding 등록(등급: HIGH).

- [ ] C10. **모듈 경계 위반(Android 멀티 모듈)**을 확인한다.
  - 프로젝트가 멀티 모듈인 경우 `:feature:*` 모듈이 다른 `:feature:*` 모듈을 직접 import하는가(보통 `:core:*` 또는 `:domain:*`을 경유해야 함).
  - `:data:*` 모듈이 `:presentation:*` / `:feature:*`를 import하는가(역방향 의존).
  - 위반 시: finding 등록(등급: HIGH).
  - 단일 모듈이면 "단일 모듈 — 해당 없음" 기록.

- [ ] C11. **Manifest와 클래스 매칭**을 확인한다.
  - `AndroidManifest.xml`에 선언된 Activity/Service/Receiver/Provider가 실제 클래스로 존재하는가.
  - 클래스 패키지 경로가 매니페스트 선언과 일치하는가(hallucination 또는 리팩토링 누락 검출).
  - 위반 시: finding 등록(등급: CRITICAL — 앱 크래시).

- [ ] C12. C1~C11 결과를 `[Android 아키텍처]` 블록으로 묶어 STEP3 본 산출물의 `레이어 확인` / `경계 확인` / `설계 원칙 확인`에 위반 건수 요약을 추가하고 상세는 `REVIEW_STEP3_OUTPUT.md` 형식대로 출력한다.
