package com.koushik.waste;

public class CollectionTask {

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    private final String taskId;
    private final WasteBin wasteBin;
    private Status status;

    public CollectionTask(String taskId, WasteBin wasteBin) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException(
                    "Task ID cannot be empty.");
        }
        if (wasteBin == null) {
            throw new IllegalArgumentException(
                    "Waste bin cannot be null.");
        }

        this.taskId = taskId;
        this.wasteBin = wasteBin;
        this.status = Status.PENDING;
    }

    public String getTaskId() {
        return taskId;
    }

    public WasteBin getWasteBin() {
        return wasteBin;
    }

    public Status getStatus() {
        return status;
    }

    public void startTask() {
        if (status != Status.PENDING) {
            throw new IllegalStateException(
                    "Only pending tasks can be started.");
        }
        status = Status.IN_PROGRESS;
    }

    public void completeTask() {
        if (status != Status.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Start the task before completing it.");
        }

        wasteBin.emptyBin();
        status = Status.COMPLETED;
    }

    @Override
    public String toString() {
        return String.format(
                "Task ID: %s | Bin: %s | Status: %s",
                taskId, wasteBin.getBinId(), status);
    }
}