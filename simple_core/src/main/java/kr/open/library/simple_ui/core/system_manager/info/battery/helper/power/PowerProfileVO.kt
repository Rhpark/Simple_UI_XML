package kr.open.library.simple_ui.core.system_manager.info.battery.helper.power

/**
 * Enum class defining power profile metric types for device power consumption information.<br><br>
 * 기기 전력 소모 정보를 표현하는 파워 프로파일 지표 유형을 정의합니다.<br>
 *
 * @param res Resource string identifier for the power profile metric.<br><br>
 *            파워 프로파일 지표에 대응하는 리소스 문자열 식별자입니다.<br>
 */
public enum class PowerProfileVO(
    public val res: String,
) {
    /**
     * Power consumption when CPU is in power collapse mode (deepest sleep state).<br><br>
     * CPU가 전원 차단 모드(가장 깊은 절전 상태)일 때의 전력 소비입니다.<br>
     */
    POWER_CPU_SUSPEND("cpu.suspend"),

    /**
     * Power consumption when CPU is awake but idle (wake lock held, no CPU activity).<br>
     * Should be zero on devices with full CPU power collapse support.<br><br>
     * CPU가 깨어 있지만 유휴 상태일 때의 전력 소비(wake lock 유지, CPU 활동 없음)입니다.<br>
     * 완전한 CPU 전원 차단을 지원하는 기기에서는 0이어야 합니다.<br>
     */
    POWER_CPU_IDLE("cpu.idle"),

    /**
     * Power consumption when CPU is actively running, excluding cluster and core power.<br><br>
     * CPU가 활발히 실행 중일 때의 전력 소비(클러스터 및 코어 전력 제외)입니다.<br>
     */
    POWER_CPU_ACTIVE("cpu.active"),

    /**
     * Power consumption when screen is at full brightness.<br><br>
     * 화면이 최대 밝기일 때의 전력 소비입니다.<br>
     */
    POWER_SCREEN_FULL("screen.full"),

    /**
     * Power consumption when WiFi driver is scanning for networks.<br><br>
     * WiFi 드라이버가 네트워크를 스캔할 때의 전력 소모입니다.<br>
     */
    POWER_WIFI_SCAN("wifi.scan"),

    /**
     * Power consumption when WiFi driver is on.<br><br>
     * WiFi 드라이버가 켜져 있을 때의 전력 소모입니다.<br>
     */
    POWER_WIFI_ON("wifi.on"),

    /**
     * Power consumption when WiFi driver is transmitting or receiving.<br><br>
     * WiFi 드라이버가 송수신 중일 때의 전력 소모입니다.<br>
     */
    POWER_WIFI_ACTIVE("wifi.active"),

    /**
     * WiFi controller power when idle (not transmitting or receiving).<br><br>
     * WiFi 컨트롤러가 유휴 상태(송수신하지 않음)일 때의 전력입니다.<br>
     */
    POWER_WIFI_CONTROLLER_IDLE("wifi.controller.idle"),

    /**
     * WiFi controller power when receiving data.<br><br>
     * WiFi 컨트롤러가 데이터를 수신할 때의 전력입니다.<br>
     */
    POWER_WIFI_CONTROLLER_RX("wifi.controller.rx"),

    /**
     * WiFi controller power when transmitting data.<br><br>
     * WiFi 컨트롤러가 데이터를 송신할 때의 전력입니다.<br>
     */
    POWER_WIFI_CONTROLLER_TX("wifi.controller.tx"),

    /**
     * WiFi controller transmit power levels.<br><br>
     * WiFi 컨트롤러 송신 전력 레벨입니다.<br>
     */
    POWER_WIFI_CONTROLLER_TX_LEVELS("wifi.controller.tx_levels"),

    /**
     * WiFi controller operating voltage.<br><br>
     * WiFi 컨트롤러 작동 전압입니다.<br>
     */
    POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE("wifi.controller.voltage"),

    /**
     * Bluetooth controller power when idle (not transmitting or receiving).<br><br>
     * 블루투스 컨트롤러가 유휴 상태(송수신하지 않음)일 때의 전력입니다.<br>
     */
    POWER_BLUETOOTH_CONTROLLER_IDLE("bluetooth.controller.idle"),

    /**
     * Bluetooth controller power when receiving data.<br><br>
     * 블루투스 컨트롤러가 데이터를 수신할 때의 전력입니다.<br>
     */
    POWER_BLUETOOTH_CONTROLLER_RX("bluetooth.controller.rx"),

    /**
     * Bluetooth controller power when transmitting data.<br><br>
     * 블루투스 컨트롤러가 데이터를 송신할 때의 전력입니다.<br>
     */
    POWER_BLUETOOTH_CONTROLLER_TX("bluetooth.controller.tx"),

    /**
     * Bluetooth controller operating voltage.<br><br>
     * 블루투스 컨트롤러 작동 전압입니다.<br>
     */
    POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE("bluetooth.controller.voltage"),

    /**
     * Modem controller power when in sleep mode.<br><br>
     * 모뎀 컨트롤러가 절전 모드일 때의 전력입니다.<br>
     */
    POWER_MODEM_CONTROLLER_SLEEP("modem.controller.sleep"),

    /**
     * Modem controller power when idle (registered but not transmitting/receiving).<br><br>
     * 모뎀 컨트롤러가 유휴 상태(등록되었지만 송수신하지 않음)일 때의 전력입니다.<br>
     */
    POWER_MODEM_CONTROLLER_IDLE("modem.controller.idle"),

    /**
     * Modem controller power when receiving data.<br><br>
     * 모뎀 컨트롤러가 데이터를 수신할 때의 전력입니다.<br>
     */
    POWER_MODEM_CONTROLLER_RX("modem.controller.rx"),

    /**
     * Modem controller power when transmitting data.<br><br>
     * 모뎀 컨트롤러가 데이터를 송신할 때의 전력입니다.<br>
     */
    POWER_MODEM_CONTROLLER_TX("modem.controller.tx"),

    /**
     * Modem controller operating voltage.<br><br>
     * 모뎀 컨트롤러 작동 전압입니다.<br>
     */
    POWER_MODEM_CONTROLLER_OPERATING_VOLTAGE("modem.controller.voltage"),

    /**
     * Power consumption when GPS is on.<br><br>
     * GPS가 켜져 있을 때의 전력 소모입니다.<br>
     */
    POWER_GPS_ON("gps.on"),

    /**
     * GPS power parameters based on signal quality.<br><br>
     * 신호 품질을 기준으로 계산되는 GPS 전력 파라미터입니다.<br>
     */
    POWER_GPS_SIGNAL_QUALITY_BASED("gps.signalqualitybased"),
    POWER_GPS_OPERATING_VOLTAGE("gps.voltage"),

    /**
     * Power consumption when cell radio is on but not on a call.<br><br>
     * 셀 라디오가 켜져 있지만 통화 중이 아닐 때의 전력 소모입니다.<br>
     */
    POWER_RADIO_ON("radio.on"),

    /**
     * Power consumption when cell radio is hunting for a signal.<br><br>
     * 셀 라디오가 신호를 탐색할 때의 전력 소모입니다.<br>
     */
    POWER_RADIO_SCANNING("radio.scanning"),

    /**
     * Power consumption when talking on the phone.<br><br>
     * 통화 중일 때의 전력 소모입니다.<br>
     */
    POWER_RADIO_ACTIVE("radio.active"),

    /**
     * Power consumed by the audio hardware when playing audio content.<br>
     * This is in addition to CPU power, likely due to a DSP and/or amplifier.<br><br>
     * 오디오 콘텐츠 재생 시 오디오 하드웨어가 사용하는 전력입니다.<br>
     * DSP 또는 앰프 때문으로 추정되어 CPU 전력에 추가로 소모됩니다.<br>
     */
    POWER_AUDIO("audio"),

    /**
     * Power consumed by media hardware when playing video content.<br>
     * This is in addition to CPU power, likely due to a DSP.<br><br>
     * 비디오 콘텐츠 재생 시 미디어 하드웨어가 사용하는 전력입니다.<br>
     * DSP 등으로 인해 CPU 전력 외에 추가로 소모됩니다.<br>
     */
    POWER_VIDEO("video"),

    /**
     * Average power consumption when the camera flashlight is on.<br><br>
     * 카메라 플래시가 켜져 있을 때의 평균 전력 소모입니다.<br>
     */
    POWER_FLASHLIGHT("camera.flashlight"),

    /**
     * Power consumption when DDR is being used.<br><br>
     * DDR을 사용할 때의 전력 소모입니다.<br>
     */
    POWER_MEMORY("memory.bandwidths"),

    /**
     * Average power consumption when the camera is on across typical use cases.<br><br>
     * 일반적인 사용 시나리오에서 카메라가 켜져 있을 때의 평균 전력 소모입니다.<br>
     */
    POWER_CAMERA("camera.avg"),

    /**
     * Power consumed by WiFi batched scanning.<br>
     * Binned by the number of channels scanned per hour.<br><br>
     * WiFi 배치 스캔 시 소비되는 전력입니다.<br>
     * 시간당 스캔한 채널 수에 따라 구간으로 나뉩니다.<br>
     */
    POWER_WIFI_BATCHED_SCAN("wifi.batchedscan"),

    /**
     * Battery capacity in milliampere-hours (mAh).<br><br>
     * 배터리 용량(mAh 단위)입니다.<br>
     */
    POWER_BATTERY_CAPACITY("battery.capacity"),

    /**
     * Power consumption when a screen is in doze/ambient/always-on mode, including backlight power.<br><br>
     * 화면이 도즈/앰비언트/올웨이즈온 모드일 때의 전력 소모(백라이트 포함)입니다.<br>
     */
    POWER_GROUP_DISPLAY_AMBIENT("ambient.on.display"),

    /**
     * Power consumption when a screen is on, excluding the backlight power.<br><br>
     * 화면이 켜져 있을 때의 전력 소모(백라이트 제외)입니다.<br>
     */
    POWER_GROUP_DISPLAY_SCREEN_ON("screen.on.display"),

    /**
     * Power consumption of a screen at full backlight brightness.<br><br>
     * 화면을 최대 백라이트 밝기로 켰을 때의 전력 소모입니다.<br>
     */
    POWER_GROUP_DISPLAY_SCREEN_FULL("screen.full.display"),

    /**
     * XML tag identifier for device element.<br><br>
     * 디바이스 요소를 위한 XML 태그 식별자입니다.<br>
     */
    TAG_DEVICE("device"),

    /**
     * XML tag identifier for item element.<br><br>
     * 항목 요소를 위한 XML 태그 식별자입니다.<br>
     */
    TAG_ITEM("item"),

    /**
     * XML tag identifier for array element.<br><br>
     * 배열 요소를 위한 XML 태그 식별자입니다.<br>
     */
    TAG_ARRAY("array"),

    /**
     * XML tag identifier for array item value.<br><br>
     * 배열 항목 값을 위한 XML 태그 식별자입니다.<br>
     */
    TAG_ARRAYITEM("value"),

    /**
     * XML attribute identifier for name.<br><br>
     * 이름을 위한 XML 속성 식별자입니다.<br>
     */
    ATTR_NAME("name"),

    /**
     * XML tag identifier for modem element.<br><br>
     * 모뎀 요소를 위한 XML 태그 식별자입니다.<br>
     */
    TAG_MODEM("modem"),

    /**
     * Number of CPU cores per cluster.<br><br>
     * 클러스터당 CPU 코어 수입니다.<br>
     */
    CPU_PER_CLUSTER_CORE_COUNT("cpu.clusters.cores"),

    /**
     * Power consumption for each CPU cluster.<br><br>
     * 각 CPU 클러스터의 전력 소비입니다.<br>
     */
    CPU_CLUSTER_POWER_COUNT("cpu.cluster_power.cluster"),

    /**
     * Prefix for CPU core speed values per cluster.<br><br>
     * 클러스터별 CPU 코어 속도 값의 접두사입니다.<br>
     */
    CPU_CORE_SPEED_PREFIX("cpu.core_speeds.cluster"),

    /**
     * Prefix for CPU core power values per cluster.<br><br>
     * 클러스터별 CPU 코어 전력 값의 접두사입니다.<br>
     */
    CPU_CORE_POWER_PREFIX("cpu.core_power.cluster"),
}
