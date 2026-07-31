# Simple System Manager 공개 API 감사

## 문서 정보
- 문서명: Simple System Manager 공개 API 감사
- 작성일: 2026-07-27
- 수정일: 2026-07-30
- 대상 모듈: simple_system_manager
- 기준 버전: 0.5.1
- 기준 파일: `simple_system_manager/api/simple_system_manager.api`
- 상태: 4단계 다음 메이저 공개 API 정리 및 전체 회귀 검증 완료

## 목적
- 소비자가 사용해야 하는 공개 계약과 모듈 내부 구현을 구분한다.
- 기준 버전과 다음 메이저 소스 상태의 공개 API 차이를 기록한다.
- Deprecated 적용, 다음 메이저 제거, 후속 감사의 판단 기준을 고정한다.
- 승인된 제거 항목을 독립 배치로 검증한 결과와 소비자 이전 경로를 제공한다.

## 범위

### 포함 범위
- `simple_system_manager`의 Kotlin/JVM 공개 ABI 기준선
- 최상위 공개 타입의 패키지별 인벤토리
- `internal` 경로에 존재하는 공개 타입
- 저장소 내부 사용처와 기능 문서에 명시된 공개 계약
- 버전 호환성을 고려한 Deprecated 및 제거 시점

### 제외 범위
- 승인 범위 밖 공개 타입의 삭제 또는 `internal` 전환
- 승인 범위 밖 공개 메서드와 프로퍼티의 개별 시그니처 축소
- 외부 저장소 소비자의 실제 사용 통계
- 다음 메이저 버전 번호 및 출시 일정 확정
- API 동작과 반환값 변경

## 기준 및 원칙

### 호환성 기준
- 기준 버전 `0.5.1`의 API 파일과 다음 메이저 소스 상태의 API 파일은 승인된 Breaking Change만큼 다르다.
- 기준 파일에 포함된 타입은 저장소 내부 사용 여부와 관계없이 이미 배포된 공개 API로 간주한다.
- 공개 타입을 즉시 `internal`로 변경하거나 삭제하는 작업은 Breaking Change로 간주한다.
- 프로젝트 버전 규칙에 따라 Breaking Change는 Major 버전에서만 수행한다.

### 판정 기준
- **유지**: 기능 문서에 공개 계약으로 명시되거나 다른 공개 API의 매개변수·반환형·상속 계약에 포함된 타입
- **Deprecated 후보**: 내부 구현이지만 이미 공개된 타입으로, 현재 버전에서 즉시 제거할 수 없는 타입
- **다음 메이저 제거 후보**: 대체 경로와 검증 조건을 마련한 뒤 Major 버전에서 제거할 타입
- **후속 감사**: 현재는 유지하되 소비자 진입점과 계약 데이터 타입을 패키지 단위로 다시 검토할 타입

### 금지 사항
- 저장소 내부 직접 사용처가 없다는 이유만으로 공개 API를 제거하지 않는다.
- 다른 공개 API가 참조하는 데이터 타입을 단독으로 내부화하지 않는다.
- `DeprecationLevel.HIDDEN`을 마이그레이션 기간 없이 적용하지 않는다.
- API 기준선에서 항목만 지워 실제 바이너리 노출과 기준선을 불일치시키지 않는다.
- 여러 기능 패키지의 공개 API는 독립 배치 검증 없이 동시에 축소하지 않는다.

## 감사 결과 요약
- 기준 버전 `0.5.1` 공개 API 기준선은 1,914줄이며 다음 메이저 소스 상태 기준선은 1,759줄이다.
- 중첩 타입을 제외한 최상위 공개 ABI 타입은 82개에서 77개로 줄었다.
- `PowerProfile`, `PowerProfileVO`, `CommonTelephonyCallback`, `TelephonyCallbackManager`를 내부화하고 생성 `BuildConfig`를 제거했다.
- `internal` 패키지 경로에 공개로 남은 최상위 타입은 없다.
- 문서에 직접 등장하지 않는 타입 중 다수는 다른 공개 API의 계약에 포함되므로 일괄 제거 대상이 아니다.

