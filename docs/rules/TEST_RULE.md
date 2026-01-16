# TEST RULES

## 테스트 작성 규칙

### 테스트 파일 디렉터리 구조
 - 테스트 파일은 반드시 테스트 유형별 패키지에 위치해야 함
 - **단위 테스트**: `src/test/java/kr/open/library/simple_ui/{모듈}/unit/{원본_패키지_경로}/`
 - **Robolectric 테스트**: `src/test/java/kr/open/library/simple_ui/{모듈}/robolectric/{원본_패키지_경로}/`
 - 원본 소스 파일의 패키지 구조를 `unit/` 또는 `robolectric/` 하위에 그대로 유지
 - 예시:
   - 소스: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateCalculator.kt`
   - 단위 테스트: `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/ui/view/recyclerview/RecyclerScrollStateCalculatorTest.kt`
   - Robolectric: `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/view/recyclerview/RecyclerScrollStateCalculatorRobolectricTest.kt`

### 단위 테스트 (Unit Test)
 - simple_core, simple_xml 모두 testUnit 태스크 사용
 - UI, Android 의존성 없는 순수 로직 테스트
 - JUnit 기반
 - 파일명: `*Test.kt` (예: `RecyclerScrollStateCalculatorTest.kt`)

### Robolectric 테스트
 - testRobolectric 태스크 사용
 - Android 컴포넌트 의존성 있는 테스트
 - Android 프레임워크 필요 시 사용
 - 파일명: `*RobolectricTest.kt` (예: `PermissionManagerRobolectricTest.kt`)

### 테스트 파일 네이밍 규칙
 - 단위 테스트: `{클래스명}Test.kt`
 - Robolectric 테스트: `{클래스명}RobolectricTest.kt`
 - 테스트 대상 클래스명을 명확히 반영

### Kover 커버리지
 - koverHtmlReport로 simple_xml, simple_core 리포트 생성
