# Android 환경 파악 규칙

analysis · review 에이전트가 코드·환경 분석 시 이 파일의 절차를 수행한다.

## 환경 파악 절차

코드·환경 분석 시 아래 순서로 파악한다. 문서 분석만 수행하는 경우 생략 가능.

1. `docs/rules/project/DEV_ENV_RULE.md` 읽기
2. 분석 대상 모듈의 `build.gradle.kts` 에서 minSdk / compileSdk 실제 값 교차 검증
3. 파악한 값을 기준으로 SDK 버전 관련 판단을 한다

완료 후 → `모듈:{모듈명} minSdk:{값} compileSdk:{값}` 한 줄 출력