## 공개 API 인벤토리

### 생성 API
| 타입 | 판정 | 근거 |
| --- | --- | --- |
| `BuildConfig` | 다음 메이저 기준선 제거 완료 | 라이브러리 본문 사용처가 없고 `buildFeatures.buildConfig = false`로 생성을 비활성화함 |

### core.base
| 타입 | 판정 |
| --- | --- |
| `BaseSystemService` | 유지 / 후속 감사 |
| `SystemResult` | 유지 |

### core.controller.alarm
| 타입 | 판정 |
| --- | --- |
| `AlarmConstants` | 유지 |
| `AlarmController` | 유지 |
| `BaseAlarmReceiver` | 유지 |
| `AlarmData` | 유지 |
| `AlarmDateData` | 유지 |
| `AlarmIdleMode` | 유지 |
| `AlarmNotificationData` | 유지 |
| `AlarmScheduleData` | 유지 |

`BaseAlarmReceiver`는 소비자가 앱의 저장소와 알림 구성을 연결하기 위해 상속하는 공개 확장 지점이다. 샘플 앱도 직접 상속하므로 내부화하거나 제거하지 않는다. 중첩 `RegisterType`은 `resolveRegisterType()`의 공개 상속 계약에 포함되므로 유지한다.

`AlarmConstants.ALARM_KEY`와 `ALARM_KEY_DEFAULT_VALUE`는 사용자 정의 Receiver와 `AlarmController` 사이의 인텐트 계약이므로 유지한다. WakeLock 정책 상수와 정확 알람 권한 변경 액션은 현재 공개 ABI를 유지하되, 다음 메이저 준비 단계에서 멤버 단위 공개 필요성을 별도로 검토한다.

### core.controller.notification
| 타입 | 판정 |
| --- | --- |
| `NotificationDefaultChannel` | 유지 |
| `SimpleNotificationController` | 유지 |
| `SimpleNotificationType` | 유지 |
| `BigPictureNotificationOption` | 유지 |
| `BigTextNotificationOption` | 유지 |
| `DefaultNotificationOption` | 유지 |
| `ProgressNotificationOption` | 유지 |
| `SimpleNotificationOptionBase` | 유지 |
| `SimplePendingIntentOption` | 유지 |

`NotificationDefaultChannel`과 `SimplePendingIntentOption`은 알림 SPEC에 공개 동작과 필드가 명시되어 있으므로 제거 후보로 분류하지 않는다.

### core.controller.vibrator / wifi
| 타입 | 판정 |
| --- | --- |
| `VibratorController` | 유지 |
| `WifiController` | 유지 |
| `WifiNetworkDetails` | 유지 |

### core.extensions
| 타입 | 판정 |
| --- | --- |
| `SystemServiceExtensionsKt` | 유지 / 승인된 멤버 정리 완료 |

파일 퍼사드 타입은 Kotlin 확장 함수의 JVM ABI 표현이므로 유지한다. 다음 메이저 소스 상태의 공개 함수 19개는 시스템 서비스 접근자 13개, Controller 생성 함수 4개, Info 생성 함수 2개로 구성된다.

Controller·Info 생성 함수 6개는 기능 문서와 앱 사용처에 소비자 진입점으로 연결되어 있으므로 유지한다. 승인된 세 Core 시스템 서비스 접근자 외의 공개 함수는 변경하지 않았다.

`getSystemNotificationManager()`는 다음 메이저 기준선에서 제거했으며 `getNotificationManager()`를 유일한 공개 알림 서비스 접근자로 유지한다.

Core의 `getWindowManager()`와 `getInputMethodManager()`는 다음 메이저 기준선에서 제거했다. `DisplayInfo`, `FloatingViewController`, `SoftKeyboardController`는 `system_manager.xml.extensions.internal` 접근자를 통해 동일 Android 서비스를 사용한다.

