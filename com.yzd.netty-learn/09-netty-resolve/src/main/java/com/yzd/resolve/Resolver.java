package com.yzd.resolve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Resolver {
    private static Resolver ourInstance = new Resolver();

    public static Resolver getInstance() {
        return ourInstance;
    }

    //
    private Resolver() {
        new Thread(() -> sendReadAllRequest()).start();
        new Thread(() -> sendWatchRequest()).start();
    }

    /**
     * 发送监听请求
     */
    private void sendWatchRequest() {
        while (true) {
            try {
                TaskInfo taskInfo = watchUriQueue.take();
                if (!isExistTaskInfo(taskInfo)) {
                    continue;
                }
                System.out.println("watch:" + taskInfo.getReadAllUri());
                Scheduler.doWork(taskInfo,taskInfo.getIntervalTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送拉取全部节点信息请求
     */
    private void sendReadAllRequest() {
        while (true) {
            try {
                TaskInfo taskInfo = readAllUriQueue.take();
                if (!isExistTaskInfo(taskInfo)) {
                    continue;
                }
                System.out.println("read all:" + taskInfo.getReadAllUri());
                Scheduler.doWorkReadAll(taskInfo,taskInfo.getIntervalTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断任务信息是否存在
     *
     * @param taskInfo
     */
    private boolean isExistTaskInfo(TaskInfo taskInfo) {
        TaskInfo currentTaskInfo = tasksMap.get(taskInfo.getKey());
        if (currentTaskInfo == null) {
            return false;
        }
        if (!taskInfo.getUuid().equals(currentTaskInfo.getUuid())) {
            return false;
        }
        return true;
    }

    /**
     * 空节点
     */
    private final Node EMPTY_NODE=new Node("0.0.0.0",0);
    //
    BlockingQueue<TaskInfo> readAllUriQueue = new LinkedBlockingQueue<>();
    BlockingQueue<TaskInfo> watchUriQueue = new LinkedBlockingQueue<>();
    //
    private Map<String, TaskInfo> tasksMap = new HashMap<String, TaskInfo>();
    private Map<String, List<Node>> nodesMap = new HashMap<String, List<Node>>();

    public void addTask(TaskInfo taskInfo) {
        tasksMap.put(taskInfo.getKey(), taskInfo);
        readAllUriQueue.add(taskInfo);
        watchUriQueue.add(taskInfo);
    }

    public void addNode(String key, List<Node> nodes) {
        nodesMap.put(key, nodes);
    }
}
