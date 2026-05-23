<!-- 파일 목적: REVIEW_STEP2_CHECKLIST.md(Android 보강) 항목이 모두 수행됐는지 검증하는 통과 기준 -->

## 하네스 (통과 기준)

- [ ] B1번 통과 — GlobalScope 전수 검색 결과가 `[Android 라이프사이클] GlobalScope`에 기록됐다.
     기록 값: [발견 위치 목록 또는 "없음 (확인 완료)" — 비어있으면 미통과]
     미통과 시: B1 재수행. `[분석 입력] 직접 파일` 전수에서 `GlobalScope` 문자열 재검색.
     심각도: 발견 자체는 CRITICAL. 누락 검증은 HIGH(`CODE_LIFE_CYCLE.md` SSOT 위반 가능성).

- [ ] B2번 통과 — ViewModel 내부 launch/async가 viewModelScope를 사용하는지 확인됐다.
     기록 값: [확인 결과 또는 "ViewModel 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: B2 재수행. ViewModel 클래스 상속 여부 + `launch` 호출부 컨텍스트 재확인.
     심각도: 위반 시 HIGH(CODE_LIFE_CYCLE.md 위반). 직접 GlobalScope 사용이면 CRITICAL로 격상.

- [ ] B3번 통과 — Activity/Fragment 내부 lifecycleScope 사용 적합성이 확인됐다(Fragment의 경우 viewLifecycleOwner.lifecycleScope 사용 확인 포함).
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: B3 재수행. Fragment에서 `lifecycleScope` 직접 사용 위치 재확인.
     심각도: Fragment에서 viewLifecycleOwner 미사용은 HIGH(CODE_LIFE_CYCLE.md 명시).

- [ ] B4번 통과 — Flow 수집의 repeatOnLifecycle / flowWithLifecycle 적용 여부가 확인됐다.
     기록 값: [확인 결과 또는 "Flow 수집 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: B4 재수행. `.collect` 호출부와 그 상위 launch 블록을 함께 확인.
     심각도: 미적용은 HIGH(백그라운드에서 UI 갱신 위험).

- [ ] B5번 통과 — CancellationException rethrow 여부가 모든 catch 블록에 대해 확인됐다.
     기록 값: [catch 블록별 처리 결과 또는 "catch 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: B5 재수행. `catch (e:` 검색으로 모든 catch 위치 재확인 후 본문 검토.
     심각도: 미rethrow는 HIGH(CODE_LIFE_CYCLE.md 명시). 명백한 빈 catch + Cancellation 삼킴은 CRITICAL.

- [ ] B6번 통과 — Context/Activity/View 장기 보유 검사가 완료됐다.
     기록 값: [발견 위치 목록 또는 "없음 (확인 완료)" — 비어있으면 미통과]
     미통과 시: B6 재수행. 싱글톤(`object`, `companion object`)과 Repository/UseCase 필드 재확인.
     심각도: 발견은 CRITICAL(메모리 누수, CODE_LIFE_CYCLE.md 명시).

- [ ] B7번 통과 — Fragment 뷰 바인딩 정리(onDestroyView에서 _binding = null) 여부가 확인됐다.
     기록 값: [확인 결과 또는 "Fragment 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: B7 재수행. Fragment 클래스마다 `onDestroyView` 존재와 binding null 처리 재확인.
     심각도: 누락은 HIGH(메모리 누수, CODE_LIFE_CYCLE.md 메모리 누수 절).

- [ ] B8번 통과 — 직접 생성한 Job/CoroutineScope의 취소 처리 여부가 확인됐다.
     기록 값: [확인 결과 또는 "직접 생성 없음 — 해당 없음" — 비어있으면 미통과]
     미통과 시: B8 재수행. `Job()` / `SupervisorJob()` / `CoroutineScope(...)` 직접 호출 위치 재확인.
     심각도: 취소 누락은 HIGH 또는 CRITICAL(누수 범위에 따라).

- [ ] B9번 통과 — LiveData/StateFlow의 메인 스레드/생명주기 위반 여부가 확인됐다.
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: B9 재수행. `setValue` / `value =` 호출부의 스레드 컨텍스트 재확인.
     심각도: 메인 스레드 외 setValue는 HIGH(IllegalStateException).

- [ ] B10번 통과 — BroadcastReceiver/Service 등록·해제 짝이 확인됐다.
     기록 값: [확인 결과 또는 "해당 없음" — 비어있으면 미통과]
     미통과 시: B10 재수행. register/bind 호출과 unregister/unbind 호출의 라이프사이클 짝 재확인.
     심각도: 누락은 HIGH(리소스 누수).

- [ ] B11번 통과 — `[Android 라이프사이클]` 블록이 `REVIEW_STEP2_OUTPUT.md` 형식대로 출력됐고 STEP2 본 산출물 `런타임 확인`에 요약 1줄이 추가됐다.
     기록 값: [요약 1줄 — 비어있으면 미통과]
     미통과 시: B11 재수행. `REVIEW_STEP2_OUTPUT.md` 예시 형식으로 재작성.
     심각도: 형식 불일치는 MEDIUM.
