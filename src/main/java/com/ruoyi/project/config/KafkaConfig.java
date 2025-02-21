package com.ruoyi.project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka配置
 *
 * @author zhuzhen
 * @date 2024/10/23 8:34
 * @description: 主要用于监听kafka消息
 */
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.putAll(kafkaProperties.buildProducerProperties());
        props.putAll(kafkaProperties.buildConsumerProperties());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> defaultContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        // // 设置手动确认,处理完业务后，手动调用Acknowledgment.acknowledge()先将offset存放到map本地缓存，在下一次poll之前从缓存拿出来批量提交。最终也是批量提交。
        // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        // 设置手动确认，每次处理完业务，手动调用Acknowledgment.acknowledge()后立即提交
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
