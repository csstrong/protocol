package com.wayeal.cloud.server.decode;

import com.wayeal.cloud.enums.MessageFrameControlEnum;
import com.wayeal.cloud.enums.ProtocolType;
import com.wayeal.cloud.exception.MessageParsingException;
import com.wayeal.cloud.model.ContentMessageRequest;
import com.wayeal.cloud.model.MessageRequest;
import com.wayeal.cloud.model.RtuMessageRequest;
import com.wayeal.cloud.model.SubData;
import com.wayeal.cloud.server.protocol.*;

import com.wayeal.cloud.session.Session;
import com.wayeal.cloud.session.SessionManager;
import com.wayeal.cloud.utils.CrcCheckUtil;
import com.wayeal.cloud.utils.DataFormatConvertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Arrays;

@Slf4j
public class RtuMessageDecoder extends ProtocolAnalysisAbstract {

    @Override
    public Object read(ByteBuf input) {
        RtuMessageRequest rtuMessage=new RtuMessageRequest();
        //crc 校验
        byte[] all=new byte[input.readableBytes()-2];
        if (input.readableBytes()<25){
            log.error("报文长度小于最小的长度");
            throw new MessageParsingException("报文长度小于最小的长度");
        }
        input.getBytes(0,all);
        String crcMod= CrcCheckUtil.getCrcModbus(all);
        byte[] bbb=new byte[2];
        input.getBytes(input.readableBytes()-2,bbb);
        String crcStr= ByteBufUtil.hexDump(bbb);
        if (!crcMod.equals(crcStr)){
            log.error("CRC校验错误,报文:{},结果:{}",crcStr,crcMod);
            throw new MessageParsingException("CRC校验错误");
        }
        MessageRequest headMessage = doHead(input);
        String start = headMessage.getStartCharacter();
        String identificationAndLength= headMessage.getIdentificationAndLength();
        byte[] bytes= DataFormatConvertUtil.hexStringToBytes(identificationAndLength);
        int data = DataFormatConvertUtil.byteArrayToUnsignedInt(bytes);
        //上下行标识
        int high4 = data >> 12;
        int len = data & 0x0fff;
        headMessage.setLength(len);
        headMessage.setUpAndDownIdentification(high4);
        //判断是否是M3模式
        if (MessageFrameControlEnum.SYN.getHexStr().equals(start)) {
            // M3模式多占用3个字节
            byte[] packageInfo = new byte[3];
            input.readBytes(packageInfo);
            int  pac = DataFormatConvertUtil.byteArrayToUnsignedInt(packageInfo);
        }
        ContentMessageRequest rtuContent= doBody(input,headMessage);
        if (rtuContent==null){
            log.error("寻找解释器失败");
            throw new MessageParsingException("rtu报文解析错误");
        }
        ContentDecoderAbstract contentDecoderAbstract=null;

        if (headMessage.getFunctionCode().equals("2f")){
            contentDecoderAbstract=new SL651_2F();
        }else if (headMessage.getFunctionCode().equals("32")){
            contentDecoderAbstract=new SL651_32();
        }else if (headMessage.getFunctionCode().equals("34")){
            contentDecoderAbstract=new SL651_34();
        }else {
        }
        ContentManage contentManage=new ContentManage(contentDecoderAbstract,rtuContent);
        contentManage.read(input);

        byte[] end=new byte[1];
        input.readBytes(end);
        String endChar= ByteBufUtil.hexDump(end);

        byte[] crc=new byte[2];
        input.readBytes(crc);
        String crcChar= ByteBufUtil.hexDump(crc);
        rtuMessage.setMessageRequest(headMessage);
        rtuMessage.setContentMessageRequest(rtuContent);
        rtuMessage.setEnd(endChar);
        rtuMessage.setCheckCode(crcChar);
        rtuMessage.setClientId(headMessage.getTelemetryStationAddress());
        return rtuMessage;
    }

    @Override
    public void write(Session session) {

    }

