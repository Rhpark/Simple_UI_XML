# CI_CD RULES

## CI/CD 워크플로우
 - **1. Android CI** (`android-ci.yml`)
   - Initialize Check
   - KtLint Check
   - Tests(Unit, Robolectric)
   - Build
 - **2. Android CD** (`android-cd.yml`)
   - Release(JitPack 자동 배포)
   - Assemble Apk
   - Firebase App Distribution
 - **3. Documentation** (`documentation.yml`)
   - Dokka API 문서 생성
   - Kover Coverage 리포트 생성

## 트리거 규칙
 - `[release]` 태그가 있는 커밋만 릴리즈 트리거
 - `[release]` 태그가 없으면 Android CI만 수행

## Dokka 문서 업데이트 시점
 - `[release]` 커밋 시 Documentation 워크플로우 자동 실행
 - Dokka HTML 생성 및 GitHub Pages 배포
 - 로컬 확인 명령: `./gradlew dokkaHtml`
 - 출력 위치: `build/dokka/html/`

## 버전 정책 연계
 - 버전 증분/태그 기준은 `docs/rules/project/VERSION_RULE.md`를 따른다.
