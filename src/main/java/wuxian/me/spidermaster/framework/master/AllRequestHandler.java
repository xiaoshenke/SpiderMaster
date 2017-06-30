package wuxian.me.spidermaster.framework.master;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import wuxian.me.spidercommon.log.LogManager;
import wuxian.me.spidermaster.framework.master.handler.HandlerExcepiton;
import wuxian.me.spidermaster.framework.master.handler.HandlerScanner;
import wuxian.me.spidermaster.framework.master.handler.IRpcRequestHandler;
import wuxian.me.spidermaster.framework.rpc.RpcRequest;
import wuxian.me.spidermaster.framework.rpc.RpcResponse;
import wuxian.me.spidermaster.framework.rpc.RpcRetCode;

/**
 * Created by wuxian on 11/6/2017.
 * <p>
 * Rpc Request统一路由。
 */
public class AllRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private SocketChannel channel;

    public AllRequestHandler(SocketChannel channel) {
        this.channel = channel;
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {

        boolean needResponse = true;

        RpcResponse response = new RpcResponse();
        response.requestId = request.requestId;

        IRpcRequestHandler handler = HandlerScanner.findHandlerBy(request.methodName);
        if (handler != null) {
            LogManager.info("getRpcRequest,rpcName: " + request.methodName + " handlerClass: " + handler.getClass());
            try {
                Object ret = handler.handleRequest(request, channel);

                response.result = ret;
                response.retCode = RpcRetCode.SUCCESS.ordinal();

                needResponse = handler.needResponse();  //其实只有heartbeat无需response。

            } catch (HandlerExcepiton e) {
                response.retCode = RpcRetCode.FAIL.ordinal();
            }

        } else {
            response.retCode = RpcRetCode.FAIL.ordinal(); //Todo:设计一些错误返回码
        }

        if (needResponse) {
            channelHandlerContext.writeAndFlush(response);
        }

    }
}
