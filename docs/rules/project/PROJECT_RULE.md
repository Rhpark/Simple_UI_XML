# PROJECT RULES

## 프로젝트 개요 및 목적
 - **Kotlin 기반 Android 개발 생산성 향상 라이브러리**
 - XML UI와 Jetpack Compose UI를 분리된 모듈로 지원한다.
 - 샘플 앱으로 활용법을 검증함 (settings.gradle.kts, app/build.gradle.kts).
 - 개발 환경/버전 기준은 `docs/rules/project/DEV_ENV_RULE.md` 및 gradle/libs.versions.toml을 따른다.
 - 반복되는 Activity/Fragment/권한/로깅/시스템 서비스 보일러플레이트를 제거하고 생산성을 높이는 것이 1차 목표 (docs/readme/README_START.md, docs/readme/README_ACTIVITY_FRAGMENT.md 등).


# 프로젝트 정의
 - 제품·라이브러리 전체 명칭은 **Simple UI**다.
 - 저장소명 **Simple_UI_XML**은 XML 모듈에서 시작된 레거시 이름이며, 현재 프로젝트 범위를 XML 전용으로 제한하지 않는다.
 - Simple UI는 Android 개발자가 반복되는 플랫폼·UI 보일러플레이트를 줄이고 더 쉽고 빠르게 개발하도록 돕는 종합 라이브러리다.
 - XML UI는 `simple_xml`, Jetpack Compose UI는 `simple_compose`가 담당한다.


# 프로젝트 구조
 - 모듈 분리 구조로 UI 비의존 코어(`simple_core`), XML 전용 UI 레이어(`simple_xml`), Compose 전용 UI 레이어(`simple_compose`), 시스템 제어·정보 기능(`simple_system_manager`)을 제공.
 - 샘플 앱(app)으로 활용법을 검증함 (settings.gradle.kts, app/build.gradle.kts).


## 예제 앱 의존성 검증 모드
 - 기본 모드는 `project(":simple_*")` 의존성을 사용하여 현재 소스를 통합 검증한다.
 - `-PuseMavenArtifacts=true`를 지정하면 Version Catalog의 현재 버전에 해당하는 Maven Central 좌표를 사용한다.
 - Maven 모드는 배포된 AAR/POM의 소비자 호환성을 확인하며, 아직 배포하지 않은 현재 소스 변경은 검증하지 않는다.
 - Maven 소비 빌드 명령: `./gradlew :app:assembleDebug -PuseMavenArtifacts=true`


## 로컬 Maven 단일 모듈 소비 검증
 - `-PlocalConsumerPublication=true`를 지정하면 현재 소스의 AAR/POM을 `build/consumer-maven`에 발행한다.
 - `maven_consumer_smoke`는 이 속성을 지정한 경우에만 포함하며, Core/XML/Compose/System Manager Maven 좌표를 각각 하나씩 사용하는 독립 소비 모듈을 컴파일한다.
 - 로컬 소비 검증은 Maven Central 저장소 구성과 서명을 활성화하지 않으며 외부 저장소에 산출물을 배포하지 않는다.
 - 단일 모듈 소비 검증 명령: `./gradlew :maven_consumer_smoke:consumerSmokeCheck -PlocalConsumerPublication=true`


# 모듈별 상세 가이드
 - **simple_core 모듈**: simple_core/AGENTS.md 참조
 - **simple_xml 모듈**: simple_xml/AGENTS.md 참조
 - **simple_compose 모듈**: simple_compose/AGENTS.md 참조
 - **simple_system_manager 모듈**: simple_system_manager/AGENTS.md 참조
 - 각 모듈별 특화 규칙 및 주의사항은 해당 모듈 AGENTS.md 확인


## 새로운 모듈 추가 절차
 - settings.gradle.kts에 모듈 추가
 - build.gradle.kts 생성 (simple_core, simple_xml 템플릿 참조)
 - namespace 설정 및 의존성 구성
 - 모듈별 AGENTS.md 작성
 - 루트 문서(AGENTS.md 및 RULE 문서)에 모듈 링크 추가
 - publishing 설정 (groupId, artifactId)
 - Dokka 설정 추가
 - Kover 설정 추가
 - test/testRobolectric 태스크 구성


# 프로젝트 가치 제안

## 대폭 보일러플레이트 절감
 - 기본 Activity/Fragment/Adapter/권한/로그/시스템 서비스 래퍼로 표준 흐름만 남기도록 설계 (docs/readme/README_RECYCLERVIEW.md, docs/readme/system_manager/info/README_SERVICE_MANAGER_INFO.md, docs/readme/system_manager/controller/README_SERVICE_MANAGER_CONTROL.md).

## 안정성과 일관성
 - `simple_system_manager`의 BaseSystemService에서 권한을 미리 검증하고 tryCatchSystemManager로 실패를 기본값 처리.
 - @RequiresPermission/@RequiresApi 표기.
 - safeCatch로 예외 안전성 확보.

## 바로 현업에 쓰기 좋은 툴링
 - 로그 파일 저장·필터·DSL, 특수 권한까지 이어받는 PermissionRequester.
 - API 35 대응 시스템 바 처리 등 실기기 이슈 대응 로직을 기본 제공.

## 문서·배포 준비 완료
 - 한/영 병기 KDoc과 세분화된 README,
 - 다중 모듈 Dokka 산출물(docs/api)과 Maven Central 배포를 제공하며, JitPack 좌표는 기존 소비자 호환용으로 유지.

