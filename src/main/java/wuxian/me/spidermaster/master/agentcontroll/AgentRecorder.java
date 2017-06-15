package wuxian.me.spidermaster.master.agentcontroll;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wuxian on 18/5/2017.
 * <p>
 * 记录所有agent
 */
public class AgentRecorder {

    private static Set<Agent> agentSet = Collections.synchronizedSet(new HashSet<Agent>());

    private AgentRecorder() {
    }

    public static void recordAgent(Agent agent) {

        if(agent == null) {
            return;
        }

        if(!agentSet.contains(agent)) {
            synchronized (agentSet) {
                if(!agentSet.contains(agent)) {
                    agentSet.add(agent);
                }
            }
        }
    }


}
