package com.ruoyi.project.mqtt.bean;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

/**
 * 上报数据的格式
 *
 * @author zhuzhen
 * @date 2025/2/21 10:21
 * @description: 转成json后的数据格式样例如下
 * {
 *     "deviceId": "唯一标识，如sn",
 *     "timestamp": 1739771910,
 *     "dataType": 1,
 *     "data": {
 *         "timestamp": 1739771910,
 *         "voltage": 60.0,
 *         "current": 100.0
 *     }
 * }
 */
@Data
public class DataReportMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 设备唯一标识,如sn
     */
    private String deviceId;

    /**
     * 上传数据时的时间戳（秒）
     */
    private Long timestamp;

    /**
     * 数据类型
     */
    private Integer dataType;

    /**
     * 上报的具体数据
     */
    private ReportData data;

    @Data
    public static class ReportData  {
        /**
         * 采集数据时的时间戳（秒）
         */
        private Long timestamp;

        /**
         * 焊接电压
         */
        private Double voltage;

        /**
         * 焊接电流
         */
        private Double current;
    }
}
