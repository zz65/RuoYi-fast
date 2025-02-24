package com.ruoyi.project.device.controller;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.device.domain.LogDeviceOperate;
import com.ruoyi.project.device.domain.vo.LogDeviceOperateVo;
import com.ruoyi.project.device.service.ILogDeviceOperateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 设备日志 控制器
 *
 * @author zhuzhen
 * @date 2025/2/24 14:05
 * @description: 详细说明
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/device/log")
public class LogDeviceOperateController extends BaseController
{
    private String prefix = "/device/log";

    private final ILogDeviceOperateService logDeviceOperateService;

    /**
     * 界面
     * @return
     */
    @RequiresPermissions("device:log:operateLog")
    @GetMapping()
    public String device()
    {
        return prefix + "/operateLog";
    }


    /**
     * 设备操作日志
     * @param logDeviceOperate
     * @return
     */
    @RequiresPermissions("device:log:operateLogList")
    @PostMapping("/operateLogList")
    @ResponseBody
    public TableDataInfo list(LogDeviceOperate logDeviceOperate)
    {
        startPage();
        List<LogDeviceOperateVo> list = logDeviceOperateService.selectPageList(logDeviceOperate);
        return getDataTable(list);
    }

}