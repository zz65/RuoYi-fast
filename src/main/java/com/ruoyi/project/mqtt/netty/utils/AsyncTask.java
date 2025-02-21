package com.ruoyi.project.mqtt.netty.utils;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.project.mqtt.netty.properties.MqttThreadPoolConfig;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zhuzhen
 * @date 2023/11/30 14:33
 * @description: 异步任务
 */
// @RequiredArgsConstructor
public abstract class AsyncTask<T> implements RunnableFuture<T>, Callable<T> {
    // private MqttThreadPoolConfig mqttThreadPoolConfig;
    // private volatile Executor sDefaultExecutor = null;

    private MqttThreadPoolConfig mqttThreadPoolConfig = new MqttThreadPoolConfig();
    int corePoolSize = mqttThreadPoolConfig.getCorePoolSize();
    int maxPoolSize = mqttThreadPoolConfig.getMaxPoolSize();
    int queueCapacity = mqttThreadPoolConfig.getQueueCapacity();
    int keepAliveSeconds = mqttThreadPoolConfig.getKeepAliveSeconds();
    String threadNamePrefix = mqttThreadPoolConfig.getThreadNamePrefix();
    private volatile Executor sDefaultExecutor = new ThreadPoolExecutor(
            corePoolSize, maxPoolSize, keepAliveSeconds,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            //线程名前缀
            new CustomizableThreadFactory(threadNamePrefix),
            new ThreadPoolExecutor.DiscardOldestPolicy()); // 丢弃最老的任务


    private FutureTask<T> futureTask;

    public AsyncTask() {
        // if (mqttThreadPoolConfig == null) {
        //     mqttThreadPoolConfig = SpringUtils.getBean(MqttThreadPoolConfig.class);
        // }
        // int corePoolSize = mqttThreadPoolConfig.getCorePoolSize();
        // int maxPoolSize = mqttThreadPoolConfig.getMaxPoolSize();
        // int queueCapacity = mqttThreadPoolConfig.getQueueCapacity();
        // int keepAliveSeconds = mqttThreadPoolConfig.getKeepAliveSeconds();
        // String threadNamePrefix = mqttThreadPoolConfig.getThreadNamePrefix();
        //
        // if (sDefaultExecutor == null) {
        //     sDefaultExecutor = new ThreadPoolExecutor(
        //             corePoolSize, maxPoolSize, keepAliveSeconds,
        //             TimeUnit.SECONDS,
        //             new LinkedBlockingQueue<>(queueCapacity),
        //             //线程名前缀
        //             new CustomizableThreadFactory(threadNamePrefix),
        //             new ThreadPoolExecutor.DiscardOldestPolicy()); // 丢弃最老的任务
        // }

        this.futureTask = new FutureTask<T>(this) {
            @Override
            protected void done() {
                AsyncTask.this.done();
                if (!isCancelled() && callback != null) {
                    callback.onDone();
                }
            }
        };
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureTask.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return futureTask.isCancelled();
    }

    @Override
    public boolean isDone() {
        return futureTask.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return futureTask.get();
        } catch (Exception e) {
            // 发生异常，尝试停止任务
            cancel(true);
            throw e;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return futureTask.get(timeout, unit);
        } catch (Exception e) {
            // 发生异常，尝试停止任务
            cancel(true);
            throw e;
        }
    }

    @Override
    public void run() {
        futureTask.run();
    }

    protected void done() {

    }

    public AsyncTask<T> execute() {
        try {
            sDefaultExecutor.execute(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public AsyncTask<T> executeOnExecutor(Executor executor) {
        try {
            executor.execute(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private Callback callback;

    public AsyncTask<T> setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public interface Callback {
        void onDone();
    }

}
