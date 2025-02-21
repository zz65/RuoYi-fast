package com.ruoyi.project.device.enums;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/21 14:04
 * @description: 详细说明
 */
public enum DataReportDataType {
    REALTIME_DATA(1, "实时数据"),
    TURN_ON_OR_OFF_DATA(2, "开关机信号");

    private Integer dataType;
    private String description;

    DataReportDataType(int dataType, String description) {
        this.dataType = dataType;
        this.description = description;
    }

    public Integer getDataType() {
        return dataType;
    }

    public String getDescription() {
        return description;
    }
}
