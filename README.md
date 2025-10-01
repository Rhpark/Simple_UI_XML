# 🚀 Simple UI XML — 복잡함은 우리가, 속도는 당신에게

> **반복되는 Activity/Fragment 세팅**, **끝나지 않는 권한 처리**, 그리고 불어나는 보일러플레이트...  
> 그 시간을 돌려주기 위해 **Simple UI XML**을 만들었다.

<br>
</br>

## ✨ Simple UI XML: 핵심 효과 & 도입 이유 (Numbers that matter)

- **250줄 → 87줄 / 4–5h → 2~3h (≈50% 단축)** — 보일러플레이트를 걷어내 핵심 흐름만 남깁니다.
- **복잡한 Android API를 "한 줄"로** — Activity 세팅·권한 처리 등 상용구 자동화.
- **현업이 매일 바라던 기능을 기본 제공** — 도입 즉시 체감 속도 상승.
- **반복 코딩 감소 → 핵심 기능 개발 집중** — 팀 전체 **리드타임·품질** 동시 향상.

<br>
</br>

### 👥 **팀 개발 생산성 혁신**
- **코드 일관성 보장**: 모든 팀원이 동일한 Base 클래스 & Extension 사용 → 버그 추적·유지보수 효율성 극대화
- **신규 멤버 온보딩 가속화**: 복잡한 Android API 학습 불필요 → 적응 기간 단축
- **코드리뷰 시간 70% 단축**: 표준화된 패턴으로 리뷰 포인트 명확화 → 핵심 로직에만 집중
- **협업 충돌 최소화**: PermissionManager, Logx 등 통합 시스템으로 중복 구현 방지


> **한 줄 결론:** 복잡함은 **Simple UI XML**에게, 속도는 **당신에게**.

<br>
</br>

## 🎯 **타겟 사용자**
**XML View 시스템** 환경

<br>
</br>

## 📋 **라이브러리 기본 설정**

- **minSdk**: 28
- **compileSdk**: 35
- **Kotlin**: 2.0.21
- **Android Gradle Plugin**: 8.8.2

<br>
</br>

## ✨ 핵심 특징

### 📱 **UI 개발 가속화**
- **기본 클래스**: RootActivity, BaseActivity, BaseBindingActivity
- **Fragment 지원**: RootFragment, BaseFragment, BaseBindingFragment, RootDialogFragment, BaseDialogFragment, BaseBindingDialogFragment
- **RecyclerView**: 다양한 Adapter, ViewHolder, DiffUtil + RecyclerScrollStateView
- **커스텀 레이아웃**: Lifecycle 지원하는 Layout 컴포넌트들
- **XML 스타일 시스템**: 포괄적인 UI 스타일 라이브러리 (style.xml)
- **MVVM 지원**: ViewModel, DataBinding 완벽 호환

<br>
</br>

### 🔧 **개발 편의성**
- **확장 함수**: Bundle, String, Date, Time, TryCatch 등 실용적인 Extensions
- **안전한 코딩**: safeCatch를 통한 예외 처리 간소화
- **권한 관리**: PermissionManager 통합 지원
- **고급 로깅**: Logx - 파일 저장, 필터링, 커스텀 포매팅 지원
- **로컬 저장**: BaseSharedPreference 관리

<br>
</br>

### ⚙️ **간단히 사용가능한 시스템 제어 (System Manager)**
- **알림 시스템**: Alarm, Notification 제어
- **네트워크 종합**: WiFi, Network Connectivity, Sim Info 상세 관리
- **통신망 정보**: Telephony (GSM/LTE/5G NR/CDMA/WCDMA)  지원
- **디바이스 정보**: Battery, Display, Location 실시간 모니터링
- **UI 제어**: SoftKeyboard, Vibrator, FloatingView (Drag/Fixed)

<br>
</br>

## 예제
- **Logx 사용** 예제 : [README_LOGX.md](README_LOGX.md)
- **MVVM 패턴 사용** 예제 : [README_MVVM.md](README_MVVM.md)
- **Extensions 사용** 예제 : [README_EXTENSIONS.md](README_EXTENSIONS.md)
- **Layout Style XML 사용** 예제 : [README_STYLE.md](README_STYLE.md)
- **Recycler/Adapter 사용** 예제 : [README_RECYCLERVIEW.md](README_RECYCLERVIEW.md)
- **System Service Manager Info** 예제 : [README_SERVICE_MANAGER_INFO.md](README_SERVICE_MANAGER_INFO.md)
- **System Service Manager Controller** 예제 : [README_SERVICE_MANAGER_CONTROL.md](README_SERVICE_MANAGER_CONTROL.md)
- **간단 사용** 예제 : [README_SAMPLE.md](README_SAMPLE.md)

<br>
</br>

## 🚀 **설치 방법**

```gradle
// 추후 추가 예정
dependencies {
    implementation 'kr.open.library:simple-ui-xml:x.x.x'
}
```

<br>
</br>

## 📄 **라이선스**

MIT License - 자유롭게 사용하세요!

---

<br>
</br>

**프로젝트 구조**: `simple_ui` 모듈이 실제 라이브러리, `app` 모듈은 테스트용 샘플

<br>
</br>

.