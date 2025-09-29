package com.wayeal.cloud.server.decode;

import com.alibaba.fastjson.JSON;
import com.wayeal.cloud.model.DataModel;
import com.wayeal.cloud.model.SubData;
import com.wayeal.cloud.server.protocol.ProtocolAnalysisInterface;
import com.wayeal.cloud.server.protocol.ProtocolDto;
import com.wayeal.cloud.session.Session;
import com.wayeal.cloud.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @author jian
 * @version 2023-02-07 15:06
 */
public class ProtocolWrapper {

    private static final Logger log = LoggerFactory.getLogger(ProtocolWrapper.class);

    private ProtocolDto[] protocolDtoS;

    public ProtocolWrapper(ProtocolDto... protocolDto) {
        this.protocolDtoS = protocolDto;
    }
    public ProtocolWrapper(List<ProtocolDto> list){
        this.protocolDtoS=new ProtocolDto[list.size()];
        for (int i=0;i<list.size();i++){
            protocolDtoS[i]=list.get(i);
        }
    }
    public ProtocolAnalysisInterface getProtocol(ByteBuf byteBuf) {
        log.info(">>>>>len:{}>>>>", byteBuf.writerIndex());
        for (ProtocolDto protocolDto : protocolDtoS) {
            int length = protocolDto.getLength();
            byte[] bytes = new byte[length];
            if (protocolDto.isStart()) {
                byteBuf.getBytes(0, bytes);
            } else {
                byteBuf.getBytes(byteBuf.writerIndex() - length, bytes);
            }
            String start;
            if (protocolDto.isHex()) {
                start = ByteBufUtil.hexDump(bytes);
            } else {
                start = new String(bytes);
            }
            String delimiter = protocolDto.getDelimiter();
            if (start.equals(delimiter)) {
                if (!protocolDto.isHex()) {
                    byte[] bytes11 = new byte[byteBuf.writerIndex()];
                    byteBuf.getBytes(0, bytes11);
                    try {
                        log.info("收到的报文>>>>>>{}", new String(bytes11, "GBK"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    log.info("收到的报文>>>>>>{}", parseStr(ByteBufUtil.hexDump(byteBuf)));
                }
                protocolDto.getProtocolAnalysisInterface().setDelimiter(delimiter);
                protocolDto
                        .getProtocolAnalysisInterface()
                        .setProtocolType(protocolDto.getProtocolType());
                return protocolDto.getProtocolAnalysisInterface();
            }
        }
        return null;
    }

    public DataModel read(ByteBuf byteBuf, SessionManager sessionManager, Channel channel) {
        try {
            ProtocolAnalysisInterface protocolAnalysisInterface = getProtocol(byteBuf);
            if (protocolAnalysisInterface != null) {
                Object data = protocolAnalysisInterface.read(byteBuf);
                SubData subData =
                        protocolAnalysisInterface.subpackage(
                                data,
                                sessionManager,
                                channel,
                                protocolAnalysisInterface.getDelimiter(),
                                protocolAnalysisInterface.getProtocolType());

                String id = subData.getChannelId();
                String clientId= subData.getId();
                Session session = sessionManager.findByClientIdAndId(clientId,id);
                //反控操作
                if (session!=null){
                    protocolAnalysisInterface.getCommand(session);
                }
                write(session);
                if (subData.getData() == null) {
                    return null;
                }
                DataModel dataModel = new DataModel();
                dataModel.setData(JSON.toJSONString(subData.getData()));
                dataModel.setProtocolType(protocolAnalysisInterface.getProtocolType());
                return dataModel;
            } else {
                log.error("没有找到解释器去读取协议");
            }
        } catch (Exception e) {
            log.error(ByteBufUtil.hexDump(byteBuf));
            log.error(new String(ByteBufUtil.getBytes(byteBuf)));
            log.error("报文解释错误: ",e);
        } finally {
            byteBuf.skipBytes(byteBuf.readableBytes());
            byteBuf.release();
        }
        return null;
    }

    public void write(Session session) {
        if (session != null) {
            for (ProtocolDto protocolDto : protocolDtoS) {
                if (protocolDto.getDelimiter().equals(session.getDelimiter())) {
                    protocolDto.getProtocolAnalysisInterface().write(session);
                    return;
                }
            }
        } else {
            log.error("没有找到session解释器去写入协议");
        }
    }

    public static String parseStr(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i += 2) {
            if (i == s.length() - 1) {
                sb.append(s.charAt(i)).append(s.charAt(i + 1));
            }
            sb.append(s.charAt(i)).append(s.charAt(i + 1)).append(' ');
        }
        return sb.toString();
    }
}
