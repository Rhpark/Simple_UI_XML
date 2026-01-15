# IMPLEMENTATION PLAN: Layout Temp

## 목적
- layout.temp 패키지에 신규 설계를 안전하게 적용하기 위한 구현 순서와 검증 기준을 정리합니다.

## 전제
- PRD: `ui/layout/temp/PRD.md` 기준으로 진행합니다.
- ParentBindingHelperForLayout는 유지하고, 이후 변경 여부를 검토합니다.
- 코디네이터는 구체 클래스 + 콜백 인터페이스 조합을 기본으로 합니다.

## 단계별 구현 순서
1) 코디네이터/유틸 확정
   - 파일명/위치 규칙 확정
   - LayoutLifecycleCoordinator, LayoutBindingCoordinator, DataBinding LifecycleOwner 유틸 정의
2) Frame 패키지 적용
   - Root → ParentsBinding → BaseDataBinding → BaseViewBinding → Normal 순서로 적용
3) Linear 패키지 적용
   - Frame과 동일 패턴으로 적용
4) Relative 패키지 적용
   - Frame/Linear와 동일 패턴으로 적용
5) Constraint(temp) 적용
   - 위 패턴과 동일하게 정리
6) 주석 정합(필요 시)
   - 정책 변화(재부착, clearBindingOnDetach 의미) 주석 명확화
   - UTF-8 기준으로 한글 주석 정리
7) 테스트 추가
   - 전체 UnitTest 작성
   - Robolectric 테스트 작성

## 구현 체크리스트
- binding 접근 시점은 attach~detach 구간으로 제한
- event collect는 attach 주기당 1회만 시작
- onInitBind는 createBinding으로 새 binding이 연결될 때만 호출
- clearBindingOnDetach 기본값 true 유지
- clearBindingOnDetach=false 사용 시 재부착 동작 차이를 문서로 명시
- EditMode 가드는 기본적으로 두지 않음(프리뷰는 호출자 책임)
- 전체 UnitTest/Robolectric 테스트 추가

## 검증 기준
- :simple_xml 모듈 컴파일 성공
- attach → detach → attach 흐름에서 정책대로 동작
- DataBinding LifecycleOwner 재시도 정상 동작
- UnitTest/Robolectric 테스트 통과

## 문서화
- README_*.md 반영은 추후 추가 예정

## 결정 필요 항목(진행 전 확정)
- onInitBind 재부착 훅 추가 여부
- 코디네이터 실제 파일명/위치 명명 규칙
- 재시도 정책(횟수/간격) 상세값
