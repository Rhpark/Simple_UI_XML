# Android 프로젝트 모듈 매핑 규칙

analysis · review 에이전트가 모듈 판단 시 이 파일을 읽는다.

## 모듈 목록

관련 모듈이 확정되면 아래 매핑에 따라 해당 모듈 `AGENTS.md`를 읽는다.

- `simple_core` → `simple_core/AGENTS.md`
- `simple_xml` → `simple_xml/AGENTS.md`
- `simple_system_manager` → `simple_system_manager/AGENTS.md`
- `simple_compose` → `simple_compose/AGENTS.md`

관련 모듈이 불명확하면 STEP1 진입 전에 사용자에게 먼저 확인한다.
