---
name: JsonConvert
description: JSON을 Kotlin Data Class로 변환하여 파일로 저장한다. 중첩 객체는 하위 패키지로 생성. "JSON 변환", "Data Class 만들어줘", "JSON을 코틀린으로" 등의 요청 시 사용.
disable-model-invocation: true
argument-hint: "{패키지명.클래스명} {JSON 내용}"
---

# JsonConvert Skill

JSON을 Kotlin Data Class로 변환하여 지정 패키지 경로에 파일로 저장한다.

## 호출 형식

```
/JsonConvert {패키지명.클래스명} {JSON}
```

**예시:**
```
/JsonConvert kr.open.library.simple_ui.core.model.UserInfo {"userId": 1, "name": "홍길동", "address": {"city": "서울"}}
```

---

## 실행 흐름

### STEP 0. 입력 파싱

`$ARGUMENTS`에서 FQCN과 JSON을 분리한다.

#### FQCN 분리 규칙
- 첫 번째 `{` 또는 `[` 문자 앞까지 (공백 제거) → FQCN
- 첫 번째 `{` 또는 `[` 부터 끝까지 → JSON 내용

#### FQCN → 패키지명 + 클래스명 분리
- 마지막 세그먼트가 **대문자로 시작** → 클래스명
- 나머지 세그먼트 → 패키지명

```
kr.open.library.simple_ui.core.model.UserInfo
→ 패키지명: kr.open.library.simple_ui.core.model
→ 클래스명: UserInfo
```

**실패 조건 (즉시 중단):**
- FQCN이 없는 경우
- 마지막 세그먼트가 소문자로 시작하는 경우 (클래스명 불명확)
- JSON 파싱이 불가능한 경우 (문법 오류 등)

파싱 완료 후, 아래 형식으로 반드시 출력한 뒤 STEP 1로 진행한다.
```
> FQCN: {FQCN}
> 패키지: {패키지명}
> 클래스: {클래스명}
```

---

### STEP 1. 모듈 자동 감지

패키지명 prefix로 저장할 모듈을 결정한다.

| 패키지 prefix | 모듈 | 소스 루트 경로 |
|---|---|---|
| `kr.open.library.simple_ui.core` | `simple_core` | `simple_core/src/main/java/` |
| `kr.open.library.simple_ui.xml` | `simple_xml` | `simple_xml/src/main/java/` |
| 그 외 | `app` | `app/src/main/java/` |

---

### STEP 1-1. 직렬화 라이브러리 감지

감지된 모듈의 `build.gradle.kts` (또는 `build.gradle`)를 직접 읽어 직렬화 라이브러리를 확인한다.

| 감지 키워드 | 라이브러리 | 어노테이션 스타일 |
|---|---|---|
| `kotlinx-serialization` / `kotlin.serialization` | kotlinx.serialization | `@Serializable` + `@SerialName` |
| `gson` | Gson | `@SerializedName` |
| `moshi` | Moshi | `@Json(name = "...")` |
| `jackson` | Jackson | `@JsonProperty` |
| 없음 | 없음 | 순수 Data Class (어노테이션 없음) |

#### 라이브러리가 없는 경우

아래 질문을 사용자에게 한다.

```
⚠️ 직렬화 라이브러리를 찾을 수 없습니다.

어떻게 진행할까요?
1) 어노테이션 없이 순수 Data Class 생성
2) kotlinx.serialization 으로 진행
3) Gson 으로 진행
4) Moshi 로 진행
5) Jackson 으로 진행
6) 기타 (라이브러리명 직접 입력)
7) 취소
```

- **1 선택** → 어노테이션 없이 생성, STEP 6에 안내 포함
- **2~5 선택** → 아래 추가 질문 진행
- **6 선택** → 아래 기타 처리 진행
- **7 선택** → 즉시 중단

##### 2~5 선택 시 추가 질문 1 - 라이브러리 추가 여부

```
라이브러리를 build.gradle에 추가할까요?
1) 라이브러리 추가 후 진행
2) 라이브러리 추가 없이 진행 (어노테이션 스타일만 적용)
```

##### 라이브러리 추가 후 진행 선택 시 추가 질문 2 - 버전 선택

```
어떤 버전을 사용할까요?
1) 최신 버전 사용
2) 특정 버전 직접 입력
```

- **최신 버전** → 현시점 안정 버전으로 자동 적용
- **특정 버전** → 사용자가 입력한 버전으로 적용
- 추가 완료 후 STEP 6 결과 보고에 **"Gradle Sync 필요"** 안내 포함

##### 6) 기타 처리

```
라이브러리명과 버전을 입력해 주세요. (예: com.example:my-json-lib:1.0.0)
```

