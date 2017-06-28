package wuxian.me.spidermaster.framework.agent.connection;


import com.sun.istack.internal.NotNull;
import wuxian.me.spidercommon.log.LogManager;
import wuxian.me.spidermaster.framework.agent.IClient;
import wuxian.me.spidermaster.framework.agent.request.IRpcCallback;
import wuxian.me.spidermaster.framework.rpc.RpcRequest;
import wuxian.me.spidermaster.framework.rpc.RpcResponse;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by wuxian on 10/6/2017.
 */
public class MessageSender {

    private Map<String, IRpcCallback> callbackMap = new ConcurrentHashMap<String, IRpcCallback>();
    private Queue<RpcRequest> requestQueue = new LinkedBlockingQueue<RpcRequest>();
    private Map<String, RpcRequest> requestMap = new ConcurrentHashMap<String, RpcRequest>();
    private IClient client;

    private Thread dispatchThread;

    private WaitNodeManager waitNodeManager;

    public MessageSender(@NotNull IClient client) {

        this.client = client;
    }

    public RpcRequest getRequestBy(String key) {
        if (key == null || key.length() == 0) {
            return null;
        }
        return requestMap.get(key);
    }

    public void onConncetSuccess() {
        synchronized (dispatchThread) {
            dispatchThread.notifyAll();
        }
    }

    //Todo:这时候应该暂停分发行为
    public void onForceDisconnect() {
        ;
    }

    public void init() {
        dispatchThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    while (true) {

                        boolean canPoll = (client != null && client.channel() != null && !requestQueue.isEmpty());
                        if (!canPoll) {
                            synchronized (this) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    ;
                                }
                            }
                        }

                        if (client.channel().isShutdown()) {
                            //连接被关闭：可能是远程主动关闭的,
                            //这时候正确的处理是方式应该是由SpiderClient进行重连 直到用户kill整个进程
                            LogManager.info("channel is shutdown...switch to wait state");
                            synchronized (this) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    ;
                                }
                            }
                        }

                        RpcRequest rpcRequest = requestQueue.poll();
                        try {
                            client.channel().writeAndFlush(rpcRequest).await();

                        } catch (InterruptedException e) {
                            LogManager.error("sender InterruptedExcepiton");
                        }
                    }
                }

            }
        };
        dispatchThread.setName("dispatchRpcRequestThread");
        dispatchThread.start();

        waitNodeManager = new WaitNodeManager(new WaitNodeManager.OnNodeTimeout() {
            @Override
            public void onNodeTimeout(String reqId) {
                if (callbackMap.containsKey(reqId)) {
                    callbackMap.get(reqId).onTimeout();
                }
            }
        });
        waitNodeManager.init();
    }

    public void put(RpcRequest request, IRpcCallback onRpcReques, Long timeout) {

        if (request == null) {
            return;
        }

        if (callbackMap.containsKey(request.requestId)) {
            return;
        }


        boolean notify = (client != null && client.channel() != null && requestQueue.isEmpty());

        requestQueue.add(request);
        requestMap.put(request.requestId, request);
        callbackMap.put(request.requestId, onRpcReques);

        if (timeout != null) {
            waitNodeManager.addWaitNode(request.requestId, timeout);
        }

        if (notify) {
            synchronized (dispatchThread) {
                dispatchThread.notifyAll();
            }
        }
    }


    public void put(RpcRequest request, IRpcCallback onRpcReques) {
        put(request, onRpcReques, null);
    }

    public void onRpcResponse(RpcResponse response) {
        if (response == null || response.requestId == null || response.requestId.length() == 0) {
            return;
        }

        waitNodeManager.removeWaitNode(response.requestId);

        if (callbackMap.containsKey(response.requestId)) {
            callbackMap.get(response.requestId).onResponseSuccess(response);
        }
    }
}
