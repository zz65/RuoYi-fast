package com.ruoyi.project.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 处理事件：上报数据处理
     * @param consumerRecords
     * @param acknowledgment
     */
    @KafkaListener(
            // topics = "${spring.kafka.custom.topic.data-report}"
            topics = "data-report"
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
}