### core.info.battery
| 타입 | 판정 | 근거 |
| --- | --- | --- |
| `BatteryStateConstants` | 유지 | Battery Info PRD/SPEC에 명시됨 |
| `BatteryStateEvent` | 유지 | `BatteryStateInfo.sfUpdate`의 공개 이벤트 계약 |
| `BatteryStateInfo` | 유지 | 소비자용 배터리 진입점 |
| `PowerProfile` | 내부화 완료 / 공개 기준선 제거 | `BatteryPropertyReader`가 소유하는 리플렉션 구현 |
| `PowerProfileVO` | 내부화 완료 / 공개 기준선 제거 | `PowerProfile` 구현용 리소스 키 열거형 |

#### PowerProfile 마이그레이션 경로
- `PowerProfile.getBatteryCapacity()` 사용자는 `BatteryStateInfo.getTotalCapacity()`로 이동한다.
- `PowerProfile.getAveragePower()`와 `PowerProfileVO`에 대응하는 안정적인 공개 대체 API는 제공하지 않는다.
- Android 비공개 `com.android.internal.os.PowerProfile` 리플렉션은 라이브러리 내부 폴백 구현으로만 유지한다.
- 다음 메이저 소스 상태에서 두 타입을 `internal`로 전환했으며 과도기 Deprecated 표시는 제거했다.
- PowerProfile 리플렉션 → chargeCounter 추정 → 오류 값의 내부 폴백 순서는 유지한다.

### core.info.location
| 타입 | 판정 |
| --- | --- |
| `LocationSharedPreference` | 유지 |
| `LocationStateConstants` | 유지 |
| `LocationStateEvent` | 유지 |
| `LocationStateInfo` | 유지 |

`LocationSharedPreference`는 위치 추적이나 권한 상태 관리 없이 `Location` 스냅샷의 저장·복원·삭제만 필요한 소비자가 직접 사용할 수 있는 경량 공개 API다. `LocationStateInfo`도 같은 저장 기능을 위임 제공하지만 생성 시 권한 상태와 `LocationManager` 관련 구성을 준비하므로 용도가 완전히 같지 않다. 공개 표면은 생성자와 메서드 3개로 제한적이며, 제거로 얻는 축소 효과보다 독립 저장 사용 사례를 유지하는 가치가 크므로 Deprecated 없이 유지한다.

### core.info.network.connectivity
| 타입 | 판정 |
| --- | --- |
| `NetworkConnectivityInfo` | 유지 |
| `NetworkBase` | 유지 |
| `NetworkCapabilitiesData` | 유지 |
| `NetworkConnectivitySummary` | 유지 |
| `NetworkLinkPropertiesData` | 유지 |

`NetworkConnectivityInfo`는 연결 상태 조회, 전송 타입 판정, 콜백 등록과 해제를 제공하는 소비자 진입점이므로 유지한다. `NetworkCapabilitiesData`, `NetworkConnectivitySummary`, `NetworkLinkPropertiesData`는 공개 콜백과 조회 결과에 직접 포함되므로 유지한다.

`NetworkBase`는 공개 `open` 기반 클래스이고 두 공개 데이터 클래스의 상위 타입이므로 소비자가 직접 상속하거나 타입으로 사용할 수 있는 계약이다. 사용자 결정에 따라 `NetworkBase`, `NetworkCapabilitiesData`, `NetworkLinkPropertiesData`의 공개 상속 관계를 유지한다. 다음 메이저 정리 범위에서도 내부 구성 전환이나 제거를 수행하지 않으며 새 Deprecated도 추가하지 않는다.

연결성 계약은 다음과 같이 고정한다.
- `isNetworkConnected()`는 활성 네트워크의 `NetworkCapabilities`와 `LinkProperties` 존재 여부이며, 검증된 인터넷 연결을 뜻하지 않는다.
- 콜백의 `handler`가 null이면 플랫폼 기본 연결성 콜백 스레드를 사용한다.
- 기본 네트워크 콜백의 `onLost`는 물리적 단절이 아니라 기본 네트워크 지위 상실만 의미할 수 있다.
- 전송 타입별 IP 조회는 `allNetworks`를 순회하는 IPv4 최선형 스냅샷이며 API 31 이상에서 권장되지 않는다.

