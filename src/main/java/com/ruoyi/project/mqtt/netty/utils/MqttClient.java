package com.ruoyi.project.mqtt.netty.utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import lombok.extern.slf4j.Slf4j;
import com.ruoyi.project.mqtt.netty.core.ConnectProcessor;
import com.ruoyi.project.mqtt.netty.core.PingProcessor;
import com.ruoyi.project.mqtt.netty.core.ProcessorResult;
import com.ruoyi.project.mqtt.netty.core.ProtocolUtils;
import com.ruoyi.project.mqtt.netty.core.PublishProcessor;
import com.ruoyi.project.mqtt.netty.core.SubscribeProcessor;
import com.ruoyi.project.mqtt.netty.core.UnsubscribeProcessor;
import com.ruoyi.project.mqtt.netty.properties.MqttConfig;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuzhen
 * @date 2023/11/29 15:20
 * @description: mqtt客户端工具类
 */
@Slf4j
public class MqttClient {
    /**
     * mqtt连接参数
     */
    private MqttConfig connectOptions;

    /**
     * 设置连接、订阅、取消订阅、发布消息、ping等动作的超时时间
     * 单位：毫秒
     */
    private long actionTimeout = 5000;
    private long connectTimeout = 5000;

    /**
     * 最大重连次数
     */
    private int maxReconnectTimesOnLost = 0;
    /**
     * 重连超时时间。单位:毫秒
     */
    private long reconnectTimeoutOnLost = 0;
    /**
     * 最短重连间隔
     */
    private final static long MIN_RECONNECT_INTERVAL = 1800L;

    /**
     * 建立netty连接的异步任务
     */
    private AsyncTask<String> connectTask;
    /**
     * 重新建立netty连接的异步任务
     */
    private AsyncTask<String> reconnectTask;
    /**
     * netty管道
     */
    private Channel channel;

    /**
     * 连接mqtt服务器的处理类
     */
    private ConnectProcessor connectProcessor;
    /**
     * ping mqtt服务器的处理类
     */
    private PingProcessor pingProcessor;
    /**
     * 订阅mqtt主题的处理类
     */
    private List<SubscribeProcessor> subscribeProcessorList = new ArrayList<>();
    /**
     * 取消订阅mqtt主题的处理类
     */
    private List<UnsubscribeProcessor> unsubscribeProcessorList = new ArrayList<>();
    /**
     * 发布mqtt主题消息的处理类
     */
    private List<PublishProcessor> publishProcessorList = new ArrayList<>();

    /**
     * 与mqtt服务器的连接是否建立
     */
    private boolean isConnected = false;
    /**
     * 与mqtt服务器的连接是否关闭
     */
    private boolean isClosed = false;

    /**
     * 客户端回调处理类
     * 包含回调方法：连接成功回调、连接失败回调、连接丢失回调、收到消息回调等等
     */
    private Callback callback;

    public void setCallback(Callback c) {
        this.callback = c;
    }

    /**
     * 设置订阅、取消订阅、发布消息、ping等动作的超时时间
     *
     * @param actionTimeout 等待动作完成的超时时间
     */
    public void setActionTimeout(long actionTimeout) {
        this.actionTimeout = actionTimeout;
    }

    /**
     * 设置mqtt连接的超时时间
     *
     * @param connectTimeout 等待连接完成的超时时间
     */
    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 当maxTimes大于0时，如果发生掉线，则自动尝试重连，重连成功则回调onConnected方法，
     * 重连次数用完则回调onConnectLost方法。
     * 当timeout大于0时，如果整个重连过程消耗时间超过timeout，此时无论重连次数是否用完都
     * 停止重试，并回调onConnectLost方法。
     *
     * @param maxTimes 重试最大次数
     * @param timeout  重试超时时间
     */
    public void setReconnectOnLost(int maxTimes, long timeout) {
        this.maxReconnectTimesOnLost = maxTimes;
        this.reconnectTimeoutOnLost = timeout;
    }

    synchronized public void connect(MqttConfig options) throws Exception {
        connect(options, connectTimeout);
    }

    synchronized public void connect(MqttConfig options, long timeout) throws Exception {
        if (this.connectOptions != null) {
            return;
        }
        this.connectOptions = options;
        this.connectTimeout = timeout;

        try {
            doConnect(options, timeout);
            onConnected();
        } catch (Exception e) {
//            e.printStackTrace();
            onConnectFailed(e);
            throw e;
        }
    }

