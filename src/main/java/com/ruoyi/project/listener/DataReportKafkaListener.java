package com.ruoyi.project.listener;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.JsonUtils;
import com.ruoyi.common.utils.LocalDateUtils;
import com.ruoyi.project.common.cache.GlobalDeviceRealTimeCache;
import com.ruoyi.project.device.domain.Device;
import com.ruoyi.project.device.domain.vo.DeviceVo;
import com.ruoyi.project.device.enums.DataReportDataType;
import com.ruoyi.project.device.service.IDeviceService;
import com.ruoyi.project.mqtt.bean.DataReportMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类的概要说明
 *
 * @author zhuzhen
 * @date 2025/2/21 9:49
 * @description: 详细说明
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DataReportKafkaListener {
    @Value("${spring.kafka.custom.topic.data-report}")
    private String kafkaDataReportTopic;

    private final IDeviceService deviceService;

    /**
     * 处理事件：上报数据处理
     * @param consumerRecords
     * @param acknowledgment
     */
    @KafkaListener(
            topics = "${spring.kafka.custom.topic.data-report}"
            ,groupId = "${spring.kafka.consumer.group-id}"
            ,clientIdPrefix = "${spring.kafka.consumer.client-id}" + "_${server.port}" //每个使用@KafkaListener注解的消费者都会有一个独特的客户端ID，格式为<clientIdPrefix>-<thread-id>
            ,containerFactory = "defaultContainerFactory"
            // ,concurrency = "1"
            // , properties = {
            //     ConsumerConfig.MAX_POLL_RECORDS_CONFIG + "=1"// 用于设置每次拉取的最大记录数。
            // }
    )
    public void handleEvent(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment acknowledgment) {
        long startTimeMillis = System.currentTimeMillis();
        try {
            log.info("handleDataReportEvent");
            if (consumerRecords != null && !consumerRecords.isEmpty()) {
                Device param = new Device();
                param.setStatus(Constants.NORMAL);
                List<Device> devices = deviceService.selectList(param);
                //key=sn, value=device
                Map<String, Device> snToDeviceMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(devices)) {
                    for (Device device : devices) {
                        snToDeviceMap.put(device.getSn(), device);
                    }
                }
                for (ConsumerRecord consumerRecord : consumerRecords) {
                    //提取变量
                    String topic = consumerRecord.topic();
                    int partition = consumerRecord.partition();
                    long offset = consumerRecord.offset();
                    long timestamp = consumerRecord.timestamp();
                    Object key = consumerRecord.key();
                    Object value = consumerRecord.value();
                    if (log.isDebugEnabled()) {
                        log.debug("consumerRecord:{},{},{},{},{},{}", topic, partition, offset, timestamp, key, value);
                    }
                    DataReportMsg reportMsg = null;
                    try {
                        reportMsg = JsonUtils.parseObject(String.valueOf(value), DataReportMsg.class);
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("", e);
                        } else {
                            log.error("json format error,value:{}", value);
                        }
                    }
                    if (reportMsg == null) {
                        continue;
                    }
                    if (reportMsg.getDeviceId() == null) {
                        log.error("field deviceId missing,value:{}", value);
                        continue;
                    }
                    if (reportMsg.getData() == null) {
                        log.error("field data missing,value:{}", value);
                        continue;
                    }
                    if (reportMsg.getDataType() == null) {
                        log.error("field dataType missing,value:{}", value);
                        continue;
                    }

                    Device device = snToDeviceMap.get(reportMsg.getDeviceId());
                    if (device == null) {
                        log.error("device not found,sn:{}", reportMsg.getDeviceId());
                        continue;
                    }

                    try {
                        if (DataReportDataType.REALTIME_DATA.getDataType().equals(reportMsg.getDataType())) {
                            DataReportMsg.RealTimeData realTimeData = null;
                            try {
                                realTimeData = JsonUtils.parseObject(JsonUtils.toJsonString(reportMsg.getData()), DataReportMsg.RealTimeData.class);
                            } catch (Exception e) {
                                if (log.isDebugEnabled()) {
                                    log.debug("", e);
                                } else {
                                    log.error("realTimeData json format error,value:{}", value);
                                }
                            }
                            if (realTimeData == null) {
                                continue;
                            }
                            Long dataTimestamp = realTimeData.getTimestamp();
                            if (dataTimestamp == null) {
                                log.error("realTimeData field timestamp missing,value:{}", value);
                                continue;
                            }
                            DeviceVo cache = deviceToCache(device, dataTimestamp);
                            // LocalDateTime localDateTime = cache.getLastHeatbeatTime();
                            //
                            // cache.setLastHeatbeatTime(localDateTime);
                            cache.setVoltage(realTimeData.getVoltage());
                            cache.setCurrent(realTimeData.getCurrent());

                            GlobalDeviceRealTimeCache.put(reportMsg.getDeviceId(), cache);
                        } else if (DataReportDataType.TURN_ON_OR_OFF_DATA.getDataType().equals(reportMsg.getDataType())) {
                            DataReportMsg.TurnOnOrOffData turnOnOrOffData = null;
                            try {
                                turnOnOrOffData = JsonUtils.parseObject(JsonUtils.toJsonString(reportMsg.getData()), DataReportMsg.TurnOnOrOffData.class);
                            } catch (Exception e) {
                                if (log.isDebugEnabled()) {
                                    log.debug("", e);
                                } else {
                                    log.error("turnOnOrOffData json format error,value:{}", value);
                                }
                            }
                            if (turnOnOrOffData == null) {
                                continue;
                            }
                            Long dataTimestamp = turnOnOrOffData.getTimestamp();
                            if (dataTimestamp == null) {
                                log.error("turnOnOrOffData field timestamp missing,value:{}", value);
                                continue;
                            }

                            DeviceVo cache = deviceToCache(device, dataTimestamp);
                            LocalDateTime localDateTime = cache.getLastHeatbeatTime();

                            // cache.setLastHeatbeatTime(localDateTime);
                            cache.setOperator(turnOnOrOffData.getOperator());
                            cache.setOperatorNo(turnOnOrOffData.getOperatorNo());
                            if (Boolean.TRUE.equals(turnOnOrOffData.getIsOn())) {
                                cache.setTurnOnTime(localDateTime);
                                if (cache.getTurnOffTime() != null && cache.getTurnOffTime().isAfter(cache.getTurnOffTime())) {
                                    cache.setTurnOffTime(null);
                                }
                            } else if (Boolean.FALSE.equals(turnOnOrOffData.getIsOn())) {
                                cache.setTurnOffTime(localDateTime);
                            }

                            GlobalDeviceRealTimeCache.put(reportMsg.getDeviceId(), cache);

                            //todo 记录操作日志
                        }
                    } catch (Exception e) {
                        log.error("consume error", e);
                    }
                }
            }
        } catch (Throwable e) {
            // 以后可以把这些错误记录到日志中去,方便自动补偿或手动补偿
            log.warn("handleDataReportEvent error", e);
        } finally {
            try {
                //偏移量 消费之后调用避免重复消费
                acknowledgment.acknowledge();
            } catch (Exception e) {
                log.warn("handleDataReportEvent acknowledge error", e);
            }
            long cost = System.currentTimeMillis() - startTimeMillis;
            log.warn("handleDataReportEvent cost:{} ms", cost);
        }
    }


    public DeviceVo deviceToCache(Device device, Long dataTimestamp) {
        if (device == null) {
            return null;
        }
        DeviceVo cache = new DeviceVo();
        cache.setId(device.getId());
        cache.setName(device.getName());
        cache.setCode(device.getCode());
        cache.setSn(device.getSn());
        LocalDateTime localDateTime = LocalDateUtils.longToDateTime(dataTimestamp * 1000);
        cache.setLastHeatbeatTime(localDateTime);

        DeviceVo oldCache = GlobalDeviceRealTimeCache.get(device.getSn());
        if (oldCache != null) {
            cache.setVoltage(oldCache.getVoltage());
            cache.setCurrent(oldCache.getCurrent());

            cache.setTurnOnTime(oldCache.getTurnOnTime());
            cache.setTurnOffTime(oldCache.getTurnOffTime());
            cache.setOperator(oldCache.getOperator());
            cache.setOperatorNo(oldCache.getOperatorNo());
        }
        return cache;
    }
}
