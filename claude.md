# 프로젝트 개요 및 목적
 - **Kotlin 기반 Android XML 개발 향상 라이브러리**
 - 샘플 앱으로 활용법을 검증함 (settings.gradle.kts, app/build.gradle.kts).
 - 배포는 JitPack 0.3.37 기준, minSdk 28/compileSdk 35, Kotlin 2.0.21/AGP 8.8.2 설정 (gradle/libs.versions.toml).
 - 반복되는 Activity/Fragment/권한/로깅/시스템 서비스 보일러플레이트를 제거하고 생산성을 높이는 것이 1차 목표 (README_START.md, README_ACTIVITY_FRAGMENT.md 등).
 



# 프로젝트 정의
 - **Simple_UI_XML**은 Android XML 사용 개발자들이 개발을 더 쉽고 빠르게 할 수 있도록 도와주는 종합 라이브러리.
 - 추후 Compose용도 대응예정




# 프로젝트 구조
 - 모듈 분리 구조로 UI 비의존 코어(simple_core)와 XML 전용 UI 레이어(simple_xml)를 제공.
 - 샘플 앱(app)으로 활용법을 검증함 (settings.gradle.kts, app/build.gradle.kts).




# 모듈별 상세 가이드
 - **simple_core 모듈**: simple_core/claude.md 참조
 - **simple_xml 모듈**: simple_xml/claude.md 참조
 - 각 모듈별 특화 규칙 및 주의사항은 해당 모듈 claude.md 확인



 ## 새로운 모듈 추가 절차
  - settings.gradle.kts에 모듈 추가
  - build.gradle.kts 생성 (simple_core, simple_xml 템플릿 참조)
  - namespace 설정 및 의존성 구성
  - 모듈별 claude.md 작성
  - 루트 claude.md에 모듈 링크 추가
  - publishing 설정 (groupId, artifactId)
  - Dokka 설정 추가
  - Kover 설정 추가
  - test/testRobolectric 태스크 구성




# 개발 환경 설정
 - Android Studio Ladybug Feature Drop | 2024.2.2 Patch 2
 - Kotlin: 2.0.21
 - compileSdk: 35
 - minSdk: 28
 - Android Gradle Plugin Version: 8.8.2
 - Gradle Version: 8.10.2



