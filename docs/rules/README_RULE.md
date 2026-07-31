### README 파일 매핑
- Activity/Fragment 변경 → docs/readme/README_ACTIVITY_FRAGMENT.md
- 확장 함수 추가/변경 → docs/readme/README_EXTENSIONS.md
- Logx 기능 변경 → docs/readme/README_LOGX.md
- ViewModel 관련 → docs/readme/README_MVVM.md
- 권한 판별/요청 흐름 → docs/readme/README_PERMISSION.md
- Compose 기능 추가/변경 → docs/readme/README_COMPOSE.md
- RecyclerView 관련 → docs/readme/README_RECYCLERVIEW.md
- System Manager Info → docs/readme/system_manager/info/README_SERVICE_MANAGER_INFO.md
- System Manager Control → docs/readme/system_manager/controller/README_SERVICE_MANAGER_CONTROL.md
- System Manager 확장 진입점 → docs/readme/system_manager/README_SYSTEM_MANAGER_EXTENSIONS.md
- System Manager 공개 API 이전 → docs/readme/system_manager/README_SYSTEM_MANAGER_MIGRATION.md
- 스타일 가이드 변경 → docs/readme/README_STYLE.md
- 샘플 앱 변경 → docs/readme/README_SAMPLE.md
- 시작 가이드 변경 → docs/readme/README_START.md
- 전체 개요 변경 → README.md

### 업데이트 원칙
- 새 기능 추가 시: 해당 README + README.md 개요 섹션
- Breaking change: 해당 README + README.md 버전 정보
- 예제 코드 변경: 반드시 실행 가능한 코드로 유지

### 사용자용 README 영문·한글 병기 규칙

- 사용자용 설명은 영문을 먼저 작성하고, 바로 다음 한글 인용문(`>`)에 같은 의미를 작성한다.
- 제목은 `English title (한글 제목)` 형식을 기본으로 한다.
  - 최상위 제목이 제품명이나 문서명인 경우에는 다음 줄의 한글 인용문으로 제목을 병기할 수 있다.
- 짧은 메타데이터, 용어 정의, 한 줄짜리 설명은 `English (한글)` 형식으로 같은 줄에 병기할 수 있다.
- 목록은 영문 목록 다음에 같은 순서의 한글 인용문 목록을 작성한다.
  - 항목이 짧으면 각 항목에서 `English (한글)` 형식으로 병기할 수 있다.
- 표는 같은 셀에서 `English<br>한글` 형식으로 병기하거나, 영문 표 다음에 같은 내용을 한글 인용문 목록으로 작성한다.
- 코드 블록, 명령어, 파일 경로, Maven 좌표, 패키지·클래스·함수명, API/SDK 버전 표기는 번역하지 않는다.
- 코드 예제는 언어별로 중복 작성하지 않는다. 설명이나 주석의 의미 전달이 필요한 경우에만 영문과 한글을 함께 작성한다.
- 영문과 한글 설명은 정보의 범위, 제약, 권장 수준이 서로 같아야 한다.
- 줄바꿈은 빈 줄 또는 `<br>`을 사용한다. 닫는 태그가 없는 HTML 요소인 `br`에 `</br>` 또는 `<br></br>`를 사용하지 않는다.
- 성능 향상률, 코드 감소율, 사용자 후기처럼 검증이 필요한 표현은 재현 가능한 측정 기준이나 출처가 있을 때만 사용한다.
- 한 언어만 작성할 수 있는 예외는 다음과 같다.
  - 라이선스 원문과 외부 서비스가 제공하는 고유 문구
  - 번역 대상이 아닌 기술 식별자만으로 구성된 제목
  - 이전 API의 정확한 명칭을 보존해야 하는 마이그레이션 표