- 입력받은 의존성을 build.gradle.kts `dependencies { }` 블록에 추가
- `./gradlew {모듈}:dependencies` 실행하여 의존성 해석 검증
  - **성공** → 어노테이션 없이 순수 Data Class로 생성, STEP 6에 "어노테이션 직접 추가 필요" 안내 포함
  - **실패** → build.gradle.kts 추가 내용 롤백 후 즉시 중단, 오류 내용 사용자에게 안내

#### 라이브러리가 여러 개 감지된 경우

아래 질문을 사용자에게 한다.

```
여러 직렬화 라이브러리가 감지됐습니다.

어떤 라이브러리로 생성할까요?
1) {감지된 라이브러리 1}
2) {감지된 라이브러리 2}
...
```

선택 후 해당 라이브러리 스타일로 진행한다.

#### 라이브러리가 1개 감지된 경우

별도 질문 없이 해당 라이브러리 스타일로 바로 진행한다.

#### build.gradle 추가 대상 라이브러리별 의존성

| 라이브러리 | build.gradle.kts 추가 내용 |
|---|---|
| kotlinx.serialization | 플러그인: `kotlin("plugin.serialization")` + 의존성: `implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:{버전}")` |
| Gson | `implementation("com.google.code.gson:gson:{버전}")` |
| Moshi | `implementation("com.squareup.moshi:moshi-kotlin:{버전}")` |
| Jackson | `implementation("com.fasterxml.jackson.module:jackson-module-kotlin:{버전}")` |

> 의존성 추가 위치: 해당 모듈의 `build.gradle.kts` `dependencies { }` 블록 내
> `gradle/libs.versions.toml`도 함께 확인하여 version catalog 방식이면 해당 파일에도 추가한다.

---

### STEP 2. 클래스 구조 분석

JSON을 분석하여 변환할 Data Class 목록과 패키지 구조를 도출한다.

#### 타입 변환 규칙

| JSON 타입 | Kotlin 타입 |
|---|---|
| String | `String` |
| Int (정수, 2,147,483,647 이하) | `Int` |
| Long (정수, 2,147,483,647 초과) | `Long` |
| Float/Double | `Double` |
| Boolean | `Boolean` |
| Object | 별도 Data Class |
| Array (primitive) | `List<T>` (첫 번째 요소 타입 기준 결정) |
| Array (object) | `List<ClassName>` |
| Array (빈 배열) | `List<Any>` + `// TODO: 빈 배열 - 실제 타입으로 교체 필요` 주석 |
| Array (혼합 타입) | `List<Any>` + `// TODO: 혼합 타입 - 실제 타입으로 교체 필요` 주석 |
| null | `Any?` + `// TODO: null 값으로 타입 불명확 - 실제 타입으로 교체 필요` 주석 |

#### 중첩 객체 패키지 규칙

- 최상위 객체 → 입력된 패키지에 생성
- 중첩 객체 → **부모 필드명을 하위 패키지**로 사용하여 생성

**예시:**
```
입력: kr.open.library.simple_ui.core.model.UserInfo
JSON: {"userId": 1, "address": {"city": "서울"}}

생성 결과:
kr.open.library.simple_ui.core.model          → UserInfo.kt  (address 필드 타입: Address)
kr.open.library.simple_ui.core.model.address  → Address.kt   (city 필드 타입: String)
```

**3단계 이상 중첩 예시:**
```
입력: kr.open.library.simple_ui.core.model.UserInfo
JSON: {"address": {"city": {"name": "서울"}}}

생성 결과:
kr.open.library.simple_ui.core.model                  → UserInfo.kt
kr.open.library.simple_ui.core.model.address          → Address.kt
kr.open.library.simple_ui.core.model.address.city     → City.kt
```

> 중첩 깊이 제한 없음. 필드명 기준으로 계속 하위 패키지로 내려간다.

#### 클래스명 네이밍 규칙

- JSON 키 → PascalCase 클래스명 (중첩 객체)
- JSON 키 → camelCase 프로퍼티명
- 직렬화 어노테이션은 STEP 1-1에서 감지된 라이브러리 기준으로 추가

#### 배열 내 객체 클래스명 규칙

- 필드명을 **영어 단수형으로 변환** 후 PascalCase 적용
- 판단이 불가능한 경우 필드명을 그대로 PascalCase로 변환

```
users      → User      items      → Item
categories → Category  buses      → Bus
status     → Status    data       → Data
```

#### 루트 JSON이 배열인 경우

- FQCN의 클래스명을 배열 요소 클래스명으로 사용
- 배열 첫 번째 요소를 기준으로 구조 분석

```
/JsonConvert kr.open.library.simple_ui.core.model.UserInfo [{"userId": 1}, {"userId": 2}]
→ UserInfo.kt 생성 (배열 요소 구조 기반)
```

---

### STEP 3. 생성 계획 보고

파일 생성 전 아래 형식으로 계획을 먼저 보고한다.

