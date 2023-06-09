package us.ajg0702.queue.common;

import us.ajg0702.queue.common.utils.QueueThreadFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class TaskManager {


    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new QueueThreadFactory("GENERIC"));
    final ScheduledExecutorService updateExecutor = Executors.newScheduledThreadPool(1, new QueueThreadFactory("UPDATE-EXECUTOR"));
    final ExecutorService serversUpdateExecutor = Executors.newCachedThreadPool(new QueueThreadFactory("SERVER-UPDATE"));

    final QueueMain main;
    public TaskManager(QueueMain main) {
        this.main = main;
    }

    public void shutdown() {
        executor.shutdown();
        updateExecutor.shutdown();
        serversUpdateExecutor.shutdown();
    }

    public ExecutorService getServersUpdateExecutor() {
        return serversUpdateExecutor;
    }

    public String taskStatus() {
        List<ScheduledFuture<?>> tasks = Arrays.asList(sendTask, updateTask, messageTask, actionBarTask, titleTask, queueEventTask, reloadServerTask);
        StringBuilder sb = new StringBuilder();
        for(ScheduledFuture<?> task : tasks) {
            sb.append(task == null ? "null" : task.isDone() ? "canceled/done" : "running");
            sb.append("\n");
        }
        return sb.toString();
    }

    ScheduledFuture<?> sendTask;
    ScheduledFuture<?> updateTask;
    ScheduledFuture<?> messageTask;
    ScheduledFuture<?> actionBarTask;
    ScheduledFuture<?> titleTask;
    ScheduledFuture<?> queueEventTask;
    ScheduledFuture<?> reloadServerTask;
    public void rescheduleTasks() {
        cancelTasks();

        sendTask = scheduleAtFixedRate(
                main.getQueueManager()::sendPlayers,
                (long) (main.getConfig().getDouble("wait-time")*1000L),
                TimeUnit.MILLISECONDS
        );

        updateTask = scheduleAtFixedRate(updateExecutor,
                main.getQueueManager()::updateServers,
                500L,
                (long) (Math.max(main.getTimeBetweenPlayers()/2, main.getConfig().getDouble("minimum-ping-time"))*1000L),
                TimeUnit.MILLISECONDS
        );

        messageTask = scheduleAtFixedRate(
                main.getQueueManager()::sendMessages,
                main.getConfig().getInt("message-time"),
                TimeUnit.SECONDS
        );

        actionBarTask = scheduleAtFixedRate(
                main.getQueueManager()::sendActionBars,
                1500L,
                TimeUnit.MILLISECONDS
        );

        titleTask = scheduleAtFixedRate(
                main.getQueueManager()::sendTitles,
                1500L,
                TimeUnit.MILLISECONDS
        );

        queueEventTask = scheduleAtFixedRate(
                main.getQueueManager()::sendQueueEvents,
                1500L,
                TimeUnit.MILLISECONDS
        );

        if(main.getConfig().getInt("reload-servers-interval") > 0) {
            reloadServerTask = scheduleAtFixedRate(
                    main.getQueueManager()::reloadServers,
                    main.getConfig().getInt("reload-servers-interval"),
                    TimeUnit.SECONDS
            );
        }

    }

    public void cancelTasks() {
        if(sendTask != null && !sendTask.isCancelled()) {
            sendTask.cancel(true);
        }
        if(updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel(true);
        }
        if(messageTask != null && !messageTask.isCancelled()) {
            messageTask.cancel(true);
        }
        if(actionBarTask != null && !actionBarTask.isCancelled()) {
            actionBarTask.cancel(true);
        }
        if(titleTask != null && !titleTask.isCancelled()) {
            titleTask.cancel(true);
        }
        if(queueEventTask != null && !queueEventTask.isCancelled()) {
            queueEventTask.cancel(true);
        }
        if(reloadServerTask != null && !reloadServerTask.isCancelled()) {
            reloadServerTask.cancel(true);
            reloadServerTask = null;
        }
    }

    private ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long period, TimeUnit unit) {
        return scheduleAtFixedRate(executor, command, 0, period, unit);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ScheduledFuture<?> runLater(Runnable runnable, long delay, TimeUnit unit) {
        return executor.schedule(runnable, delay, unit);
    }

    public Future<?> runNow(Runnable runnable) {
        return executor.submit(runnable);
    }


    private ScheduledFuture<?> scheduleAtFixedRate(ScheduledExecutorService executor, Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(() -> {
            try {
                command.run();
            } catch (Exception e) {
                System.out.println("An error ocurred while running an ajQueue task");
                e.printStackTrace();
            }
        }, initialDelay, period, unit);
    }
}
