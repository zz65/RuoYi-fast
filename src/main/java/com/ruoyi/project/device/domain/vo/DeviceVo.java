package com.ruoyi.project.device.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.xss.Xss;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.enums.OnlineState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备 值对象
 *
 * @author zhuzhen
 * @date 2025/2/18 13:39
 * @description: 详细说明
 */
@Data
public class DeviceVo extends Device implements Serializable {
    private static final long serialVersionUID = 1L;
    /* 在线状态 */
    private OnlineState onlineState;

    /* 最后一次心跳时间 */
    private LocalDateTime lastHeatbeatTime;

    /* 焊接电压 */
    private Double voltage;

    /* 焊接电流 */
    private Double current;

    /* 开机时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime turnOnTime;

    /* 关机时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime turnOffTime;

    /* 焊工姓名 */
    private String operator;

    /* 焊工工号 */
    private String operatorNo;
}
