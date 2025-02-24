package com.ruoyi.project.device.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.LogDeviceOperate;
import com.ruoyi.project.device.domain.vo.LogDeviceOperateVo;
import com.ruoyi.project.device.mapper.DeviceMapper;
import com.ruoyi.project.device.mapper.LogDeviceOperateMapper;
import com.ruoyi.project.device.service.ILogDeviceOperateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final DeviceMapper deviceMapper;

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

    @Override
    public List<LogDeviceOperateVo> selectPageList(LogDeviceOperate logDeviceOperate) {
        if (logDeviceOperate == null) {
            return null;
        }
        List<LogDeviceOperateVo> logDeviceOperateList = logDeviceOperateMapper.selectPageList(logDeviceOperate);
        if (!CollectionUtils.isEmpty(logDeviceOperateList)) {
            List<Long> deviceIds = logDeviceOperateList.stream().map(i -> i.getDeviceId()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(deviceIds)) {
                List<Device> devices = deviceMapper.selectByIds(deviceIds);
                if (!CollectionUtils.isEmpty(devices)) {
                    Map<Long, Device> deviceMap = new HashMap<>();
                    for (Device device : devices) {
                        if (device != null) {
                            deviceMap.put(device.getId(), device);
                        }
                    }
                    logDeviceOperateList.forEach(i->{
                        Device device = deviceMap.get(i.getDeviceId());
                        i.setDeviceName(device != null ? device.getName() : null);
                        i.setDeviceSn(device != null ? device.getSn() : null);
                    });
                }
            }

        }
        return logDeviceOperateList;
    }
}