    private void doConnect(MqttConfig options, long timeout) throws Exception {
        // 创建长连接
        EventLoopGroup group = new NioEventLoopGroup();
        connectTask = new AsyncTask<String>() {
            @Override
            public String call() throws Exception {
                Bootstrap b = new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast("decoder", new MqttDecoder())//解码
                                        .addLast("encoder", MqttEncoder.INSTANCE)//编码
                                        .addLast("handler", new MqttHandler());
                            }
                        });
                ChannelFuture ch = b.connect(options.getHost(), options.getPort()).sync();
                channel = ch.channel();
                Log.i("--已连接->" + channel.localAddress().toString());
                return null;
            }
        }.execute();
        try {
            connectTask.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
//            e.printStackTrace();
            Log.i("-->连接异常：" + e);
            group.shutdownGracefully();
            throw e;
        }

        if (channel == null)
            return;

        // 发送mqtt协议连接报文
        doConnect0(channel, options, timeout);

        // 等待连接关闭的任务
        connectTask = new AsyncTask<String>() {
            @Override
            public String call() throws Exception {
                try {
                    channel.closeFuture().sync();
                } catch (Exception e) {
//                    e.printStackTrace();
                    Log.i("-->连接断开异常：" + e);
                } finally {
                    group.shutdownGracefully();
                    if (!isClosed()) {
                        // 非主动断开，可能源于服务器原因
                        Exception e = new ConnectException("Connection closed unexpectedly");
                        Log.i("-->连接断开：" + e);
                        onConnectLost(e);
                    } else {
                        Log.i("-->连接断开：主动");
                    }
                }
                return null;
            }
        }.execute();
    }

    private void doConnect0(Channel channel, MqttConfig options, long timeout) throws Exception {
        if (channel == null)
            return;

        try {
            connectProcessor = new ConnectProcessor();
            //netty连接mqtt
            String s = connectProcessor.connect(channel, options, timeout);
            if (ProcessorResult.RESULT_SUCCESS.equals(s)) {
                Log.i("-->连接成功");
            } else {
                throw new CancellationException();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            if (e instanceof CancellationException) {
                Log.i("-->连接取消");
            } else {
                Log.i("-->连接异常：" + e);
                throw e;
            }
        }
    }

    /**
     * 重连mqtt
     * @param options
     * @param maxTimes
     * @param timeout
     * @param t
     */
    private void doReconnect(MqttConfig options, final int maxTimes, final long timeout, Throwable t) {
        reconnectTask = new AsyncTask<String>() {
            @Override
            public String call() throws Exception {
                long interval = MIN_RECONNECT_INTERVAL;
                if (timeout > 0) {
                    interval = timeout / maxTimes;
                    if (interval < MIN_RECONNECT_INTERVAL)
                        interval = MIN_RECONNECT_INTERVAL;
                }

                boolean bSuccess = false;
                int num = 0;
                long start = System.nanoTime();
                do {
                    ++num;
                    Log.i("-->重连开始：" + num);
                    onReconnectStart(num);

                    long begin = System.nanoTime();
                    try {
                        doConnect(options, interval);
                        Log.i("<--重连成功：" + num);
                        bSuccess = true;
                        break;
                    } catch (Exception e) {
//                        e.printStackTrace();
                        Log.i("<--重连失败：" + num);
                    }

                    if (maxTimes <= num) { // 重试次数已经消耗殆尽
                        break;
                    }

                    // 判断是否timeout
                    if (timeout > 0) { // 只在配置了重连超时时间情况下才进行相关判断
                        // 重连总消耗时间
                        long spendTotal = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                        if (timeout <= spendTotal) {// 超时时间已经消耗殆尽
                            break;
                        }
                    }

                    // 单次连接消耗时间
                    long spend = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin);
                    long sleepTime = interval - spend;
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
//                            e.printStackTrace();
                            break;
                        }
                    }
                } while (!isCancelled());

                if (!isCancelled()) {
                    if (bSuccess) {
                        onConnected();
                    } else {
                        onReconnectFailed(t);
                    }
                }
                return null;
            }
        }.execute();
    }

    /**
     * 开启发送ping心跳的任务
     */
    private void startPingTask() {
        if (channel == null)
            return;

        if (pingProcessor == null
                || pingProcessor.isCancelled()
                || pingProcessor.isDone())
            pingProcessor = new PingProcessor();
        pingProcessor.start(channel, connectOptions.getKeepAliveInterval(), new PingCallback());
    }

    /**
     * 订阅主题
     *
     * @param topics
     * @throws Exception
     */
    public void subscribe(String... topics) throws Exception {
        subscribe(0, topics);
    }

    /**
     * 订阅主题
     *
     * @param qos    0-至多发1次
     *               1-至少送达1次
     *               2-完全送达并回应
     * @param topics 主题集
     * @throws Exception 失败异常
     */
    public void subscribe(int qos, String... topics) throws Exception {
        if (channel == null)
            return;

        SubscribeProcessor sp = new SubscribeProcessor();
        subscribeProcessorList.add(sp);
        try {
            String result = sp.subscribe(channel, qos, topics, actionTimeout);
            if (ProcessorResult.RESULT_SUCCESS.equals(result)) {
                Log.i("-->订阅成功：" + Arrays.toString(topics));
            } else {
                throw new CancellationException();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            if (e instanceof CancellationException) {
                Log.i("-->订阅取消：" + Arrays.toString(topics));
            } else {
                Log.i("-->订阅异常：" + Arrays.toString(topics) + "    " + e);
                throw e;
            }
        } finally {
            subscribeProcessorList.remove(sp);
        }
    }

    /**
     * 取消订阅
     * @param topics
     * @throws Exception
     */
    public void unsubscribe(String... topics) throws Exception {
        if (channel == null)
            return;

        UnsubscribeProcessor usp = new UnsubscribeProcessor();
        unsubscribeProcessorList.add(usp);
        try {
            String result = usp.unsubscribe(channel, topics, actionTimeout);
            if (ProcessorResult.RESULT_SUCCESS.equals(result)) {
                Log.i("-->取消订阅成功：" + Arrays.toString(topics));
            } else {
                throw new CancellationException();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            if (e instanceof CancellationException) {
                Log.i("-->取消订阅取消：" + Arrays.toString(topics));
            } else {
                Log.i("-->取消订阅异常：" + Arrays.toString(topics) + "    " + e);
                throw e;
            }
        } finally {
            unsubscribeProcessorList.remove(usp);
        }
    }

    /**
     * 发布主题消息
     * @param topic
     * @param content
     * @throws Exception
     */
    public void publish(String topic, String content) throws Exception {
        if (channel == null)
            return;

        PublishProcessor pp = new PublishProcessor();
        publishProcessorList.add(pp);
        try {
            String result = pp.publish(channel, topic, content, actionTimeout);
            if (ProcessorResult.RESULT_SUCCESS.equals(result)) {
                Log.i("-->发布成功：" + content);
            } else {
                throw new CancellationException();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            if (e instanceof CancellationException) {
                Log.i("-->发布取消：" + content);
            } else {
                Log.i("-->发布异常：" + content + "    " + e);
                throw e;
            }
        } finally {
            publishProcessorList.remove(pp);
        }
    }

    /**
     * 向mqtt发起断开连接请求
     * @throws Exception
     */
    public void disConnect() throws Exception {
        if (channel == null)
            return;

        MqttMessage msg = ProtocolUtils.disConnectMessage();
        Log.i("-->发起断开连接：" + msg);
        channel.writeAndFlush(msg);
    }

    /**
     * 关闭mqtt客户端所有的任务或连接
     */
    public void close() {
        setConnected(false);
        setClosed(true);

        if (reconnectTask != null)
            reconnectTask.cancel(true);

        if (connectProcessor != null) {
            connectProcessor.cancel(true);
        }

        if (pingProcessor != null) {
            pingProcessor.cancel(true);
        }

        if (subscribeProcessorList.size() > 0) {
            for (SubscribeProcessor sp : subscribeProcessorList) {
                sp.cancel(true);
            }
        }

        if (unsubscribeProcessorList.size() > 0) {
            for (UnsubscribeProcessor usp : unsubscribeProcessorList) {
                usp.cancel(true);
            }
        }

        if (publishProcessorList.size() > 0) {
            for (PublishProcessor pp : publishProcessorList) {
                pp.cancel(true);
            }
        }

        if (channel != null) {
            try {
                disConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                channel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            channel = null;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void setConnected(boolean b) {
        isConnected = b;
    }

    public boolean isClosed() {
        return isClosed;
    }

    private void setClosed(boolean b) {
        isClosed = b;
    }

    private void onConnected() {
        setConnected(true);
        setClosed(false);
        startPingTask();
        if (callback != null)
            callback.onConnected();
    }

    private void onConnectFailed(Throwable t) {
        close();
        if (callback != null)
            callback.onConnectFailed(t);
    }

    private void onConnectLost(Throwable t) {
        close();

        if (maxReconnectTimesOnLost > 0) {
            doReconnect(connectOptions, maxReconnectTimesOnLost, reconnectTimeoutOnLost, t);
        } else {
            if (callback != null) {
                callback.onConnectLost(t);
            }
        }
    }

    private void onReconnectStart(int num) {
        if (callback != null)
            callback.onReconnectStart(num);
    }

    private void onReconnectFailed(Throwable t) {
        close();
        if (callback != null)
            callback.onConnectLost(t);
    }

    private void onMessageArrived(String topic, String s) {
        Log.i("-->收到消息：" + topic + " | " + s);
        if (callback != null) {
            callback.onMessageArrived(topic, s);
        }
    }

    class MqttHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Log.i("");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Log.i("");
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
            Log.i("");
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
            Log.i("");
        }


        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msgx) throws Exception {
            if (msgx == null) {
                return;
            }

            MqttMessage msg = (MqttMessage) msgx;
            MqttFixedHeader mqttFixedHeader = msg.fixedHeader();
            if (mqttFixedHeader.messageType() == MqttMessageType.PINGRESP) {
                Log.i("[ping]-->channelRead0 : " + msgx);
            } else {
                Log.i("-->channelRead0 : " + msgx);
            }
            switch (mqttFixedHeader.messageType()) {
                case CONNACK:
                    if (connectProcessor != null)
                        connectProcessor.processAck(ctx.channel(), (MqttConnAckMessage) msg);
                    break;
                case SUBACK:
                    if (subscribeProcessorList.size() > 0) {
                        for (SubscribeProcessor subscribeProcessor : subscribeProcessorList) {
                            subscribeProcessor.processAck(ctx.channel(), (MqttSubAckMessage) msg);
                        }
                    }
                    break;
                case UNSUBACK:
                    if (unsubscribeProcessorList.size() > 0) {
                        for (UnsubscribeProcessor unsubscribeProcessor : unsubscribeProcessorList) {
                            unsubscribeProcessor.processAck(ctx.channel(), (MqttUnsubAckMessage) msg);
                        }
                    }
                    break;
                case PUBLISH:
                    MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
                    MqttPublishVariableHeader mqttPublishVariableHeader = publishMessage.variableHeader();
                    String topicName = mqttPublishVariableHeader.topicName();
                    ByteBuf payload = publishMessage.payload();
                    String content = payload.toString(StandardCharsets.UTF_8);
                    onMessageArrived(topicName, content);

                    if (mqttFixedHeader.qosLevel() == MqttQoS.AT_LEAST_ONCE
                            || mqttFixedHeader.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                        // qos为1、2级别的消息需要发送回执
                        // 注：需要完成数据的完全读取后才能发送回执
                        MqttPubAckMessage mqttPubAckMessage = ProtocolUtils.pubAckMessage(mqttPublishVariableHeader.messageId());
                        Log.i("-->发送消息回执：" + mqttPubAckMessage);
                        ctx.channel().writeAndFlush(mqttPubAckMessage);
                    }
                    break;
                case PUBACK:
                    // qos = 1的发布才有该响应
                    if (publishProcessorList.size() > 0) {
                        for (PublishProcessor publishProcessor : publishProcessorList) {
                            publishProcessor.processAck(ctx.channel(), (MqttPubAckMessage) msg);
                        }
                    }
                    break;
                case PUBREC:
                    // qos = 2的发布才参与
                    break;
                case PUBREL:
                    // qos = 2的发布才参与
                    break;
                case PUBCOMP:
                    // qos = 2的发布才参与
                    break;
                case PINGRESP:
                    // 心跳请求响应
                    if (pingProcessor != null) {
                        pingProcessor.processAck(ctx.channel(), msg);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
//            Log.i("");
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            Log.i("");
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            super.channelWritabilityChanged(ctx);
            Log.i("");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            Log.i("-->exceptionCaught : " + cause);
        }
    }

    class PingCallback implements PingProcessor.Callback {

        @Override
        public void onConnectLost(Throwable t) {
            Log.i("-->发生异常：" + t);
            MqttClient.this.onConnectLost(t);
        }
    }

    public interface Callback {

        void onConnected();

        void onConnectFailed(Throwable e);

        void onConnectLost(Throwable e);

        /**
         * @param cur 第几次重连
         */
        void onReconnectStart(int cur);

        void onMessageArrived(String topic, String s);
    }

}



