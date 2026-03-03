# Step 4. 검증

구현 후 빌드/테스트/API 호환성을 검증한다.

## 체크리스트

- [ ] 빌드가 성공하는가?
- [ ] 기존 테스트가 모두 통과하는가?
- [ ] 변경된 기능(추가/개선/제거)의 검증 테스트가 통과하는가?
- [ ] 의도하지 않은 public API 변경이 없는가? (apiCheck로 자동 검증)
- [ ] 개선/제거의 경우, 기존 호출부에 영향이 없는가?

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

# 공개 API 바이너리 호환성 검사
./gradlew simple_core:apiCheck simple_xml:apiCheck
```

> 추가/개선으로 공개 API가 변경된 경우:
> `./gradlew simple_core:apiDump simple_xml:apiDump` 로 api 파일 갱신 후 커밋

## 코드 품질 간이 점검

- [ ] SRP: 새로 추가한 클래스/함수가 단일 책임을 가지는가?
- [ ] 가독성: 코드가 명확한가?
- [ ] KDoc: 공개 API에 주석이 작성되었는가?

> 상세 코드 리뷰가 필요하다면 agent-review 사용

## 심각도 기준

- CRITICAL: 테스트 실패 상태로 완료 처리
- HIGH: apiCheck 실패 미확인
- MEDIUM: 새 공개 API에 KDoc 미작성
