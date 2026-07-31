# 실기기 검증 체크리스트

## 문서 목적

릴리스 후보를 `PHY-01`에서 검증할 때 SPEC의 33개 시나리오를 같은 순서와 기준으로 실행하기 위한 공통 체크리스트입니다.

## 실행 전 확인

- [ ] 검증 대상 소스 커밋 SHA를 고정했습니다.
- [ ] 해당 커밋의 Android CI가 성공했습니다.
- [ ] `adb devices -l`에 `R3CM50A575V`만 `device` 상태로 표시됩니다.
- [ ] 단말이 Samsung `SM-G977N`, Android 12/API 31, `arm64-v8a`인지 확인했습니다.
- [ ] 배터리가 30% 이상이고 절전 모드 상태를 기록했습니다.
- [ ] SIM 없음, Wi-Fi, 위치 및 관련 권한의 초기 상태를 기록했습니다.
- [ ] 권한 복귀 시나리오를 실행하기 전에 verification 앱의 `다른 앱 위에 표시` 권한을 OFF로 맞추고 변경 전 상태를 기록했습니다.
- [ ] Wi-Fi 시나리오를 실행하기 전에 Wi-Fi가 켜져 있고 실제 AP에 연결됐는지 확인했습니다.
- [ ] 이전 검증의 알림, 알람, 플로팅 뷰 및 앱 데이터가 결과에 영향을 주지 않도록 정리했습니다.

## 자동 테스트

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleVerification
.\gradlew.bat :app:connectedDebugAndroidTest
```

- Gradle HTML/XML 결과 경로를 릴리스 결과 문서에 기록합니다.
- 자동 테스트 실패를 수동 결과로 덮어쓰지 않습니다.
- 수동 또는 혼합 시나리오는 아래 표에 따라 별도로 판정합니다.

## 공통 결과 규칙

| 상태 | 사용 기준 |
| --- | --- |
| `PASS` | 기대 결과를 충족했습니다. |
| `FAIL` | 실행했으나 기대 결과를 충족하지 못했습니다. |
| `BLOCKED` | 테스트 코드, APK 또는 절차 문제로 판정하지 못했습니다. |
| `ENV_UNAVAILABLE` | 필요한 물리 환경이 없습니다. P1만 릴리스별 예외 승인이 가능합니다. |
| `NOT_RUN` | 아직 실행하지 않았습니다. |
| `OUT_OF_SCOPE` | 사전에 정의된 적용 조건에 해당하지 않습니다. |

- `HYBRID_DEVICE`는 자동 결과와 수동 결과를 별도로 기록하며 둘 다 `PASS`일 때만 최종 결과를 `PASS`로 기록합니다.
- 사전 조건을 맞출 수 없어 기대 동작을 판정하지 못하면 `FAIL`이 아니라 `BLOCKED`로 기록합니다. P0·P1의 `BLOCKED`는 릴리스를 차단합니다.
- 자동 테스트 메서드에 시나리오 ID가 있어도 아래 수동 절차가 남아 있으면 해당 시나리오 전체가 완료된 것이 아닙니다.

## `simple_core`

| ID | 우선순위 | 방식 | 실행 항목 | 기대 결과 | 복구 |
| --- | --- | --- | --- | --- | --- |
| `CORE-P0-001` | P0 | 자동 | 앱 Context에서 core 공개 API에 접근합니다. | 명시적 초기화 없이 접근하고 정상 결과를 반환합니다. | 앱 데이터 유지 |
| `CORE-P0-002` | P0 | 자동 | CAMERA 권한의 허용·거부 상태를 실제 PackageManager와 비교합니다. | 라이브러리 판정이 실제 상태와 일치합니다. | 변경한 권한 원복 |
| `CORE-P0-003` | P0 | 혼합 | 특수 권한 설정으로 이동한 뒤 앱으로 복귀합니다. | 최신 상태를 재판정하고 요청이 중복 완료되지 않습니다. | 특수 권한 원복 |
| `CORE-P0-004` | P0 | 자동 | SharedPreferences 값을 저장·조회·삭제합니다. | 재생성 후 유지되고 삭제 후 남지 않습니다. | 테스트 키 삭제 |
| `CORE-P1-001` | P1 | 혼합 | Logx 파일·필터를 자동 판정하고 앱 재실행 후 파일 유지를 확인합니다. | 설정한 경로·필터·형식으로 로그가 생성·유지됩니다. | 테스트 로그 삭제 |

### `simple_core` 혼합 시나리오

#### `CORE-P0-003` 특수 권한 설정 복귀

1. 다음 명령으로 verification 앱의 현재 오버레이 권한 상태를 증빙에 기록합니다.

   ```powershell
   adb shell appops get kr.open.library.simpleui_xml.verification SYSTEM_ALERT_WINDOW
   adb shell am start -a android.settings.action.MANAGE_OVERLAY_PERMISSION -d package:kr.open.library.simpleui_xml.verification
   ```

2. `다른 앱 위에 표시`를 OFF로 맞춘 뒤 다음 화면을 실행합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.permission.PermissionsActivity
   ```

