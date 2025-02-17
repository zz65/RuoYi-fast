package com.ruoyi.project.mqtt.netty.core;

public enum MessageStatus {
    /**
     * none
     */
    None,
    /**
     * Qos1
     */
    PUB,
    /**
     * Qos2
     */
    PUBREC,
    /**
     * Qos2
     */
    PUBREL,
    /**
     * finish
     */
    COMPLETE,
}
