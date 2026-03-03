# Step 4. 검증

리팩토링 후에도 동일하게 동작하는가?

## 체크리스트

- [ ] 빌드가 성공하는가?
- [ ] 기존 테스트가 모두 통과하는가?
- [ ] Step 2에서 기록한 동작과 동일한가?
- [ ] 새로운 코드 스멜이 발생하지 않았는가?
- [ ] 의도하지 않은 public API 변경이 없는가? (apiCheck로 자동 검증)

## 빌드/테스트 재실행

```bash
# 전체 빌드
./gradlew build

# 단위 테스트
./gradlew test

# Robolectric 테스트
./gradlew testRobolectric

# KtLint 확인
./gradlew ktlintCheck

# 공개 API 바이너리 호환성 검사 (항상 실행)
./gradlew simple_core:apiCheck simple_xml:apiCheck
```

## 코드 품질 간이 점검

- [ ] SRP: 각 클래스/함수가 단일 책임을 가지는가?
- [ ] 중복 제거: 리팩토링 목표를 달성했는가?
- [ ] 가독성: 변경 후 코드가 더 명확한가?

> 상세 코드 리뷰가 필요하다면 /CodeReview 스킬 사용

## 심각도 기준

- CRITICAL: 테스트 실패 상태로 완료 처리
- HIGH: Step 2 동작과 다른 결과 발생
- MEDIUM: 새로운 코드 스멜 발생 미보고
