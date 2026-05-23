<!-- 파일 목적: a STEP2(로직 & 안정성)에서 Android 플랫폼 추가 체크 항목 — CODE_LIFE_CYCLE.md SSOT 사용 -->

## 실행 체크리스트

- [ ] B1. **GlobalScope 사용 여부**를 전수 검색한다.
  - `[분석 입력] 직접 파일`에서 `GlobalScope.launch` / `GlobalScope.async` / `GlobalScope.actor` 호출을 찾는다.
  - 발견 시: CODE_LIFE_CYCLE.md 규칙 위반 → finding으로 기록(등급: CRITICAL).
  - 미발견 시: `[Android 라이프사이클] GlobalScope`에 "없음 (확인 완료)" 기록.

- [ ] B2. **ViewModel 내부 코루틴 스코프 적합성**을 확인한다.
  - 호출부 클래스가 `ViewModel` 또는 그 하위 클래스인지 확인.
  - `launch { ... }` / `async { ... }`가 `viewModelScope.` 접두사 없이 호출됐는지 확인.
  - 위반 시: finding으로 기록(등급: HIGH 또는 CRITICAL — `REVIEW_STEP2_SEVERITY.md` 참조).
  - 정상이면 `[Android 라이프사이클] ViewModel 스코프`에 "적합 (viewModelScope 사용 확인)" 기록.

- [ ] B3. **Activity / Fragment 내부 코루틴 스코프 적합성**을 확인한다.
  - 호출부가 `Activity` 또는 `Fragment` 하위 클래스인지 확인.
  - `lifecycleScope` 사용 여부 확인.
  - `Fragment`인 경우 **`onViewCreated` 이후의 뷰 갱신을 포함하는 launch는 `viewLifecycleOwner.lifecycleScope`인지** 추가 확인.
  - 위반 시: finding으로 기록(Fragment에서 `lifecycleScope` 직접 사용 = HIGH).
  - 정상이면 `[Android 라이프사이클] Activity/Fragment 스코프`에 "적합" 기록.

- [ ] B4. **Flow 수집의 생명주기 정합성**을 확인한다.
  - `flow.collect { ... }` 호출이 `repeatOnLifecycle(STARTED)` 또는 `flowWithLifecycle(...)` 블록 안에 있는가.
  - 또는 `viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { collect { ... } } }` 형태인가.
  - Activity/Fragment에서 위 패턴이 없는 `collect`는 백그라운드에서 UI를 갱신할 가능성 — finding 등록.
  - 정상이면 `[Android 라이프사이클] Flow 수집`에 "적합" 기록.

- [ ] B5. **CancellationException 처리**를 확인한다.
  - `try { ... } catch (e: Exception) { ... }` 또는 `catch (e: Throwable) { ... }` 블록 안에 `CancellationException` rethrow 또는 `if (e is CancellationException) throw e` 코드가 있는가.
  - 없으면 코루틴 취소가 catch에 의해 삼켜져 부모 Job이 취소되지 않는 위험 — finding 등록.
  - `runCatching { ... }`도 같은 위험을 가지므로 동일 기준 적용.
  - 정상이면 `[Android 라이프사이클] 취소 처리`에 "적합" 기록.

- [ ] B6. **Context / Activity / View 장기 보유**를 확인한다.
  - `companion object` 또는 `object` 안에 `Context` / `Activity` / `Fragment` / `View` 타입 필드가 있는가.
  - `Repository` / `ViewModel` / `UseCase` 클래스 필드에 `Activity` / `Fragment` / `View` 타입이 저장되는가.
  - `ApplicationContext` 외 `Context`가 싱글톤에 저장되는가.
  - 위반 시: finding 등록(등급: CRITICAL — 메모리 누수).

- [ ] B7. **Fragment 뷰 바인딩 정리**를 확인한다.
  - `Fragment`에서 `_binding` / `binding` 패턴을 사용하는 경우 `onDestroyView()`에서 `_binding = null` 처리가 있는가.
  - `onDestroyView`가 override되지 않았거나 `null` 할당이 누락되면 finding 등록(등급: HIGH — `CODE_LIFE_CYCLE.md` 메모리 누수 절).
  - Compose 또는 ViewBinding 미사용 Fragment는 "해당 없음".

- [ ] B8. **Job 취소 전파**를 확인한다.
  - `Job()` 또는 `SupervisorJob()`을 명시적으로 생성한 코드가 있다면 해당 Job의 `cancel()`이 라이프사이클 종료 시점에 호출되는가.
  - `CoroutineScope(Job())`를 직접 생성한 곳은 라이프사이클과 무관하므로 누수 위험 — finding 등록.
  - 정상이면 `[Android 라이프사이클] Job 취소`에 "적합" 기록.

- [ ] B9. **LiveData / StateFlow 백그라운드 emit**을 확인한다.
  - `setValue` / `value = ...`이 메인 스레드 외에서 호출되는가(LiveData `setValue`는 메인 스레드 전용 — `IllegalStateException`).
  - `StateFlow.value = ...` 또는 `MutableStateFlow.update { ... }`가 onDestroyView 이후 호출 경로에 있는가.
  - 위반 시: finding 등록.

- [ ] B10. **BroadcastReceiver / Service 등록·해제 짝**을 확인한다.
  - `registerReceiver` / `bindService`가 있다면 동일 라이프사이클 메서드 쌍에 `unregisterReceiver` / `unbindService`가 있는가.
  - 누락 시 finding 등록(등급: HIGH — 리소스 누수).

- [ ] B11. B1~B10 결과를 `[Android 라이프사이클]` 블록으로 묶어 STEP2 본 산출물 `런타임 확인`에 1줄 요약을 추가하고 상세는 `REVIEW_STEP2_OUTPUT.md` 형식대로 출력한다.
