package com.ruoyi.project.device.mapper;

import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.vo.DeviceVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备表 数据层
 *
 * @author zhuzhen
 * @date 2025/2/17 17:25
 * @description: 详细说明
 */
public interface DeviceMapper
{

    /**
     * 新增
     *
     * @param device
     * @return 结果
     */
    int insert(Device device);

    /**
     * 通过ID删除
     * 
     * @param id
     * @return 结果
     */
    int deleteById(Long id);

    /**
     * 批量删除
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteByIds(Long[] ids);

    /**
     * 修改
     * 
     * @param device
     * @return 结果
     */
    int update(Device device);

    /**
     * 校验设备名称是否唯一
     *
     * @param name 设备名称
     * @return 结果
     */
    Device checkNameUnique(String name);

    /**
     * 校验设备序列号是否唯一
     *
     * @param sn 设备序列号
     * @return 结果
     */
    Device checkSnUnique(String sn);

    /**
     * 根据条件查询列表
     *
     * @param device
     * @return 集合信息
     */
    List<Device> selectList(Device device);

    /**
     * 根据条件分页查询列表
     *
     * @param device
     * @return 集合信息
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
     * 通过id集合查询设备集合
     * @param ids
     * @return
     */
    List<Device> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 根据角色ID查询设备
     *
     * @param roleId 角色ID
     * @return 设备列表
     */
    List<String> selectRoleDeviceTree(Long roleId);

}
