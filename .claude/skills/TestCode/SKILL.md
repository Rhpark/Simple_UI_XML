---
name: TestCode
description: SimpleUI_XML 프로젝트의 테스트 코드를 작성. 대상 코드, 기존 테스트 관례, 관련 문서를 직접 읽고 Unit 또는 Robolectric 테스트를 판단한다. "테스트 코드 작성", "테스트 만들어줘", "test code" 등의 요청 시 사용.
disable-model-invocation: true
argument-hint: "파일경로 or 패키지명 or 클래스명"
---

# TestCode Skill

이 프로젝트(SimpleUI_XML)의 테스트 코드를 작성한다.
반드시 아래 규칙을 따른다.

## 대상 코드 결정 우선순위

1. `$ARGUMENTS` 가 있으면 → 해당 파일 경로, 패키지명, 클래스명을 대상으로 한다.
2. `$ARGUMENTS` 가 없으면 → 현재 IDE에서 선택된 코드(`ide_selection`)를 대상으로 한다.
3. `ide_selection` 도 없으면 → 사용자에게 대상 파일 경로, 패키지명, 클래스명을 요청한 후 진행한다.

## 작업 순서

### STEP 1. 대상 코드 분석
대상 파일을 직접 읽고, 가능하면 같은 패키지/기능의 기존 테스트도 함께 읽는다.

반드시 확인할 항목:
- 대상 클래스의 실제 동작
- Android 런타임 의존 여부
- 같은 패키지/기능에 이미 존재하는 테스트의 유형
- 공개 API인지, 내부 구현인지

STEP 1 완료 후, 아래 형식으로 반드시 출력한 뒤 STEP 2로 진행한다.
```
> 대상: {파일 경로}
> Android 런타임 의존: [예 / 아니오]
> 기존 테스트 관례: [Unit / Robolectric / 없음]
```

### STEP 2. 관련 문서 확인 조건
아래 조건 중 하나에 해당하면 대상 코드 외에 관련 문서를 함께 읽는다.

- `simple_core/docs/feature/` 또는 `simple_xml/docs/feature/` 아래 문서가 존재하는 기능
- README에 노출된 공개 기능
- 상태/결과/권한/시스템바/어댑터처럼 계약 중심 기능

확인 대상:
- 기능별 `AGENTS.md`
- `PRD.md`
- `SPEC.md`
- `IMPLEMENTATION_PLAN.md`

단순 내부 유틸, 값 객체, enum, sealed model 수준의 테스트는 대상 코드와 기존 테스트 관례 중심으로 진행할 수 있다.

### STEP 3. 테스트 유형 결정
테스트 유형은 import 존재 여부가 아니라 실제 검증에 필요한 실행 환경 기준으로 판단한다.

우선순위:
1. 같은 패키지/기능의 기존 테스트 관례
2. 실제 Android 런타임 필요 여부
3. 값/상수/상태 모델 수준 검증이면 Unit 우선
4. Android 프레임워크 실제 동작 검증이면 Robolectric

**Unit 테스트 우선 검토 예시**
- 순수 Kotlin/Java 로직
- enum, data class, sealed class, 결과 모델 검증
- Android/AndroidX import가 있어도 상수 또는 값 객체 수준으로만 사용하는 경우
- 기존 같은 기능 테스트가 `unit/`에 위치하는 경우

**Robolectric 테스트 필요 예시**
- `Context`, `Activity`, `Fragment`, `View`, `Window`, `Resources` 실제 동작 검증
- 시스템 서비스 조회/실행
- `WindowInsets`, `ActivityResult`, 레이아웃 inflate/measure/bind
- Android 프레임워크 라이프사이클/콜백을 실제로 흘려야 하는 경우

주의:
- Android/AndroidX import 존재만으로 Robolectric으로 단정하지 않는다.
- 분류가 애매하거나 복잡하면 자동 생성하지 않고 사용자님께 사유를 설명한 뒤 확인을 구한다.

STEP 3 완료 후, 아래 형식으로 반드시 출력한 뒤 STEP 4로 진행한다.
```
> 테스트 유형: [Unit / Robolectric]
> 근거: {판단 근거 1줄}
```

