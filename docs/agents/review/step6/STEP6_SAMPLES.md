# STEP6 점수 평가 산출물 예시

각 예시는 실제 리뷰 시나리오를 기반으로 한다. STEP6_SCORE.md 본문의 기본 예시와 중복되지 않는 케이스를 다룬다.

---

## 예시 1 — 고품질 코드 (S등급, 플랫폼 API 사용)

**시나리오**: Android Foreground Service + MediaStore API를 사용하는 음악 재생 기능. 테스트 커버리지 충분, finding 없음.

```text
[점수 평가]
현재 단계: 리뷰 - STEP6 점수 평가

관점별 점수:
기능 정확성  : 20/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 0개 / LOW 0개
  호환성     : 감지된 API — ForegroundService(API 26), MediaStore.Audio(API 29) / 체크 결과 — minSDK 26 확인, API 29 분기 처리 존재
  정성 보정  : 없음
  개선 항목  : 없음
로직 & 안정성: 19/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 1개 (-1) / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. null 반환 가능 경로 방어 처리 (PlayerRepository.kt:54) [MEDIUM]
아키텍처     : 20/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 없음
코드 품질    : 15/15
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 없음
테스트       : 10/10
  감점 내역  : 테스트 파일 있음 / 주요 경로 커버됨 / 엣지 케이스 커버됨 / 단언 품질 양호 / 모킹 적절
  정성 보정  : 없음
  개선 항목  : 없음
개발자 편의성: 15/15
  감점 내역  : API 명확성 양호 / 에러 메시지 명확 / 로그 적절 / 문서화 충분
  정성 보정  : 없음
  개선 항목  : 없음

종합 점수    : 99/100
등급         : S

총평         :
  finding이 로직 관점 MEDIUM 1건에 불과하며 모든 관점이 만점 또는 근사치를 달성했다.
  플랫폼 API 호환성 분기 처리가 적절하고 테스트 커버리지가 충분해 즉시 배포 가능한 수준이다.
```

---

## 예시 2 — 주요 개선 필요 (B등급, 테스트 없음 + 다수 HIGH)

**시나리오**: 결제 처리 모듈. 테스트 파일 없음, 예외 처리 미흡, 아키텍처 분리 불완전.

```text
[점수 평가]
현재 단계: 리뷰 - STEP6 점수 평가

관점별 점수:
기능 정확성  : 16/20
  감점 내역  : CRITICAL 0개 / HIGH 1개 (-4) / MEDIUM 0개 / LOW 0개
  호환성     : 해당 없음 (플랫폼 API 미사용)
  정성 보정  : 없음
  개선 항목  : 1. 결제 상태 불일치 수정 (PaymentRepository.kt:41) [HIGH]
로직 & 안정성: 12/20
  감점 내역  : CRITICAL 0개 / HIGH 2개 (-8) / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. 결제 실패 예외 미전파 수정 (PaymentUseCase.kt:72) [HIGH]
               2. 타임아웃 미처리 수정 (PaymentUseCase.kt:89) [HIGH]
아키텍처     : 12/20
  감점 내역  : CRITICAL 0개 / HIGH 2개 (-8) / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. UI 계층 비즈니스 로직 분리 (PaymentFragment.kt:103) [HIGH]
               2. Repository 직접 참조 제거 (PaymentFragment.kt:67) [HIGH]
코드 품질    : 13/15
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 2개 (-2) / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. 중복 검증 로직 통합 (PaymentValidator.kt:28) [MEDIUM]
               2. 매직 넘버 상수화 (PaymentUseCase.kt:94) [MEDIUM]
테스트       : 6/10
  감점 내역  : 테스트 파일 없음 — CRITICAL (-4)
  정성 보정  : 없음
  개선 항목  : 1. 테스트 코드 작성 (PaymentRepository.kt 전체) [CRITICAL]
개발자 편의성: 12/15
  감점 내역  : 에러 메시지 불명확 — HIGH (-3)
  정성 보정  : 없음
  개선 항목  : 1. 에러 메시지 구체화 (PaymentRepository.kt:55) [HIGH]

종합 점수    : 71/100
등급         : B (60~74 — 개선 후 재검토 권고)

총평         :
  로직 & 안정성 관점의 예외 미처리와 아키텍처 관점의 계층 혼재가 핵심 취약점이다.
  테스트 파일 자체가 없어 회귀 안전망이 전무하며, 개선 후 재검토가 필요한 수준이다.
  기능 정확성과 코드 품질은 상대적으로 양호하나 구조 개선이 선행돼야 한다.
```

