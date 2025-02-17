package com.ruoyi.project.mqtt.netty.core;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnacceptableProtocolVersionException;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import com.ruoyi.project.mqtt.netty.properties.MqttConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhuzhen
 * @date 2023/11/29 15:37
 * @description: mqtt协议相关处理
 */
public class ProtocolUtils {
    /**
     * 构建mqtt连接消息实体
     * @param options
     * @return
     */
    public static MqttConnectMessage connectMessage(MqttConfig options) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(
                MqttMessageType.CONNECT,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                10);
        MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader(
                options.getMqttVersion().protocolName(),
                options.getMqttVersion().protocolLevel(),
                options.isHasUserName(),
                options.isHasPassword(),
                options.isWillRetain(),
                options.getWillQos(),
                options.isWillFlag(),
                options.isCleanSession(),
                options.getKeepAliveInterval());
        MqttConnectPayload payload = new MqttConnectPayload(
                options.getClientId(),
                options.getWillTopic(),
                options.getWillMessage(),
                options.getUsername(),
                options.getPassword() != null ? options.getPassword().getBytes() : null);
        return new MqttConnectMessage(fixedHeader, variableHeader, payload);
    }

    /**
     * 构建mqtt连接确认消息实体
     * @param returnCode
     * @param sessionPresent
     * @return
     */
    public static MqttConnAckMessage connAckMessage(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(
                MqttMessageType.CONNACK,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                0);
        MqttConnAckVariableHeader variableHeader = new MqttConnAckVariableHeader(
                returnCode,
                sessionPresent);
        return (MqttConnAckMessage) MqttMessageFactory.newMessage(
                fixedHeader,
                variableHeader,
                null);
    }

    /**
     * 根据异常映射对应的连接响应返回返回码
     * @param cause
     * @return
     */
    public static MqttConnectReturnCode connectReturnCodeForException(Throwable cause) {
        MqttConnectReturnCode code;
        if (cause instanceof MqttUnacceptableProtocolVersionException) {
            // 不支持的协议版本
            code = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
        } else if (cause instanceof MqttIdentifierRejectedException) {
            // 不合格的clientId
            code = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
        } else {
            code = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
        }
        return code;
    }

    /**
     * 构建mqtt断开连接的消息实体
     * @return
     */
    public static MqttMessage disConnectMessage() {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE,
                false, 0x02);
        return new MqttMessage(mqttFixedHeader);
    }

    /**
     * mqtt订阅主题的消息实体SubscriptionTopic转化成字符串类型的订阅主题
     * @param subscriptionTopics
     * @return
     */
    public static List<String> getTopics(SubscriptionTopic[] subscriptionTopics) {
        if (subscriptionTopics != null) {
            List<String> topics = new LinkedList<>();
            for (SubscriptionTopic sb : subscriptionTopics) {
                topics.add(sb.getTopic());
            }
            return topics;
        } else {
            return null;
        }
    }

    /**
     * 构建mqtt订阅主题的消息实体SubscriptionTopic
     * @param subscriptionTopics
     * @return
     */
    public static List<MqttTopicSubscription> getTopicSubscriptions(SubscriptionTopic[] subscriptionTopics) {
        if (subscriptionTopics != null && subscriptionTopics.length > 0) {
            List<MqttTopicSubscription> list = new LinkedList<>();
            for (SubscriptionTopic sm : subscriptionTopics) {
                list.add(new MqttTopicSubscription(sm.getTopic(), MqttQoS.valueOf(sm.getQos())));
            }
            return list;
        }
        return null;
    }

    /**
     * 构建mqtt订阅主题的消息实体MqttSubscribeMessage
     * @param messageId
     * @param topics
     * @return
     */
    public static MqttSubscribeMessage subscribeMessage(int messageId, String... topics) {
        return subscribeMessage(messageId, 0, topics);
    }

    public static MqttSubscribeMessage subscribeMessage(int messageId, int qos, String... topics) {
        List<SubscriptionTopic> list = new ArrayList<>();
        for (String topic : topics) {
            SubscriptionTopic sb = new SubscriptionTopic();
            sb.setQos(qos);
            sb.setTopic(topic);
            list.add(sb);
        }
        return subscribeMessage(messageId, list.toArray(new SubscriptionTopic[0]));
    }

    public static MqttSubscribeMessage subscribeMessage(int messageId, SubscriptionTopic... subscriptionTopics) {
        return subscribeMessage(messageId, getTopicSubscriptions(subscriptionTopics));
    }

    public static MqttSubscribeMessage subscribeMessage(int messageId, List<MqttTopicSubscription> mqttTopicSubscriptions) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE,
                false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(mqttTopicSubscriptions);
        return new MqttSubscribeMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);
    }

    /**
     * 构建mqtt订阅主题的确认消息的实体
     * @param messageId
     * @param mqttQoSList
     * @return
     */
    public static MqttSubAckMessage subAckMessage(int messageId, List<Integer> mqttQoSList) {
        return (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                new MqttSubAckPayload(mqttQoSList));
    }

    /**
     * 构建mqtt取消订阅主题的消息的实体
     * @param messageId
     * @param topicList
     * @return
     */
    public static MqttUnsubscribeMessage unsubscribeMessage(int messageId, List<String> topicList) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_MOST_ONCE,
                false, 0x02);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttUnsubscribePayload mqttUnsubscribeMessage = new MqttUnsubscribePayload(topicList);
        return new MqttUnsubscribeMessage(mqttFixedHeader, variableHeader, mqttUnsubscribeMessage);
    }

    /**
     * 构建mqtt取消订阅主题的确认消息的实体
     * @param messageId
     * @return
     */
    public static MqttUnsubAckMessage unsubAckMessage(int messageId) {
        return (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
    }

    /**
     * 构建mqtt的心跳请求消息的实体
     * @return
     */
    public static MqttMessage pingReqMessage() {
        return MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0),
                null,
                null);
    }

    /**
     * 构建mqtt的心跳响应消息的实体
     * @return
     */
    public static MqttMessage pingRespMessage() {
        return MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                null,
                null);
    }

    /**
     * 构建mqtt发布主题消息的实体
     * @param mqttMessage
     * @return
     */
    public static MqttPublishMessage publishMessage(MessageData mqttMessage) {
        return publishMessage(mqttMessage.getTopic(), mqttMessage.getPayload(), mqttMessage.getQos(),
                mqttMessage.isRetained(), mqttMessage.getMessageId(), mqttMessage.isDup());
    }

    public static MqttPublishMessage publishMessage(String topic, byte[] payload, int qosValue, int messageId, boolean isRetain) {
        return publishMessage(topic, payload, qosValue, isRetain, messageId, false);
    }

    public static MqttPublishMessage publishMessage(String topic, byte[] payload, int qosValue, boolean isRetain, int messageId, boolean isDup) {
        return (MqttPublishMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBLISH, isDup, MqttQoS.valueOf(qosValue), isRetain, 0),
                new MqttPublishVariableHeader(topic, messageId),
                Unpooled.buffer().writeBytes(payload));
    }

    /**
     * 构建mqtt的发布主题消息确认消息的实体
     * @param messageId
     * @return
     */
    public static MqttPubAckMessage pubAckMessage(int messageId) {
        return (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
    }

    /**
     * 构建mqtt的PUBREC消息的实体
     * QoS2消息回执（保证交付第一步）
     * @param messageId
     * @return
     */
    public static MqttMessage pubRecMessage(int messageId) {
        return MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
    }

    /**
     * 构建mqtt的PUBREL消息的实体
     * QoS2消息释放（保证交付第二步）
     * @param messageId
     * @return
     */
    public static MqttMessage pubRelMessage(int messageId, boolean isDup) {
        return MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, isDup, MqttQoS.AT_LEAST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
    }


    /**
     * 构建mqtt的PUBCOMP消息的实体
     * QoS2消息回执（保证交付第三步）
     * @param messageId
     * @return
     */
    public static MqttMessage pubCompMessage(int messageId) {
        return MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId),
                null);
    }
}