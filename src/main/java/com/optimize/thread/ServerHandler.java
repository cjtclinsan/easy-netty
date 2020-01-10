package com.optimize.thread;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author taosh
 * @create 2020-01-10 14:13
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    public static final ChannelHandler INSTANCE = new ServerHandler();

    //主线程
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        ByteBuf data = Unpooled.directBuffer();

        //从客户端读一个时间戳
        data.writeBytes(byteBuf);
        //模拟一次业务处理
        Object result = getResult(data);
        //重新写给客户端
        ctx.channel().writeAndFlush(result);
    }

    public Object getResult(ByteBuf data) {
        int level = ThreadLocalRandom.current().nextInt(1, 1000);

        //计算每次响应时间   来用作QPS的参考数据
        int time;
        if (level <= 900) {
            time = 1;
            //95.0% == 10ms    1000 50 > 10ms
        } else if (level <= 950) {
            time = 10;
            //99.0% == 100ms    1000 10 > 100ms
        } else if (level <= 990) {
            time = 100;
            //99.9% == 1000ms    1000 1 > 1000ms
        } else {
            time = 1000;
        }

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return data;
    }
}