---

## 예시 3 — 보안 CRITICAL 포함 (B등급, CRITICAL 다수)

**시나리오**: 인증 토큰 관리 모듈. 토큰 평문 저장(CRITICAL), 갱신 로직 경쟁 조건(CRITICAL), 테스트 없음.

```text
[점수 평가]
현재 단계: 리뷰 - STEP6 점수 평가

관점별 점수:
기능 정확성  : 12/20
  감점 내역  : CRITICAL 1개 (-8) / HIGH 0개 / MEDIUM 0개 / LOW 0개
  호환성     : 해당 없음 (플랫폼 API 미사용)
  정성 보정  : 없음
  개선 항목  : 1. 토큰 암호화 저장 전환 (TokenStorage.kt:23) [CRITICAL]
로직 & 안정성: 4/20
  감점 내역  : CRITICAL 1개 (-8) / HIGH 2개 (-8) / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. 갱신 뮤텍스 적용 (TokenRefreshUseCase.kt:48) [CRITICAL]
               2. 갱신 실패 시 재시도 누락 수정 (TokenRefreshUseCase.kt:61) [HIGH]
               3. 만료 토큰 사용 경쟁 조건 해결 (TokenRefreshUseCase.kt:73) [HIGH]
아키텍처     : 16/20
  감점 내역  : CRITICAL 0개 / HIGH 1개 (-4) / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. 토큰 저장 책임 분리 (TokenManager.kt:34) [HIGH]
코드 품질    : 14/15
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 0개 / LOW 1개 (-1)
  정성 보정  : 없음
  개선 항목  : 1. 상수 분산 정리 (TokenStorage.kt:12) [LOW]
테스트       : 6/10
  감점 내역  : 테스트 파일 없음 — CRITICAL (-4)
  정성 보정  : 없음
  개선 항목  : 1. 테스트 코드 작성 (TokenRefreshUseCase 전체) [CRITICAL]
개발자 편의성: 12/15
  감점 내역  : 에러 메시지 불명확 — MEDIUM (-1) / 로그 없음 — MEDIUM (-1) / 문서화 미흡 — MEDIUM (-1)
  정성 보정  : 없음
  개선 항목  : 1. 에러 메시지 구체화 (TokenRefreshUseCase.kt:55) [MEDIUM]
               2. 갱신 흐름 로그 추가 (TokenRefreshUseCase.kt:48) [MEDIUM]
               3. 토큰 관련 함수 문서화 추가 (TokenManager.kt) [MEDIUM]

종합 점수    : 64/100
등급         : B (60~74 — 개선 후 재검토 권고)

총평         :
  토큰 평문 저장은 보안 CRITICAL로 즉시 수정이 필요하며 배포 전 차단 항목이다.
  로직 관점의 갱신 경쟁 조건 역시 데이터 손상을 유발할 수 있어 우선 해결이 필요하다.
  테스트가 없고 에러 처리·문서화가 부실해 안전망 확보와 함께 전반적인 보완이 필요하다.
```

---

## 예시 4 — 정성 보정 적용 (긍정 보정)

**시나리오**: 내부 유틸리티 함수 묶음. finding은 있으나 라이브러리 특성상 테스트 환경 구성이 구조적으로 어렵다.