3. `Special Only`를 누르고 설정 화면에서 권한을 ON으로 변경한 뒤 앱으로 돌아옵니다.
4. 결과 목록과 Snackbar가 한 번만 추가되고 허용 결과를 반영하는지 확인합니다.
5. 같은 흐름에서 홈 화면을 거쳐 복귀해도 콜백이 중복되지 않는지 확인합니다.
6. 테스트 종료 후 오버레이 권한을 실행 전 상태로 복구합니다.

#### `CORE-P1-001` Logx 파일과 앱 재실행

- 자동 부분은 `CoreLogxIntegrationTest`가 실제 파일 생성, 파일명·DEBUG 라인 포맷, 로그 타입 허용 목록, 태그 차단 목록 및 테스트 파일 삭제를 판정합니다.
- 수동 부분은 verification 앱 프로세스 종료·재실행 후 기존 파일 유지와 샘플 화면의 사용자 지정 경로를 확인합니다.

1. 자동 테스트 결과에서 `coreP1001_logxFileOutputAppliesTypeAndTagFilters`가 `PASS`인지 확인한 뒤 Logx 화면을 실행합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.logx.LogxActivity
   ```

2. `저장 경로 설정`, `파일 로그`, `태그 블록리스트`를 순서대로 누릅니다.
3. 화면의 마지막 동작에 `logx_custom` 경로가 표시되고 `ALLOWED_TAG`는 기록되며 `BLOCKED_TAG`는 차단되는지 확인합니다.
4. 앱을 종료·재실행한 뒤 기존 로그 파일이 유지되는지 확인합니다.

   ```powershell
   adb shell am force-stop kr.open.library.simpleui_xml.verification
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.logx.LogxActivity
   adb shell ls -R /sdcard/Android/data/kr.open.library.simpleui_xml.verification/files/logx_custom
   adb pull /sdcard/Android/data/kr.open.library.simpleui_xml.verification/files/logx_custom docs/verification/device/releases/{version}/evidence/CORE-P1-001
   ```

5. 내려받은 파일에서 저장 경로 로그와 허용 태그가 존재하고 차단 태그가 없는지 확인합니다.
6. 증빙을 저장한 뒤 단말의 `logx_custom` 테스트 디렉터리를 삭제하고 삭제 여부를 확인합니다.

   ```powershell
   adb shell rm -r /sdcard/Android/data/kr.open.library.simpleui_xml.verification/files/logx_custom
   adb shell ls /sdcard/Android/data/kr.open.library.simpleui_xml.verification/files
   ```

## `simple_system_manager`

| ID | 우선순위 | 방식 | 실행 항목 | 기대 결과 | 복구 |
| --- | --- | --- | --- | --- | --- |
| `SYS-P0-001` | P0 | 자동 | 지원 시스템 서비스와 `SystemResult`를 조회합니다. | 서비스가 안전하게 조회되고 오류가 규격화됩니다. | 없음 |
| `SYS-P0-002` | P0 | 혼합 | 알림을 표시하고 클릭한 뒤 취소합니다. | 알림 표시·진입·취소가 모두 동작합니다. | 테스트 알림·채널 정리 |
| `SYS-P0-003` | P0 | 혼합 | 짧은 테스트 알람을 등록하고 발화 후 해제합니다. | 알람이 발화하고 해제 후 재발화하지 않습니다. | PendingIntent·저장값 제거 |
| `SYS-P0-004` | P0 | 혼합 | 짧은 진동을 실행하고 중지합니다. | 실제 진동이 발생하고 중지됩니다. | 진동 취소 |
| `SYS-P0-005` | P0 | 혼합 | Wi-Fi 상태와 연결 정보를 조회합니다. | 실제 상태 또는 제한 원인을 안전하게 반환합니다. | Wi-Fi 초기 상태 복원 |
| `SYS-P0-006` | P0 | 혼합 | 위치 상태와 업데이트를 시작·중지합니다. | 권한·설정을 반영하고 중지 후 업데이트가 끝납니다. | 콜백 해제·설정 복원 |
| `SYS-P0-007` | P0 | 자동 | 배터리 상태와 이벤트를 조회합니다. | 유효한 상태를 반환하고 수신을 정리합니다. | 수신기 해제 |
| `SYS-P0-008` | P0 | 혼합 | Wi-Fi 연결 상태 변경을 관찰합니다. | 연결·해제를 반영하고 콜백이 정리됩니다. | 콜백 해제·Wi-Fi 복원 |
| `SYS-P0-009` | P0 | 자동 | SIM 없는 상태에서 SIM·전화망 정보를 조회합니다. | 예외 없이 없음·미사용 상태를 반환합니다. | 없음 |
| `SYS-P0-010` | P0 | 혼합 | 시스템 바 스타일을 적용하고 화면에서 이탈합니다. | 적용 후 원래 스타일로 복원됩니다. | Window 상태 복원 |
| `SYS-P0-011` | P0 | 수동 | 입력란 포커스와 키보드 표시·숨김을 조작합니다. | 키보드와 포커스가 요청대로 변경됩니다. | 키보드 숨김 |
| `SYS-P0-012` | P0 | 혼합 | 플로팅 뷰를 추가·이동·제거하고 권한 거부도 확인합니다. | 허용·거부 모두 안전하며 잔여 뷰가 없습니다. | 뷰 제거·권한 원복 |
| `SYS-P1-001` | P1 | 자동 | 실제 SIM 구독과 전화망 정보를 조회합니다. | 실제 SIM 상태와 반환 정보가 일치합니다. | 현재 `ENV_UNAVAILABLE` 예외 검토 |
| `SYS-P2-001` | P2 | 자동 | 멀티 SIM 슬롯별 정보를 조회합니다. | 슬롯과 반환 정보가 일치합니다. | 현재 미실행 사유 기록 |

### `simple_system_manager` 수동·혼합 시나리오

#### 알림·알람·진동 (`SYS-P0-002`~`SYS-P0-004`)

1. 알림 화면에서 `Show Notification`을 누르고 알림 패널에 표시되는지 확인합니다. 알림을 눌러 `MainActivity`로 진입한 뒤 알림 화면으로 돌아와 `Cancel All Notifications`를 실행합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.system_service_manager.controller.notification.NotificationControllerActivity
   ```

