package com.ruoyi.project.common.cache;

import com.ruoyi.project.device.domain.vo.DeviceVo;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备实时数据缓存
 *
 * @author zhuzhen
 * @date 2025/2/18 13:54
 * @description: 详细说明
 */
public final class GlobalDeviceRealTimeCache {
    /**
     * 设备实时数据缓存
     * key=sn,value=设备实时数据
     */
    private static final Map<String, DeviceVo> realTimeMap = new ConcurrentHashMap();

    /**
     * 添加设备实时数据到缓存
     * @param sn
     * @param deviceVo
     */
    public static void put(String sn, DeviceVo deviceVo) {
        realTimeMap.put(sn, deviceVo);
    }

    /**
     * 根据设备sn获取设备实时数据缓存
     * @param sn
     * @return
     */
    public static DeviceVo get(String sn) {
        DeviceVo deviceVo = realTimeMap.get(sn);
        // // fixme 测试代码，正式请删除
        // if (deviceVo == null) {
        //     deviceVo = new DeviceVo();
        //     deviceVo.setLastHeatbeatTime(LocalDateTime.now());
        //     deviceVo.setVoltage(60.0);
        //     deviceVo.setCurrent(100.0);
        //     deviceVo.setTurnOnTime(LocalDateTime.now());
        //     deviceVo.setTurnOffTime(LocalDateTime.now());
        //     deviceVo.setOperator("李白");
        //     deviceVo.setOperatorNo("000023");
        // }
        return deviceVo;
    }


}