### STEP 4. 테스트 파일 경로 결정

**단위 테스트 경로:**
```
src/test/java/kr/open/library/simple_ui/{모듈}/unit/{원본_패키지_경로}/{클래스명}Test.kt
```

**Robolectric 테스트 경로:**
```
src/test/java/kr/open/library/simple_ui/{모듈}/robolectric/{원본_패키지_경로}/{클래스명}RobolectricTest.kt
```

**경로 예시:**
- 소스: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateCalculator.kt`
- 단위 테스트: `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/ui/view/recyclerview/RecyclerScrollStateCalculatorTest.kt`
- Robolectric: `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/view/recyclerview/RecyclerScrollStateCalculatorRobolectricTest.kt`

### STEP 5. 충돌 체크
생성 예정 경로에 파일이 이미 존재하면 즉시 중단하고 사용자에게 안내한다.
- 충돌 파일 경로 표시
- 대안 제시 (파일명 변경 또는 기존 파일 확인 권장)

### STEP 6. 테스트 코드 작성
각 함수/클래스에 대한 테스트 케이스를 작성한다.

작성 원칙:
- 기존 테스트 스타일과 패키지 구조를 우선 따른다.
- 테스트는 문서 계약 또는 공개 동작을 검증한다.
- 구현 세부사항에 과도하게 결합된 테스트는 피한다.
- 테스트 코드도 UTF-8 한글 기준을 따른다.
- 필요 시 테스트 목적을 짧게 주석으로 남기되, 프로젝트 KDoc/문서 규칙과 충돌하지 않게 작성한다.

### STEP 7. 정적 검증
작성한 테스트가 아래 규칙을 준수하는지 확인한다.
- 파일 경로가 `unit/` 또는 `robolectric/` 하위에 올바르게 위치하는가
- 파일명이 `*Test.kt` 또는 `*RobolectricTest.kt` 규칙을 따르는가
- 분류 기준이 기존 테스트 관례와 충돌하지 않는가
- 테스트가 구현 우연이 아니라 공개 동작/문서 계약을 검증하는가
- 복잡한 Robolectric 테스트는 작성하지 않고 사용자님께 사유를 알리고 승인 요청하는가

검증 실패 시 → STEP 6으로 돌아가 해당 항목을 수정 후 재검증한다.

### STEP 8. 테스트 실행 검증
가능하면 관련 테스트 명령을 실행한다.

실행 원칙:
- 수정 범위와 가장 가까운 테스트만 우선 실행한다.
- 필요 시 모듈 단위 테스트로 넓힌다.
- 실행하지 못한 경우, 미실행 사유를 결과에 반드시 명시한다.

### STEP 9. 결과 요약
테스트 파일 작성 및 검증이 끝난 뒤 아래 형식으로 결과를 요약한다.

```
✅ TestCode 완료
- 파일: {경로}
- 테스트 유형: Unit / Robolectric
- 테스트 케이스 수: N개
- 파일 생성: 완료 / 미완료
- 테스트 실행: 성공 / 실패 / 미실행
- 실행 명령: {명령어}
- 비고: {실패 사유 또는 미실행 사유}
```

## 테스트 실행 명령어

```bash
# 전체 단위 테스트
./gradlew testUnit

# 전체 Robolectric
./gradlew testRobolectric

# 특정 클래스만
./gradlew :simple_xml:testRobolectric --tests 'full.class.name'

# 커버리지
./gradlew :simple_core:koverHtmlReport
./gradlew :simple_xml:koverHtmlReport
```

## 주의사항

- 복잡한 Robolectric 테스트(WindowInsets 등)는 작성하지 않고 사유를 설명 후 승인 요청
- `simple_core` 테스트에서도 Android/AndroidX import가 있다는 이유만으로 Robolectric으로 단정하지 않는다
- `simple_core`는 UI 비의존 모듈이므로 테스트도 모듈 경계를 존중해야 한다
- 공개 기능 테스트는 관련 feature 문서와 README 계약을 우선한다
- 테스트를 실행하지 않았다면 "동작 확인 완료"처럼 단정하지 않는다
- 테스트 파일 작성 시 반드시 UTF-8로 저장
- 거짓말 하지 않는다.
