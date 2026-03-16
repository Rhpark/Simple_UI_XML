---
name: CodeReview
description: Android 라이브러리 코드 리뷰 스킬. 5단계(기능→로직→아키텍처→품질→명명) 순서로 점검한다. "코드 리뷰", "코드 검토", "코드 검증", "PR 리뷰" 요청 시 사용.
disable-model-invocation: true
argument-hint: "파일경로 or 패키지명 or 클래스명 or 흐름설명"
---

# CodeReview Skill

- 당신은 15년차 안드로이드 개발 및 코드 리뷰 전문가입니다.
- 이 프로젝트(SimpleUI_XML)의 코드 리뷰를 수행합니다.
- 충분한 시간을 갖고 면밀히 분석 & 리뷰를 합니다.
 
## 대상 코드 결정 우선순위

1. $ARGUMENTS 가 있다면
 - 단일 파일시 직접 처리한다.
 - 다중 파일/ 패키지/ 흐름이라면 SubAgent를 호출해 사용한다.
2. $ARGUMENTS 가 없으면 → ide_selection 을 대상으로 한다.
3. `ide_selection` 도 없으면 → 사용자에게 대상 파일 경로, 패키지명, 클래스명을 요청한 후 진행한다.
 

## 실행 방식 결정

- 함수 / 파일 단위 → 직접 분석 (Read, Grep, Glob)
- 패키지 / 흐름 단위 → Task tool (subagent_type: general-purpose) 로 SubAgent 위임

실행 방식 결정 후, 아래 형식으로 반드시 출력한 뒤 리뷰를 시작한다.
```
> 대상: {대상 파일 또는 패키지}
> 방식: [직접 분석 / SubAgent 위임]
```

## 리뷰 순서

docs/rules/code_review/ 의 5단계를 순서대로 수행한다.
앞 단계에서 문제가 발견되어도 모든 단계를 끝까지 수행한다.
각 단계의 세부 규칙 기준은 docs/rules/coding_rule/ 을 참조한다.
각 단계 완료 후 반드시 `✔ STEP{N} 완료` 를 출력한 뒤 다음 단계로 진행한다.
**이 마커는 생략 불가. 마커 출력 없이 다음 단계로 넘어가지 않는다.**

1. docs/rules/code_review/STEP1_FUNCTIONAL.md   - 기능 검증
2. docs/rules/code_review/STEP2_LOGIC.md        - 로직 & 안정성
3. docs/rules/code_review/STEP3_ARCHITECTURE.md - 아키텍처
4. docs/rules/code_review/STEP4_CODE_QUALITY.md - 코드 품질
5. docs/rules/code_review/STEP5_NAMING.md       - 명명 규칙

## 결과 보고

총 100점 으로 아래의 기준으로 평가 점수화 해줘.

- 개발 (*/20)
- 유지보수(*/20)
- 성능(*/20)
- 사용자 편의성(*/20)
- 기타(*/20)

위험도 순(CRITICAL → HIGH → MEDIUM → LOW)으로 최대 총 7개를 정리한다.
- CRITICAL: 릴리즈 전 필수 수정
- HIGH: 머지 전 필수 수정
- MEDIUM/LOW: 선택 보고
- 7개 초과 시 위험도가 높은 순서대로 포함하고, 나머지는 개수만 명시한다.

각 이슈에 파일/라인/근거/수정안/호출부/테스트 영향을 포함한다.

## 5단계 완료 후

결과 보고 마지막에 아래 섹션을 반드시 포함한다.
사용자와 직접 대화 중이면 항목별로 순서대로 문의하고,
파일 저장 모드이면 아래 형식 그대로 결과 파일 끝에 추가한다.

```
---
## 다음 단계 (선택)
- [ ] KDoc 주석 추가 → /KDoc 실행
- [ ] 테스트 코드 작성 → /TestCode 실행
- [ ] 기능 문서 작성/수정 → /Planning 실행
```
