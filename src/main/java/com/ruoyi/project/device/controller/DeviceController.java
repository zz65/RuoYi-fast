package com.ruoyi.project.device.controller;

import com.ruoyi.common.constant.ErrorConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.common.cache.GlobalDeviceRealTimeCache;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.vo.DeviceVo;
import com.ruoyi.project.device.enums.OnlineState;
import com.ruoyi.project.device.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/17 15:25
 * @description: 详细说明
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/device/list")
public class DeviceController extends BaseController
{
    private String prefix = "device/list";

    private final IDeviceService deviceService;

    /**
     * 界面
     * @return
     */
    @RequiresPermissions("device:list:view")
    @GetMapping()
    public String device()
    {
        return prefix + "/device";
    }

    /**
     * 新增设备
     */
    @RequiresPermissions("device:list:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存设备
     */
    @RequiresPermissions("device:list:add")
    @Log(title = "设备管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated Device device)
    {
        if (!deviceService.checkNameUnique(device))
        {
            return error("新增设备'" + device.getName() + "'失败，设备名称已存在");
        } else if (!deviceService.checkSnUnique(device)) {
            return error("新增设备'" + device.getSn() + "'失败，设备序列号已存在");
        }
        return toAjax(deviceService.insert(device));

    }

    @RequiresPermissions("device:list:remove")
    @Log(title = "设备管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(deviceService.deleteByIds(ids));
    }

    /**
     * 修改设备
     */
    @RequiresPermissions("device:list:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        //todo 数据权限
        // deviceService.checkRoleDataScope(id);
        mmap.put("device", deviceService.selectById(id));
        return prefix + "/edit";
    }

    /**
     * 修改保存设备
     */
    @RequiresPermissions("device:list:edit")
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated Device device)
    {
        //todo 数据权限
        // roleService.checkRoleAllowed(device);
        // roleService.checkRoleDataScope(device.getRoleId());
        if (!deviceService.checkNameUnique(device))
        {
            return error("修改设备'" + device.getName() + "'失败，设备名称已存在");
        } else if (!deviceService.checkSnUnique(device)) {
            return error("修改设备'" + device.getSn() + "'失败，设备序列号已存在");
        }
        return toAjax(deviceService.update(device));
    }

    /**
     * 校验设备名称
     */
    @PostMapping("/checkNameUnique")
    @ResponseBody
    public boolean checkNameUnique(Device device)
    {
        return deviceService.checkNameUnique(device);
    }

    /**
     * 校验设备序列号
     */
    @PostMapping("/checkSnUnique")
    @ResponseBody
    public boolean checkSnUnique(Device device)
    {
        return deviceService.checkSnUnique(device);
    }

    /**
     * 启用状态修改
     */
    @Log(title = "设备管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("device:list:edit")
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(Device device)
    {
        //todo 数据权限
        // roleService.checkRoleAllowed(device);
        // roleService.checkRoleDataScope(device.getRoleId());
        if (device == null) {
            throw new ServiceException(ErrorConstants.PARAM_MISMATCH);
        }
        return toAjax(deviceService.changeStatus(device.getId(), device.getStatus()));
    }


    @RequiresPermissions("device:list:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Device device)
    {
        startPage();
        List<DeviceVo> list = deviceService.selectPageList(device);
        return getDataTable(list);
    }

    // @Log(title = "设备管理", businessType = BusinessType.EXPORT)
    // @RequiresPermissions("device:list:export")
    // @PostMapping("/export")
    // @ResponseBody
    // public AjaxResult export(Device device)
    // {
    //     List<Device> list = deviceService.selectList(device);
    //     ExcelUtil<Device> util = new ExcelUtil<Device>(Device.class);
    //     return util.exportExcel(list, "设备数据");
    // }
}