2. 알람 화면에서 고유 키와 다음 실행 가능한 시각을 지정하고 `Register Exact Alarm (Idle)`을 누릅니다. `Exists?`가 등록 상태를 표시하고 정해진 시각에 알람 알림이 발생하는지 확인한 뒤 `Remove`를 누르고 재발화하지 않는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.system_service_manager.controller.alarm.AlarmControllerActivity
   ```

3. 진동 화면에서 기본 진동과 패턴을 실행해 실제 진동을 확인하고 `Cancel` 후 진동이 끝나는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.system_service_manager.controller.vibrator.VibratorControllerActivity
   ```

4. 생성한 알림·채널·알람을 제거하고 진동을 중지합니다.

#### Wi-Fi·네트워크·위치 (`SYS-P0-005`, `SYS-P0-006`, `SYS-P0-008`)

1. Wi-Fi가 켜지고 AP에 연결된 상태에서 Wi-Fi 화면의 `Get WiFi Info`, `Check Status`를 실행해 시스템 상태와 화면 결과가 일치하는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.system_service_manager.controller.wifi.WifiControllerActivity
   ```

2. 상태 정보 화면에서 `Network Register`를 누른 뒤 Wi-Fi를 OFF·ON하고 결과 목록에 연결 해제·재연결이 반영되는지 확인합니다. 마지막에 `Network Unregister`를 누릅니다.
3. 위치 권한과 위치 기능을 켠 상태에서 `Location Register`, `Get Last Location`, `Location Unregister`를 순서대로 실행합니다. 등록 중 결과가 갱신되고 해제 후 새 결과가 계속 추가되지 않는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.system_service_manager.info.ServiceManagerInfoActivity
   ```

