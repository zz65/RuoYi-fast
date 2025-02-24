package com.ruoyi.project.device.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.device.domain.LogDeviceOperate;
import com.ruoyi.project.device.mapper.LogDeviceOperateMapper;
import com.ruoyi.project.device.service.ILogDeviceOperateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/24 10:43
 * @description: 详细说明
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LogDeviceOperateServiceImpl implements ILogDeviceOperateService {
    private final LogDeviceOperateMapper logDeviceOperateMapper;

    @Override
    public boolean insert(LogDeviceOperate logDeviceOperate) {
        if (logDeviceOperate == null) {
            return false;
        }
        int rows = logDeviceOperateMapper.insert(logDeviceOperate);
        return rows > 0;
    }

    @Override
    public boolean updateById(Long id, LogDeviceOperate logDeviceOperate) {
        if (id == null) {
            return false;
        }
        if (logDeviceOperate == null) {
            return false;
        }
        logDeviceOperate.setId(id);
        int rows = logDeviceOperateMapper.updateById(logDeviceOperate);
        return rows > 0;
    }

    @Override
    public LogDeviceOperate selectLastOperateByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        LogDeviceOperate logDeviceOperate = logDeviceOperateMapper.selectLastOperateByDeviceId(deviceId);
        return logDeviceOperate;
    }
}
