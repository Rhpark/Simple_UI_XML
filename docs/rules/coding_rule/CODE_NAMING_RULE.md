# 명명 규칙 (Naming Rule)

## 클래스 / 파일

- 클래스: PascalCase
- 파일명: 클래스명과 동일 (PascalCase)

## 함수 / 변수

- 함수: camelCase
- 변수: camelCase
- Boolean 변수: is / has / should / can 접두사 필수

## XML ID

- camelCase 사용 (snake_case 금지)
- 접두사 규칙:
  - Button → btn*
  - EditText → edt*
  - TextView → tv*
  - RecyclerView → rcv*
  - CheckBox → cb*

## 심각도 기준

- HIGH: XML ID snake_case 사용
- MEDIUM: 네이밍 규칙 위반

## 예시

### XML ID snake_case 사용

❌ BAD
```xml
<Button android:id="@+id/btn_submit_payment" />
```

✅ GOOD
```xml
<Button android:id="@+id/btnSubmitPayment" />
```
