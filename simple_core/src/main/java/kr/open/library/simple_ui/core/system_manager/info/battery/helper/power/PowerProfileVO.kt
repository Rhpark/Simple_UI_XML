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
    /*
     * POWER_CPU_SUSPEND: Power consumption when CPU is in power collapse mode.
     * POWER_CPU_IDLE: Power consumption when CPU is awake (when a wake lock is held). This should
     *                 be zero on devices that can go into full CPU power collapse even when a wake
     *                 lock is held. Otherwise, this is the power consumption in addition to
     * POWER_CPU_SUSPEND due to a wake lock being held but with no CPU activity.
     * POWER_CPU_ACTIVE: Power consumption when CPU is running, excluding power consumed by clusters
     *                   and cores.
     *
     * CPU Power Equation (assume two clusters):
     * Total power(POWER_CPU_SUSPEND  (always added)
     *               + POWER_CPU_IDLE   (skip this and below if in power collapse mode)
     *               + POWER_CPU_ACTIVE (skip this and below if CPU is not running, but a wakelock
     *                                   is held)
     *               + cluster_power.cluster0 + cluster_power.cluster1 (skip cluster not running)
     *               + core_power.cluster0 * num running cores in cluster 0
     *               + core_power.cluster1 * num running cores in cluster 1
     */
    POWER_CPU_SUSPEND("cpu.suspend"),
    POWER_CPU_IDLE("cpu.idle"),
    POWER_CPU_ACTIVE("cpu.active"),

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

    //
    // Updated power constants. These are not estimated, they are real world
    // currents and voltages for the underlying bluetooth and wifi controllers.
    //
    POWER_WIFI_CONTROLLER_IDLE("wifi.controller.idle"),
    POWER_WIFI_CONTROLLER_RX("wifi.controller.rx"),
    POWER_WIFI_CONTROLLER_TX("wifi.controller.tx"),
    POWER_WIFI_CONTROLLER_TX_LEVELS("wifi.controller.tx_levels"),
    POWER_WIFI_CONTROLLER_OPERATING_VOLTAGE("wifi.controller.voltage"),

    POWER_BLUETOOTH_CONTROLLER_IDLE("bluetooth.controller.idle"),
    POWER_BLUETOOTH_CONTROLLER_RX("bluetooth.controller.rx"),
    POWER_BLUETOOTH_CONTROLLER_TX("bluetooth.controller.tx"),
    POWER_BLUETOOTH_CONTROLLER_OPERATING_VOLTAGE("bluetooth.controller.voltage"),

    POWER_MODEM_CONTROLLER_SLEEP("modem.controller.sleep"),
    POWER_MODEM_CONTROLLER_IDLE("modem.controller.idle"),
    POWER_MODEM_CONTROLLER_RX("modem.controller.rx"),
    POWER_MODEM_CONTROLLER_TX("modem.controller.tx"),
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

    TAG_DEVICE("device"),
    TAG_ITEM("item"),
    TAG_ARRAY("array"),
    TAG_ARRAYITEM("value"),
    ATTR_NAME("name"),

    TAG_MODEM("modem"),

    CPU_PER_CLUSTER_CORE_COUNT("cpu.clusters.cores"),
    CPU_CLUSTER_POWER_COUNT("cpu.cluster_power.cluster"),
    CPU_CORE_SPEED_PREFIX("cpu.core_speeds.cluster"),
    CPU_CORE_POWER_PREFIX("cpu.core_power.cluster"),
}
