package com.datarecv.cloud.server.decode;

import com.datarecv.cloud.rabbit.RabbitMqInterface;
import com.datarecv.cloud.model.DataModel;
import com.datarecv.cloud.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolDecoder  extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ProtocolDecoder.class);

    private ProtocolWrapper   protocolWrapper;

    private SessionManager    sessionManager;

    private RabbitMqInterface rabbitMq;

    public ProtocolDecoder (ProtocolWrapper protocolWrapper,SessionManager sessionManager,RabbitMqInterface rabbitMq){
        this.protocolWrapper=protocolWrapper;
        this.sessionManager=sessionManager;
        this.rabbitMq=rabbitMq;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf in = (ByteBuf) msg;
        DataModel data= protocolWrapper.read(in,sessionManager,ctx.channel());
        if(data!=null){
            rabbitMq.sendMessage(data.getData(),data.getProtocolType());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("<<<<<终端连接ip:{},id:{}", ctx.channel().remoteAddress(),ctx.channel().id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        sessionManager.remove(ctx.channel().id().asLongText());
        log.info(">>>>>终端断开ip:{},id:{}", ctx.channel().remoteAddress(),ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
          //异常处理
          super.exceptionCaught(ctx, cause);
    }
}
