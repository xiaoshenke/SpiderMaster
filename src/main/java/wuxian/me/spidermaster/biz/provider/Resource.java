package wuxian.me.spidermaster.biz.provider;

/**
 * Created by wuxian on 21/6/2017.
 */
public class Resource {

    public Resource() {
    }

    public String name;

    public Object data;

    @Override
    public String toString() {
        return "Resource{" +
                "name='" + name + '\'' +
                ", data=" + data +
                '}';
    }
}
