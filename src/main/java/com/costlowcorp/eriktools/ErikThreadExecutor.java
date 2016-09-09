/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 *
 * @author ecostlow
 */
enum ErikThreadExecutor implements ExecutorService {
    INSTANCE;

    private final ErikThreadPool actualService = new ErikThreadPool(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), r -> {
                final Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            });
    
    public void setWhenNoJobsLeft(Runnable noJobsLeft){
        actualService.setWhenNoJobsLeft(noJobsLeft);
    }

    @Override
    public void shutdown() {
        actualService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return actualService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return actualService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return actualService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return actualService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return actualService.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return actualService.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return actualService.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return actualService.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return actualService.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return actualService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return actualService.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        actualService.execute(command);
    }

    private static class ErikThreadPool extends ThreadPoolExecutor {
        
        private Runnable whenNoJobsLeft = () -> {
            
        };

        public ErikThreadPool(int corePoolSize,
                int maximumPoolSize,
                long keepAliveTime,
                TimeUnit unit, BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }
        
        void setWhenNoJobsLeft(Runnable whenNoJobsLeft){
            this.whenNoJobsLeft=whenNoJobsLeft;
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            final int activeCount = getActiveCount();
            //System.out.println("Done with job and now have " + activeCount + " jobs");
            try {
                if (activeCount == 0 || (activeCount==1 && getQueue().isEmpty())) {
                    //System.out.println("This is the last job.");
                    whenNoJobsLeft.run();
                }
            } catch (RuntimeException e) {
                Logger.getLogger(ErikThreadExecutor.class.getSimpleName()).info("Index error looking at completed jobs: " + e.getMessage());
            }
        }

    }
}
