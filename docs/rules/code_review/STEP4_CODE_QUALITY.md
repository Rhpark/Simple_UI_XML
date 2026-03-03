# Step 4. 코드 품질

불필요한 코드가 없는가?

## 체크리스트

- [ ] 미사용 import가 없는가?
- [ ] 미사용 변수, 함수가 없는가?
- [ ] 중복 로직이 없는가?
- [ ] deprecated API를 사용하지 않는가?
- [ ] 존재하지 않는 API/클래스/메서드를 사용하지 않는가? (hallucination 방지)
- [ ] 주석 처리된 코드가 남아있지 않은가?

## 심각도 기준

- HIGH: 존재하지 않는 API 사용 (hallucination)
- HIGH: deprecated API 신규 사용
- MEDIUM: 미사용 코드, 중복 로직
