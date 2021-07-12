package us.ajg0702.queue.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TaskManager {

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    ScheduledExecutorService updateExecutor = Executors.newScheduledThreadPool(1);

    QueueMain main;
    public TaskManager(QueueMain main) {
        this.main = main;
    }

    public String taskStatus() {
        List<ScheduledFuture<?>> tasks = Arrays.asList(sendTask, updateTask, messageTask, actionBarTask, queueEventTask, reloadServerTask);
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
    ScheduledFuture<?> queueEventTask;
    ScheduledFuture<?> reloadServerTask;
    public void rescheduleTasks() {
        cancelTasks();

        sendTask = scheduleAtFixedRate(
                main.getQueueManager()::sendPlayers,
                0L,
                (long) (main.getConfig().getDouble("wait-time")*1000L),
                TimeUnit.MILLISECONDS
        );

        updateTask = scheduleAtFixedRate(updateExecutor,
                main.getQueueManager()::updateServers,
                0L,
                (long) (Math.max(main.getTimeBetweenPlayers(), 2)*1000L),
                TimeUnit.MILLISECONDS
        );

        messageTask = scheduleAtFixedRate(
                main.getQueueManager()::sendMessages,
                0L,
                main.getConfig().getInt("message-time"),
                TimeUnit.SECONDS
        );

        actionBarTask = scheduleAtFixedRate(
                main.getQueueManager()::sendActionBars,
                0L,
                2L,
                TimeUnit.SECONDS
        );

        queueEventTask = scheduleAtFixedRate(
                main.getQueueManager()::sendQueueEvents,
                0L,
                2L,
                TimeUnit.SECONDS
        );

        if(main.getConfig().getInt("reload-servers-interval") > 0) {
            reloadServerTask = scheduleAtFixedRate(
                    main.getQueueManager()::reloadServers,
                    0L,
                    main.getConfig().getInt("reload-servers-interval"),
                    TimeUnit.SECONDS
            );
        }

    }

    public void cancelTasks() {
        if(sendTask != null && !sendTask.isCancelled()) {
            sendTask.cancel(false);
        }
        if(updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel(false);
        }
        if(messageTask != null && !messageTask.isCancelled()) {
            messageTask.cancel(false);
        }
        if(actionBarTask != null && !actionBarTask.isCancelled()) {
            actionBarTask.cancel(false);
        }
        if(queueEventTask != null && !queueEventTask.isCancelled()) {
            queueEventTask.cancel(false);
        }
        if(reloadServerTask != null && !reloadServerTask.isCancelled()) {
            reloadServerTask.cancel(false);
            reloadServerTask = null;
        }
    }

    private ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduleAtFixedRate(executor, command, initialDelay, period, unit);
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