4. Wi-Fi, 위치 및 권한을 실행 전 상태로 복구합니다.

#### Window 계열 (`SYS-P0-010`~`SYS-P0-012`)

1. 시스템 바 샘플에서 색상·아이콘 명암·표시 상태를 변경하고 Reset 및 화면 이탈 후 원래 상태로 돌아오는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.activity_fragment.activity.BaseActivityExample
   ```

2. 소프트키보드 화면에서 입력란에 포커스를 두고 `Show`, `Hide`, 지연 표시·숨김을 실행합니다. 키보드와 포커스가 요청대로 바뀌고 종료 후 키보드가 남지 않는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.system_service_manager.controller.softkeyboard.SoftKeyboardControllerActivity
   ```

3. 오버레이 권한 OFF 상태에서 플로팅 뷰 추가를 시도해 안전한 거부 또는 설정 이동을 확인합니다. 권한을 ON으로 바꾼 뒤 Drag·Fixed 뷰를 추가하고 이동한 다음 `Remove`로 모두 제거합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.system_service_manager.controller.floating.FloatingViewControllerActivity
   ```

4. 오버레이 권한과 Window 상태를 실행 전 값으로 복구합니다.

## `simple_compose`

| ID | 우선순위 | 방식 | 실행 항목 | 기대 결과 | 복구 |
| --- | --- | --- | --- | --- | --- |
| `COMPOSE-P0-001` | P0 | 자동 | Compose 예제 화면을 실행하고 핵심 semantics를 조회합니다. | 화면이 표시되고 의미 노드를 찾습니다. | Activity 종료 |
| `COMPOSE-P0-002` | P0 | 혼합 | 권한 요청·설정 이동·앱 복귀를 실행합니다. | 단계와 결과가 실제 상태에 맞고 중복 요청이 없습니다. | 권한 원복 |
| `COMPOSE-P0-003` | P0 | 자동 | ViewModel 이벤트와 Flow 수집 중 화면 상태를 변경합니다. | 활성 상태에서만 수집되고 중복 소비가 없습니다. | Activity 종료 |
| `COMPOSE-P0-004` | P0 | 혼합 | 시스템 바 스타일을 적용하고 컴포지션에서 이탈합니다. | 이탈 시 원래 스타일로 복원됩니다. | Window 상태 복원 |
| `COMPOSE-P0-005` | P0 | 자동 | LazyList를 시작·중간·끝으로 스크롤합니다. | 방향과 엣지 상태가 규격대로 갱신됩니다. | 목록 시작 위치 복원 |
| `COMPOSE-P1-001` | P1 | 자동 | 권한 요청 도중 Activity를 재생성합니다. | 저장 대상 상태가 유실되거나 중복 완료되지 않습니다. | 권한·Activity 정리 |

### `simple_compose` 혼합 시나리오

#### `COMPOSE-P0-002` 권한 설정 복귀

- 자동 부분은 `ComposePermissionIntegrationTest`가 설정 이동 게이트, 중복 요청 방지 및 취소 완료를 판정합니다.
- 수동 부분은 verification 빌드의 `ComposePermissionManualVerificationActivity`에서 실행합니다.

1. verification APK를 설치하고 오버레이 권한을 OFF로 맞춘 뒤 검증 화면을 실행합니다.

   ```powershell
   adb shell appops set kr.open.library.simpleui_xml.verification android:system_alert_window deny
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.deviceverification.ComposePermissionManualVerificationActivity
   ```

2. 초기 상태가 `IDLE`, `isRequesting=false`, `allGranted=false`, 콜백 수 0인지 확인합니다.
3. `권한 요청 시작`을 누르고 `SETTINGS_NAVIGATION_REQUIRED`, `isRequesting=true`, 설정 이동 대기 권한이 `SYSTEM_ALERT_WINDOW`인지 확인합니다.
4. `진행 중 중복 요청 시도`를 누릅니다. `request()` 호출 수만 증가하고 설정 화면이 열리거나 결과 콜백 수가 증가하지 않는지 확인합니다.
5. `설정 화면으로 이동`을 누르고 오버레이 권한을 허용하지 않은 채 앱으로 돌아옵니다. `COMPLETED`, `allGranted=false`, `DENIED`, 결과 콜백 수 1인지 확인합니다.
6. `권한 요청 시작`과 `설정 화면으로 이동`을 다시 누르고 설정에서 오버레이 권한을 허용한 뒤 앱으로 돌아옵니다. `COMPLETED`, `allGranted=true`, 거부 결과 없음, 결과 콜백 수 2인지 확인합니다.
7. 허용·거부 복귀 상태와 콜백 횟수 화면을 각각 캡처한 뒤 오버레이 권한을 OFF로 복구하고 앱을 종료합니다.

   ```powershell
   adb shell appops set kr.open.library.simpleui_xml.verification android:system_alert_window deny
   adb shell am force-stop kr.open.library.simpleui_xml.verification
   ```

#### `COMPOSE-P0-004` 시스템 바 시각 확인

1. Compose 예제 화면을 실행하고 `아이콘 명암 전환`을 눌러 화면 상태 문구와 실제 시스템 바 아이콘 명암이 함께 바뀌는지 확인합니다.
2. 화면에서 이탈한 뒤 이전 Activity의 시스템 바 명암이 복원되는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.compose.ComposeExamplesActivity
   ```

