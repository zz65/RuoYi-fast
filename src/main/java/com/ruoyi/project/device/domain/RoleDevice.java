package com.ruoyi.project.device.domain;

import lombok.Data;


/**
 * 角色与设备的关联关系
 *
 * @author zhuzhen
 * @date 2025/2/25 13:53
 * @description: 详细说明
 */
@Data
public class RoleDevice {
    private static final long serialVersionUID = 1L;

    private Long roleId;
    private Long deviceId;
}
