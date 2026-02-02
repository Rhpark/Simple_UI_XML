# PROJECT RULES

## 프로젝트 개요 및 목적
 - **Kotlin 기반 Android XML 개발 향상 라이브러리**
 - 샘플 앱으로 활용법을 검증함 (settings.gradle.kts, app/build.gradle.kts).
 - 개발 환경/버전 기준은 DEV_ENV_RULE.md 및 gradle/libs.versions.toml을 따른다.
 - 반복되는 Activity/Fragment/권한/로깅/시스템 서비스 보일러플레이트를 제거하고 생산성을 높이는 것이 1차 목표 (docs/readme/README_START.md, docs/readme/README_ACTIVITY_FRAGMENT.md 등).


# 프로젝트 정의
 - **Simple_UI_XML**은 Android XML 사용 개발자들이 개발을 더 쉽고 빠르게 할 수 있도록 도와주는 종합 라이브러리.
 - 추후 Compose 용도 대응 예정


# 프로젝트 구조
 - 모듈 분리 구조로 UI 비의존 코어(simple_core)와 XML 전용 UI 레이어(simple_xml)를 제공.
 - 샘플 앱(app)으로 활용법을 검증함 (settings.gradle.kts, app/build.gradle.kts).


# 모듈별 상세 가이드
 - **simple_core 모듈**: simple_core/AGENTS.md 참조
 - **simple_xml 모듈**: simple_xml/AGENTS.md 참조
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
 - BaseSystemService.kt에서 권한 미리 검증 후 tryCatchSystemManager로 실패를 기본값 처리.
 - @RequiresPermission/@RequiresApi 표기.
 - safeCatch로 예외 안전성 확보.

## 바로 현업에 쓰기 좋은 툴링
 - 로그 파일 저장·필터·DSL, 특수 권한까지 이어받는 PermissionRequester.
 - API 35 대응 시스템 바 처리 등 실기기 이슈 대응 로직을 기본 제공.

## 문서·배포 준비 완료
 - 한/영 병기 KDoc과 세분화된 README,
 - 다중 모듈 Dokka 산출물(docs/api) 및 JitPack 퍼블리싱 스크립트.(Maven 예정)

