package com.wayeal.cloud.server;

import com.wayeal.cloud.config.ProtocolConfig;
import com.wayeal.cloud.enums.ProtocolType;
import com.wayeal.cloud.rabbit.RabbitMqInterface;
import com.wayeal.cloud.rabbit.RabbitMqSendUtil;
import com.wayeal.cloud.server.decode.*;
import com.wayeal.cloud.server.encode.ResponseDecoder;
import com.wayeal.cloud.server.protocol.DelimiterDto;
import com.wayeal.cloud.server.protocol.ProtocolDto;
import com.wayeal.cloud.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.List;

/**
 * @author jian
 * @version 2023-02-13 11:25
 */
public class TcpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private SessionManager sessionManager;

    private RabbitMqInterface rabbitMqSendUtil;

    private ProtocolConfig protocolConfig;

    public TcpServerChannelInitializer(
            SessionManager sessionManager,
            RabbitMqInterface rabbitMqSendUtil,
            ProtocolConfig protocolConfig) {
        this.sessionManager = sessionManager;
        this.rabbitMqSendUtil = rabbitMqSendUtil;
        this.protocolConfig = protocolConfig;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        List<DelimiterDto> delimiterDtoList= protocolConfig.getDelimiter();
        List<ProtocolDto>  protocolDtoList=protocolConfig.getProtocol();
        if (delimiterDtoList!=null && delimiterDtoList.size()>0){
            for (DelimiterDto delimiterDto:delimiterDtoList){

                if (delimiterDto.getValue()!=null){
                    if (delimiterDto.getDelimiterType()!=null && delimiterDto.getDelimiterType()== DelimiterDto.DelimiterType.arr){
                        String[] strings= delimiterDto.getValue().split(",");
                        byte[] bytes=new byte[strings.length];
                        for (int i=0;i<strings.length;i++){
                          byte b= Byte.parseByte(strings[i],16);
                          bytes[i]=b;
                        }
                        ByteBuf delimiter = Unpooled.copiedBuffer(bytes);
                        delimiterDto.setDelimiter(delimiter);
                    }else {
                        ByteBuf delimiter  = Unpooled.copiedBuffer(delimiterDto.getValue().getBytes());
                        delimiterDto.setDelimiter(delimiter);
                    }
                }
            }
        }

        ProtocolWrapper protocolWrapper=new ProtocolWrapper(protocolDtoList);
        DelimiterFrameDecoder delimiterFrameDecoder = new DelimiterFrameDecoder(delimiterDtoList);

        ch.pipeline().addLast("logger", new LoggingHandler(LogLevel.DEBUG));
        ch.pipeline().addLast("delimiter", delimiterFrameDecoder);
        ch.pipeline()
                .addLast(
                        "protocol",
                        new ProtocolDecoder(protocolWrapper, sessionManager, rabbitMqSendUtil));
        ch.pipeline().addLast("response", new ResponseDecoder());
    }
}
