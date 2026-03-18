# AGENT

## 역할
 - 이 문서는 프로젝트 규칙 문서의 인덱스입니다.
 - 세부 규칙은 아래 문서를 먼저 확인합니다.

## 사용자와 대화 주의 사항
 - 항상 배려하는 마음을 갖고 존칭을 사용한다.
 - 질문과 답은 무조건 한글로 한다.
 - 문서,코드 변경시(추가, 삭제, 변경)시 무조건 UTF-8 형식 한글로 작성한다.
   - 반드시 글자 깨짐이 있는지 검수 한다.

## 분석 작업 규칙
 - 분석 요청 시 사용자가 명시적으로 제외한 파일 외에는 모두 핵심 파일로 간주하고 직접 읽는다.
 - 불확실한 부분이 있으면 반드시 해당 파일을 직접 읽어 확인한 뒤 결론을 낸다.
 - 코드로 확인 가능한 것을 "불명확하다"고 처리하지 않는다.
 - 분석 범위나 요청 의도가 모호한 경우 시작 전에 먼저 질문하여 방향을 확인한다.
 - 코드/문서 수정은 검토 결과를 먼저 보고하고 사용자 승인을 받은 후 실행한다.

## SubAgent 사용 규칙
 - 분석, 탐색 등 서브에이전트에 위임한 결과는 반드시 직접 핵심 파일을 읽어 교차 검증한다.
 - 검증 없이 서브에이전트 결과를 사용자에게 그대로 전달하지 않는다.
 - 종합 분석처럼 범위가 넓고 정확도가 중요한 작업은 서브에이전트 위임 없이 직접 파일을 읽으며 수행한다.
 - 서브에이전트 사용 시 model: "opus"를 지정하여 정확도를 높인다.
 - 서브에이전트 프롬프트에 정보를 직접 명시하더라도, 에이전트 내부에서 반드시 관련 파일(DEV_ENV_RULE.md 등)을 직접 읽어야 한다. 프롬프트 정보만으로 파일 읽기를 대체할 수 없다.

## 첫 실행 시 확인 사항
 - 세션 첫 실행 시 안드로이드 최신 문서를 항상 확인한다.
 - 세션 첫 실행 시 Kotlin 최신 문서를 항상 확인한다.
 - 세션 첫 실행 시, 또는 토큰을 새로 할당 받은 경우 AGENTS.md 및 여기에 소개된 자료들을 모두 끝까지 직접 읽는다.
 - 첫 응답에 연결되어있는 내용을 읽었는지 결과를 공유한다.


# 작업시 해당 모듈 문서 참조

## 모듈별 AGENTS.md 문서
 - simple_core 모듈 가이드: simple_core/AGENTS.md
 - simple_system_manager 모듈 가이드: simple_system_manager/AGENTS.md
 - simple_xml 모듈 가이드: simple_xml/AGENTS.md

