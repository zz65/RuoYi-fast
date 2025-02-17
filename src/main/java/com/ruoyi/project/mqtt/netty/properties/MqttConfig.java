package com.ruoyi.project.mqtt.netty.properties;

import com.ruoyi.common.utils.StringUtils;
import io.netty.handler.codec.mqtt.MqttVersion;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhuzhen
 * @date 2023/11/29 15:00
 * @description: mqtt连接参数
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {
    private String host;
    private int port;

    /**
     * #固定报文头
     * MQTT消息体结构参考类 MqttConnectMessage
     * MQTT固定报文头最少有两个字节
     * 第一字节包含消息类型（Message Type）和QoS级别等标志位。
     * 第二字节开始是剩余长度字段，该长度是后面的可变报文头加消息负载的总长度，该字段最多允许四个字节。
     *
     * QoS有三个等级，QoS 0，QoS 1和 QoS 2。
     * QoS 0：最多分发一次。消息的传递完全依赖底层的TCP/IP网络，协议里没有定义应答和重试，消息要么只会到达服务端一次，要么根本没有到达。
     * QoS 1：至少分发一次。服务器的消息接收由PUBACK消息进行确认，如果通信链路或发送设备异常，或者指定时间内没有收到确认消息，
     * 发送端会重发这条在消息头中设置了DUP位的消息。
     * QoS 2：只分发一次。这是最高级别的消息传递，消息丢失和重复都是不可接受的，使用这个服务质量等级会有额外的开销。
     */

    /**
     * #可变报文头部分
     */
    /**
     * mqtt协议版本
     */
    private MqttVersion mqttVersion = MqttVersion.MQTT_3_1_1;


    /**
     * #有效载荷
     * Payload直译为载荷，可能让人摸不着头脑，实际上可以理解为消息主体（body）。
     * 当MQTT发送的消息类型是CONNECT（连接）、PUBLISH（发布）、SUBSCRIBE（订阅）、SUBACK（订阅确认）、UNSUBSCRIBE（取消订阅）时，则会带有负荷。
     */
    /**
     * 用户名
     */
    private String username = "";
    /**
     * 密码
     */
    private String password;

    /**
     * #遗愿标志
     * 三个Will标志位：Will Flag、Will QoS和Will Retain Flag，这些Will字段用于监控客户端与服务器之间的连接状况。
     * 如果设置了Will Flag，就必须设置Will QoS和Will Retain标志位，消息主体中也必须有Will Topic和Will Message字段。
     *
     * 那遗愿消息是怎么回事呢？
     * 服务器与客户端通信时，当遇到异常或客户端心跳超时的情况，MQTT服务器会替客户端发布一个Will消息。
     * 当然如果服务器收到来自客户端的DISCONNECT消息，则不会触发Will消息的发送。
     * 因此，Will字段可以应用于设备掉线后需要通知用户的场景。
     */
    private boolean isWillFlag = false;
    private boolean isWillRetain = false;
    private int willQos = 0;

    /**
     * 维持连接时间，单位：秒
     *
     * MQTT客户端可以设置一个心跳间隔时间（Keep Alive Timer），表示在每个心跳间隔时间内发送一条消息。
     * 如果在这个时间周期内，没有业务数据相关的消息，客户端会发一个PINGREQ消息，相应的，服务器会返回一个PINGRESP消息进行确认。
     * 如果服务器在一个半（1.5）心跳间隔时间周期内没有收到来自客户端的消息，就会断开与客户端的连接。
     * 心跳间隔时间最大值大约可以设置为18个小时，0值意味着客户端不断开。
     */
    private int keepAliveInterval = 60;

    /**
     * 设置订阅、取消订阅、发布消息、ping等动作的超时时间
     * 单位：秒
     */
    private int connectionTimeout = 10;

    /**
     * 连接的超时时间
     * 单位：秒
     */
    private int actionTimeout = 10;

    /**
     * 客户端标识符
     */
    private String clientId = "";
    /**
     * 遗嘱主题
     */
    private String willTopic = "";
    /**
     * 遗嘱消息
     */
    private byte[] willMessage;

    /**
     * 如果清理会话（CleanSession）标志被设置为true，
     * 客户端和服务端在重连后，会丢弃之前的任何会话相关内容及配置
     *
     */
    private boolean cleanSession = false;

    /**
     * 订阅主题
     */
    private String topic = "inhand/#";

    public boolean isHasUserName() {
        return !StringUtils.isEmpty(username);
    }

    public boolean isHasPassword() {
        return !StringUtils.isEmpty(password);
    }
}
