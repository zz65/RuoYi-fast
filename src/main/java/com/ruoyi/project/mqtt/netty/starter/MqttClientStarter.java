package com.ruoyi.project.mqtt.netty.starter;

import com.ruoyi.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.ruoyi.project.mqtt.netty.constant.KafkaTopicConstant;
import com.ruoyi.project.mqtt.netty.constant.MqttTopicConstant;
import com.ruoyi.project.mqtt.netty.properties.MqttConfig;
import com.ruoyi.project.mqtt.netty.utils.Log;
import com.ruoyi.project.mqtt.netty.utils.MqttClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @author zhuzhen
 * @date 2024/1/5 10:55
 * @description: 类描述
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class MqttClientStarter {
    @Resource
    private final MqttConfig options;

    private final KafkaTemplate kafkaTemplate;

    private MqttClient mqttClient = null;

    @Value("${spring.kafka.custom.topic.data-report}")
    private String kafkaDataReportTopic;


    @PostConstruct
    public void start() throws Exception {
        mqttClient = new MqttClient();
        //单位转化，毫秒转秒
        long actionTimeout = Long.valueOf(options.getActionTimeout()) * 1000;
        mqttClient.setActionTimeout(actionTimeout);
        //单位转化，毫秒转秒
        long connectionTimeout = Long.valueOf(options.getConnectionTimeout()) * 1000;
        mqttClient.setConnectTimeout(connectionTimeout);
        // 配置掉线重连
        mqttClient.setReconnectOnLost(1, 10000);
        mqttClient.setCallback(new MqttClient.Callback() {
            @Override
            public void onConnected() {
                try {
                    // // 订阅主题 topic 中可使用 /# ，表示模糊匹配该主题
                    // // 示例：订阅主题 topic1/# ，可接收 topic1、
                    // // topic1/aaa、topic1/bbb等主题下消息
                    // mqttClient.subscribe("topic1/#");
                    // // 发布一个消息到主题topic1/aaa
                    // mqttClient.publish("topic1/aaa", "hello, netty mqtt!-2-");
                    // // 取消订阅
                    // mqttClient.unsubscribe("topic1/#");
                    // 订阅主题
                    String[] topicArray = options.getTopic().split(",");
                    mqttClient.subscribe(1, topicArray);

                    // //test用 fixme
                    // mqttClient.subscribe(1, "topic111");
                    // mqttClient.publish("topic111", "hello, netty mqtt!");
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
                if (topic == null) {
                    return;
                }
                if (topic.startsWith(options.getTopic())) {
                    String origin = content;
                    String decodeStr = StringUtils.asciiToNative(origin);
                    if (log.isDebugEnabled()) {
                        if (origin.equals(decodeStr)) {
                            log.debug("mqtt receive topic:{}, origin message: {}", topic, origin);
                        } else {
                            log.debug("mqtt receive topic:{}, decoded message: {}", topic, decodeStr);
                        }
                    }

                    //写入kafka
                    // String sn = StringUtils.isNotBlank(topic) ? topic.replace(MqttTopicConstant.INHAND, "").replace("examine/", "") : "";
                    // String kafkaMessage = StringUtils.append(decodeStr, "###", sn);
                    String kafkaMessage = decodeStr;
                    kafkaTemplate.send(kafkaDataReportTopic, kafkaMessage)
                        .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                            @Override
                            public void onSuccess(SendResult<String, String> result) {
                                if (log.isDebugEnabled()) {
                                    RecordMetadata recordMetadata = result.getRecordMetadata();
                                    log.debug("sendKafkaMessage success：{}-{}-{}", recordMetadata.topic(),
                                            recordMetadata.partition(), recordMetadata.offset());
                                }
                            }
                            public void onFailure(Throwable ex) {
                                log.error("sendKafkaMessage error：", ex);
                            }
                        });
                }
            }
        });

        try {
            mqttClient.connect(options);
        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i("--->连接失败了：" + e);
            log.error("--->连接失败了", e);
        }
    }

    @PreDestroy
    public void destory() throws Exception{
        if (log.isInfoEnabled()) {
            log.info("mqttService destroy");
        }
        mqttClient.close();
    }
}
