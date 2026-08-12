# VERSION RULES

## 버전 관리 목적
 - 릴리즈 기준을 일관되게 유지한다.
 - 버전 증분(Major/Minor/Patch) 판단 기준을 명확히 한다.
 - 태그/릴리즈 트리거 조건을 표준화한다.

## 버전 변경 절차
 - `gradle/libs.versions.toml`의 `appVersion` 업데이트
 - 릴리즈 메타 정보 작성
   - Tag: `x.x.x` (`appVersion`과 동일)
   - Title: 릴리즈 제목
   - Describe: 상세 설명
 - 릴리즈 트리거 조건 확인
   - `[release]` 태그가 있는 커밋만 릴리즈 트리거
   - `[release]` 태그가 없으면 CI만 수행

## 버전 번호 규칙
 - `Major.Minor.Patch` (Semantic Versioning)
 - `0.x` 개발 단계
   - 호환되지 않는 API 변경 또는 새 기능 추가: Minor 증가
   - 호환 가능한 버그 수정: Patch 증가
 - `1.0.0` 이상 안정 단계
   - 호환되지 않는 API 변경: Major 증가
   - 호환 가능한 새 기능 추가: Minor 증가
   - 호환 가능한 버그 수정: Patch 증가
 - `1.0.0`은 공개 API 안정성과 이후 호환성 관리 정책을 선언할 준비가 되었을 때 발행한다.

## CI/CD 연계 규칙
 - 버전 정책과 연결되는 파이프라인 상세는 `docs/rules/project/CI_CD_RULE.md`를 따른다.
 - 릴리즈 태그/버전 불일치 상태에서 배포를 진행하지 않는다.
