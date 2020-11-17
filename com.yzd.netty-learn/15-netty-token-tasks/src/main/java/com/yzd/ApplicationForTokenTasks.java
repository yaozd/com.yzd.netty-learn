package com.yzd;

/**
 * @Author: yaozh
 * @Description:
 */
public class ApplicationForTokenTasks {
    public static void main(String[] args) {
        TaskContext taskContext = new TaskContext();
        AsynchronousTask task = new AsynchronousTask();
        task.setParam("token-value");
        task.setTaskFunction(new TokenFunction());
        taskContext.addTask(task);
        boolean isCompleteAllTask = taskContext.isCompleteAllTask();
        System.out.println("Is complete all task:" + isCompleteAllTask);
    }
}
