package com.ruoyi.project.device.enums;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/18 13:43
 * @description: 详细说明
 */
public enum OnlineState {
    ONLINE("在线"),
    OFFLINE("离线"),
    UNKNOWN("未知");

    private String value;

    OnlineState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
