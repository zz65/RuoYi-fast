package com.ruoyi.project.mqtt.netty.properties;

import com.ruoyi.common.utils.StringUtils;
import io.netty.handler.codec.mqtt.MqttVersion;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhuzhen
 * @date 2023/11/29 15:00
 * @description: mqtt连接参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "mqtt.thread-pool")
public class MqttThreadPoolConfig {
    private int corePoolSize = 2;
    private int maxPoolSize = 2;
    private int queueCapacity = 10000;
    private int keepAliveSeconds = 60;
    private String threadNamePrefix = "mqtt-thread-";
}
