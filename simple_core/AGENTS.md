# simple_core 모듈 개요
 - **전역 규칙은 루트 AGENTS.md 참조**  
 - 주석 스타일, 코딩 컨벤션, 대화 규칙 등은 루트 AGENTS.md를 따름
 - **현재 버전**: 0.3.46 (JitPack)
 - **Maven 좌표**: `com.github.Rhpark:Simple_UI_Core:0.3.46`



 ## 기능별 전용 규칙
  - 기능별 상세 규칙은 `simple_core/docs/feature/<기능명>/AGENTS.md`에 위치한다.
  - permissions: simple_core/docs/feature/permissions/AGENTS.md



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
   - 참고 문서
     - simple_core/docs/feature/logcat/*.md


  ### permissions
   - **Context 확장 기반 권한 체크** (simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/extentions/PermissionExtensions.kt)
   - 일반 권한 + 특수 권한(SYSTEM_ALERT_WINDOW, WRITE_SETTINGS 등) 통합 처리
   - 권한 VO 모델 (simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/vo/)
   - simple_xml의 PermissionRequester가 이 레이어를 사용


  ### system_manager
   - **base**: 시스템 서비스 기본 클래스 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/base/BaseSystemService.kt)
   - **info**: 시스템 정보 Flow(SharedFlow/StateFlow) 제공
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


 ## 개발 시 주의사항

  ### 권한 처리
   - 시스템 서비스 래퍼 작성 시 BaseSystemService 상속 필수
   - requiredPermissions 생성자 파라미터로 필요 권한 명시
   - tryCatchSystemManager로 안전한 기본값 반환 패턴 유지
   - @RequiresPermission 어노테이션 명시

  ### 테스트 용이성
   - 모든 시스템 서비스는 인터페이스 또는 추상 클래스 기반
   - Context 의존성은 생성자 주입으로 테스트 가능하게
   - 부작용(side effect) 최소화



 ## 모듈 의존성 규칙

  ### 허용되는 의존성
   - Android SDK (android.*, androidx.lifecycle, androidx.core 등)
   - Kotlin stdlib & coroutines
   - Context, Application


 ## 테스트 작성 규칙
  - **docs/rules/TEST_RULE.md 참조**


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
