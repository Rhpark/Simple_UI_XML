# AGENT

## 역할
 - 이 문서는 프로젝트 규칙 문서의 인덱스입니다.
 - 세부 규칙은 아래 문서를 먼저 확인합니다.

## 사용자와 대화 주의 사항
 - 항상 배려하는 마음을 갖고 존칭을 사용한다.
 - 질문과 답은 무조건 한글로 한다.
 - 문서, 코드 변경시(추가, 삭제, 변경)시 무조건 UTF-8 형식 한글로 작성한다.
   - 반드시 글자 깨짐이 있는지 검수 한다.

## 분석 작업 규칙
 - 분석 요청 시 사용자가 명시적으로 제외한 파일 외에는 모두 핵심 파일로 간주하고 직접 읽는다.
 - 불확실한 부분이 있으면 반드시 해당 파일을 직접 읽어 확인한 뒤 결론을 낸다.
 - 코드로 확인 가능한 것을 "불명확하다"고 처리하지 않는다.
 - 분석 범위나 요청 의도가 모호한 경우 시작 전에 먼저 질문하여 방향을 확인한다.
 - 코드/문서 수정은 검토 결과를 먼저 보고하고 사용자 승인을 받은 후 실행한다.

## 첫 실행 시 확인 사항
 - docs/rules/project/DEV_ENV_RULE.md 를 읽어 현재 compileSdk, minSdk, Kotlin 버전을 확인한다.
 - 확인한 버전 정보를 첫 응답에 포함한다.
 - 버전이 변경되어 있다면 사용자에게 알린다.

## 모듈 감지 및 AGENTS.md 읽기 규칙
 - 요청을 받으면 메인 에이전트가 먼저 관련 모듈을 판단하고 해당 모듈의 AGENTS.md를 직접 읽는다.
 - 모듈 판단 기준:
   1. 요청에 모듈명이 명시된 경우 → 해당 모듈 AGENTS.md 읽기
   2. 파일 경로가 주어진 경우 → 경로에서 모듈 추론 후 읽기
   3. 클래스/기능명만 있는 경우 → Grep/Glob으로 파일 위치 확인 후 모듈 판단
   4. 모듈이 불명확한 경우 → 사용자에게 먼저 확인
 - 위임(SubAgent) 시에도 해당 모듈 AGENTS.md 경로를 프롬프트에 명시하고 읽도록 지시한다.

## 판단과 수행 분리 원칙
 - 메인 에이전트는 판단자다.
   - 관련 AGENTS.md를 읽고 규칙을 파악한다.
   - 작업 범위를 판단하여 직접 수행 또는 위임을 결정한다.
   - 위임 결과를 교차 검증한다.
 - 수행 주체는 판단에 따라 결정된다.
   - 단일 파일 / 단일 클래스 / 간단한 질문 → 메인 에이전트가 직접 수행
   - 패키지 / 흐름 / 다중 파일 분석 → 위임(SubAgent)하여 수행

## 위임 작업 규칙
 - 범위가 넓고 정확도가 중요한 종합 분석은 위임하지 않고 직접 파일을 읽으며 수행한다.
 - 위임한 결과는 반드시 핵심 파일을 직접 읽어 교차 검증한다.
 - 검증 없이 위임 결과를 사용자에게 그대로 전달하지 않는다.
 - 위임 시 프롬프트에 아래 4가지를 반드시 명시한다.
   1. 목적: 무엇을 알아내야 하는가
   2. 범위: 반드시 읽어야 할 파일/패키지 경로 (해당 모듈 AGENTS.md 포함)
   3. 출력 형식: 어떤 구조로 보고할 것인가
   4. 금지 사항: 코드 수정 불가, 추정으로 결론 내리지 않기 등
 - 위임 프롬프트에 정보를 직접 명시하더라도, 내부에서 반드시 관련 파일을 직접 읽어야 한다.
   프롬프트 정보만으로 파일 읽기를 대체할 수 없다.

## 작업 도구 (Skills)
 - 아래 작업은 전용 절차(Skill)가 있다. 요청에 맞는 Skill을 활용한다.
 - 코드 리뷰         : 5단계(기능→로직→아키텍처→품질→명명) 순서로 점검, 100점 평가
 - 테스트 코드 작성  : Unit / Robolectric 유형 판단 후 작성
 - KDoc 주석 작성    : 한·영 병기 규칙 준수
 - 정적 분석 실행    : ktlintCheck → lintDebug → apiDump 순서
 - 파일 세트 생성    : Activity / Fragment / DialogFragment / Adapter / Layout
 - JSON 변환         : JSON → Kotlin Data Class, 직렬화 라이브러리 자동 감지
 - XML 레이아웃 점검 : Style · 명명 · 하드코딩 · 성능 · 접근성 검사

### Skills 실행 방법 (Claude Code 전용)
 - 아래 슬래시 명령으로 전용 절차(Skill)를 실행한다
 - /CodeReview    : 코드 리뷰
 - /TestCode      : 테스트 코드 작성
 - /KDoc          : KDoc 주석 작성
 - /CheckQuality  : 정적 분석 일괄 실행
 - /Create        : Activity / Fragment / DialogFragment / Adapter / Layout 생성
 - /JsonConvert   : JSON → Kotlin Data Class 변환
 - /XmlInspector  : XML 레이아웃 점검


# 작업시 해당 모듈 문서 참조

## 모듈별 AGENTS.md 문서
 - simple_core 모듈 가이드: simple_core/AGENTS.md
 - simple_system_manager 모듈 가이드: simple_system_manager/AGENTS.md
 - simple_xml 모듈 가이드: simple_xml/AGENTS.md

## 코딩 규칙 문서 위치
 - 코딩 규칙은 각 작업 에이전트(agent-feature, agent-refactor)가 직접 참조한다.
 - 인덱스: docs/rules/CODING_RULE_INDEX.md

## 프로젝트/환경/배포 규칙 문서 위치
 - 인덱스: docs/rules/PROJECT_RULE_INDEX.md
 - PROJECT RULE: docs/rules/project/PROJECT_RULE.md
 - DEV ENV RULE: docs/rules/project/DEV_ENV_RULE.md
 - VERSION RULE: docs/rules/project/VERSION_RULE.md
 - CI/CD RULE: docs/rules/project/CI_CD_RULE.md

## 행동/페르소나 규칙 문서 위치
 - PROCESS RULE: docs/rules/PROCESS_RULE.md
 - PERSONA RULE: docs/rules/PERSONA_RULE.md

## 참고 문서
 - README 목록: docs/readme/ 하위 참조

## 기능 수정/분석 시 참조
 - 기능별 검토, 분석, 수정 시 해당 모듈의 AGENTS.md를 먼저 확인한다.
 - 기능별 세부 경로(PRD/SPEC/IMPLEMENTATION_PLAN)는 각 모듈의 AGENTS.md에 명시되어 있다.


