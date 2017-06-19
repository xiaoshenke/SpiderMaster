package wuxian.me.spidermaster.rpc;

import com.sun.istack.internal.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import wuxian.me.spidercommon.log.LogManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxian on 26/5/2017.
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private List<Class<?>> classList = null;

    public RpcDecoder(final Class<?> clazz) {
        List<Class<?>> classList = null;
        if (clazz != null) {
            classList = new ArrayList<Class<?>>();
            classList.add(clazz);
        }

        init(classList);
    }

    private void init(@Nullable List<Class<?>> list) {
        if (list == null) {
            list = new ArrayList<Class<?>>();
        }

        this.classList = list;
    }

    public RpcDecoder(List<Class<?>> clazList) {
        init(clazList);
    }

    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) throws Exception {

        LogManager.info("RpcDecoder.decode");
        if (classList == null || classList.size() == 0) {
            return;
        }

        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = null;
        boolean success = true;

        for (Class<?> clazz : classList) {  //支持多个decoder解析器
            try {
                obj = SerializationUtil.deserialize(data, clazz);

                if (obj != null) {
                    success = true;
                    break;
                }
            } catch (IllegalStateException e) {
                success = false;
            } catch (Exception e) {
                success = false;
            }
        }

        if (success && obj != null) {
            out.add(obj);
        }

    }
}
