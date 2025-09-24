package kr.open.library.simple_ui.system_manager.info.battery.power

public enum class PowerProfileVO(public val  res: String) {
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
     * Power consumption when WiFi driver is scanning for networks.
     */
    POWER_WIFI_SCAN("wifi.scan"),

    /**
     * Power consumption when WiFi driver is on.
     */
    POWER_WIFI_ON("wifi.on"),

    /**
     * Power consumption when WiFi driver is transmitting/receiving.
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
     * Power consumption when GPS is on.
     */
    POWER_GPS_ON("gps.on"),

    /**
     * GPS power parameters based on signal quality
     */
    POWER_GPS_SIGNAL_QUALITY_BASED("gps.signalqualitybased"),
    POWER_GPS_OPERATING_VOLTAGE("gps.voltage"),

    /**
     * Power consumption when cell radio is on but not on a call.
     */
    POWER_RADIO_ON("radio.on"),

    /**
     * Power consumption when cell radio is hunting for a signal.
     */
    POWER_RADIO_SCANNING("radio.scanning"),

    /**
     * Power consumption when talking on the phone.
     */
    POWER_RADIO_ACTIVE("radio.active"),

    /**
     * Power consumed by the audio hardware when playing back audio content. This is in addition
     * to the CPU power, probably due to a DSP and / or amplifier.
     */
    POWER_AUDIO("audio"),

    /**
     * Power consumed by any media hardware when playing back video content. This is in addition
     * to the CPU power, probably due to a DSP.
     */
    POWER_VIDEO("video"),

    /**
     * Average power consumption when camera flashlight is on.
     */
    POWER_FLASHLIGHT("camera.flashlight"),

    /**
     * Power consumption when DDR is being used.
     */
    POWER_MEMORY("memory.bandwidths"),

    /**
     * Average power consumption when the camera is on over all standard use cases.
     *
     * Add more fine-grained camera power metrics.
     */
    POWER_CAMERA("camera.avg"),

    /**
     * Power consumed by wif batched scaning.  Broken down into bins by
     * Channels Scanned per Hour.  May do 1-720 scans per hour of 1-100 channels
     * for a range of 1-72,000.  Going logrithmic (1-8, 9-64, 65-512, 513-4096, 4097-)!
     */
    POWER_WIFI_BATCHED_SCAN("wifi.batchedscan"),

    /**
     * Battery capacity in milliAmpHour (mAh).
     */
    POWER_BATTERY_CAPACITY("battery.capacity"),

    /**
     * Power consumption when a screen is in doze/ambient/always-on mode, including backlight power.
     */
    POWER_GROUP_DISPLAY_AMBIENT("ambient.on.display"),

    /**
     * Power consumption when a screen is on, not including the backlight power.
     */
    POWER_GROUP_DISPLAY_SCREEN_ON("screen.on.display"),

    /**
     * Power consumption of a screen at full backlight brightness.
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