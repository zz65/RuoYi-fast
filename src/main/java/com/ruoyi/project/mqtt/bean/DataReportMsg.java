package com.ruoyi.project.mqtt.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

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
     * 设备唯一标识,如sn。注意这里的deviceId是设备端传上来的，不是iot_device表的id
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
    private Map data;

    /**
     * 上传实时数据的参数结构
     */
    @Data
    public static class RealTimeData  {
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


    /**
     * 上传开关机信号的参数结构
     */
    @Data
    public static class TurnOnOrOffData {
        /**
         * 采集数据时的时间戳（秒）
         */
        private Long timestamp;

        /**
         * 通用唯一码
         */
        private String uuid;

        /**
         * 是否开机 true开机 false关机
         */
        private Boolean isOn;

        /**
         * 焊工姓名
         */
        private String operator;

        /**
         * 焊工工号
         */
        private String operatorNo;
    }
}
