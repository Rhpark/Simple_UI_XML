# 코드 패턴 인덱스

코드 작성 시 라이브러리가 제공하는 패턴을 우선 사용한다.
세부 규칙은 patterns/ 하위 파일을 참조한다.

## 패턴 목록

| 파일 | 목적 | 핵심 규칙 |
|------|------|----------|
| patterns/CODE_PATTERNS_LOGGING.md | 로깅 | Log.d, println 금지 → Logx 사용 |
| patterns/CODE_PATTERNS_EXCEPTION.md | 예외 처리 | 빈 catch 금지 → safeCatch 사용 |
| patterns/CODE_PATTERNS_SDK.md | SDK 버전 분기 | SDK_INT 직접 비교 금지 → checkSdkVersion 사용 |
| patterns/CODE_PATTERNS_PERMISSION.md | 권한 요청 | registerForActivityResult 금지 → requestPermissions 사용 |
| patterns/CODE_PATTERNS_XML.md | XML 레이아웃 | 반복 속성 직접 작성 금지 → Simple UI Style 사용 |
| patterns/CODE_PATTERNS_ACTIVITY_FRAGMENT.md | Activity/Fragment | Base 클래스 상속, getViewModel(), repeatOnLifecycle 필수 |
| patterns/CODE_PATTERNS_MVVM.md | MVVM 이벤트 | Channel 직접 구성 금지 → BaseViewModelEvent + sendEventVm 사용 |
| patterns/CODE_PATTERNS_RECYCLERVIEW.md | RecyclerView/Adapter | Adapter 직접 구현 금지 → Simple UI Adapter 사용 |
| patterns/CODE_PATTERNS_EXTENSIONS.md | 확장 함수 | Toast/dp변환/중복클릭 직접 구현 금지 → 확장 함수 사용 |

## 패턴 적용 기준

- 로깅 코드 작성 시 → CODE_PATTERNS_LOGGING.md
- try/catch 작성 시 → CODE_PATTERNS_EXCEPTION.md
- SDK 버전 분기 작성 시 → CODE_PATTERNS_SDK.md
- 권한 요청 코드 작성 시 → CODE_PATTERNS_PERMISSION.md
- XML 레이아웃 작성 시 → CODE_PATTERNS_XML.md
- Activity/Fragment 작성 시 → CODE_PATTERNS_ACTIVITY_FRAGMENT.md
- ViewModel 이벤트 시스템 작성 시 → CODE_PATTERNS_MVVM.md
- RecyclerView/Adapter 작성 시 → CODE_PATTERNS_RECYCLERVIEW.md
- Toast/단위변환/클릭처리 등 UI 코드 작성 시 → CODE_PATTERNS_EXTENSIONS.md
