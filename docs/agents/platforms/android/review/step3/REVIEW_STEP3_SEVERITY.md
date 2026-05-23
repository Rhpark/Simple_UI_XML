<!-- 파일 목적: a STEP3(Android 보강) finding의 심각도 판정 기준 -->

### 심각도 기준

#### CRITICAL

- **AndroidManifest.xml 선언과 실제 클래스 경로 불일치**: 앱 시작 또는 컴포넌트 실행 시 `ClassNotFoundException`. 사용자 영향: 크래시.
- **존재하지 않는 Android 아키텍처 컴포넌트 클래스/메서드 참조(hallucination)**: 예 — `androidx.lifecycle.ViewModel`이 아닌 `androidx.lifecycle.AndroidViewModelLegacy`(실제 없음) 상속, `LifecycleScope.launch`(실제 없음, `lifecycleScope`임) 호출.

#### HIGH

- **레이어 건너뜀**: presentation에서 `androidx.room` / `retrofit2` / `okhttp3` / `androidx.datastore` 등 data 계층 라이브러리 직접 import. 사용자 영향: 직접 없음(테스트성·유지보수성 저하).
- **ViewModel이 DAO 또는 Retrofit Service 직접 호출**: Repository 우회로 데이터 계층 책임이 ViewModel에 침투.
- **Repository/DataSource/UseCase에 UI 상태(MutableStateFlow<UiState>) 보유**: data 계층이 presentation 모델을 알게 됨.
- **ViewModel에 비-Application Context 주입(Activity/Fragment Context)**: 누수 위험 동반 — STEP2와 겹치면 STEP2가 대표 STEP, STEP3는 보조 근거.
- **ViewModel 가변 상태 외부 노출**: `MutableStateFlow` / `MutableLiveData` public 또는 backing property 미사용. 외부 모듈이 상태 직접 변경 가능.
- **Composable / View / Adapter가 ViewModel 내부 상태(`_uiState`) 직접 변경**: 단방향 데이터 흐름 위반.
- **멀티 모듈 역방향 의존**: `:data` → `:presentation` import 등.
- **DI 컨테이너를 사용하면서도 ViewModel에서 구현체 직접 생성**: DI의 의미를 상실, 테스트 곤란.

#### MEDIUM

- **Fragment / Activity SRP 위반**: 200줄 이상 + UI 렌더링·비즈니스 로직·데이터 변환 혼재. 동작은 정상.
- **`setOnClickListener` 안에 데이터 가공/네트워크 호출 직접 포함**: SRP 경계 모호.
- **DI 패턴 미식별(C4 미통과)**: DIP 판정 신뢰도 저하.
- **단일 책임 위반이지만 변경 빈도가 낮은 클래스**: 즉시 영향 없음.
- **C12 형식 누락**.

#### LOW

- **인터페이스가 없으나 단일 구현체로 충분한 단순 클래스(Util성)**: 추상화 부재가 실질 영향 없음.
- **DI 모듈이 너무 잘게 쪼개진 경우**: 동작 정상, 가독성 저하.
- **`:core:common`에 사소한 UI 유틸이 섞여 있는 경우**: 모듈 경계 약함.
