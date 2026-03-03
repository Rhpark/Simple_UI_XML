# Step 5. 명명 규칙

프로젝트 컨벤션에 맞게 작성되었는가?

> 규칙 기준: docs/rules/CODING_RULE_INDEX.md

## 체크리스트

- [ ] 클래스: PascalCase
- [ ] 함수/변수: camelCase
- [ ] XML ID: camelCase (snake_case 금지), 접두사 규칙 준수
- [ ] Boolean 변수: is/has/should/can 접두사
- [ ] 로깅: Logx 사용 (Log.d, println 금지)
- [ ] SDK 버전 분기: checkSdkVersion 패턴 사용

## 심각도 기준

- HIGH: Logx 미사용 (Log.d, println 사용)
- HIGH: XML ID snake_case 사용
- HIGH: SDK 직접 분기 사용 (if Build.VERSION...)
- MEDIUM: 네이밍 규칙 위반
