package com.ruoyi.project.device.mapper;

import com.ruoyi.project.device.domain.RoleDevice;

import java.util.List;

/**
 * 角色与设备关联表 数据层
 *
 * @author zhuzhen
 * @date 2025/2/25 14:18
 * @description: 详细说明
 */
public interface RoleDeviceMapper
{
    /**
     * 通过角色ID删除角色和设备关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleDeviceByRoleId(Long roleId);

    /**
     * 批量删除角色设备关联信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteRoleDevice(Long[] ids);

    /**
     * 查询设备使用数量
     * 
     * @param deviceId 设备ID
     * @return 结果
     */
    public int selectCountRoleDeviceByDeviceId(Long deviceId);

    /**
     * 批量新增角色设备信息
     * 
     * @param roleDeviceList 角色设备列表
     * @return 结果
     */
    public int batchRoleDevice(List<RoleDevice> roleDeviceList);
}
