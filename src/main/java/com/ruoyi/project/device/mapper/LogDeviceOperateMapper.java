package com.ruoyi.project.device.mapper;

import com.ruoyi.project.device.domain.LogDeviceOperate;
import com.ruoyi.project.device.domain.vo.LogDeviceOperateVo;

import java.util.List;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/24 9:10
 * @description: 详细说明
 */
public interface LogDeviceOperateMapper {
    int insert(LogDeviceOperate logDeviceOperate);

    int updateById(LogDeviceOperate logDeviceOperate);

    int selectById(Long id);

    LogDeviceOperate selectLastOperateByDeviceId(Long deviceId);

    List<LogDeviceOperateVo> selectPageList(LogDeviceOperate logDeviceOperate);


}
