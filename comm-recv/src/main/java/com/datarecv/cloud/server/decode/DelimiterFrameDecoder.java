package com.datarecv.cloud.server.decode;

import com.datarecv.cloud.server.protocol.DelimiterDto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @des 拆包, 粘包处理
 */
public class DelimiterFrameDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(DelimiterFrameDecoder.class);

    private static final int BASE_MIN_LENGTH = 10;

    private static final int BASE_MAX_LENGTH = 4096;

    private DelimiterDto[] delimiterDtoS;

    public DelimiterFrameDecoder(DelimiterDto... delimiterDtoS) {
        this.delimiterDtoS = delimiterDtoS;
    }

    public DelimiterFrameDecoder(List<DelimiterDto> list) {
        this.delimiterDtoS = new DelimiterDto[list.size()];
        for (int i = 0; i < list.size(); i++) {
            delimiterDtoS[i] = list.get(i);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (!ctx.channel().isActive()) {
            return;
        }

        if (in.readableBytes() <= BASE_MIN_LENGTH) {
            return;
        }
        if (in.readableBytes() > BASE_MAX_LENGTH) {
            log.error("length is too max =>{}", ByteBufUtil.hexDump(in));
            in.skipBytes(in.readableBytes());
        }

        DelimiterDto minDelim = null;
        int minFrameLength = in.readableBytes();
        for (DelimiterDto delim : delimiterDtoS) {
            // frameLength>=0说明报文中已经有匹配我们定义的分割符
            int frameLength = indexOf(in, delim.getDelimiter(), true);
            if (frameLength >= 0) {
                if (!delim.isSeparator()) {
                    frameLength = indexOf(in, delim.getDelimiter(), delim.isSeparator());
                    frameLength = endWith(in,frameLength, delim.getDelimiter());
                } else {
                    //去除报文前的分割字符
                    while (frameLength == 0) {
                        in.readBytes(delim.getDelimiter().capacity());
                        frameLength = indexOf(in, delim.getDelimiter(), delim.isSeparator());
                    }
                    if (-1 == frameLength) {
                        return;
                    }
                }
                if (frameLength>0){
                    minFrameLength = frameLength;
                    minDelim = delim;
                }
                break;
            }
        }
        ByteBuf frame;
        if (minDelim == null) {
            return;
        }
        int minDelimLength = minDelim.getDelimiter().capacity();
        if (minDelim.isSave()) {
            frame = in.readRetainedSlice(minFrameLength + minDelimLength);
        } else {
            frame = in.readRetainedSlice(minFrameLength);
        }
        log.info("报文：{}", ByteBufUtil.prettyHexDump(frame));
        log.info(">>>in:{}>剩余未读的包len:{}>>包len:{}>>>>",ctx.channel().id().asShortText(), in.readableBytes(), frame.readableBytes());
        out.add(frame);
    }

    private static int isContain(ByteBuf in, ByteBuf[] byteBufS) {
        int i = -1;
        for (ByteBuf buf : byteBufS) {
            i = indexOf(in, buf, false);
            if (i != -1) {
                return i;
            }
        }
        return i;
    }

    public static int endWith(ByteBuf haystack,int endIndex, ByteBuf needle) {
       if ( haystack.getByte(endIndex+needle.capacity())==needle.getByte(needle.capacity()-1)){
           return endIndex+1;
       }
        return endIndex;
    }

    private static int indexOf(ByteBuf haystack, ByteBuf needle, boolean separator) {
        for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
            int haystackIndex = i;
            int needleIndex;
            for (needleIndex = 0; needleIndex < needle.capacity(); needleIndex++) {
                if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
                    break;
                } else {
                    haystackIndex++;
                    if (haystackIndex == haystack.writerIndex() && needleIndex != needle.capacity() - 1) {
                        return -1;
                    }
                }
            }

            if (needleIndex == needle.capacity()) {
                // Found the needle from the haystack!
                int index = i - haystack.readerIndex();
                if (separator) {
                    return index;
                }
                if (index != 0) {
                    return index;
                }
            }
        }
        return -1;
    }
}
