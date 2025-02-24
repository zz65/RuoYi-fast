package com.ruoyi.project.device.service;

import com.ruoyi.project.device.domain.LogDeviceOperate;
import com.ruoyi.project.device.domain.vo.LogDeviceOperateVo;

import java.util.List;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/24 10:42
 * @description: 详细说明
 */
public interface ILogDeviceOperateService {
    boolean insert(LogDeviceOperate logDeviceOperate);

    boolean updateById(Long id, LogDeviceOperate logDeviceOperate);

    LogDeviceOperate selectLastOperateByDeviceId(Long deviceId);

    List<LogDeviceOperateVo> selectPageList(LogDeviceOperate logDeviceOperate);
}