#### Connectivity 검증 결과
- 기존 Connectivity 테스트 5개 스위트를 유지하면서 공개 조회, 전송 타입, 콜백 생명주기, SDK 분기, 문자열 폴백 경계 테스트 18개를 추가했다.
- Connectivity 테스트는 총 50개이며 전체 모듈 테스트는 72개 스위트, 1,296개 모두 통과했다.
- `NetworkConnectivityInfo` 라인 커버리지는 14.9%에서 99.0%, 브랜치는 9.1%에서 84.1%로 상승했다.
- `NetworkCapabilitiesData` 라인 커버리지는 12.3%에서 88.4%, 브랜치는 4.2%에서 84.5%로 상승했다.
- `NetworkConnectivitySummary` 라인 커버리지는 0%에서 100%로 상승했다.
- `NetworkLinkPropertiesData` 라인 커버리지는 35.5%에서 96.8%, 브랜치는 8.3%에서 66.7%로 상승했다.
- 모듈 전체 라인 커버리지는 76.7%에서 82.7%, 브랜치는 61.4%에서 67.0%로 상승했다.

### core.info.network.sim
| 타입 | 판정 |
| --- | --- |
| `SimInfo` | 유지 |

### core.info.network.telephony
| 타입 | 판정 |
| --- | --- |
| `TelephonyInfo` | 유지 |
| `CommonTelephonyCallback` | 내부화 완료 / 공개 기준선 제거 |
| `TelephonyCallbackManager` | 내부화 완료 / 공개 기준선 제거 |
| `CellIdentityCdmaData` | 유지 |
| `CellInfoCdmaData` | 유지 |
| `CellSignalStrengthCdmaData` | 유지 |
| `CellIdentityGsmData` | 유지 |
| `CellInfoGsmData` | 유지 |
| `CellSignalStrengthGsmData` | 유지 |
| `CellIdentityLteData` | 유지 |
| `CellInfoLteData` | 유지 |
| `CellSignalStrengthLteData` | 유지 |
| `CellIdentityNrData` | 유지 |
| `CellInfoNrData` | 유지 |
| `CellSignalStrengthNrData` | 유지 |
| `CellIdentityTdscdmaData` | 유지 |
| `CellInfoTdscdmaData` | 유지 |
| `CellSignalStrengthDataTdscdma` | 유지 |
| `CellIdentityWcdmaData` | 유지 |
| `CellInfoWcdmaData` | 유지 |
| `CellSignalStrengthWcdmaData` | 유지 |
| `CurrentCellInfo` | 유지 |
| `CurrentServiceState` | 유지 |
| `CurrentSignalStrength` | 유지 |
| `TelephonyNetworkDetailType` | 유지 |
| `TelephonyNetworkState` | 유지 |
| `TelephonyNetworkType` | 유지 |

Telephony의 다음 메이저 공개 타입은 총 25개다. `TelephonyInfo`를 소비자용 단일 진입점으로 유지하고 두 콜백 구현 타입은 내부화했다. 셀 규격별 데이터 타입과 `Current*`, `TelephonyNetwork*` 타입은 공개 콜백과 조회 결과에 연결되어 있으므로 유지한다.

