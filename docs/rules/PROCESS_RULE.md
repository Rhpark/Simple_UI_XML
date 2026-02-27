# PROCESS RULES

## 코드 수정 기준
 - 빠른 응답보다 정확한 응답이 중요하다.

## 파일/패키지 생성 및 수정 규칙
 - 새 파일/패키지 생성 전, 반드시 기존 구조와 위치를 먼저 확인한다.
 - 패키지나 클래스의 실제 존재 여부를 Glob/Grep 도구로 검증한다.
 - 추정이나 가정으로 파일을 생성하지 않는다.
 - 불확실한 경우 반드시 사용자에게 확인 후 진행한다.

## 기능 추가 수정 시 검토 사항
 - 기능 추가/수정 전, 반드시 사이드 이펙트 발생 유무를 확인한다.
 - 기능 추가/수정 전, 수정 부분에 대해 반드시 사용자에게 고지할 것.
 - 기능 추가/수정 시, Android OS(SDK Version) 별 분기를 나눠야 할 필요가 있는지 반드시 확인한다.
 - 기능 추가/수정 시, 기존 작성된 코드 스타일과 비슷한 구조로 개발한다.


## 버전 관리 규칙

### 버전 변경 절차
 - gradle/libs.versions.toml의 appVersion 업데이트
 - [release]
    Tag: x.x.x (appVersion과 동일)
    Title: 릴리즈 제목
    Describe: 상세 설명
 - CI → CD → Documentation 자동 실행
 - JitPack 자동 배포
 - [release] 태그가 있는 커밋만 릴리즈 트리거, 없다면 CI만 수행

### 버전 번호 규칙
 - Major.Minor.Patch (Semantic Versioning)
 - Breaking changes: Major 증가
 - 새 기능 추가: Minor 증가
 - 버그 수정: Patch 증가


## CI/CD 워크플로우
 - **1. Android CI** (android-ci.yml): Initialize Check, KtLint Check, Tests(Unit, Robolectric) Build 구조
 - **2. Android CD** (android-cd.yml): Release(JitPack 자동 배포), Assemble Apk,  Firebase App Distribution,
 - **3. Documentation** (documentation.yml): Dokka API 문서 + Kover Coverage 리포트 생성
 - [release] 태그가 있는 커밋만 릴리즈 트리거, 없다면 "1. Android CI"만 수행

### Dokka 문서 업데이트 시점
 - [release] 커밋 시 Documentation 워크플로우가 자동 실행
 - Dokka HTML 생성 및 GitHub Pages 배포
 - 로컬 확인: ./gradlew dokkaHtml
 - 출력 위치: build/dokka/html/


## 문서 갱신 기준 (AGENT/RULE)

### AGENT/RULE 문서 갱신이 필요한 경우
 - 새로운 모듈 추가
 - 주요 아키텍처 변경 (예: 새로운 디자인 패턴 도입)
 - 개발 환경 변경 (Kotlin, Gradle 버전 업)
 - 새로운 개발 규칙 추가

### AGENT/RULE 문서 갱신이 불필요한 경우
 - 단순 버그 수정
 - 기존 기능 개선 (규칙 변경 없음)


## 파일/폴더 삭제 규칙
 - 파일/폴더 삭제 시 삭제 이유를 자세히 서술하고 사용자의 승인을 반드시 구한다.


## 대량 수정 작업 시 검증 프로세스
 - 대량 파일 수정 작업 전, 전체 범위와 수정 대상을 정확히 파악한다.
 - 각 단계별로 완전히 검증 후 다음 단계로 진행한다.
 - 최종 결과를 여러 방법(grep, 카운팅 등)으로 교차 검증한다.
 - 작업 완료 후 "완료되었다"고 성급하게 결론내리지 않고, 재검토한다.
 - 사용자가 직접 확인할 수 있도록 겸손한 자세로 결과를 보고한다.


## README 파일 업데이트 가이드
 - docs/rules/README_RULE.md 를 확인한다.


## 수정/작업 금지 패턴
 - 요청하지 않은 파일을 수정하지 않는다.
 - 사용자 승인 없이 코드를 수정하지 않는다.
 - 추정/가정으로 파일이나 패키지를 생성하지 않는다.
 - 불필요한 리팩토링, 대량 포맷 변경을 하지 않는다.
 - 일부만 보고 전체를 수정하지 않는다.