## `simple_xml`

| ID | 우선순위 | 방식 | 실행 항목 | 기대 결과 | 복구 |
| --- | --- | --- | --- | --- | --- |
| `XML-P0-001` | P0 | 자동 | BaseActivity와 BaseBindingActivity를 생성·종료합니다. | 바인딩·생명주기가 연결되고 종료 후 정리됩니다. | Activity 종료 |
| `XML-P0-002` | P0 | 자동 | BaseFragment와 BaseBindingFragment를 교체·재생성합니다. | View 생명주기와 바인딩이 일치합니다. | Activity 종료 |
| `XML-P0-003` | P0 | 혼합 | DialogFragment를 표시·조작·해제합니다. | 중복 표시·해제 오류 없이 정리됩니다. | Dialog 해제 |
| `XML-P0-004` | P0 | 혼합 | 일반·특수 권한 요청과 설정 복귀를 실행합니다. | 실제 상태와 결과가 일치하고 결과가 중복·유실되지 않습니다. | 권한 원복 |
| `XML-P0-005` | P0 | 자동 | RecyclerView 데이터를 추가·변경·삭제하고 스크롤합니다. | 화면과 내부 데이터가 일치합니다. | 샘플 데이터 초기화 |
| `XML-P0-006` | P0 | 자동 | View 표시·클릭·텍스트·단위 변환 확장을 실행합니다. | 실제 View 상태와 반환값이 일치합니다. | Activity 종료 |
| `XML-P1-001` | P1 | 수동 | Toast·Snackbar·애니메이션을 실행합니다. | 내용과 동작이 표시되고 잔여 동작이 없습니다. | Activity 종료 |
| `XML-P1-002` | P1 | 혼합 | 구성 변경과 프로세스 수준 재생성 후 재진입합니다. | 저장 대상 상태가 복원되고 중복 이벤트가 없습니다. | 앱 정상 재실행 |