#### Telephony 콜백 API 마이그레이션 경로
- `TelephonyCallbackManager` 사용자는 `TelephonyInfo`로 이동한다.
- `registerSimpleCallback()`과 `unregisterSimpleCallback()`은 각각 `TelephonyInfo.registerCallback()`과 `TelephonyInfo.unregisterCallback()`으로 이동한다.
- `registerAdvancedCallbackFromDefaultUSim()`과 `registerAdvancedCallback()`은 각각 `TelephonyInfo.registerTelephonyCallBackFromDefaultUSim()`과 `TelephonyInfo.registerTelephonyCallBack()`으로 이동한다.
- `unregisterAdvancedCallback()`은 `TelephonyInfo.unregisterCallBack()`으로 이동한다.
- 세 StateFlow 프로퍼티와 SIM 슬롯별 setter·조회 API는 `TelephonyInfo`의 동일 기능을 사용한다.
- `CommonTelephonyCallback`의 원시 Android 콜백 객체에는 직접 대체 API를 제공하지 않는다. SDK 분기와 콜백 생명주기는 `TelephonyInfo`가 관리한다.
- 다음 메이저 소스 상태에서 두 구현 타입과 중첩 콜백 타입을 공개 기준선에서 제거했다.
- 등록·해제, SDK·권한 분기, StateFlow, 슬롯별 setter와 `onDestroy()` 동작은 변경하지 않았다.

### xml.controller.softkeyboard
| 타입 | 판정 |
| --- | --- |
| `SoftKeyboardActionResult` | 유지 |
| `SoftKeyboardController` | 유지 |
| `SoftKeyboardFailureReason` | 유지 |
| `SoftKeyboardResizePolicy` | 유지 |

### xml.controller.systembar
| 타입 | 판정 |
| --- | --- |
| `SystemBarController` | 유지 |
| `SystemBarStableState` | 유지 |
| `SystemBarVisibleState` | 유지 |

### xml.controller.window
| 타입 | 판정 |
| --- | --- |
| `FloatingViewController` | 유지 |
| `FloatingDragView` | 유지 |
| `FloatingFixedView` | 유지 |
| `FloatingViewCollisionsType` | 유지 |
| `FloatingViewTouchType` | 유지 |

### xml.display / extensions
| 타입 | 판정 |
| --- | --- |
| `DisplayInfo` | 유지 |
| `DisplayInfoBarInsets` | 유지 |
| `DisplayInfoSize` | 유지 |
| `SystemServiceExtensionsXmlKt` | 유지 |

XML 확장 함수 5개는 README와 기능 PRD/SPEC에 공개 진입점으로 명시되어 있다. `getSystemBarController()`와 `destroySystemBarControllerCache()`는 Window별 캐시·정리 계약을 포함하므로 단순 생성 편의 함수로 축소할 수 없다. 두 파일 퍼사드는 기존 테스트에서 라인·메서드 100%를 충족하고, XML 퍼사드는 브랜치도 100%다.

## 단계별 처리 계획

### 1단계: 현행 공개 API 감사
- 상태: 완료
- API 기준선과 0.5.1 태그의 동일성 확인
- 최상위 공개 ABI 타입 82개 인벤토리 작성
- 즉시 제거 없이 후보 분류

### 2단계: PowerProfile 계열 마이그레이션 경고
- 상태: 완료
- `PowerProfile`, `PowerProfileVO`에 일반 경고 수준의 Deprecated 적용
- `PowerProfile.getBatteryCapacity()`의 대체 경로로 `BatteryStateInfo.getTotalCapacity()` 안내
- 평균 전력 리플렉션 API는 안정적인 공개 계약이 아님을 KDoc과 Deprecated 메시지에 명시
- 내부 `BatteryPropertyReader`의 의도적인 사용에는 범위를 한정해 경고 억제
- `apiDump` 실행 후 타입과 시그니처가 삭제되지 않고 기존 공개 ABI가 유지되는지 확인
- 단위 테스트, Robolectric 테스트, API 검증 실행

### 3단계: 패키지별 상세 감사
1. Telephony의 진입점, 콜백 관리자, 셀 데이터 계약 — 완료
   - `TelephonyInfo`와 공개 결과 모델 24개 유지
   - `CommonTelephonyCallback`, `TelephonyCallbackManager`에 일반 경고 수준의 Deprecated 적용
   - 다음 메이저에서 콜백 구현 계층 내부화
