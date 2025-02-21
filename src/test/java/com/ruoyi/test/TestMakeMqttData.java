package com.ruoyi.test;

import com.alibaba.druid.support.json.JSONUtils;
import com.ruoyi.project.mqtt.bean.DataReportMsg;
import com.ruoyi.project.mqtt.netty.properties.MqttConfig;
import com.ruoyi.project.mqtt.netty.utils.Log;
import com.ruoyi.project.mqtt.netty.utils.MqttClient;
import com.ruoyi.test.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 测试，造mqtt数据
 *
 * @author zhuzhen
 * @date 2025/2/21 10:17
 * @description: 详细说明
 */
@Slf4j
public class TestMakeMqttData {
    public static void main(String[] args) {
        String topicPublish = "data-report";
        String topicSubscribe = topicPublish;

        MqttConfig options = new MqttConfig();
        options.setHost("broker-cn.emqx.io");
        options.setPort(1883);
        options.setUsername(null);
        options.setPassword(null);
        options.setTopic(topicSubscribe);
        options.setClientId("admin-local");

        Log.enablePing(true);
        MqttClient mqttClient = new MqttClient();
        //单位转化，毫秒转秒
        long actionTimeout = Long.valueOf(options.getActionTimeout()) * 1000;
        mqttClient.setActionTimeout(actionTimeout);
        //单位转化，毫秒转秒
        long connectionTimeout = Long.valueOf(options.getConnectionTimeout()) * 1000;
        mqttClient.setConnectTimeout(connectionTimeout);
        // 配置掉线重连
        mqttClient.setReconnectOnLost(5, 10000);


        int globalYear = 2025;
        int globalMonth = 2;
        int globalDay = 21;
        int globalHour = 0;
        int globalMinute = 1;
        int globalEndHour = 0;
        int globalEndMinute = 1;
        int[] globalArr = new int[] {globalYear, globalMonth, globalDay, globalHour, globalMinute};
        //每个设备x分钟做一条数据,x必须是60的约数
        int interval = 1;

        mqttClient.setCallback(new MqttClient.Callback() {
            @Override
            public void onConnected() {
                try {
                    // 发布一个消息到主题
                    // mqttClient.subscribe(topicSubscribe);
                    int paramYear = globalArr[0];
                    int paramMonth = globalArr[1];
                    int paramDay = globalArr[2];
                    int paramHour = globalArr[3];
                    int paramMinute = globalArr[4];

                    LocalDateTime dateTime = LocalDateTime.of(paramYear, paramMonth, paramDay, paramHour, paramMinute, 0);
                    int hour;
                    int minute;
                    int day;
                    while ((day = dateTime.getDayOfMonth()) == paramDay && (hour = dateTime.getHour()) <= globalEndHour && (minute = dateTime.getMinute()) <= globalEndMinute) {
                        log.info("dateTime:{}", dateTime);
                        for (int i = 0; i < 1; i++) {
                            DataReportMsg dataReportMsg = new DataReportMsg();
                            dataReportMsg.setDeviceId("device-" + i);
                            dataReportMsg.setTimestamp(System.currentTimeMillis() / 1000);
                            dataReportMsg.setDataType(1);
                            DataReportMsg.ReportData reportData = new DataReportMsg.ReportData();
                            reportData.setTimestamp(System.currentTimeMillis() / 1000);
                            reportData.setCurrent(100.0);
                            reportData.setVoltage(60.0);
                            dataReportMsg.setData(reportData);

                            String topic = topicPublish;
                            String jsonString = JsonUtils.toJsonString(dataReportMsg);
                            mqttClient.publish(topic, jsonString);
                        }

                        if (hour == 23 && minute == 58 && dateTime.getSecond() == 59) {
                            //进入下一天
                            dateTime = dateTime.plusMinutes(1).plusSeconds(1);
                        } else if (hour == 23 && minute >= 60 - interval) {
                            //为了保证进入下一个循环，最后一个23:58:59能入库
                            dateTime = LocalDateTime.of(paramYear, paramMonth, paramDay, 23, 58, 59);
                        } else {
                            dateTime = dateTime.plusMinutes(interval);
                        }

                        globalArr[0] = dateTime.getYear();
                        globalArr[1] = dateTime.getMonthValue();
                        globalArr[2] = dateTime.getDayOfMonth();
                        globalArr[3] = dateTime.getHour();
                        globalArr[4] = dateTime.getMinute();
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            @Override
            public void onConnectFailed(Throwable e) {

            }

            @Override
            public void onConnectLost(Throwable e) {
                Log.i("-->onConnectLost : " + e);
            }

            @Override
            public void onReconnectStart(int cur) {

            }

            @Override
            public void onMessageArrived(String topic, String content) {
                if (log.isDebugEnabled()) {
                    log.debug("-->收到消息：{} # {} ", topic, content);
                }
            }
        });

        try {
            mqttClient.connect(options);
        } catch (Exception e) {
            log.error("--->连接失败了", e);
        }

        //
        for (;;) {
            int i=0;
        }
    }
}