    @Override
    public SubData subpackage(Object data, SessionManager sessionManager, Channel channel, String delimiter, ProtocolType protocolType) {
        SubData subData=new SubData();
        RtuMessageRequest rtu = (RtuMessageRequest) data;
        Session session = sessionManager.findByClientIdAndId(rtu.getClientId(),channel.id().asLongText());
        if (session == null) {
            session =
                new Session(
                    channel, channel.remoteAddress(), delimiter,rtu.getClientId(),protocolType,data);
            sessionManager.put(rtu.getClientId(),channel.id().asLongText(),session);
        }
        rtu.setIp(channel.remoteAddress().toString());
        subData.setData(rtu);
        subData.setId(rtu.getClientId());
        subData.setChannelId(channel.id().asLongText());
        return subData;
    }

    @Override
    public void getCommand(Session session) {

    }

    public MessageRequest doHead(ByteBuf byteBuf){
       // log.info("rtu message is{}",ByteBufUtil.hexDump(byteBuf));
        MessageRequest messageRequest=new MessageRequest();
        //length = 2,desc = "帧起始符"
         byte[] str=new byte[2];
         byteBuf.readBytes(str);
         String st= ByteBufUtil.hexDump(str);
         messageRequest.setStartCharacter(st);
        //length = 1,desc = "中心站地址"
         byte[] cen=new byte[1];
         byteBuf.readBytes(cen);
         String cenStr=ByteBufUtil.hexDump(cen);
         messageRequest.setCenterAddress(cenStr);
        //length = 5,desc = "遥测站地址"
         byte[] yc=new byte[5];
         byteBuf.readBytes(yc);
         String ycStr=ByteBufUtil.hexDump(yc);
         messageRequest.setTelemetryStationAddress(ycStr);
        //length = 2,desc = "密码"
         byte[] pass=new byte[2];
         byteBuf.readBytes(pass);
         String passStr=ByteBufUtil.hexDump(pass);
         messageRequest.setPassword(passStr);
        //length = 1,desc = "功能码"
         byte[] fun=new byte[1];
         byteBuf.readBytes(fun);
         String funStr=ByteBufUtil.hexDump(fun);
         messageRequest.setFunctionCode(funStr);
        //length = 2,desc = "报文上行标识及长度"
         byte[] packUp=new byte[2];
         byteBuf.readBytes(packUp);
         String packUpStr=ByteBufUtil.hexDump(packUp);
         messageRequest.setIdentificationAndLength(packUpStr);
        //length = 1,desc = "报文起始符"
        byte[] starChar=new byte[1];
        byteBuf.readBytes(starChar);
        String starCharStr=ByteBufUtil.hexDump(starChar);
        messageRequest.setStartCharacter(starCharStr);
        return  messageRequest;
    }

    public ContentMessageRequest doBody(ByteBuf byteBuf,MessageRequest request){

        ContentMessageRequest contentMessageRequest=new ContentMessageRequest();

        //length = 2,desc = "流水号"
        byte[] lx=new byte[2];
        byteBuf.readBytes(lx);
        String lxStr=ByteBufUtil.hexDump(lx);
        contentMessageRequest.setSerialNumber(lxStr);

        //length = 6,desc = "发报时间 "
        byte[] time=new byte[6];
        byteBuf.readBytes(time);
        String timeStr=ByteBufUtil.hexDump(time);
        contentMessageRequest.setSendingTime(timeStr);

        if (request.getFunctionCode().equals("2f")){
            return contentMessageRequest;
        }

        //length = 7,desc = "引导符+遥测站地址"
        byte[] y=new byte[2];
        byteBuf.readBytes(y);
        byte[] yc=new byte[5];
        byteBuf.readBytes(yc);
        String ycStr=ByteBufUtil.hexDump(yc);
        contentMessageRequest.setStationCode(ycStr);

        //length = 1,desc = "遥测站分类码
        byte[] fl=new byte[1];
        byteBuf.readBytes(fl);
        String flStr=ByteBufUtil.hexDump(fl);
        contentMessageRequest.setStationClassificationCode(flStr);

        return contentMessageRequest;
    }



}