2. Alarm의 기반 클래스와 상수 공개 필요성 — 완료
   - `BaseAlarmReceiver`와 중첩 `RegisterType`을 소비자 확장 계약으로 유지
   - `AlarmConstants.ALARM_KEY`, `ALARM_KEY_DEFAULT_VALUE`를 Receiver 인텐트 계약으로 유지
   - WakeLock 정책 상수와 정확 알람 권한 변경 액션은 현 ABI를 유지하고 다음 메이저 준비 시 멤버 단위 재검토
3. Location 저장소 직접 노출 필요성 — 완료
   - `LocationStateInfo`를 위치 상태 수집·조회·저장의 주요 진입점으로 유지
   - `LocationSharedPreference`를 위치 스냅샷 저장만 필요한 소비자용 경량 API로 유지
   - 공개 표면이 작고 독립 사용 사례가 있으므로 Deprecated 또는 다음 메이저 내부화 대상으로 분류하지 않음
4. Connectivity 기반 데이터 클래스 상속 구조 — 완료
   - `NetworkConnectivityInfo`와 공개 콜백·조회 데이터 타입 3개 유지
   - `NetworkBase`와 두 공개 데이터 타입의 상속 계약을 Deprecated 없이 유지
   - 다음 메이저 공개 API 정리 범위에서 내부 구성 전환과 공개 상속 제거를 제외
   - 연결 판정, 콜백 스레드, 기본 네트워크 상실, IPv4 스냅샷의 계약 경계를 문서화
5. core/xml 확장 함수 파일 퍼사드 — 완료
   - Core 공개 확장 22개와 XML 공개 확장 5개의 ABI·구현·문서·사용처 확인
   - Controller·Info 생성 확장과 XML 확장 5개를 소비자 진입점으로 유지
   - `getSystemNotificationManager()` 구현을 `getNotificationManager()` 위임으로 단일화하고 4단계 제거 대상으로 확정
   - XML 계층에서만 사용하는 `getWindowManager()`, `getInputMethodManager()`를 4단계 XML 내부 접근자 전환 대상으로 확정
   - 두 파일 퍼사드의 기존 테스트 커버리지 100%를 확인해 테스트 추가 없이 현행 스위트 유지

각 패키지는 별도 검토 결과를 보고하고 승인받은 뒤 변경한다.

### 4단계: 다음 메이저 정리
- 상태: 구현 및 전체 회귀 검증 완료
- `PowerProfile`, `PowerProfileVO`를 `internal`로 전환했다.
- `CommonTelephonyCallback`, `TelephonyCallbackManager`를 `internal`로 전환했다.
- `getSystemNotificationManager()`를 제거하고 `getNotificationManager()`를 대표 API로 유지했다.
- `getWindowManager()`, `getInputMethodManager()`를 Core 공개 API에서 제거하고 XML 계층 내부 접근자로 전환했다.
- `NetworkBase`, `NetworkCapabilitiesData`, `NetworkLinkPropertiesData`의 공개 상속 계약은 변경하지 않았다.
- `BuildConfig` 테스트 의존을 제거하고 `buildFeatures.buildConfig = false`를 적용했다.
- 배치별 대상 테스트, 컴파일, API diff, `apiCheck`를 통과했다.
- Breaking Change와 마이그레이션 경로를 `README_SYSTEM_MANAGER_MIGRATION.md`에 기록했다.

## 최종 회귀 검증 결과
- Unit 17개 스위트 326개 테스트와 Robolectric 55개 스위트 973개 테스트가 실패·오류·건너뜀 없이 통과했다.
- 모듈 debug AAR과 소비자 앱 debug APK 빌드가 성공했다.
- `ktlintCheck`, `:simple_system_manager:lintDebug`, `:simple_system_manager:apiCheck`, `git diff --check`가 통과했다.
- 공개 API 기준선 차이는 추가 0줄, 삭제 155줄이며 승인된 다섯 정리 항목 외 삭제는 없다.
- `NetworkBase`, `NetworkCapabilitiesData`, `NetworkLinkPropertiesData`의 공개 상속과 `TelephonyInfo`, `BatteryStateInfo.getTotalCapacity()`, `getNotificationManager()`가 기준선에 남아 있다.
- debug AAR과 공개 API 기준선에서 `simple_system_manager.BuildConfig`가 제거되었음을 확인했다.
- 변경 파일 22개는 UTF-8로 정상 디코딩되고 `U+FFFD`가 없으며, 마이그레이션 문서의 로컬 링크 4개는 모두 유효하다.

