package com.ruoyi.project.device.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.ErrorConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.aspectj.lang.annotation.DataScopeDevice;
import com.ruoyi.framework.web.domain.Ztree;
import com.ruoyi.project.common.cache.GlobalDeviceRealTimeCache;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.vo.DeviceVo;
import com.ruoyi.project.device.enums.OnlineState;
import com.ruoyi.project.device.mapper.DeviceMapper;
import com.ruoyi.project.device.service.IDeviceService;
import com.ruoyi.project.system.dept.domain.Dept;
import com.ruoyi.project.system.role.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Device> selectListWithoutDataScope(Device device) {
        return selectList(device);
    }

    @Override
    @DataScopeDevice(deviceAlias = "d", deviceIdColumnName="id")
    public List<Device> selectList(Device device) {
        if (device == null) {
            return null;
        }
        List<Device> list = deviceMapper.selectList(device);
        return list;
    }

    @Override
    @DataScopeDevice(deviceAlias = "d", deviceIdColumnName="id")
    public List<DeviceVo> selectPageList(Device device) {
        if (device == null) {
            return null;
        }
        List<DeviceVo> list = deviceMapper.selectPageList(device);
        if (!CollectionUtils.isEmpty(list)) {
             list.stream().forEach(i -> {
                 i.setName(i.getName());
                 i.setCode(i.getCode());
                 i.setSn(i.getSn());
                 i.setFirstOnlineTime(i.getFirstOnlineTime());
                 i.setStatus(i.getStatus());
                 i.setCreateTime(i.getCreateTime());
                 i.setCreateBy(i.getCreateBy());
                 i.setUpdateTime(i.getUpdateTime());
                 i.setUpdateBy(i.getUpdateBy());

                DeviceVo cache = GlobalDeviceRealTimeCache.get(i.getSn());
                //实体类之外的扩展属性
                if (cache != null) {
                    if (isOnline(cache.getLastHeatbeatTime())) {
                        //最后一次心跳在10min以内就是在线
                        i.setOnlineState(OnlineState.ONLINE);
                    }

                    i.setVoltage(cache.getVoltage());
                    i.setCurrent(cache.getCurrent());
                    i.setTurnOnTime(cache.getTurnOnTime());
                    i.setTurnOffTime(cache.getTurnOffTime());
                    i.setOperator(cache.getOperator());
                    i.setOperatorNo(cache.getOperatorNo());
                }
                if (!OnlineState.ONLINE.equals(i.getOnlineState())) {
                    i.setOnlineState(OnlineState.OFFLINE);
                }
            });
        }
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

    @Override
    public boolean isOnline(LocalDateTime lastHeartbeatTime) {
        if (lastHeartbeatTime == null) {
            return false;
        }
        if (lastHeartbeatTime.isAfter(LocalDateTime.now().plusMinutes(-10))) {
            return true;
        }
        return false;
    }

    /**
     * 根据角色ID查询部门（数据权限）
     *
     * @param role 角色对象
     * @return 部门列表（数据权限）
     */
    @Override
    public List<Ztree> roleDeviceTreeData(Role role)
    {
        Long roleId = role.getRoleId();
        List<Ztree> ztrees = new ArrayList<Ztree>();
        List<Device> deviceList = SpringUtils.getAopProxy(this).selectList(new Device());
        if (StringUtils.isNotNull(roleId))
        {
            List<String> roleDeviceList = deviceMapper.selectRoleDeviceTree(roleId);
            ztrees = initZtree(deviceList, roleDeviceList);
        }
        else
        {
            ztrees = initZtree(deviceList, null);
        }
        return ztrees;
    }


    /**
     * 对象转设备树
     *
     * @param deviceList 设备列表
     * @param roleDeviceList 角色已存在菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<Device> deviceList, List<String> roleDeviceList)
    {
        List<Ztree> ztrees = new ArrayList<Ztree>();
        boolean isCheck = StringUtils.isNotNull(roleDeviceList);
        for (Device device : deviceList)
        {
            if (Constants.NORMAL.equals(device.getStatus()))
            {
                Ztree ztree = new Ztree();
                ztree.setId(device.getId());
                // ztree.setpId(device.getParentId());
                ztree.setpId(0L);
                ztree.setName(device.getName());
                ztree.setTitle(device.getName());
                if (isCheck)
                {
                    ztree.setChecked(roleDeviceList.contains(device.getId() + device.getName()));
                }
                ztrees.add(ztree);
            }
        }
        return ztrees;
    }
}
