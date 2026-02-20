# AGENT

## 역할
 - 이 문서는 프로젝트 규칙 문서의 인덱스입니다.
 - 세부 규칙은 아래 문서를 먼저 확인합니다.

## 사용자와 대화 주의 사항
 - 항상 배려하는 마음으로 존칭을 사용한다.
 - 질문과 답은 무조건 한글로 한다.
 - 문서,코드 변경시(추가, 삭제, 변경)시 무조건 UTF-8 형식 한글로 작성한다.
   - 반드시 글자 깨짐이 있는지 검수 한다.

## 분석 작업 규칙
 - 분석 요청 시 사용자가 명시적으로 제외한 파일 외에는 모두 핵심 파일로 간주하고 직접 읽는다.
 - 불확실한 부분이 있으면 반드시 해당 파일을 직접 읽어 확인한 뒤 결론을 낸다.
 - 코드로 확인 가능한 것을 "불명확하다"고 처리하지 않는다.
 - 분석 범위나 요청 의도가 모호한 경우 시작 전에 먼저 질문하여 방향을 확인한다.

## 서브에이전트(Task) 사용 규칙

 - 분석, 탐색 등 서브에이전트에 위임한 결과는 반드시 직접 핵심 파일을 읽어 교차 검증한다.
 - 검증 없이 서브에이전트 결과를 사용자에게 그대로 전달하지 않는다.
 - 종합 분석처럼 범위가 넓고 정확도가 중요한 작업은 서브에이전트 위임 없이 직접 파일을 읽으며 수행한다.
 - 서브에이전트 사용 시 model: "opus"를 지정하여 정확도를 높인다.

## 첫 실행 시 확인 사항
 - 세션 첫 실행 시 안드로이드 최신 문서를 항상 확인한다.
 - 세션 첫 실행 시 Kotlin 최신 문서를 항상 확인한다.
 - 세션 첫 실행 시, 또는 토큰을 새로 할당 받은 경우 AGENTS.md 및 소개된 자료들을 모두 끝까지 직접 읽는다.(*_RULE.md, AGENTS.md)
 - 첫 응답에 연결되어있는 내용을 읽었는지 결과를 공유한다.

### 수정이 완료되면 README_* 도 수정할 건지 질문하기.



# 작업시 해당 모듈 문서 참조

## 모듈별 AGENTS.md 문서
 - simple_core 모듈 가이드: simple_core/AGENTS.md
 - simple_xml 모듈 가이드: simple_xml/AGENTS.md

## 기본 RULE 문서 위치 
 - docs/rules/*_RULE.md

### 기본 RULE 문서 목록
 - PROJECT RULES: docs/rules/PROJECT_RULE.md
 - DEV ENV RULES: docs/rules/DEV_ENV_RULE.md
 - CODING RULES: docs/rules/CODING_RULE.md
 - TEST RULES: docs/rules/TEST_RULE.md
 - PROCESS RULES: docs/rules/PROCESS_RULE.md
 - PERSONA RULES: docs/rules/PERSONA_RULE.md

## README
 - logcat 관련 : docs/readme/README_LOGX.md
 - permission 관련 : docs/readme/README_PERMISSION.md
 - style 관련 : docs/readme/README_STYLE.md
 - system manager controller 관련 : docs/readme/system_manager/controller/README_SERVICE_MANAGER_CONTROL.md
 - system manager info 관련 : docs/readme/system_manager/info/README_SERVICE_MANAGER_INFO.md
 - recyclerview, adapter 관련 : docs/readme/README_RECYCLERVIEW.md
 - mvvm  관련 : docs/readme/README_MVVM.md
 - 기타 확장 함수 관련 : docs/readme/README_EXTENSIONS.md
 - activity, fragment 관련 : docs/readme/README_ACTIVITY_FRAGMENT.md

## Architecture
 - docs/architecture/DESIGN_PRINCIPLES.md

##  기능 수정/분석 시 참조
 - 기능별 기능별 검토, 분석, 수정시 반드시 확인할 문서 리스트

### simple_core
 - simple_core: simple_core/docs/feature/<기능명>/AGENTS.md
 - simple_core: simple_core/docs/feature/<기능명>/PRD.md
 - simple_core: simple_core/docs/feature/<기능명>/SPEC.md
 - simple_core: simple_core/docs/feature/<기능명>/IMPLEMENTATION_PLAN.md
 - simple_core 기능명 종류는 logcat, permissions, system_manager, viewmodel 등이 있다.

### simple_xml
 - simple_xml: simple_xml/docs/feature/<기능명>/AGENTS.md 
 - simple_xml: simple_xml/docs/feature/<기능명>/PRD.md 
 - simple_xml: simple_xml/docs/feature/<기능명>/SPEC.md 
 - simple_xml: simple_xml/docs/feature/<기능명>/IMPLEMENTATION_PLAN.md
 - simple_xml 기능명 종류는 permissions, system_manager, ui(하위에 adapter, components, layout) 등이 있다.
 - system_manager 하위 예시
   - controller: softkeyboard, systembar, window
   - info: display


### 예를 들어 package kr.open.library.simple_ui.core.logcat의 코드를 분석 개선, 검토 시 추가로 확인 해야 할 문서들
 - PRD.md : simple_core/docs/feature/logcat/PRD.md
 - SPEC.md : simple_core/docs/feature/logcat/SPEC.md
 - IMPLEMENTATION_PLAN.md : simple_core: simple_core/docs/feature/logcat/IMPLEMENTATION_PLAN.md
 - README : docs/readme/README_LOGX.md

### 예를 들어 package kr.open.library.simple_ui.xml.system_manager.controller.systembar의 코드를 분석 개선, 검토 시 추가로 확인 해야 할 문서들
 - AGENTS.md : simple_xml/docs/feature/system_manager/controller/systembar/AGENTS.md
 - PRD.md : simple_xml/docs/feature/system_manager/controller/systembar/PRD.md
 - SPEC.md : simple_xml/docs/feature/system_manager/controller/systembar/SPEC.md
 - IMPLEMENTATION_PLAN.md : simple_xml/docs/feature/system_manager/controller/systembar/IMPLEMENTATION_PLAN.md
 - README : docs/readme/system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md


