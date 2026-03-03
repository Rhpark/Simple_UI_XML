---
name: CodeReview
description: Android 라이브러리 코드 리뷰 스킬. 5단계(기능→로직→아키텍처→품질→명명) 순서로 점검한다. "코드 리뷰", "코드 검토", "코드 검증", "PR 리뷰" 요청 시 사용.
disable-model-invocation: true
argument-hint: "파일경로 or 패키지명 or 클래스명 or 흐름설명"
---

# CodeReview Skill

이 프로젝트(SimpleUI_XML)의 코드 리뷰를 수행한다.

## 대상 코드 결정 우선순위

1. $ARGUMENTS 가 있다면
 - 단일 파일시 직접 처리한다.
 - 다중 파일/ 패키지/ 흐름이라면 SubAgent를 호출해 사용한다.
2. $ARGUMENTS 가 없으면 → ide_selection 을 대상으로 한다.
 

## 실행 방식 결정

- 함수 / 파일 단위 → 직접 분석 (Read, Grep, Glob)
- 패키지 / 흐름 단위 → Task tool (subagent_type: general-purpose) 로 SubAgent 위임

## 리뷰 순서

docs/rules/code_review/ 의 5단계를 순서대로 수행한다.
앞 단계에서 문제가 발견되어도 모든 단계를 끝까지 수행한다.
각 단계의 세부 규칙 기준은 docs/rules/coding_rule/ 을 참조한다.

1. docs/rules/code_review/STEP1_FUNCTIONAL.md   - 기능 검증
2. docs/rules/code_review/STEP2_LOGIC.md        - 로직 & 안정성
3. docs/rules/code_review/STEP3_ARCHITECTURE.md - 아키텍처
4. docs/rules/code_review/STEP4_CODE_QUALITY.md - 코드 품질
5. docs/rules/code_review/STEP5_NAMING.md       - 명명 규칙

## 결과 보고

심각도 기준으로 최대 총 7개를 정리한다.
- CRITICAL: 릴리즈 전 필수 수정
- HIGH: 머지 전 필수 수정
- MEDIUM/LOW: 선택 보고

각 이슈에 파일/라인/근거/수정안/호출부/테스트 영향을 포함한다.

## 5단계 완료 후

Optional.md 의 항목을 순서대로 사용자에게 문의한다.
