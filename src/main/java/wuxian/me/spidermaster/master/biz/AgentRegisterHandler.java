package wuxian.me.spidermaster.master.biz;

import com.google.gson.reflect.TypeToken;
import io.netty.channel.socket.SocketChannel;
import wuxian.me.spidercommon.log.LogManager;
import wuxian.me.spidercommon.model.SpiderFeature;
import wuxian.me.spidermaster.agent.biz.GsonProvider;
import wuxian.me.spidermaster.master.agentcontroll.Agent;
import wuxian.me.spidermaster.master.agentcontroll.AgentRecorder;
import wuxian.me.spidermaster.master.agentcontroll.Spider;
import wuxian.me.spidermaster.master.agentcontroll.StatusEnum;
import wuxian.me.spidermaster.util.RpcMethodName;
import wuxian.me.spidermaster.rpc.RpcRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxian on 18/5/2017.
 * <p>
 * Todo:
 */

@RpcMethodName(methodName = "register")
public class AgentRegisterHandler extends BaseBizHandler {

    //Fixme:这个接口的返回值设计有点问题
    //1 错误的时候怎么给出错误码
    //2 需要有返回值的时候怎么给出response的填充值
    public void handleRequest(RpcRequest request, SocketChannel channel) {

        LogManager.info("receive register rpc");
        LogManager.info("current thread: " + Thread.currentThread());
        LogManager.info("current channel: " + channel.toString());

        String data = request.datas;

        List<SpiderFeature> featureList = GsonProvider.gson().fromJson(data,new TypeToken<List<SpiderFeature>>(){}.getType());

        if(featureList == null ) {
            featureList = new ArrayList<SpiderFeature>();
        }

        Agent agent = new Agent();
        agent.setCurrentState(StatusEnum.REGISTERED);
        agent.setChannel(channel);

        List<Spider> spiderList = new ArrayList<Spider>();
        for(SpiderFeature feature:featureList) {
            spiderList.add(Spider.fromFeature(feature));
        }

        agent.setSpiderList(spiderList);
        AgentRecorder.recordAgent(agent);

    }
}