## 코딩 규칙 문서 위치 (코드 작성 시 항상 준수)
 - 인덱스: docs/rules/CODING_RULE_INDEX.md
 - docs/rules/coding_rule/*.md

### 코딩 규칙 문서 목록
 - 명명 규칙: docs/rules/coding_rule/CODE_NAMING_RULE.md
 - 아키텍처: docs/rules/coding_rule/CODE_ARCHITECTURE.md
 - 라이프사이클: docs/rules/coding_rule/CODE_LIFE_CYCLE.md
 - 코드 패턴 인덱스: docs/rules/coding_rule/CODE_PATTERNS_INDEX.md
 - 코드 패턴 상세: docs/rules/coding_rule/patterns/*.md

## 사용 가능한 Skills (슬래시 명령으로 호출)
 - /CodeReview  : 코드 리뷰 (절차 문서는 Skill 내부 참조)
 - /Refactor    : 코드 리팩토링 (절차 문서는 Skill 내부 참조)
 - /TestCode    : 테스트 코드 작성 (Unit / Robolectric 자동 판단)
 - /KDoc        : KDoc 주석 작성 (한·영 병기)
 - /Readme      : README 문서 작성
 - /Planning    : 기능 개발 계획 (PRD/SPEC/IMPLEMENTATION_PLAN 초안)

## 프로젝트/환경/배포 규칙 문서 위치
 - 인덱스: docs/rules/PROJECT_RULE_INDEX.md
 - PROJECT RULE: docs/rules/project/PROJECT_RULE.md
 - DEV ENV RULE: docs/rules/project/DEV_ENV_RULE.md
 - VERSION RULE: docs/rules/project/VERSION_RULE.md
 - CI/CD RULE: docs/rules/project/CI_CD_RULE.md

## 에이전트 행동 규칙 문서 위치
 - PROCESS RULE: docs/rules/PROCESS_RULE.md
 - PERSONA RULE: docs/rules/PERSONA_RULE.md

## README
 - README 파일 목록: docs/readme/ 하위 참조

## Architecture
 - docs/architecture/DESIGN_PRINCIPLES.md

## 기능 수정/분석 시 참조
 - 기능별 기능별 검토, 분석, 수정시 반드시 확인할 문서 리스트

### simple_core
 - simple_core: simple_core/docs/feature/<기능명>/AGENTS.md
 - simple_core: simple_core/docs/feature/<기능명>/PRD.md
 - simple_core: simple_core/docs/feature/<기능명>/SPEC.md
 - simple_core: simple_core/docs/feature/<기능명>/IMPLEMENTATION_PLAN.md
 - simple_core 기능명 종류는 logcat, permissions, viewmodel 등이 있다.

### simple_xml
 - simple_xml: simple_xml/docs/feature/<기능명>/AGENTS.md 
 - simple_xml: simple_xml/docs/feature/<기능명>/PRD.md 
 - simple_xml: simple_xml/docs/feature/<기능명>/SPEC.md 
 - simple_xml: simple_xml/docs/feature/<기능명>/IMPLEMENTATION_PLAN.md
 - simple_xml 기능명 종류는 permissions, ui(하위에 adapter, components, layout) 등이 있다.

### simple_system_manager
 - simple_system_manager: simple_system_manager/docs/feature/system_manager/<영역명>/<기능명>/AGENTS.md
 - simple_system_manager: simple_system_manager/docs/feature/system_manager/<영역명>/<기능명>/PRD.md
 - simple_system_manager: simple_system_manager/docs/feature/system_manager/<영역명>/<기능명>/SPEC.md
 - simple_system_manager: simple_system_manager/docs/feature/system_manager/<영역명>/<기능명>/IMPLEMENTATION_PLAN.md
 - system_manager 하위 예시
   - controller: alarm, notification, vibrator, wifi, softkeyboard, systembar, window
   - info: battery, location, network, telephony, sim, display


### 예를 들어 package kr.open.library.simple_ui.core.logcat의 코드를 분석 개선, 검토 시 추가로 확인 해야 할 문서들
 - PRD.md : simple_core/docs/feature/logcat/PRD.md
 - SPEC.md : simple_core/docs/feature/logcat/SPEC.md
 - IMPLEMENTATION_PLAN.md : simple_core: simple_core/docs/feature/logcat/IMPLEMENTATION_PLAN.md
 - README : docs/readme/README_LOGX.md

### 예를 들어 package kr.open.library.simple_ui.xml.system_manager.controller.systembar의 코드를 분석 개선, 검토 시 추가로 확인 해야 할 문서들
 - AGENTS.md : simple_system_manager/docs/feature/system_manager/controller/systembar/AGENTS.md
 - PRD.md : simple_system_manager/docs/feature/system_manager/controller/systembar/PRD.md
 - SPEC.md : simple_system_manager/docs/feature/system_manager/controller/systembar/SPEC.md
 - IMPLEMENTATION_PLAN.md : simple_system_manager/docs/feature/system_manager/controller/systembar/IMPLEMENTATION_PLAN.md
 - README : docs/readme/system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md


