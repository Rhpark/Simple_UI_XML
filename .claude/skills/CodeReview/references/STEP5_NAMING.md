# Step 5. 명명 규칙(스타일)

프로젝트 컨벤션에 맞게 작성되었는가?

> 규칙 기준: docs/rules/CODING_RULE_INDEX.md
>
> 기계적 명명 규칙(클래스 PascalCase, 함수·변수 camelCase 등)은 ktlint·lint가 자동 검출하므로 리뷰에서 중복 점검하지 않는다.
> XML ID 규칙은 `/XmlInspector` 스킬이 담당한다.
> 이 단계는 **린터가 못 잡는 의미적 명명 + 프로젝트 커스텀 규칙(Logx, checkSdkVersion)** 에 집중한다.

## 체크리스트 (린터가 못 잡는 항목)

- [ ] 이름이 의도를 드러내는가? (모호한 이름·과도한 약어 없는가 — 의미적 명명)
- [ ] Boolean 변수: is/has/should/can 접두사
- [ ] 로깅: Logx 사용 (Log.d, println 금지) — 프로젝트 커스텀
- [ ] SDK 버전 분기: checkSdkVersion 패턴 사용 — 프로젝트 커스텀

## 심각도 기준

- HIGH: Logx 미사용 (Log.d, println 사용)
- HIGH: SDK 직접 분기 사용 (if Build.VERSION...)
- MEDIUM: 의미가 불명확한 명명 (의도 전달 실패)