```
변환 계획
- 적용 라이브러리: {감지된 라이브러리 이름 or "없음 (순수 Data Class)"}
- build.gradle 수정 예정: {모듈}/build.gradle.kts  ← 2~5번 선택 + 라이브러리 추가 선택 시에만 표시
- build.gradle 수정 완료: {모듈}/build.gradle.kts  ← 6번 기타 선택 + 빌드 검증 성공 시에만 표시
- 클래스 수: N개
- 생성 파일 목록:
  · {경로}/UserInfo.kt          → package kr.open.library.simple_ui.core.model
  · {경로}/address/Address.kt   → package kr.open.library.simple_ui.core.model.address
- ⚠️ TODO 항목: {필드명} - {사유}  ← 없으면 생략
```

> 계획 보고 후 사용자 승인 없이 STEP 4(충돌 체크)로 바로 진행한다.

---

### STEP 4. 충돌 체크

생성 예정 파일 중 이미 존재하는 파일이 있으면 **즉시 중단**하고 안내한다.

- 충돌 파일 목록
- 대안 (클래스명 변경 제안 등)

---

### STEP 5-1. build.gradle 수정 (라이브러리 추가 선택 시에만)

라이브러리 추가를 선택한 경우에만 실행한다. 그 외에는 STEP 5-2로 바로 진행한다.
> **6번 기타** 선택 시: build.gradle 수정 및 빌드 검증이 STEP 1-1에서 이미 완료됐으므로 이 STEP은 건너뛴다.

- `gradle/libs.versions.toml` 존재 여부 확인
  - **version catalog 방식** → `libs.versions.toml`에 버전 및 라이브러리 항목 추가 후 build.gradle.kts에 catalog 참조 형식으로 추가
  - **직접 방식** → build.gradle.kts `dependencies { }` 블록에 직접 추가
- 추가 내용은 `build.gradle 추가 대상 라이브러리별 의존성` 테이블 기준으로 적용

---

### STEP 5-2. 파일 생성

충돌 없음이 확인된 후 파일을 생성한다.

#### Data Class 작성 규칙

STEP 1-1에서 감지된 라이브러리에 따라 아래 스타일을 적용한다.

**kotlinx.serialization:**
```kotlin
package {패키지명}

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class {ClassName}(
    @SerialName("{원본키}")
    val {camelCaseKey}: {Type},
)
```

**Gson:**
```kotlin
package {패키지명}

import com.google.gson.annotations.SerializedName

data class {ClassName}(
    @SerializedName("{원본키}")
    val {camelCaseKey}: {Type},
)
```

**Moshi:**
```kotlin
package {패키지명}

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class {ClassName}(
    @Json(name = "{원본키}")
    val {camelCaseKey}: {Type},
)
```

**Jackson:**
```kotlin
package {패키지명}

import com.fasterxml.jackson.annotation.JsonProperty

data class {ClassName}(
    @JsonProperty("{원본키}")
    val {camelCaseKey}: {Type},
)
```

**없음 (순수 Data Class):**
```kotlin
package {패키지명}

data class {ClassName}(
    val {camelCaseKey}: {Type},
)
```

**공통 규칙:**
- 중첩 객체 타입은 하위 패키지 클래스로 참조하며, 해당 클래스 import 추가
- 파일명 = 클래스명 + `.kt`
- UTF-8 인코딩 필수

---

### STEP 6. 빌드 확인

파일 생성 완료 후 반드시 terminal 도구를 사용하여 프로젝트를 빌드하고 컴파일 에러가 없는지 확인한다.

```bash
./gradlew :simple_core:assembleDebug --quiet
# 또는
./gradlew :simple_xml:assembleDebug --quiet
```

빌드 실패 시 → 오류 내용을 사용자에게 안내하고 생성된 파일을 수정한다.

### STEP 7. 결과 보고

```
✅ 완료
- 적용 라이브러리: {감지된 라이브러리 이름 or "없음 (순수 Data Class)"}
- 생성 파일:
  · {경로} (클래스명, 필드 N개)
- ⚠️ TODO 항목: {필드명} - {사유}  ← 없으면 생략
- ⚠️ 직렬화 라이브러리 없음: 어노테이션 없이 생성됨. 필요 시 라이브러리 추가 후 재실행 권장  ← 1번(순수 Data Class) 선택 시에만 표시
- ⚠️ 라이브러리 추가 필요: 어노테이션은 적용됐지만 build.gradle에 의존성이 없습니다. 직접 추가하지 않으면 컴파일 오류가 발생합니다.  ← 2~5번 선택 + 라이브러리 추가 없이 진행 선택 시에만 표시
- ⚠️ Gradle Sync 필요: build.gradle에 의존성이 추가됐습니다. Android Studio에서 Sync를 실행하세요.  ← build.gradle 수정 시에만 표시
- ⚠️ 어노테이션 직접 추가 필요: 기타 라이브러리는 어노테이션 스타일을 자동 적용할 수 없습니다. 각 필드에 직접 추가해 주세요.  ← 6번 기타 선택 시에만 표시
```
