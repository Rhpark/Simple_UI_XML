## 주석 스타일
- 주석은 한·영 병기 규칙을 따른다.
- 먼저 영어 설명 이후, 곧바로 <br><br>로 두 줄 공백을 만든다
- 뒤 같은 내용을 한글로 반복해 “영문 → 빈 줄 → 국문” 구성을 유지한다.
- 국문이 끝나는 곳에 <br>로 한 줄 공백을 만든다.
- @return 도 동일하게 첫 줄은 “반환값”과 “로그 동작”을 영어로 설명한다
- <br><br>로 줄을 바꾼 뒤 한글 설명을 붙인다.
- 한글이 끝나는 곳에 <br>로 한 줄 공백을 만든다.
- 각 @param 블록은 첫 줄에 영어 설명을 적고 <br><br>로 두 줄을 비운다
- 다음, 동일 의미의 한글을 다음 줄에 쓴다.
- 추가 설명이 필요하면 들여쓰기를 유지한 채 이어지는 줄에 적는다.
- 주석 예제 코드 (package kr.open.library.simple_ui.core.extensions.date)

## 주석 예시

### Good
```kotlin
/**
* Formats date to string.<br><br>
* 날짜를 문자열로 변환합니다.<br>
  */
```

### Bad
```kotlin
/** 날짜를 문자열로 변환 */
```

## 클래스 문서화 템플릿 (선택 사항)
- 복잡한 클래스나 라이브러리의 핵심 클래스는 아래 템플릿을 사용하여 포괄적인 문서를 작성할 수 있다.
- 템플릿의 각 섹션 제목은 "**영문 / 한글:**<br>" 형식을 따른다.
- 템플릿의 내용은 "영문 → <br><br> → 한글 → <br>" 형식을 따른다.
- "Important notes / 주의사항" 섹션은 선택 사항이며, 필요시에만 추가한다.

```kotlin
/**
 * Brief description.<br><br>
 * 간단한 설명.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Reason 1<br>
 * - Reason 2<br><br>
 * - 이유 1<br>
 * - 이유 2<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Decision 1<br>
 * - Decision 2<br><br>
 * - 결정 1<br>
 * - 결정 2<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Note 1<br>
 * - Note 2<br><br>
 * - 주의사항 1<br>
 * - 주의사항 2<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Step 1<br>
 * 2. Step 2<br><br>
 * 1. 단계 1<br>
 * 2. 단계 2<br>
 */
```

## 금지 항목
 - 코드를 절대 건들지 않는다.

## 준수 사항
 - 반드시 UTF-8로 작성한다.