package com.ruoyi.project.device.service;

import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.vo.DeviceVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备信息 服务层
 *
 * @author zhuzhen
 * @date 2025/2/17 19:19
 * @description: 详细说明
 */
public interface IDeviceService {
    /**
     * 新增保存信息
     *
     * @param device
     * @return 结果
     */
    boolean insert(Device device);

    /**
     * 批量删除
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    boolean deleteByIds(String ids);

    /**
     * 根据id删除
     *
     * @param id 需要删除的数据ID
     * @return 结果
     */
    boolean deleteById(Long id);

    /**
     * 修改保存信息
     * 
     * @param  device
     * @return 结果
     */
    boolean update(Device device);

    /**
     * 修改状态
     *
     * @param  id
     * @param  status
     * @return 结果
     */
    boolean changeStatus(Long id, String status);

    /**
     * 查询集合
     *
     * @param  device
     * @return 集合
     */
    List<Device> selectList(Device device);

    /**
     *
     * @param device
     * @return
     */
    List<DeviceVo> selectPageList(Device device);

    /**
     * 通过ID查询
     *
     * @param id
     * @return 对象信息
     */
    Device selectById(Long id);

    /**
     * 校验名称
     * 
     * @param  device
     * @return 结果
     */
    boolean checkNameUnique(Device device);

    /**
     * 校验序列号
     *
     * @param  device
     * @return 结果
     */
    boolean checkSnUnique(Device device);

    /**
     * 根据设备的最后一次心跳时间判断设备是否在线
     * @param lastHeartbeatTime
     * @return
     */
    boolean isOnline(LocalDateTime lastHeartbeatTime);
}
