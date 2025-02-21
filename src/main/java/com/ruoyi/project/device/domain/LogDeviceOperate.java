package com.ruoyi.project.device.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.web.domain.BaseEntity;
import com.ruoyi.project.device.enums.OperateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 操作日志对象 iot_log_device_operate
 *
 * @author zhuzhen
 * @date 2025/2/17 15:25
 * @description: 详细说明
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogDeviceOperate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 日志id */
    private Long id;

    /** 用于唯一确定对电焊机的使用记录。用于方便查找某次电焊机使用记录的开机时间和关机时间，方便计算操作时长。 */
    private String uuid;

    /** 设备id,iot_device表主键 */
    private Long deviceId;

    /** 操作员姓名 */
    private String operator;

    /** 操作员工号 */
    private String operatorNo;


    /**
     * 操作类型
     */
    private OperateType operateType;

    /** 操作开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operateStartTime;

    /** 操作结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operateEndTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
