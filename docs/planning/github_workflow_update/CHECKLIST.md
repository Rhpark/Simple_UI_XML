# .github 반영 체크리스트

## 목적
- `simple_system_manager` 모듈 추가 이후 `.github` 설정이 현재 모듈 구조를 정확히 반영하도록 정리한다.
- 모든 작업은 `검토 -> 수정 -> 검증` 순서로 진행한다.
- 검증이 실패한 상태에서는 다음 단계로 진행하지 않는다.

## 대상 파일
- [.github/workflows/android-ci.yml](/d:/Android%20Project/SimpleUI_XML/.github/workflows/android-ci.yml)
- [.github/workflows/android-cd.yml](/d:/Android%20Project/SimpleUI_XML/.github/workflows/android-cd.yml)
- [.github/workflows/documentation.yml](/d:/Android%20Project/SimpleUI_XML/.github/workflows/documentation.yml)
- [.github/ISSUE_TEMPLATE/action-error.yml](/d:/Android%20Project/SimpleUI_XML/.github/ISSUE_TEMPLATE/action-error.yml)

## 작업 원칙
- 각 단계 시작 전 영향 범위와 수정 포인트를 다시 검토한다.
- 각 단계 수정 후 관련 문자열 검색과 파일 재검토로 즉시 검증한다.
- 워크플로 파일 수정 시 YAML 구조와 경로 문자열을 함께 검증한다.
- 최종 단계에서는 `.github` 전체 잔여 참조 검색을 다시 수행한다.

## 1단계 CI 워크플로 반영
### 검토
- `android-ci.yml`에서 `simple_system_manager`가 빠진 작업을 식별한다.
- 테스트 결과 업로드, 리포트 업로드, coverage 생성/업로드 범위를 확인한다.
- `apiCheck`, `koverHtmlReport`, `test-results`, `reports/tests`, `reports/kover/html` 경로를 확인한다.

### 수정
- `simple_system_manager`를 CI 테스트 및 coverage 대상에 추가한다.
- 결과 업로드 경로에 `simple_system_manager`를 추가한다.

### 검증
- `android-ci.yml` 내 `simple_system_manager` 누락 검색
- 업로드 path 누락 여부 재검토

## 2단계 CD 워크플로 반영
### 검토
- `android-cd.yml`의 coverage relay 구조를 확인한다.
- `simple_core`, `simple_xml`만 처리하는 부분을 찾는다.

### 수정
- coverage relay와 downstream artifact에 `simple_system_manager`를 추가한다.
- coverage 구조 검증 조건을 새 모듈 포함 기준으로 갱신한다.

### 검증
- `android-cd.yml` 내 coverage relay 경로 재검색
- 새 모듈 포함 여부 확인

## 3단계 documentation 워크플로 반영
### 검토
- `documentation.yml`의 AGENTS 버전 갱신 대상과 docs coverage 복사 범위를 확인한다.
- `simple_system_manager/AGENTS.md`와 coverage docs 반영 누락 여부를 확인한다.

### 수정
- `simple_system_manager/AGENTS.md` 버전 갱신 단계를 추가한다.
- docs coverage 복사와 검증에 `simple_system_manager`를 추가한다.
- commit 대상 파일 목록에 필요한 경우 `simple_system_manager/AGENTS.md`를 포함한다.

### 검증
- `documentation.yml` 내 `simple_system_manager` 문자열 재검색
- coverage 복사/검증 경로 일치 여부 확인

## 4단계 템플릿 및 문구 정리
### 검토
- `.github` 내 삭제된 `simple_ui` 예시와 옛 모듈 문구를 찾는다.

### 수정
- `action-error.yml`의 예시 문구를 현재 모듈 구조 기준으로 교체한다.
- 필요 시 관련 설명 문구도 함께 정리한다.

### 검증
- `.github` 전체에서 `simple_ui` 검색
- 옛 모듈 예시 잔존 여부 확인

## 5단계 최종 재검증
### 검토
- `.github` 전체에서 `simple_system_manager`, `simple_core`, `simple_xml`, `simple_ui` 참조를 다시 검색한다.

### 검증
- `.github` 전체 잔여 참조 검색
- 수정 파일 UTF-8 및 글자 깨짐 검사
- 변경 diff 최종 검토
