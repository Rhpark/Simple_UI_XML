# simple_core 모듈 개요
 - **전역 규칙은 루트 claude.md 참조**  
 - 주석 스타일, 코딩 컨벤션, 대화 규칙 등은 루트 claude.md를 따름
 - **현재 버전**: 0.3.35 (JitPack)
 - **Maven 좌표**: `com.github.Rhpark:Simple_UI_Core:0.3.35`



 ## 모듈 정의
  - **UI 기술 독립적인 코어 라이브러리**
  - Android UI 레이어(XML, Compose 등)와 무관하게 동작하는 범용 유틸리티 및 비즈니스 로직 제공
  - Context 의존성은 있으나 View/Activity/Fragment 등 UI 컴포넌트 의존성은 없음



 ## 모듈 분리 이유

  ### UI 기술 독립성
   - XML 기반 UI(simple_xml)와 향후 Compose 기반 UI에서 모두 사용 가능
   - UI 레이어 변경 시에도 코어 로직 재사용 가능
   - 멀티 플랫폼 확장 시 비즈니스 로직 공유 용이


  ### 재사용성 극대화
   - 다른 프로젝트에서 simple_core만 독립적으로 사용 가능
   - UI 없는 서비스/백그라운드 작업에서도 활용 가능
   - 테스트 용이성 향상 (UI 의존성 제거)


  ### 명확한 책임 분리
   - simple_core: 비즈니스 로직, 데이터 처리, 시스템 서비스 추상화
   - simple_xml: UI 렌더링, 사용자 인터랙션, 레이아웃 관리
   - 각 모듈의 역할이 명확하여 유지보수 효율 향상



 ## 주요 패키지 구조 (총 97개 파일)

  ### extensions (9개 하위 패키지)
   - **bundle**: Bundle 생성/접근 간편화 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/bundle/BundleInline.kt)
   - **collection**: 컬렉션 조건부 처리 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/collection/)
   - **conditional**: SDK 버전 분기, 조건부 실행 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/conditional/SdkVersionInline.kt)
   - **date**: 날짜/시간 포맷팅 및 변환 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/date/DateExtensions.kt)
   - **display**: dp/sp/px 단위 변환 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/display/DisplayUnitExtensions.kt)
   - **round_to**: 반올림/올림/내림 헬퍼 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/round_to/RoundToInline.kt)
   - **string**: 문자열 처리 유틸 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/string/StringExtensions.kt)
   - **time**: 시간 측정 유틸 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/time/MeasureTimeExtensions.kt)
   - **trycatch**: 안전한 예외 처리 패턴 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/trycatch/TryCatchExtensions.kt)


  ### logcat
   - **DSL 기반 로깅 시스템** (simple_core/src/main/java/kr/open/library/simple_ui/core/logcat/Logx.kt)
   - **config**: 로그 설정 관리 (simple_core/src/main/java/kr/open/library/simple_ui/core/logcat/config/)
   - **internal**: 파일 저장/필터링/포맷팅 구현체 (simple_core/src/main/java/kr/open/library/simple_ui/core/logcat/internal/)
   - 파일 저장, 포매터, 필터, 스택트레이스 지원


  ### permissions
   - **Context 확장 기반 권한 체크** (simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/extentions/PermissionExtensions.kt)
   - 일반 권한 + 특수 권한(SYSTEM_ALERT_WINDOW, WRITE_SETTINGS 등) 통합 처리
   - 권한 VO 모델 (simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/vo/)
   - simple_xml의 PermissionManager가 이 레이어를 사용


  ### system_manager
   - **base**: 시스템 서비스 기본 클래스 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/base/BaseSystemService.kt)
   - **info**: 시스템 정보 StateFlow 제공
     - battery: 배터리 상태 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/battery/)
     - location: 위치 정보 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/)
     - network: 네트워크/SIM/Telephony (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/network/)
   - **controller**: 시스템 제어
     - alarm: 알람 설정 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/alarm/)
     - notification: 알림 관리 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/notification/)
     - vibrator: 진동 제어 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/vibrator/)
     - wifi: Wi-Fi 제어 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/)


  ### viewmodel
   - **BaseViewModel**: 라이프사이클 옵저버 포함 (simple_core/src/main/java/kr/open/library/simple_ui/core/viewmodel/BaseViewModel.kt)
   - **BaseViewModelEvent**: 이벤트 채널 제공 (simple_core/src/main/java/kr/open/library/simple_ui/core/viewmodel/BaseViewModelEvent.kt)


  ### local
   - **SharedPreferences 래퍼** (simple_core/src/main/java/kr/open/library/simple_ui/core/local/base/BaseSharedPreference.kt)



 ## 핵심 설계 원칙

  ### 1. Coroutine & StateFlow 우선
   - 모든 비동기 작업은 Coroutine 기반
   - 시스템 상태는 StateFlow로 반응형 제공
   - 이벤트는 SharedFlow/Channel로 처리
   - Mutex를 통한 동시성 제어


  ### 2. 안전한 예외 처리 패턴
   - **safeCatch**: 모든 예외를 안전하게 처리하고 기본값 반환 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/trycatch/TryCatchExtensions.kt)
   - **tryCatchSystemManager**: BaseSystemService에서 권한 검증 + 예외 처리 통합 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/base/BaseSystemService.kt)
   - 예외 발생 시 Logx로 자동 로깅
   - 에러 처리 규칙은 루트의 claude.md(에러 처리 규칙)를 참조


  ### 3. BaseSystemService 권한 검증 패턴
   - 생성 시점에 필요 권한 자동 체크
   - 권한 부족 시 경고 로그 + 기본값 반환
   - refreshPermissions()로 권한 재검증
   - 예제: (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/LocationStateInfo.kt)


  ### 4. API 레벨 분기 헬퍼
   - **checkSdkVersion**: 인라인 함수로 API 분기 간소화 (simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/conditional/SdkVersionInline.kt)
   - @RequiresApi 어노테이션으로 최소 SDK 명시
   - by lazy로 시스템 서비스 지연 초기화


  ### 5. 통합 로깅 시스템
   - 모든 로그는 Logx를 통해 단일 경로로 수집
   - DSL 기반 설정으로 파일 저장/필터링/포맷팅 제어
   - 스택트레이스 자동 수집 옵션



 ## 개발 시 주의사항

  ### UI 의존성 금지
   - simple_core에서는 View, Activity, Fragment, DataBinding 등 UI 관련 import 절대 금지
   - Context는 사용 가능하나 UI 조작 로직은 simple_xml로 위임
   - UI 관련 기능이 필요하면 simple_xml 모듈에 구현


  ### 권한 처리
   - 시스템 서비스 래퍼 작성 시 BaseSystemService 상속 필수
   - requiredPermissions 생성자 파라미터로 필요 권한 명시
   - tryCatchSystemManager로 안전한 기본값 반환 패턴 유지
   - @RequiresPermission 어노테이션 명시


  ### API 레벨 대응
   - SDK 버전별 분기가 필요한 경우 checkSdkVersion 사용
   - @RequiresApi 어노테이션으로 최소 API 레벨 표기
   - Deprecated API 사용 시 주석으로 이유 명시


  ### StateFlow/SharedFlow 사용
   - 시스템 상태는 StateFlow로 제공 (초기값 필수)
   - 일회성 이벤트는 SharedFlow 또는 Channel 사용
   - 백그라운드 스레드에서 안전하게 수집 가능하도록 설계


  ### 테스트 용이성
   - 모든 시스템 서비스는 인터페이스 또는 추상 클래스 기반
   - Context 의존성은 생성자 주입으로 테스트 가능하게
   - 부작용(side effect) 최소화



 ## 모듈 의존성 규칙

  ### 허용되는 의존성
   - Android SDK (android.*, androidx.lifecycle, androidx.core 등)
   - Kotlin stdlib & coroutines
   - Context, Application


 ### 금지되는 의존성
  - View, ViewGroup 등 UI 컴포넌트
  - Activity, Fragment 등 UI 컨트롤러
  - DataBinding, ViewBinding
  - simple_xml 모듈 (역방향 의존 금지)



 ## 테스트 작성 규칙

  ### 테스트 파일 디렉터리 구조
   - 테스트 파일은 반드시 테스트 유형별 패키지에 위치해야 함
   - **단위 테스트**: `src/test/java/kr/open/library/simple_ui/core/unit/{원본_패키지_경로}/`
   - **Robolectric 테스트**: `src/test/java/kr/open/library/simple_ui/core/robolectric/{원본_패키지_경로}/`
   - 파일명: 단위 테스트는 `*Test.kt`, Robolectric은 `*RobolectricTest.kt`


  ### testUnit: UI 의존성 없는 순수 로직 테스트
   - extensions, logcat, permissions, system_manager 등


  ### testRobolectric: Android 컴포넌트 의존성 테스트
   - Context, SharedPreferences 등


  ### 테스트 실행
   - ./gradlew :simple_core:koverHtmlReport
   - ./gradlew :simple_xml:koverHtmlReport
   - 또는 ./gradlew :simple_core:koverHtmlReport :simple_xml:koverHtmlReport



 ## 주요 클래스 역할 요약

  ### BaseSystemService
   - 모든 시스템 서비스의 기본 클래스
   - 권한 자동 검증 및 갱신
   - tryCatchSystemManager로 안전한 실행
   - 예제: LocationStateInfo, WifiController


  ### Logx
   - DSL 기반 로깅 설정
   - 파일 저장, 필터, 포맷터 지원
   - 스택트레이스 수집 옵션
   - 프로젝트 전역에서 통일된 로깅



  ### checkSdkVersion
   - API 레벨 분기를 간소화하는 인라인 함수
   - 코드 가독성 향상
   - 예제: WifiController, RootActivity


  ### BaseViewModel & BaseViewModelEvent
   - 라이프사이클 인식 ViewModel
   - 이벤트 채널 기본 제공
   - simple_xml의 UI 컨트롤러에서 사용