# 라이브러리 주요 기능

 ## 베이스 UI 스캐폴딩
  - 시스템 바·권한·edge-to-edge 처리까지 포함한 simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/activity/RootActivity.kt. 
  - 자동 레이아웃/데이터바인딩 포함 BaseActivity.kt.
  - BaseBindingActivity.kt.
  - 동일 컨셉의 Fragment/Dialog/라이프사이클 레이아웃 클래스들.

 ## RecyclerView 편의
  - 안전한 리스트 연산 큐 simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/queue/AdapterOperationQueue.kt. 
  - Diff/List/Binding 어댑터.
  - 스크롤 방향·엣지 감지용 RecyclerScrollStateView.kt.

 ## 확장 함수 팩
  - 문자열/날짜/번들/단위 변환/try-catch 등 범용 확장 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/...). 
  - View/Resource/Toast/SnackBar/Anim 확장 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/...).

 ## System Manager 정보·제어
  - 배터리·위치·디스플레이·네트워크·Telephony·SIM을 StateFlow 기반으로 제공 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/*). 
  - Wi-Fi/알람/알림/진동 등 제어기 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/*).
  - 키보드·플로팅뷰 컨트롤러 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/*).

 ## 로깅
  - DSL 구성, 파일 저장, 포매터/필터/스택트레이스 지원하는 simple_core/src/main/java/kr/open/library/simple_ui/core/logcat/Logx.kt.
  - 내부 구성·작성기 구현체들.

 ## 권한 처리
  - 플랫폼 특수 권한까지 아우르는 Context 확장 (simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/extentions/PermissionExtensions.kt).
  - ActivityResult 기반 큐·재요청·특수 권한 흐름을 묶은 오케스트레이터 simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/manager/PermissionManager.kt.

 ## MVVM 베이스
  - 라이프사이클 옵저버를 포함한 simple_core/src/main/java/kr/open/library/simple_ui/core/viewmodel/BaseViewModel.kt
  - 이벤트 채널 제공 BaseViewModelEvent.kt.



 ## 프로젝트 가치 제안

  ### 대폭 보일러플레이트 절감
   - 기본 Activity/Fragment/Adapter/권한/로그/시스템 서비스 래퍼로 표준 흐름만 남기도록 설계 (README_RECYCLERVIEW.md, README_SERVICE_MANAGER_INFO.md, README_SERVICE_MANAGER_CONTROL.md).


  ### 안정성과 일관성
   - BaseSystemService.kt에서 권한 미리 검증 후 tryCatchSystemManager로 실패를 기본값 처리. 
   - @RequiresPermission/@RequiresApi 표기. 
   - safeCatch로 예외 안전성 확보.
 

  ### 바로 현업에 쓰기 좋은 툴링
   - 로그 파일 저장·필터·DSL, 특수 권한까지 이어받는 PermissionManager.
   - API 35 대응 시스템 바 처리 등 실기기 이슈 대응 로직을 기본 제공.
 

  ### 문서·배포 준비 완료
   - 한/영 병기 KDoc과 세분화된 README,
   - 다중 모듈 Dokka 산출물(docs/api) 및 JitPack 퍼블리싱 스크립트.(Maven 에정)



 ## 코딩 컨벤션 & 스타일

  ### Kotlin/Coroutine/Flow 우선
   - 상태 StateFlow 관리.
   - 이벤트 SharedFlow/Channel 관리.
   - 동시성 Mutex/SupervisorJob 관리.
   - 예제 코드(simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/manager/PermissionManager.kt, simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateView.kt).


  ### 명시적 가시성·어노테이션
   - public/private를 드러내고 @RequiresPermission, @RequiresApi로 API·권한 요구사항을 문서화 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/LocationStateInfo.kt, simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/activity/RootActivity.kt).


  ### API 분기 헬퍼·게으른 초기화
   - checkSdkVersion 인라인 분기와 by lazy로 시스템 서비스 접근을 캡슐화 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/WifiController.kt).


  ### 에러/로깅 패턴
   - 모든 예외/상태 로깅은 Logx를 통한 단일 경로로 수집.
   - safeCatch로 기본값 반환 패턴 유지 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/trycatch/TryCatchExtensions.kt).


  ### 도메인별 패키징·타입 세분화
   - extensions, system_manager, logcat, ui/adapter 등 도메인 단위 디렉터리.
   - 연산 모델은 sealed class(AdapterOperationQueue.kt), 값 객체는 data class(telephony/network 등).


  ### 도구 체인
   - DataBinding 가능.
   - Kover로 커버리지 태스크.
   - Robolectric·단위 테스트 태스크 분리
   - Dokka 자동 적용 (simple_core/build.gradle.kts, simple_xml/build.gradle.kts, build.gradle.kts 루트).




# 개발 규칙

 ##  코딩 컨벤션 & 스타일
  - 네이밍: PascalCase for classes, camelCase for functions
  - Kotlin 스타일: Android 공식 가이드 준수
  - XML 에서 ID 생성 시 camelCase으로 id 생성



 ## 주석 스타일
  - 주석은 한·영 병기 규칙을 따른다. 
  - 먼저 영어 설명 이후, 곧바로 <br><br>로 두 줄 공백을 만든다
  - 뒤 같은 내용을 한글로 반복해 “영문 → 빈 줄 → 국문” 구성을 유지한다.
  - 국문이 끝나는 곳에 <br>로 한 줄 공백을 만든다.
  - @return 도 동일한 방식이다. 첫 줄은 “반환값”과 “로그 동작”을 영어로 설명한다
  - <br><br>로 줄을 바꾼 뒤 한글 설명을 붙인다.
  - 한글이 끝나는 곳에 <br>로 한 줄 공백을 만든다.
  - 각 @param 블록은 첫 줄에 영어 설명을 적고 <br><br>로 두 줄을 비운다
  - 다음, 동일 의미의 한글을 다음 줄에 쓴다.
  - 추가 설명이 필요하면 들여쓰기를 유지한 채 이어지는 줄에 적는다.
  - 주석 예제 코드 (package kr.open.library.simple_ui.core.extensions.date) 



 ## 에러 처리 규칙
  - try-catch에서 return값이 필요한 경우 safeCatch을 권장한다.
  - 시스템 서비스는 tryCatchSystemManager 사용을 권장한다.
  - Logx로 통일된 로깅을 권장한다.
  - 에러 발생 시 기본값 반환, null 반환 최소화



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



 ## 특별 주의사항

  ### XML Id 규칙
   - Button에 id를 추가 할 경우 +id/btn* 으로 시작한다.
   - EditText에 id를 추가 할 경우 +id/edt* 으로 시작한다.
   - TextView에 id를 추가 할 경우 +id/tv* 으로 시작한다.
   - RecyclerScrollStateView에 id를 추가 할 경우 +id/rcv* 으로 시작한다.
   - Checkbox에 id를 추가 할 경우 +id/cb* 으로 시작한다.
   - 기타 다른 View에 Id를 추가시 위와 비슷한 형식으로 id를 생성한다.
  
  ### 코드 수정 기준
   - 함부로 코드를 수정 하지 않는다. 대신 관련된 질문을 한다.
   - 모든 질문이 완료 된 이후 코드를 수정한다.
   - 코드 수정은 반드시 사용자의 허락(승인)이 필요하다.
   - 암묵적 승인은 없다.
   - 빠른 응답 보다 정확한 응답이 중요하다.
  
  ### 파일/패키지 생성 및 수정 규칙
   - 새 파일/패키지 생성 전, 반드시 기존 구조와 위치를 먼저 확인한다.
   - 패키지나 클래스의 실제 존재 여부를 Glob/Grep 도구로 검증한다.
   - 추정이나 가정으로 파일을 생성하지 않는다.
   - 불확실한 경우 반드시 사용자에게 확인 후 진행한다.
  
  ### 기능 추가 수정 시 검토 사항
   - 기능 추가/수정 전, 반드시 사이드 이펙트 발생 유무를 확인한다.
   - 기능 추가/수정 전, 수정 부분에 대해 반드시 사용자에게 고지 할 것.
   - 기능 추가/수정 시, Android OS(SDK Version) 별 분기를 나눠야 할 필요가 있는지 반드시 확인 한다.
   - 기능 추가/수정 시, 기존 작성된 코드 스타일과 비슷한 구조로 개발 한다.



 ## claude.md 갱신이 필요한 경우
  - 새로운 모듈 추가
  - 주요 아키텍처 변경 (예: 새로운 디자인 패턴 도입)
  - 개발 환경 변경 (Kotlin, Gradle 버전 업)
  - 새로운 개발 규칙 추가


 
 ## claude.md 갱신이 불필요한 경우
  - 단순 버그 수정
  - 기존 기능 개선 (규칙 변경 없음)



 ## 파일/폴더 삭제시 삭제 이유를 자세히 서술하고 사용자의 승인을 반드시 구한다.



 ## 대량 수정 작업 시 검증 프로세스
  - 대량 파일 수정 작업 전, 전체 범위와 수정 대상을 정확히 파악한다.
  - 각 단계별로 완전히 검증 후 다음 단계로 진행한다.
  - 최종 결과를 여러 방법(grep, 카운팅 등)으로 교차 검증한다.
  - 작업 완료 후 "완료되었다"고 성급하게 결론내리지 않고, 재검토한다.
  - 사용자가 직접 확인할 수 있도록 겸손한 자세로 결과를 보고한다.




# 사용자와 대화 주의 사항

 ## 너는 안드로이드 15년차 개발자이다.
  - 항상 최신의 안드로이드 개발 문서를 파악 하고 있는다.


 ## 코드를 먼저 수정하지 않는다.
  - 15년차는 항상 분석과 질문 위주로 한다.



 ## 판단을 마음데로 하지 않는다.
  - 15년차는 혼자 판단 하지 않는다.
  - 마음데로 축약 해서 부분만 수정 하기 금지.



 ## 의문이 조금이라도 있는 경우 반드시 질문 한다.



 ## 코드 수정은 더 이상 질문이 없는경우 사용자에게 승인을 구한 후 진행한다.



 ## 무조건 한글로 답한다.(UTF8형식)



 ## 수정이 완료되면 README_* 도 수정할 건지 질문하기.

 
 ## 거짓말 하지 않기.
  - 모르면 모른다 답하기
  - 일부만 보고 판단하지 않기.
  - 전부 분석요청 했으나, 일부만 분석 하지 않기.
  - 거짓말이 제일 나쁜것.



 ## README 파일 업데이트 가이드

  ### README 파일 매핑
   - Activity/Fragment 변경 → README_ACTIVITY_FRAGMENT.md
   - 확장 함수 추가/변경 → README_EXTENSIONS.md
   - Logx 기능 변경 → README_LOGX.md
   - ViewModel 관련 → README_MVVM.md
   - RecyclerView 관련 → README_RECYCLERVIEW.md
   - System Manager Info → README_SERVICE_MANAGER_INFO.md
   - System Manager Control → README_SERVICE_MANAGER_CONTROL.md
   - 스타일 가이드 변경 → README_STYLE.md
   - 샘플 앱 변경 → README_SAMPLE.md
   - 시작 가이드 변경 → README_START.md
   - 전체 개요 변경 → README.md


  ### 업데이트 원칙
   - 새 기능 추가 시: 해당 README + README.md 개요 섹션
   - Breaking change: 해당 README + README.md 버전 정보
   - 예제 코드 변경: 반드시 실행 가능한 코드로 유지


