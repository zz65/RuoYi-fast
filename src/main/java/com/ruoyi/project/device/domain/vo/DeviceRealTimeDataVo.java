package com.ruoyi.project.device.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备实时数据统计
 *
 * @author zhuzhen
 * @date 2025/2/19 13:59
 * @description: 详细说明
 */
@Data
public class DeviceRealTimeDataVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /* 总数 */
    private int total;

}
