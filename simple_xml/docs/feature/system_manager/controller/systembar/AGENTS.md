# SystemBar Feature AGENT

## 역할
- 이 문서는 `system_manager/controller/systembar` 기능 문서의 인덱스입니다.
- 구현/분석/수정 시 아래 문서를 순서대로 확인합니다.

## 문서 순서
1. `PRD.md`
2. `SPEC.md`
3. `IMPLEMENTATION_PLAN.md`
4. `docs/readme/system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md`

## 실제 사용 경로(중요)
- **권장 진입점**: `Window.getSystemBarController()`
- **정리 경로**: `Window.destroySystemBarControllerCache()`
- 직접 생성(`SystemBarController(window)`)보다 Window 확장 함수 경로를 우선 사용합니다.

## 관련 코드 범위
- 컨트롤러
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/SystemBarController.kt`
- 상태 모델
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/model/SystemBarState.kt`
- 내부 헬퍼
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/internal/helper/StatusBarHelper.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/internal/helper/NavigationBarHelper.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/controller/systembar/internal/helper/base/SystemBarHelperBase.kt`
- 확장 함수/캐시 태그
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/system_manager/extensions/SystemServiceExtensionsXml.kt`
  - `simple_xml/src/main/res/values/ids.xml`

## 테스트 확인 경로
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/system_manager/controller/systembar/internal/helper/SystemBarHelperStateRobolectricTest.kt`
