package com.ruoyi.project.device.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.web.domain.BaseEntity;
import com.ruoyi.project.device.domain.LogDeviceOperate;
import com.ruoyi.project.device.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 操作日志 值对象
 *
 * @author zhuzhen
 * @date 2025/2/24 10:13
 * @description: 详细说明
 */
@Data
public class LogDeviceOperateVo extends LogDeviceOperate {
    /* 设备序列号 */
    private String deviceSn;

    /* 设备名称 */
    private String deviceName;
}