### `simple_xml` 혼합 시나리오

#### `XML-P0-003` Dialog 상호작용

1. Activity/Fragment 예제 화면을 실행합니다.
2. `BaseDialogFragment Example`과 `BaseBindingDialogFragment Example`을 각각 열고 `OK`·`Cancel`을 실제로 누릅니다.
3. 같은 Dialog를 반복해서 열고 닫아 중복 표시·해제 오류나 잔여 화면이 없는지 확인합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.activity_fragment.ActivityFragmentActivity
   ```

#### `XML-P0-004` 일반·특수 권한 설정 복귀

1. `CORE-P0-003`과 같은 `PermissionsActivity`에서 CAMERA 일반 권한의 허용·거부 결과를 확인합니다.
2. 오버레이 권한을 OFF로 맞추고 `Special Only`를 실행한 뒤 설정에서 ON으로 변경해 복귀합니다.
3. 결과 목록이 요청당 한 번만 갱신되고 실제 권한 상태와 일치하는지 확인한 뒤 권한을 원복합니다.

### `XML-P1-001` 시간 기반 UI 확인

1. verification APK를 설치하고 수동 검증 화면을 실행합니다.

   ```powershell
   adb install -r app/build/outputs/apk/verification/app-verification.apk
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.deviceverification.XmlManualVerificationActivity
   ```

2. `Toast 표시`를 눌러 `XML-P1-001 Toast` 문구가 표시되었다가 사라지는지 확인합니다.
3. `Snackbar 표시`를 눌러 `XML-P1-001 Snackbar` 문구가 화면 아래에 표시되었다가 사라지는지 확인합니다.
4. `회전 애니메이션`을 눌러 대상 문구가 한 바퀴 회전하고 `애니메이션 완료`로 바뀌는지 확인합니다.
5. 화면에서 나간 뒤 Toast·Snackbar·애니메이션이 남거나 다시 실행되지 않는지 확인합니다.

### `XML-P1-002` 프로세스 재생성 확인

계측 테스트는 Activity 재생성 후 `EditText` 저장 상태를 자동 판정합니다. 실제 프로세스 재생성은 다음 순서로 추가 확인합니다.

1. verification APK의 `ExtensionsStyleActivity`를 열고 이메일 입력란에 `device-restoration@example.com`을 입력합니다.
2. 홈 화면으로 이동한 뒤 다음 명령으로 백그라운드 앱 프로세스를 종료합니다.

   ```powershell
   adb shell input keyevent KEYCODE_HOME
   adb shell am kill kr.open.library.simpleui_xml.verification
   adb shell pidof kr.open.library.simpleui_xml.verification
   ```

3. 다음 명령으로 같은 화면에 재진입합니다.

   ```powershell
   adb shell am start -W -n kr.open.library.simpleui_xml.verification/kr.open.library.simpleui_xml.extenstions_style.ExtensionsStyleActivity
   ```

4. 입력값이 복원되고 Toast·Snackbar 등 일회성 이벤트가 중복 표시되지 않는지 확인합니다.
5. `am force-stop`은 태스크를 중지 상태로 바꾸어 저장 상태 복원 조건과 달라지므로 이 시나리오의 프로세스 종료 명령으로 사용하지 않습니다.

## 실행 후 확인

- [ ] 알림과 알람을 모두 제거했습니다.
- [ ] 진동을 중지했습니다.
- [ ] 위치·네트워크·배터리 콜백을 해제했습니다.
- [ ] 플로팅 뷰와 테스트 Window 상태를 정리했습니다.
- [ ] Wi-Fi, 위치, 권한 및 절전 모드를 초기 상태로 복구했습니다.
- [ ] 자동 테스트 결과와 수동 증빙을 릴리스 결과 문서에 연결했습니다.
- [ ] P0·P1의 `FAIL`, `BLOCKED`, `NOT_RUN` 여부를 확인했습니다.
- [ ] 실제 SIM P1 예외를 이번 릴리스 기준으로 다시 승인했습니다.
