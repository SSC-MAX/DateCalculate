package org.example.llm.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ThreadUtil {
    public static <T> List<T> submitBatchTask(int threadSize, List<Supplier<T>> taskList) {
        // 创建一个固定大小的线程池，包含 5 个线程
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        // 存储任务结果的列表
        List<T> results = new CopyOnWriteArrayList<>();
        // 创建任务列表
        List<Callable<Void>> tasks = new ArrayList<>();

        // 添加任务到任务列表
        for (Supplier<T> tSupplier : taskList) {
            tasks.add(() -> {
                // 模拟任务执行
                T result = tSupplier.get();
                // 将结果添加到共享列表
                results.add(result);
                return null;
            });
        }

        try {
            // 执行所有任务
            List<Future<Void>> futures = executorService.invokeAll(tasks);

            // 确保所有任务都完成
            for (Future<Void> future : futures) {
                future.get(); // 阻塞直到任务完成
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            System.err.println("任务执行被中断: " + e.getMessage());
        } finally {
            // 关闭线程池
            executorService.shutdown();
        }
        // 所有任务完成后，消费结果
        return results;
    }

}
