<!-- 파일 목적: a STEP2(Android 보강) finding의 심각도 판정 기준 — CODE_LIFE_CYCLE.md SSOT 사용 -->

### 심각도 기준

#### CRITICAL

- **GlobalScope 사용**: CODE_LIFE_CYCLE.md 명시. 어떤 컨텍스트에서든 발견 시 CRITICAL.
- **Activity / Fragment / View / non-Application Context를 싱글톤 또는 장기 보유 객체에 저장**: CODE_LIFE_CYCLE.md 「메모리 누수 방지」 위반. 메모리 누수.
- **CancellationException을 빈 catch 또는 로그-only로 삼킴(rethrow 없음) + 코루틴 누수로 확인됨**: 라이프사이클 종료가 전파되지 않아 백그라운드 작업 누적. CODE_LIFE_CYCLE.md 「코루틴 취소 처리」 위반.
- **존재하지 않는 라이프사이클 API/필드 호출(hallucination)**: 예 — `Fragment.onViewDestroyed()`(실제 없음, `onDestroyView`임), `Lifecycle.State.ON_START`(실제 없음, `Lifecycle.Event.ON_START`임).

#### HIGH

- **ViewModel에서 viewModelScope 외 launch/async**: CODE_LIFE_CYCLE.md 위반.
- **Fragment 뷰 관찰에서 viewLifecycleOwner 미사용 (`lifecycleScope` 직접 사용)**: CODE_LIFE_CYCLE.md 명시(HIGH).
- **Activity/Fragment에서 Flow 수집 시 repeatOnLifecycle/flowWithLifecycle 미사용**: 백그라운드에서 UI 갱신 가능.
- **CancellationException rethrow 누락(코루틴 누수 가능성)**: CRITICAL 조건이 명백히 입증되지 않은 경우.
- **Fragment에서 onDestroyView에 `_binding = null` 처리 누락**: ViewBinding 메모리 누수.
- **LiveData setValue가 메인 스레드 외에서 호출됨**: IllegalStateException 크래시.
- **명시적으로 생성한 Job/CoroutineScope가 라이프사이클 종료 시 취소되지 않음**: 코루틴 누수.
- **BroadcastReceiver registerReceiver가 있고 unregisterReceiver 누락**: 리소스 누수.
- **bindService가 있고 unbindService 누락**: ServiceConnection 누수.

#### MEDIUM

- **SupervisorJob 없이 자식 코루틴이 형제를 취소하는 구조**: 의도된 격리가 깨질 위험이 있으나 라이프사이클 자체는 정상.
- **`runCatching` 블록 안에서 `CancellationException`이 rethrow되지 않음**: 일반 catch보다 발견이 어려움. 누수 위험이 입증되지 않으면 MEDIUM.
- **B11 형식 누락**: 본 보강 산출물 형식 불일치.

#### LOW

- **deprecated된 라이프사이클 API(`Fragment.setUserVisibleHint`, `LiveData.observeForever`) 사용이지만 즉시 누수로 이어지지 않음**: 향후 보강 필요.
- **불필요한 SupervisorJob 사용**: 동작은 정상이나 의미 없는 보호.
