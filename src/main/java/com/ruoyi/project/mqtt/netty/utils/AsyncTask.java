package com.ruoyi.project.mqtt.netty.utils;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

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
// @Component
public abstract class AsyncTask<T> implements RunnableFuture<T>, Callable<T> {
    // @Value("${thread.pool.core-pool-size: 5}")
    // private int corePoolSize;
    //
    // @Value("${thread.pool.maximum-pool-size: 15}")
    // private int maximumPoolSize;
    //
    // @Value("${thread.pool.keep-alive-time: 120}")
    // private long keepAliveTime;
    //
    // @Value("${thread.pool.queue.capacity: 50000}")
    // private int capacity;

    /**
     * 20231130 zhuzhen改，因为资源有限，所以把线程数量，队列长度修改了。
     */
    // private static volatile Executor sDefaultExecutor = Executors.newCachedThreadPool();
    private volatile Executor sDefaultExecutor = new ThreadPoolExecutor(
            /* corePoolSize, maximumPoolSize, keepAliveTime,*/
            4, 4, 120,
            TimeUnit.SECONDS,
            // new LinkedBlockingQueue<>(capacity),
            new LinkedBlockingQueue<>(20000),
            //线程名前缀
            new CustomizableThreadFactory("AsyncTask-"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private FutureTask<T> futureTask;

    public AsyncTask() {
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
