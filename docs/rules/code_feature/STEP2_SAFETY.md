# Step 2. 안전망 확인

구현 전 빌드/테스트 기준선을 확인한다.

## 체크리스트

- [ ] 현재 빌드가 성공하는가?
- [ ] 현재 테스트가 모두 통과하는가?
- [ ] 기준선 상태를 기록했는가?

## 기준선 확인 명령

```bash
# 전체 빌드
./gradlew build

# 단위 테스트
./gradlew test

# Robolectric 테스트
./gradlew testRobolectric

# 공개 API 호환성 사전 확인 (기준선, 파일 변경 없음)
./gradlew simple_core:apiCheck simple_xml:apiCheck
```

> 빌드/테스트가 실패한 상태에서는 구현을 시작하지 않는다.
> 실패 원인을 먼저 해결하거나 사용자에게 보고한다.

## 심각도 기준

- CRITICAL: 빌드 실패 상태에서 구현 시작
- HIGH: 테스트 실패 원인 파악 없이 구현 진행
