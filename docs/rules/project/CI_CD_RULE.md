# CI_CD RULES

## CI/CD 워크플로우
 - **1. Android CI** (`android-ci.yml`)
   - Initialize Check
   - KtLint Check
   - Tests(Unit, Robolectric)
   - Maven Consumer Check (`[release]` 또는 수동 실행)
   - Build
 - **2. Android CD** (`android-cd.yml`)
   - Release(GitHub Release 생성)
   - Publish Maven Central
   - Assemble Apk
   - Firebase App Distribution
 - **3. Documentation** (`documentation.yml`)
   - Dokka API 문서 생성
   - Kover Coverage 리포트 생성

## 트리거 규칙
 - `[release]` 태그가 있는 커밋만 릴리즈 트리거
 - `[release]` 태그가 없으면 Android CI만 수행
 - 수동 실행은 Android CI에서만 지원한다.
 - Android CD는 성공한 Android CI의 `workflow_run`으로만 실행한다.
 - Documentation은 성공한 Android CD의 `workflow_run`으로만 실행한다.

## 릴리즈 소스 고정 규칙
 - Android CI는 릴리즈 메타데이터에 검증한 커밋 SHA를 기록한다.
 - Android CD는 메타데이터 SHA와 `workflow_run.head_sha`가 일치할 때만 릴리즈를 진행한다.
 - Release, Maven Central, 검증 APK, Firebase 배포는 모두 동일한 검증 SHA를 checkout한다.
 - Git Tag는 기본 브랜치의 최신 HEAD가 아니라 검증 SHA에 직접 생성한다.
 - 릴리즈 메타데이터는 `push`로 시작된 CI 실행에서만 승인한다.

## Gradle 캐시 규칙
 - Gradle 캐시는 `gradle/actions/setup-gradle`에서만 관리한다.
 - `actions/setup-java`의 `cache: gradle`을 함께 사용하지 않는다.
 - 캐시 공급자는 공개형 GitHub Actions 캐시를 사용하는 `basic`으로 고정한다.

## 권한 규칙
 - 각 워크플로의 기본 권한은 비활성화한다.
 - 각 Job은 수행에 필요한 최소 권한만 명시한다.
 - 다른 워크플로의 Artifact를 조회하는 Job에는 `actions: read`만 부여한다.

## Maven 소비자 검증 규칙
 - 일반 Push와 Pull Request에서는 Maven 소비자 검증을 생략하고 로컬 검증을 사용한다.
 - `[release]` 커밋 또는 Android CI 수동 실행에서는 단위 테스트 성공 후 Maven 소비자 검증을 수행한다.
 - 현재 소스의 네 라이브러리 모듈을 로컬 전용 Maven 저장소에 발행한다.
 - 각 소비자 모듈은 `core`, `system-manager`, `xml`, `compose` 중 하나의 Maven 좌표만 직접 의존한다.
 - 릴리스 실행에서는 네 소비자 모듈이 모두 컴파일되어야 Build 단계로 진행한다.
 - Maven 소비자 Job이 생략된 일반 실행에서는 단위 테스트 성공 후 Build 단계로 진행한다.
 - 로컬 검증에서는 Maven Central 배포와 서명을 수행하지 않는다.
 - 검증 명령: `./gradlew :maven_consumer_smoke:consumerSmokeCheck -PlocalConsumerPublication=true`

## Dokka 문서 업데이트 시점
 - `[release]` 커밋 시 Documentation 워크플로우 자동 실행
 - Dokka HTML 생성 및 GitHub Pages 배포
 - 로컬 전체 문서 확인 명령: `./gradlew dokkaHtmlMultiModuleCustom`
 - 모듈별 확인 명령: `./gradlew :<module>:dokkaGenerateHtml`
 - 모듈별 출력 위치: `<module>/build/dokka/html/`
 - 통합 복사 위치: `docs/api/`

## 버전 정책 연계
 - 버전 증분/태그 기준은 `docs/rules/project/VERSION_RULE.md`를 따른다.
