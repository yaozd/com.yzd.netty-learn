package com.yzd;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @Author: yaozh
 * @Description:
 */
public class TaskContext {
    private LinkedList<AsynchronousTask> taskLinkedList;

    public void addTask(AsynchronousTask task) {
        if (taskLinkedList == null) {
            taskLinkedList = new LinkedList<>();
        }
        taskLinkedList.add(task);
    }

    public boolean isCompleteAllTask() {
        if (taskLinkedList == null || taskLinkedList.isEmpty()) {
            return true;
        }
        Iterator<AsynchronousTask> iterator = taskLinkedList.iterator();
        while (iterator.hasNext()) {
            AsynchronousTask next = iterator.next();
            if (next.execute()) {
                iterator.remove();
            }
        }
        return taskLinkedList.isEmpty();
    }
}