```text
[점수 평가]
현재 단계: 리뷰 - STEP6 점수 평가

관점별 점수:
기능 정확성  : 18/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 1개 (-2) / LOW 0개
  호환성     : 해당 없음 (플랫폼 API 미사용)
  정성 보정  : 없음
  개선 항목  : 1. 빈 문자열 처리 분기 추가 (StringUtils.kt:18) [MEDIUM]
로직 & 안정성: 18/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 1개 (-2) / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. 입력 검증 로직 방어 처리 (StringUtils.kt:45) [MEDIUM]
아키텍처     : 18/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 1개 (-2) / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. 유틸 함수 범위 과부하 분리 검토 (StringUtils.kt 전체) [MEDIUM]
코드 품질    : 15/15
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 없음
테스트       : 9/10
  감점 내역  : 테스트 파일 있음 / 주요 경로 일부 미검증 — HIGH (-2) / 엣지 케이스 부분 커버
  정성 보정  : +1 — 순수 함수 구조상 런타임 부작용 없음, 누락된 경로의 실질 위험 낮음
  개선 항목  : 1. 경계값 테스트 케이스 추가 (StringUtils.kt:31) [HIGH]
개발자 편의성: 14/15
  감점 내역  : 문서화 미흡 — LOW (-1)
  정성 보정  : 없음
  개선 항목  : 1. 함수 파라미터 문서화 추가 (StringUtils.kt) [LOW]

종합 점수    : 92/100
등급         : S

총평         :
  전반적으로 높은 완성도를 보이며 finding이 모두 MEDIUM 이하 수준이다.
  테스트 커버리지 일부 공백이 있으나 순수 함수 구조 덕분에 실질 위험은 낮고 즉시 배포 가능하다.
```

---

## 예시 5 — 정성 보정 적용 (부정 보정)

**시나리오**: finding 수치는 낮으나 코드 전반 가독성이 낮고 팀 내 컨벤션 위반이 광범위함.

```text
[점수 평가]
현재 단계: 리뷰 - STEP6 점수 평가

관점별 점수:
기능 정확성  : 20/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 0개 / LOW 0개
  호환성     : 해당 없음 (플랫폼 API 미사용)
  정성 보정  : 없음
  개선 항목  : 없음
로직 & 안정성: 20/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 0개 / LOW 0개
  정성 보정  : 없음
  개선 항목  : 없음
아키텍처     : 18/20
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 1개 (-2) / LOW 0개
  정성 보정  : 없음
  개선 항목  : 1. 레이어 간 DTO 변환 누락 수정 (DataRepository.kt:88) [MEDIUM]
코드 품질    : 10/15
  감점 내역  : CRITICAL 0개 / HIGH 0개 / MEDIUM 2개 (-2) / LOW 1개 (-1)
  정성 보정  : -2 — 컨벤션 위반이 단일 파일을 넘어 모듈 전반에 걸쳐 일관성을 해침
  개선 항목  : 1. 컨벤션 일관성 정비 (모듈 전반) [MEDIUM + 정성 보정]
               2. 불필요한 중첩 조건 평탄화 (DataRepository.kt:61) [MEDIUM]
               3. 상수 네이밍 규칙 통일 (Constants.kt) [LOW]
테스트       : 9/10
  감점 내역  : 테스트 파일 있음 / 주요 경로 커버됨 / 엣지 케이스 일부 누락 — LOW (-1)
  정성 보정  : 없음
  개선 항목  : 1. 엣지 케이스 테스트 추가 (DataRepositoryTest.kt) [LOW]
개발자 편의성: 13/15
  감점 내역  : API 명확성 미흡 — MEDIUM (-1) / 문서화 미흡 — MEDIUM (-1)
  정성 보정  : 없음
  개선 항목  : 1. API 파라미터명 명확화 (DataRepository.kt) [MEDIUM]
               2. 문서화 보완 (DataRepository.kt) [MEDIUM]

종합 점수    : 90/100
등급         : S (90~100 — 즉시 배포 가능)

총평         :
  기능·로직 정확성은 완벽하나 코드 품질 관점에서 컨벤션 일관성 문제가 팀 유지보수 비용을 높인다.
  배포 자체는 가능하나 후속 PR에서 컨벤션 정리를 권고한다.
```
