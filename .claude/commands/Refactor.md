docs/rules/CODING_RULE.md 의 코드 작성 금지 패턴을 읽고,
$ARGUMENTS 에 해당하는 범위 전체에서 위반 사항을 찾아 수정해줘.

Task tool (subagent_type: general-purpose, model: opus) 을 사용해서 아래 작업을 수행해줘.

## SubAgent 작업 내용
1. $ARGUMENTS 범위의 모든 .kt 파일을 탐색한다.
2. CODING_RULE.md 금지 패턴 위반 항목을 파일별로 수집한다.
    - Log.d / Log.e / println 등 → Logx 교체 대상
    - try-catch 빈 catch 블록 → safeCatch 교체 대상
    - if (Build.VERSION.SDK_INT >= ...) → checkSdkVersion 교체 대상
    - deprecated API 사용 탐지
3. 파일별 위반 목록과 수정 방향을 정리해서 반환한다.

## SubAgent 결과 처리
4. SubAgent 결과를 직접 검토하여 교차 검증한다.
5. 전체 위반 목록을 사용자에게 고지하고 승인을 요청한다.
6. 승인 후 파일에 직접 반영한다.
7. 수정된 항목 목록과 성공/실패 여부를 요약해서 알려준다.