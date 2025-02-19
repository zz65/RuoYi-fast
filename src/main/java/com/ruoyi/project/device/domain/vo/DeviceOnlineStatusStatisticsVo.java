package com.ruoyi.project.device.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备在线状态数据统计
 *
 * @author zhuzhen
 * @date 2025/2/19 13:40
 * @description: 详细说明
 */
@Data
public class DeviceOnlineStatusStatisticsVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /* 总数 */
    private int total;

    /* 在线数 */
    private int onlineCount;

    /* 离线数 */
    private int offlineCount;
}
