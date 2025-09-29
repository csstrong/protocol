package com.wayeal.cloud.server.encode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author jian
 * @version 2023-02-17 17:05
 */
public class ResponseDecoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof String) {
            out.writeBytes(msg.toString().getBytes());
        }
        if (msg instanceof ByteBuf) {
            out.writeBytes((ByteBuf) msg);
        }
    }
}
