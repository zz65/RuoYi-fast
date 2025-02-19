package com.ruoyi.project.device.controller;

import com.ruoyi.common.constant.ErrorConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.vo.DeviceVo;
import com.ruoyi.project.device.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 首页 控制器
 *
 * @author zhuzhen
 * @date 2025/2/19 10:54
 * @description: 详细说明
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/homePage")
public class HomePageController extends BaseController
{
    /**
     * 统计设备在线状态：总数、在线数、离线数
     * @return
     */
    @RequiresPermissions("homePage:device:statisticsOnlineStatus")
    @PostMapping("/statisticsOnlineStatus")
    @ResponseBody
    public TableDataInfo statisticsOnlineStatus()
    {
        return getDataTable(null);
    }

    /**
     * 分页查询实时数据列表
     * @return
     */
    @RequiresPermissions("homePage:device:realTimeDataList")
    @PostMapping("/realTimeDataList")
    @ResponseBody
    public TableDataInfo realTimeDataList()
    {
        startPage();
        return getDataTable(null);
    }

}