# simple_system_manager 모듈 개요
- 전역 규칙과 코딩/문서 규칙은 루트 [AGENTS.md](/d:/Android%20Project/SimpleUI_XML/AGENTS.md)를 우선 참조합니다.
- 이 모듈은 `simple_core`에만 의존하며, `system_manager` 기능을 독립적으로 제공합니다.

## 모듈 정의
- `kr.open.library.simple_ui.system_manager.core.*`
- `kr.open.library.simple_ui.system_manager.xml.*`
- 위 두 패키지를 같은 모듈에서 함께 소유합니다.

## 핵심 원칙
- `system_manager` 관련 source package와 테스트 패키지는 모두 `simple_system_manager` 기준으로 정리합니다.
- `simple_xml`은 이 모듈을 의존하지 않습니다.
- system_manager 기능이 필요한 앱/소비자는 `simple_system_manager`를 직접 의존합니다.
- `simple_core`에는 공통 유틸만 남기고 `system_manager` main/test 소스는 이 모듈이 소유합니다.

## 우선 참조 문서
- 계획 문서: [PLAN.md](/d:/Android%20Project/SimpleUI_XML/docs/planning/module_separation/PLAN.md)
- 실행 체크리스트: [IMPLEMENTATION_CHECKLIST.md](/d:/Android%20Project/SimpleUI_XML/docs/planning/module_separation/IMPLEMENTATION_CHECKLIST.md)
- README 인덱스: [README.md](/d:/Android%20Project/SimpleUI_XML/README.md)
- system_manager 문서 인덱스:
  - [README_SYSTEM_MANAGER_EXTENSIONS.md](/d:/Android%20Project/SimpleUI_XML/docs/readme/system_manager/README_SYSTEM_MANAGER_EXTENSIONS.md)
  - [README_SERVICE_MANAGER_CONTROL.md](/d:/Android%20Project/SimpleUI_XML/docs/readme/system_manager/controller/README_SERVICE_MANAGER_CONTROL.md)
  - [README_SERVICE_MANAGER_INFO.md](/d:/Android%20Project/SimpleUI_XML/docs/readme/system_manager/info/README_SERVICE_MANAGER_INFO.md)

## 기능별 전용 규칙

- 기능 문서 위치 패턴: `simple_system_manager/docs/feature/system_manager/<controller|info>/<기능명>/`
  - 각 기능별 PRD.md / SPEC.md / IMPLEMENTATION_PLAN.md 포함
- systembar (행동 규칙 포함): simple_system_manager/docs/feature/system_manager/controller/systembar/AGENTS.md

## 검증 원칙
- 작은 그룹으로 수정하고 즉시 검증합니다.
- 검증 실패 상태에서 다음 단계로 진행하지 않습니다.
- 기본 검증 순서:
  - `./gradlew :simple_system_manager:assembleDebug`
  - `./gradlew :simple_system_manager:compileDebugUnitTestKotlin`
  - `./gradlew :simple_system_manager:testAll`
