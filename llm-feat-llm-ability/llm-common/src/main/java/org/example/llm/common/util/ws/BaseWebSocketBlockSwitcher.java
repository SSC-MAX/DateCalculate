package org.example.llm.common.util.ws;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author : zybi
 * @date : 2024/10/31 20:06
 */
public class BaseWebSocketBlockSwitcher {
    /**
     * 首帧数据阻塞等待
     */
    //private final SettableFuture<Boolean> firstFrameDataAcceptedFuture = SettableFuture.create();

    /**
     * 任务完成阻塞等待
     */
    private final SettableFuture<Boolean> completedFuture = SettableFuture.create();


    private final Long maxWaitTimeOfFirstFrameData;   // 最大首帧数据阻塞等待时间, 单位秒

    private final Long maxWaitTimeOfCompleted;        // 最大任务完成阻塞等待时间, 单位秒


    public BaseWebSocketBlockSwitcher(Long maxWaitTimeOfFirstFrameData, Long maxWaitTimeOfCompleted) {
        this.maxWaitTimeOfFirstFrameData = maxWaitTimeOfFirstFrameData;
        this.maxWaitTimeOfCompleted = maxWaitTimeOfCompleted;
    }


    /**
     * 打开阻塞
     * @throws ExecutionException 执行异常
     * @throws InterruptedException 阻塞被中断异常
     * @throws TimeoutException 超时异常
     */
    public void turnOn() throws ExecutionException, InterruptedException, TimeoutException {
        //firstFrameDataAcceptedFuture.get(maxWaitTimeOfFirstFrameData, TimeUnit.SECONDS);
        completedFuture.get(maxWaitTimeOfCompleted, TimeUnit.SECONDS);
    }


    /**
     * 关闭首帧数据响应阻塞
     */
    public void turnOffFirstFrameDataWait() {
        /*if (!firstFrameDataAcceptedFuture.isDone()) {
            firstFrameDataAcceptedFuture.set(Boolean.TRUE);
        }*/
    }

    /**
     * 关闭任务完成阻塞
     */
    public void turnOffCompletedWait() {
        if (!completedFuture.isDone()) {
            completedFuture.set(Boolean.TRUE);
        }
    }

    /**
     * 关闭所有阻塞
     */
    public void turnOffAll() {
        turnOffFirstFrameDataWait();
        turnOffCompletedWait();
    }
}
