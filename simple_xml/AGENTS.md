# simple_xml 모듈 개요
 - **전역 규칙은 루트 AGENTS.md 참조**
 - 주석 스타일, 코딩 컨벤션, 대화 규칙 등은 루트 AGENTS.md를 따름
 - **현재 버전**: 0.4.16 (JitPack)
 - **Maven 좌표**: `com.github.Rhpark:Simple_UI_XML:0.4.16`



 ## 기능별 전용 규칙

  - ui/adapter (행동 규칙 포함): simple_xml/docs/feature/ui/adapter/AGENTS.md
  - permissions (리팩터링 계획): simple_xml/docs/feature/permissions/


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
   - BaseActivity, BaseDataBindingActivity 등 바인딩 자동화
   - View 확장 함수로 XML UI 조작 간소화

  ### 라이프사이클 관리
   - Activity/Fragment 생명주기 통합 관리
   - PermissionRequester의 ActivityResult 기반 권한 처리
   - RecyclerView ListAdapter 계열의 안전한 리스트 연산 큐(AdapterOperationQueue)



 ## 주요 패키지 구조 (총 125개 파일)

  ### ui/activity
   - **RootActivity**: 시스템 바 제어, 권한 관리 기본 클래스 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/activity/root/RootActivity.kt)
   - **BaseActivity**: 자동 레이아웃 바인딩 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/activity/normal/BaseActivity.kt)
   - **BaseDataBindingActivity**: DataBinding 자동 바인딩 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/activity/binding/BaseDataBindingActivity.kt)
   - StatusBar/NavigationBar 색상 제어, Edge-to-edge 대응


  ### ui/fragment
   - **BaseFragment**: Fragment 자동 레이아웃 바인딩 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/fragment/)
   - **BaseDataBindingFragment**: DataBinding Fragment
   - **dialog**: DialogFragment 기본 구현 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/fragment/dialog/)


  ### ui/adapter

   - 세부 구조 및 코드 범위: simple_xml/docs/feature/ui/adapter/AGENTS.md 참조


  ### ui/view
   - **RecyclerScrollStateView**: 스크롤 방향/엣지 감지 RecyclerView (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateView.kt)
   - 스크롤 상태를 SharedFlow로 제공


  ### ui/layout
   - 라이프사이클 인식 레이아웃 클래스 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/layout/)


  ### permissions/api
   - **PermissionRequester**: ActivityResult 기반 권한 요청 오케스트레이터 (simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/api/PermissionRequester.kt)
   - 일반 권한 + 특수 권한(SYSTEM_ALERT_WINDOW 등) 통합 처리
   - 큐 기반 순차 처리, 재요청 로직
   - **register**: PermissionRequestInterface (simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/register/PermissionRequestInterface.kt)


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





 ## 핵심 설계 원칙

  ### 1. 자동 바인딩 패턴
   - BaseActivity/BaseFragment: layoutId만 지정하면 자동 setContentView/inflate
   - BaseDataBindingActivity/BaseDataBindingFragment: DataBinding 자동 생성 및 바인딩
   - 보일러플레이트 최소화


  ### 2. 라이프사이클 통합
   - RootActivity: onCreate에서 시스템 바 자동 설정
   - PermissionRequester: ActivityResult API 활용, 라이프사이클 안전
   - RecyclerScrollStateView: LifecycleOwner 인식, 자동 구독 해제


  ### 3. Adapter 업데이트 전략 분리
   - list 패키지(`BaseRcvListAdapter`)는 DiffUtil + 큐 기반으로 리스트 연산(add, remove, update)을 순차 처리
   - normal 패키지(`BaseRcvAdapter`, `HeaderFooterRcvAdapter`)는 내부 리스트를 즉시 갱신하고 notify 계열 API로 반영
   - normal/list를 분리해 동기화 안정성, 실패 전달 방식, 업데이트 비용의 트레이드오프를 명확히 관리


  ### 4. Adapter 결과 모델 분리
   - normal 패키지는 `NormalAdapterResult` 기반 동기 결과 모델 사용
   - list 패키지는 `ListAdapterResult` 기반 비동기 결과 모델 사용
   - 결과 모델을 분리해 즉시 반영과 큐 기반 처리의 실패 의미를 혼동하지 않도록 설계


  ### 5. 권한 처리 통합
   - PermissionRequester: 일반 권한 + 특수 권한 단일 인터페이스
   - 큐 기반 순차 요청으로 사용자 경험 개선
   - 재요청 로직 내장 (거부 시 설정 화면 이동 옵션)


  ### 6. SharedFlow 기반 스크롤 상태 관리
   - RecyclerScrollStateView: 스크롤 상태를 SharedFlow로 제공
   - UI 컴포넌트 상태를 반응형으로 관찰 가능
   - simple_core의 Flow 패턴과 일관성 유지



 ## 개발 시 주의사항

  ### XML ID 규칙 (필수 준수)
   - **.claude/skills/CodeReview/rules/CODING.md의 "XML Id 규칙" 참조**
   - **camelCase 사용** (snake_case 금지)


  ### RootActivity 상속 체계
   - RootActivity: 시스템 바, 권한, Edge-to-edge 처리
   - BaseActivity: RootActivity + 자동 레이아웃 바인딩
   - BaseDataBindingActivity: RootActivity + DataBinding
   - 커스텀 Activity는 이 중 하나를 상속하여 일관성 유지


  ### DataBinding 사용 시
   - BaseDataBindingActivity/BaseDataBindingFragment 상속
   - binding 프로퍼티 자동 초기화됨
   - onDestroy에서 binding null 처리 자동


  ### RecyclerView Adapter

   - 어댑터 선택 기준 및 사용 원칙: simple_xml/docs/feature/ui/adapter/AGENTS.md 참조


  ### 권한 요청
   - RootActivity 기반 클래스에서 PermissionDelegate 자동 초기화
   - requestPermissions() 메서드로 통합 요청
   - 특수 권한(SYSTEM_ALERT_WINDOW 등)도 동일 인터페이스




  ### View 확장 함수
   - extensions/view 패키지의 확장 함수 적극 활용
   - Toast, SnackBar 등 공통 UI는 확장 함수로 통일
   - 애니메이션도 ViewAnimExtensions 사용


 ## 테스트 작성 규칙
  - **.claude/skills/CodeReview/rules/TEST.md 참조**


 ## 모듈 의존성 규칙

  ### 허용되는 의존성
   - simple_core 모듈 (모든 기능 사용 가능)
   - Android SDK (android.*, androidx.*)
   - DataBinding, ViewBinding
   - Activity, Fragment, View 등 UI 컴포넌트



 ## 주요 클래스 역할 요약

  ### RootActivity
   - 모든 Activity의 최상위 기본 클래스
   - PermissionRequester 통합 (requestPermissions() 공개 API)
   - beforeOnCreated() 훅으로 super.onCreate() 이전 초기화 지원
   - 권한 상태 Bundle 저장/복원 (onSaveInstanceState / onCreate)


  ### BaseActivity / BaseDataBindingActivity
   - 자동 레이아웃/DataBinding 바인딩
   - layoutId 지정만으로 setContentView 자동
   - 보일러플레이트 최소화


  ### PermissionRequester
   - ActivityResult 기반 권한 요청 큐 관리
   - 일반 권한 + 특수 권한 통합 처리
   - 재요청 로직 내장


  ### AdapterOperationQueue (ListAdapter 계열)
   - BaseRcvListAdapter 리스트 연산 안전성 보장
   - sealed class 기반 연산 모델
   - 순차 처리로 동기화 문제 방지
   - queue policy를 통해 backpressure 동작 제어 가능


  ### RootRcvAdapter / BaseRcvAdapter / HeaderFooterRcvAdapter
   - `RootRcvAdapter`: normal 어댑터 공통 기반, 스레드 가드/클릭/legacy bridge 공통 처리
   - `BaseRcvAdapter`: content 전용 normal 어댑터
   - `HeaderFooterRcvAdapter`: Header / Content / Footer 섹션을 포함하는 normal 어댑터


  ### RecyclerScrollStateView
   - 스크롤 방향(UP/DOWN) 및 엣지(TOP/BOTTOM) 감지
   - SharedFlow로 상태 제공
   - 라이프사이클 인식 자동 구독 해제



 ## simple_core와의 관계

  ### simple_xml이 사용하는 simple_core 기능
   - Logx: 모든 로깅
   - 에러 처리 규칙은 루트의 .claude/skills/CodeReview/rules/CODING.md를 참조
   - checkSdkVersion: API 분기
   - PermissionExtensions: 권한 체크 기반
   - BaseViewModel: ViewModel 기반 클래스
   - 단위 변환, 문자열, 날짜 등 확장 함수


  ### simple_xml이 추가 제공하는 기능
   - UI 컴포넌트 (Activity, Fragment, Adapter, View)
   - DataBinding/ViewBinding 자동화
   - 권한 요청 UI 흐름 (PermissionRequester)
   - View 확장 함수 (Toast, SnackBar, Animation 등)
   - 다이얼로그/윈도우 크기 계산 등 XML UI 보조 제어


  ### 금지되는 의존성
   - Compose 관련 라이브러리 (향후 simple_compose 모듈에서 처리)
   - 다른 UI 프레임워크
   - **주의**: simple_core는 simple_xml을 의존할 수 없음 (역방향 의존 금지)


 ## 개발 예제 참고

  ### Activity 작성
   - RootActivity 상속 예제: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/activity/root/RootActivity.kt)
   - BaseActivity 사용법: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/activity/normal/BaseActivity.kt)


  ### Adapter 작성

   - 코드 경로 및 예제: simple_xml/docs/feature/ui/adapter/AGENTS.md 참조


  ### 권한 요청
   - PermissionRequester 사용: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/api/PermissionRequester.kt)


  ### View 확장
   - Toast/SnackBar: (simple_xml/src/main/java/kr/open/library/simple_ui/xml/extensions/view/)




## simple_system_manager와의 관계

  ### 분리 원칙
   - `simple_xml`은 `simple_system_manager`를 직접 의존하지 않음
   - system_manager 기능은 별도 모듈 `simple_system_manager`가 소유
   - `DialogConfig` 등 `simple_xml` 내부 UI 기능은 Android 표준 API 또는 모듈 내부 helper로 처리

  ### system_manager 기능이 필요할 때
   - 상태바/소프트키보드/플로팅 뷰/display info/system service controller가 필요하면 `simple_system_manager`를 직접 의존
   - 관련 문서는 `simple_system_manager/docs/feature/system_manager/`와 `docs/readme/system_manager/`를 우선 참조
