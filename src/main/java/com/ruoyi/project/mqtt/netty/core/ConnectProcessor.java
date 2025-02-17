package com.ruoyi.project.mqtt.netty.core;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import lombok.extern.slf4j.Slf4j;
import com.ruoyi.project.mqtt.netty.properties.MqttConfig;
import com.ruoyi.project.mqtt.netty.utils.AsyncTask;
import com.ruoyi.project.mqtt.netty.utils.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhuzhen
 * @date 2023/11/29 15:34
 * @description: netty连接mqtt的处理类
 */
@Slf4j
public class ConnectProcessor extends AsyncTask<String> {

    /**
     * 等待连接确认结果的超时时间。单位：毫秒
     */
    private long timeout;
    /**
     * 是否收到连接确认成功的结果
     */
    private final AtomicBoolean receivedAck = new AtomicBoolean(false);
    /**
     * 连接中出现异常
     */
    private Exception e;

    /**
     * AsyncTask在执行execute方法后执行call方法
     * 获取异步任务的执行结果，如果收不到确认信号或者没有出异常，就一直等待；
     * @return
     * @throws Exception
     */
    @Override
    public String call() throws Exception {
        if (!isCancelled() && !receivedAck.get() && e == null) {
            synchronized (receivedAck) {
                receivedAck.wait(timeout);
            }
        }

        if (e != null) {
            throw e;
        }

        return receivedAck.get() ? ProcessorResult.RESULT_SUCCESS : ProcessorResult.RESULT_FAIL;
    }

    /**
     * netty客户端连接mqtt服务器
     * @param channel
     * @param options
     * @param timeout
     * @return
     * @throws Exception
     */
    public String connect(Channel channel, MqttConfig options, long timeout) throws Exception {
        this.timeout = timeout;
        //构造mqtt连接请求的消息体
        MqttConnectMessage msg = ProtocolUtils.connectMessage(options);
        Log.i("-->发起连接：" + msg);
        channel.writeAndFlush(msg);
        return execute().get(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 处理mqtt连接确认返回的消息
     * @param channel
     * @param msg
     */
    public void processAck(Channel channel, MqttConnAckMessage msg) {
        MqttConnAckVariableHeader mqttConnAckVariableHeader = msg.variableHeader();
        String errormsg = "";
        MqttConnectReturnCode mqttConnectReturnCode = mqttConnAckVariableHeader.connectReturnCode();
        switch (mqttConnectReturnCode) {
            case CONNECTION_ACCEPTED:
                synchronized (receivedAck) {
                    receivedAck.set(true);
                    receivedAck.notify();
                }
                return;
            case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:
                errormsg = "用户名密码错误";
                break;
            case CONNECTION_REFUSED_IDENTIFIER_REJECTED:
                errormsg = "clientId不允许链接";
                break;
            case CONNECTION_REFUSED_SERVER_UNAVAILABLE:
                errormsg = "服务不可用";
                break;
            case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:
                errormsg = "mqtt 版本不可用";
                break;
            case CONNECTION_REFUSED_NOT_AUTHORIZED:
                errormsg = "未授权登录";
                break;
            default:
                log.error("未知问题,connectReturnCode:{}", mqttConnectReturnCode);
                errormsg = "未知问题";
                break;
        }

        synchronized (receivedAck) {
            e = new IOException(errormsg);
            receivedAck.notify();
        }
    }
}
