# simple_xml 모듈 개요
 - **전역 규칙은 루트 claude.md 참조**
 - 주석 스타일, 코딩 컨벤션, 대화 규칙 등은 루트 claude.md를 따름
 - **현재 버전**: 0.3.34 (JitPack)
 - **Maven 좌표**: `com.github.Rhpark:Simple_UI_XML:0.3.34`



 ## 모듈 정의
  - **Android XML UI 전용 레이어**
  - XML 기반 레이아웃 시스템에 특화된 UI 컴포넌트 및 헬퍼 제공
  - Activity, Fragment, Adapter, View 등 UI 컨트롤러 및 위젯 확장
  - simple_core 모듈을 기반으로 UI 레이어 기능 구현



 ## 모듈 분리 이유

  ### UI 기술 분리
   - XML 기반 UI 로직을 simple_core와 분리
   - 향후 Compose 모듈(simple_compose) 추가 시 명확한 경계
   - UI 기술 변경 시 simple_core는 영향 없음

  ### DataBinding/ViewBinding 집중
   - XML 레이아웃에 특화된 바인딩 시스템 활용
   - BaseActivity, BaseBindingActivity 등 바인딩 자동화
   - View 확장 함수로 XML UI 조작 간소화

  ### 라이프사이클 관리
   - Activity/Fragment 생명주기 통합 관리
   - PermissionManager의 ActivityResult 기반 권한 처리
   - RecyclerView Adapter의 안전한 리스트 연산 큐



 ## 주요 패키지 구조 (총 47개 파일)

  ### ui/activity
   - **RootActivity**: 시스템 바 제어, 권한 관리 기본 클래스 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/activity/RootActivity.kt)
   - **BaseActivity**: 자동 레이아웃 바인딩 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/activity/BaseActivity.kt)
   - **BaseBindingActivity**: DataBinding 자동 바인딩 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/activity/BaseBindingActivity.kt)
   - StatusBar/NavigationBar 색상 제어, Edge-to-edge 대응


  ### ui/fragment
   - **BaseFragment**: Fragment 자동 레이아웃 바인딩 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/fragment/)
   - **BaseBindingFragment**: DataBinding Fragment
   - **dialog**: DialogFragment 기본 구현 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/fragment/dialog/)


  ### ui/adapter
   - **queue**: 안전한 리스트 연산 큐 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/queue/AdapterOperationQueue.kt)
   - **list**: ListAdapter 기반 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/)
     - diffutil: DiffUtil 지원 어댑터
     - simple: 간단한 ListAdapter
   - **normal**: RecyclerView.Adapter 기반 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/)
   - **viewholder**: ViewHolder 기본 구현 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/viewholder/)


  ### ui/view
   - **RecyclerScrollStateView**: 스크롤 방향/엣지 감지 RecyclerView (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateView.kt)
   - 스크롤 상태를 StateFlow로 제공


  ### ui/layout
   - 라이프사이클 인식 레이아웃 클래스 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/layout/)


  ### permissions/manager
   - **PermissionManager**: ActivityResult 기반 권한 요청 오케스트레이터 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/manager/PermissionManager.kt)
   - 일반 권한 + 특수 권한(SYSTEM_ALERT_WINDOW 등) 통합 처리
   - 큐 기반 순차 처리, 재요청 로직
   - **register**: PermissionDelegate, PermissionRequester (simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/register/)


  ### extensions/view
   - **ViewExtensions**: View 공통 확장 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/ViewExtensions.kt)
   - **ViewLayoutExtensions**: 레이아웃 파라미터 조작 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/ViewLayoutExtensions.kt)
   - **ViewAnimExtensions**: 애니메이션 확장 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/ViewAnimExtensions.kt)
   - **ToastExtensions**: Toast 헬퍼 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/ToastExtensions.kt)
   - **SnackBarExtensions**: SnackBar 헬퍼 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/SnackBarExtensions.kt)
   - **TextViewExtensions**: TextView 확장 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/TextViewExtensions.kt)
   - **EditTextExtensions**: EditText 확장 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/EditTextExtensions.kt)
   - **ImageViewExtensions**: ImageView 확장 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/ImageViewExtensions.kt)


  ### extensions/resource
   - **ResourceExtensions**: 리소스 접근 간소화 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/resource/ResourceExtensions.kt)


  ### system_manager/controller
   - **softkeyboard**: 키보드 제어 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/softkeyboard/SoftKeyboardController.kt)
   - **window**: 플로팅 뷰 관리 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/window/)
     - drag: 드래그 가능 플로팅 뷰 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/window/drag/FloatingDragView.kt)
     - fixed: 고정 플로팅 뷰 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/window/fixed/FloatingFixedView.kt)


  ### system_manager/info
   - **display**: 디스플레이 정보 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/info/display/DisplayInfo.kt)



 ## 핵심 설계 원칙

  ### 1. 자동 바인딩 패턴
   - BaseActivity/BaseFragment: layoutId만 지정하면 자동 setContentView/inflate
   - BaseBindingActivity/BaseBindingFragment: DataBinding 자동 생성 및 바인딩
   - 보일러플레이트 최소화


  ### 2. 라이프사이클 통합
   - RootActivity: onCreate에서 시스템 바 자동 설정
   - PermissionManager: ActivityResult API 활용, 라이프사이클 안전
   - RecyclerScrollStateView: LifecycleOwner 인식, 자동 구독 해제


  ### 3. AdapterOperationQueue 안전성
   - 리스트 연산(add, remove, update)을 큐에 쌓아 순차 처리
   - notifyDataSetChanged 등 Adapter 메서드 동기화
   - ConcurrentModificationException 방지


  ### 4. 권한 처리 통합
   - PermissionManager: 일반 권한 + 특수 권한 단일 인터페이스
   - 큐 기반 순차 요청으로 사용자 경험 개선
   - 재요청 로직 내장 (거부 시 설정 화면 이동 옵션)


  ### 5. StateFlow 기반 상태 관리
   - RecyclerScrollStateView: 스크롤 상태를 StateFlow로 제공
   - UI 컴포넌트 상태를 반응형으로 관찰 가능
   - simple_core의 StateFlow 패턴과 일관성 유지



 ## 개발 시 주의사항

  ### XML ID 규칙 (필수 준수)
   - Button: `+id/btn*` 으로 시작 (예: btnSubmit, btnCancel)
   - EditText: `+id/edt*` 으로 시작 (예: edtEmail, edtPassword)
   - TextView: `+id/tv*` 으로 시작 (예: tvTitle, tvDescription)
   - RecyclerScrollStateView: `+id/rcv*` 으로 시작 (예: rcvList, rcvItems)
   - Checkbox: `+id/cb*` 으로 시작 (예: cbAgree, cbRemember)
   - 기타 View: 위와 비슷한 형식으로 일관성 유지
   - **camelCase 사용** (snake_case 금지)


  ### RootActivity 상속 체계
   - RootActivity: 시스템 바, 권한, Edge-to-edge 처리
   - BaseActivity: RootActivity + 자동 레이아웃 바인딩
   - BaseBindingActivity: RootActivity + DataBinding
   - 커스텀 Activity는 이 중 하나를 상속하여 일관성 유지


  ### DataBinding 사용 시
   - BaseBindingActivity/BaseBindingFragment 상속
   - binding 프로퍼티 자동 초기화됨
   - onDestroy에서 binding null 처리 자동


  ### RecyclerView Adapter
   - 리스트 변경 시 AdapterOperationQueue 사용 권장
   - DiffUtil 사용 시 list 패키지의 어댑터 활용
   - ViewHolder는 viewholder 패키지 기본 클래스 참고


  ### 권한 요청
   - RootActivity 기반 클래스에서 PermissionDelegate 자동 초기화
   - requestPermissions() 메서드로 통합 요청
   - 특수 권한(SYSTEM_ALERT_WINDOW 등)도 동일 인터페이스


  ### 시스템 바 제어
   - RootActivity의 setStatusBarColor, setNavigationBarColor 사용
   - API 35 대응 Edge-to-edge 자동 처리
   - 커스텀 배경 뷰 지원


  ### View 확장 함수
   - extensions/view 패키지의 확장 함수 적극 활용
   - Toast, SnackBar 등 공통 UI는 확장 함수로 통일
   - 애니메이션도 ViewAnimExtensions 사용



 ## 테스트 작성 규칙

  ### 테스트 파일 디렉터리 구조
   - 테스트 파일은 반드시 테스트 유형별 패키지에 위치해야 함
   - **단위 테스트**: `src/test/java/kr/open/library/simple_ui/xml/unit/{원본_패키지_경로}/`
   - **Robolectric 테스트**: `src/test/java/kr/open/library/simple_ui/xml/robolectric/{원본_패키지_경로}/`
   - 파일명: 단위 테스트는 `*Test.kt`, Robolectric은 `*RobolectricTest.kt`


  ### testUnit
   - UI 의존성 없는 순수 로직 테스트


  ### testRobolectric
   - Activity/Fragment/View 등 UI 컴포넌트 테스트


  ### 테스트 실행
   - ./gradlew :simple_xml:koverHtmlReport



 ## 모듈 의존성 규칙

  ### 허용되는 의존성
   - simple_core 모듈 (모든 기능 사용 가능)
   - Android SDK (android.*, androidx.*)
   - DataBinding, ViewBinding
   - Activity, Fragment, View 등 UI 컴포넌트


  ### 금지되는 의존성
   - Compose 관련 라이브러리 (향후 simple_compose 모듈에서 처리)
   - 다른 UI 프레임워크



 ## 주요 클래스 역할 요약

  ### RootActivity
   - 모든 Activity의 최상위 기본 클래스
   - 시스템 바 색상/가시성 제어
   - PermissionDelegate 통합
   - Edge-to-edge 대응 (API 35+)


  ### BaseActivity / BaseBindingActivity
   - 자동 레이아웃/DataBinding 바인딩
   - layoutId 지정만으로 setContentView 자동
   - 보일러플레이트 최소화


  ### PermissionManager
   - ActivityResult 기반 권한 요청 큐 관리
   - 일반 권한 + 특수 권한 통합 처리
   - 재요청 로직 내장


  ### AdapterOperationQueue
   - RecyclerView Adapter 리스트 연산 안전성 보장
   - sealed class 기반 연산 모델
   - 순차 처리로 동기화 문제 방지


  ### RecyclerScrollStateView
   - 스크롤 방향(UP/DOWN) 및 엣지(TOP/BOTTOM) 감지
   - StateFlow로 상태 제공
   - 라이프사이클 인식 자동 구독 해제



 ## simple_core와의 관계

  ### simple_xml이 사용하는 simple_core 기능
   - Logx: 모든 로깅
   - 에러 처리 규칙은 루트의 claude.md(에러 처리 규칙)를 참조
   - checkSdkVersion: API 분기
   - PermissionExtensions: 권한 체크 기반
   - BaseViewModel: ViewModel 기반 클래스
   - 단위 변환, 문자열, 날짜 등 확장 함수


  ### simple_xml이 추가 제공하는 기능
   - UI 컴포넌트 (Activity, Fragment, Adapter, View)
   - DataBinding/ViewBinding 자동화
   - 권한 요청 UI 흐름 (PermissionManager)
   - View 확장 함수 (Toast, SnackBar, Animation 등)
   - 키보드, 플로팅 뷰 등 UI 특화 제어


  ### 금지되는 의존성
   - Compose 관련 라이브러리 (향후 simple_compose 모듈에서 처리)
   - 다른 UI 프레임워크
   - **주의**: simple_core는 simple_xml을 의존할 수 없음 (역방향 의존 금지)


 ## 개발 예제 참고

  ### Activity 작성
   - RootActivity 상속 예제: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/activity/RootActivity.kt)
   - BaseActivity 사용법: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/activity/BaseActivity.kt)


  ### Adapter 작성
   - AdapterOperationQueue 활용: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/queue/AdapterOperationQueue.kt)
   - DiffUtil Adapter: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/diffutil/)


  ### 권한 요청
   - PermissionManager 사용: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/manager/PermissionManager.kt)


  ### View 확장
   - Toast/SnackBar: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/)

