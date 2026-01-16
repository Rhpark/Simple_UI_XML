# IMPLEMENTATION PLAN: RecyclerView Adapter Temp

## 목적
- ui.temp 패키지의 Adapter 베이스 설계를 안전하게 적용하기 위한 구현 순서와 검증 기준을 정리합니다.

## 전제
- PRD: `ui/temp/PRD.md` 기준으로 진행합니다.
- DiffUtil은 공통 베이스 + 헬퍼/옵션으로 제공합니다.
- 간단형(simple)은 단일 ViewType 전용입니다.
- Normal 계열은 단일(BaseSingle)과 다중(BaseMulti)을 모두 포함합니다.
- BaseSingle 계열은 단일 ViewType 전용이며 Simple이 이를 상속합니다.
- BaseMulti 계열은 다중 ViewType 전용입니다.
- 간단형은 람다 기반 사용을 허용하며, 일반형은 추상 메서드 기반을 기본으로 합니다.
- 단일 ViewType 사용이 가장 쉽도록 기본 흐름을 설계합니다.

## 단계별 구현 순서
1) 공통 base 확정
   - 공통 인터페이스/헬퍼/정책 확정
   - DiffUtil 옵션/헬퍼 설계
2) ListAdapter(바인딩) 정리
   - list.binding.simple.databind
   - list.binding.simple.viewbind
   - list.binding.normal.databind
   - list.binding.normal.viewbind
3) ListAdapter(Non-binding) 정리
   - list.normal.simple
   - list.normal.normal
4) RecyclerView.Adapter(바인딩) 정리
   - normal.binding.simple.databind
   - normal.binding.simple.viewbind
   - normal.binding.normal.databind
   - normal.binding.normal.viewbind
5) RecyclerView.Adapter(Non-binding) 정리
   - normal.normal.simple
   - normal.normal.normal
6) ViewHolder 계열 정리
   - viewholder.binding
   - viewholder.normal
7) 사용 흐름 정합/예제 정리
   - 간단형/일반형 사용 흐름 확인
   - 다중 ViewType 처리 흐름 확인
8) 테스트 추가
   - UnitTest
   - Robolectric 테스트

## 구현 체크리스트
- RecyclerView.Adapter / ListAdapter 모두 제공
- DataBinding / ViewBinding / Non-binding 모두 제공
- 간단형(simple) / 일반형(normal) 분리
- 간단형은 단일 ViewType 전용
- BaseSingle 계열은 단일 ViewType 전용이며 Simple이 이를 상속합니다.
- BaseMulti 계열은 다중 ViewType 전용입니다.
- DiffUtil 옵션/헬퍼 적용 가능
- 공통 정책은 base 계층으로 집중

## 검증 기준
- :simple_xml 모듈 컴파일 성공
- 다중 ViewType 시나리오 동작 확인
- DiffUtil 옵션 적용 시 정상 동작
- UnitTest/Robolectric 테스트 통과

## 문서화
- README_*.md 반영은 추후 추가 예정