## BuildConfig 제거 조건
- 라이브러리 본문과 테스트의 `simple_system_manager.BuildConfig` 직접 사용처는 0건이다.
- SystemBar debug 테스트는 생성 클래스 없이 오프 메인 스레드의 `IllegalStateException`을 직접 검증한다.
- 생성 타입에는 소스 수준 Deprecated를 직접 적용하지 않는다.
- `buildFeatures.buildConfig = false` 적용과 API 제거를 같은 배치에서 완료했다.
- debug AAR과 공개 API 기준선에 `simple_system_manager.BuildConfig`가 남지 않는다.
- 소비자는 라이브러리의 `BuildConfig`가 아니라 자신의 앱 또는 모듈 빌드 설정을 사용해야 한다.

## 검증 기준

### 문서 단계
- 인벤토리 수가 API 기준선의 최상위 공개 타입 수와 일치해야 한다.
- `internal` 경로의 공개 타입을 누락하지 않아야 한다.
- 기능 PRD/SPEC에 명시된 타입을 근거 없이 제거 후보로 분류하지 않아야 한다.

### 코드 변경 단계
1. `./gradlew :simple_system_manager:compileDebugKotlin`
2. `./gradlew :simple_system_manager:compileDebugUnitTestKotlin`
3. `./gradlew :simple_system_manager:testAll`
4. `./gradlew :simple_system_manager:apiDump`
5. `./gradlew :simple_system_manager:apiCheck`

### 다음 메이저 제거 단계
- 소비자 대체 경로가 문서화되어 있어야 한다.
- 기존 Deprecated가 있던 구현 타입은 과도기 안내를 거쳤으며, 별칭·생성 API에는 새 Deprecated를 추가하지 않는다.
- API 기준선 차이를 Breaking Change로 승인해야 한다.
- 릴리스 노트에 제거 타입과 대체 방법을 기록해야 한다.

## 현 단계 결론
- 승인된 다음 메이저 공개 API 정리 5개 배치의 구현과 배치별 검증을 완료했다.
- `PowerProfile`, `PowerProfileVO`는 내부화했으며 `BatteryStateInfo.getTotalCapacity()`를 공개 대체 경로로 유지한다.
- `BuildConfig` 생성과 공개 기준선 항목을 제거했다.
- Telephony 상세 감사 결과 `TelephonyInfo`와 공개 결과 모델 24개는 유지한다.
- `CommonTelephonyCallback`, `TelephonyCallbackManager`는 내부화했고 공개 위임 동작은 `TelephonyInfo`에 유지한다.
- Alarm 상세 감사 결과 8개 최상위 공개 타입은 모두 유지한다.
- Location 상세 감사 결과 `LocationSharedPreference`를 포함한 4개 최상위 공개 타입은 모두 유지한다.
- Connectivity 상세 감사 결과 진입점과 공개 데이터 타입 3개는 유지한다.
- `NetworkBase`와 두 공개 데이터 타입의 상속 계약은 다음 메이저 공개 API 정리에서도 유지한다.
- 확장 함수 퍼사드는 유지하고 승인된 Core 멤버 세 개만 제거했다. XML 공개 확장 5개는 유지한다.
- `getNotificationManager()`를 유일한 공개 알림 서비스 접근자로 유지한다.
- WindowManager·InputMethodManager의 라이브러리 내부 사용 경로는 XML 내부 접근자로 이동했다.
- 전체 테스트·정적 분석·모듈 및 소비자 앱 빌드를 포함한 최종 회귀 검증을 완료했다.
