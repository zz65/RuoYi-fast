package com.ruoyi.project.device.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.ErrorConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.mapper.DeviceMapper;
import com.ruoyi.project.device.service.IDeviceService;
import com.ruoyi.project.system.role.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/18 8:42
 * @description: 详细说明
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceServiceImpl implements IDeviceService {
    private final DeviceMapper deviceMapper;

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean insert(Device device) {
        if (device == null) {
            return false;
        }
        device.setCreateBy(ShiroUtils.getSysUser().getLoginName());
        boolean flag = deviceMapper.insert(device) > 0;
        if (flag) {
            //编码字符串拼接
            StringBuffer codeStrBuffer = new StringBuffer(String.valueOf(device.getId()));
            //长度8位，id长度不够前面补0
            while (codeStrBuffer.length() < 8) {
                codeStrBuffer.insert(0, "0");
            }
            Device param = new Device();
            param.setCode(codeStrBuffer.toString());
            param.setId(device.getId());
            SpringUtils.getAopProxy(this).update(param);
        }
        return flag;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean deleteByIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            return false;
        }
        Long[] idArray = Convert.toLongArray(ids);
        boolean flag = deviceMapper.deleteByIds(idArray) > 0;
        return flag;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        boolean flag = deviceMapper.deleteById(id) > 0;
        return flag;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean update(Device param) {
        if (param == null || param.getId() == null) {
            return false;
        }
        param.setUpdateBy(ShiroUtils.getSysUser().getLoginName());
        boolean flag = deviceMapper.update(param) > 0;
        return flag;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean changeStatus(Long id, String status) {
        if (id == null || StringUtils.isBlank(status)) {
            return false;
        }
        Device param = new Device();
        param.setId(id);
        param.setStatus(status);
        param.setUpdateBy(ShiroUtils.getSysUser().getLoginName());
        boolean flag = deviceMapper.update(param) > 0;
        return flag;
    }

    @Override
    public List<Device> selectList(Device device) {
        if (device == null) {
            return null;
        }
        List<Device> list = deviceMapper.selectList(device);
        return list;
    }

    @Override
    public Device selectById(Long id) {
        if (id == null) {
            return null;
        }
        Device device = deviceMapper.selectById(id);
        return device;
    }

    @Override
    public boolean checkNameUnique(Device device) {
        if (device == null) {
            throw new ServiceException(ErrorConstants.PARAM_MISMATCH);
        }
        Device info = deviceMapper.checkNameUnique(device.getName());
        if (StringUtils.isNotNull(info) && !info.getId().equals(device.getId())) {
            return Constants.NOT_UNIQUE;
        }
        return Constants.UNIQUE;
    }

    @Override
    public boolean checkSnUnique(Device device) {
        if (device == null) {
            throw new ServiceException(ErrorConstants.PARAM_MISMATCH);
        }
        Device info = deviceMapper.checkSnUnique(device.getSn());
        if (StringUtils.isNotNull(info) && !info.getId().equals(device.getId())) {
            return Constants.NOT_UNIQUE;
        }
        return Constants.UNIQUE;
    }
}
