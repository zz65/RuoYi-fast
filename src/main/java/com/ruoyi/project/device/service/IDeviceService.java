package com.ruoyi.project.device.service;

import com.ruoyi.project.device.domain.Device;

import java.util.List;

/**
 * 设备信息 服务层
 *
 * @author zhuzhen
 * @date 2025/2/17 19:19
 * @description: 详细说明
 */
public interface IDeviceService
{
    /**
     * 查询集合
     * 
     * @param  device
     * @return 集合
     */
    List<Device> selectList(Device device);

    /**
     * 通过ID查询
     * 
     * @param id
     * @return 对象信息
     */
    Device selectById(Long id);

    /**
     * 批量删除
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteByIds(String ids);

    /**
     * 新增保存信息
     * 
     * @param device
     * @return 结果
     */
    int insert(Device device);

    /**
     * 修改保存信息
     * 
     * @param  device
     * @return 结果
     */
    int update(Device device);


    /**
     * 校验名称
     * 
     * @param  name
     * @return 结果
     */
    boolean checkNameUnique(String name);
}
