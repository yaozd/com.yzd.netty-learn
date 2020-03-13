package com.yzd.resolve;

import com.yzd.resolve.data.Node;
import com.yzd.resolve.data.RequestData;
import com.yzd.resolve.data.RequestType;
import com.yzd.resolve.data.TaskInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Resolver {
    private static Resolver ourInstance = new Resolver();

    public static Resolver getInstance() {
        return ourInstance;
    }

    private Resolver() {
        new ResolverThreadFactory().newThread(() -> ListenRequestDataQueue()).start();
    }

    private void ListenRequestDataQueue() {
        while (true) {
            try {
                RequestData requestData = requestDataQueue.take();
                if (!isExistTaskInfo(requestData.getTaskInfo())) {
                    continue;
                }
                setRequestUuidForReadAllRequestType(requestData);
                ResolverScheduler.doWork(requestData);
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
    public boolean isExistTaskInfo(TaskInfo taskInfo) {
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
     * 为拉取所有节点数据类型的请求，设置当前任务的请求ID
     * @param requestData
     */
    private void setRequestUuidForReadAllRequestType(RequestData requestData){
        TaskInfo currentTaskInfo = tasksMap.get(requestData.getTaskInfo().getKey());
        if(currentTaskInfo==null){
            return;
        }
        if(RequestType.READ_ALL_URI.equals(requestData.getRequestType())
        &&requestData.getTaskInfo().getUuid().equals(currentTaskInfo.getUuid()))
        {
            currentTaskInfo.setRequestUuid(requestData.getUuid());
        }
    }

    /**
     * 空节点
     */
    private final Node EMPTY_NODE = new Node("0.0.0.0", 0);
    BlockingQueue<RequestData> requestDataQueue = new LinkedBlockingQueue<>();
    private Map<String, TaskInfo> tasksMap = new ConcurrentHashMap<String, TaskInfo>();
    private Map<String, List<Node>> nodesMap = new ConcurrentHashMap<String, List<Node>>();

    public void addTask(TaskInfo taskInfo) {
        tasksMap.put(taskInfo.getKey(), taskInfo);
        addRequestDataQueue(new RequestData(taskInfo, RequestType.READ_ALL_URI));
        addRequestDataQueue(new RequestData(taskInfo, RequestType.WATCH_URI));
    }

    /**
     * @param requestData
     */
    public void addRequestDataQueue(RequestData requestData) {
        this.requestDataQueue.add(requestData);
    }

    /**
     * @param key
     * @param nodes
     */
    public void addNode(String key, List<Node> nodes) {
        nodesMap.put(key, nodes);
    }
}
