docs/rules/README_RULE.md 의 매핑 규칙을 읽고,
아래 우선순위에 따라 변경된 기능을 파악해서 해당 README를 업데이트해줘.

## 대상 결정 우선순위
1. $ARGUMENTS 가 있으면 → 해당 기능명/파일을 변경 대상으로 한다.
2. $ARGUMENTS 가 없으면 → 현재 IDE에서 선택된 코드(ide_selection)를 변경 대상으로 한다.

## 작업 순서
1. docs/rules/README_RULE.md 의 매핑으로 업데이트할 README 파일을 결정한다.
2. 해당 README 파일을 읽어 현재 내용을 파악한다.
3. 변경 사항에 맞게 README 내용을 업데이트한다.
4. 업데이트 원칙(새 기능/Breaking change/예제 코드)에 맞게 작성되었는지 검증한다.
5. 검증을 통과하면 README 파일에 직접 반영한다.
6. 업데이트된 파일 목록과 성공/실패 여부를 요약해서 알려준다.