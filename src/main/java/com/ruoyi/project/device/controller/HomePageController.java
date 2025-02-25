package com.ruoyi.project.device.controller;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.common.cache.GlobalDeviceRealTimeCache;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.vo.DeviceOnlineStatusStatisticsVo;
import com.ruoyi.project.device.domain.vo.DeviceVo;
import com.ruoyi.project.device.enums.OnlineState;
import com.ruoyi.project.device.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
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
    private final IDeviceService deviceService;

    /**
     * 统计设备在线状态：总数、在线数、离线数
     * @return
     */
    // @RequiresPermissions("homePage:device:statisticsOnlineStatus")
    @PostMapping("/statisticsOnlineStatus")
    @ResponseBody
    public DeviceOnlineStatusStatisticsVo statisticsOnlineStatus()
    {
        DeviceOnlineStatusStatisticsVo vo = new DeviceOnlineStatusStatisticsVo();
        List<Device> devices = deviceService.selectList(new Device());
        if (!CollectionUtils.isEmpty(devices)) {
            int total = 0;
            int onlineCount = 0;
            for (Device device : devices) {
                if (device != null && Constants.NORMAL.equals(device.getStatus())) {
                    //启用的设备
                    total++;

                    DeviceVo cache = GlobalDeviceRealTimeCache.get(device.getSn());
                    if (cache != null) {
                        boolean online = deviceService.isOnline(cache.getLastHeatbeatTime());
                        if (online) {
                            onlineCount++;
                        }
                    }
                }
            }
            int offlineCount = total - onlineCount;
            vo.setTotal(total);
            vo.setOnlineCount(onlineCount);
            vo.setOfflineCount(offlineCount);
        }
        return vo;
    }

    /**
     * 分页查询实时数据列表
     * @return
     */
    // @RequiresPermissions("homePage:device:realTimeDataList")
    @PostMapping("/realTimeDataList")
    @ResponseBody
    public TableDataInfo realTimeDataList()
    {
        startPage();
        Device param = new Device();
        param.setStatus(Constants.NORMAL);
        List<DeviceVo> vos = deviceService.selectPageList(param);
        if (!CollectionUtils.isEmpty(vos)) {
            vos.stream().forEach(i -> {
                DeviceVo cache = GlobalDeviceRealTimeCache.get(i.getSn());
                if (cache != null) {
                    i.setVoltage(cache.getVoltage());
                    i.setCurrent(cache.getCurrent());
                    i.setOperator(cache.getOperator());
                    i.setOperatorNo(cache.getOperatorNo());
                    if (deviceService.isOnline(cache.getLastHeatbeatTime())) {
                        i.setOnlineState(OnlineState.ONLINE);
                    }
                }
                if (!OnlineState.ONLINE.equals(i.getOnlineState())) {
                    i.setOnlineState(OnlineState.OFFLINE);
                }
            });
        }
        return getDataTable(vos);
    }

}