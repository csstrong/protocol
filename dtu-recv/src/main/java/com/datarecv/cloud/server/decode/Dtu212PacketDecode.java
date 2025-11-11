package com.datarecv.cloud.server.decode;

import com.datarecv.cloud.command.CommandSubject;
import com.datarecv.cloud.enums.ProtocolType;
import com.datarecv.cloud.exception.MessageParsingException;
import com.datarecv.cloud.model.Dtu212Dto;
import com.datarecv.cloud.model.SubData;
import com.datarecv.cloud.server.protocol.ProtocolAnalysisAbstract;
import com.datarecv.cloud.session.Session;
import com.datarecv.cloud.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Dtu212PacketDecode extends ProtocolAnalysisAbstract {

    private static final Logger log = LoggerFactory.getLogger(Dtu212PacketDecode.class);
    /** 开始符 */
    private static final int start = 2;
    /** 字符长度 */
    private static final int startStr = 4;
    /** crc 校验结果 */
    private static final int checkStr = 4;
    /** 结束符 */
    private static final int end = 2;

    @Override
    public Object read(ByteBuf byteBuf) {
        // 根据国标212协议解析报文
        int len = byteBuf.readableBytes();
        // 字符开始
        byte[] startArr = new byte[start];
        byteBuf.getBytes(0, startArr);
        // 字符长度
        byte[] strLen = new byte[startStr];
        byteBuf.getBytes(start, strLen);
        // 校验结果
        byte[] checkCode = new byte[checkStr];
        byteBuf.getBytes(len - checkStr - end, checkCode);
        // 字符串结果
        byte[] bytes = new byte[len - start - end - startStr - checkStr];
        byteBuf.getBytes(start + startStr, bytes);

        Dtu212Dto dtu212Dto = new Dtu212Dto();
        try {
            String dtuStr = new String(bytes, "GBK");
            // log.info("收到的报文是:{}", dtuStr);
            short checkMsg = getCheckCode(bytes);
            String strCheckCode = String.format("%04x", checkMsg);
            String oldCheck = new String(checkCode).toLowerCase(Locale.ROOT);
            if (!strCheckCode.equals(oldCheck)) {
                log.error("报文:{},错误校验码:{},正确校验码:{}", dtuStr, oldCheck, strCheckCode);
                throw new MessageParsingException("dtu报文校验错误");
            }
            //
            int indexCp = dtuStr.indexOf("CP=&&");
            int endCp = dtuStr.lastIndexOf("&&");
            String cp = dtuStr.substring(indexCp, endCp);
            cp = cp.replace("CP=&&", "");
            String[] strings = dtuStr.split(";");
            for (String s : strings) {
                String[] arr = s.split("=");
                if (arr.length == 2) {
                    String key = arr[0];
                    String value = arr[1];
                    if ("CN".equals(key)) {
                        dtu212Dto.setCn(value);
                    } else if ("MN".equals(key)) {
                        dtu212Dto.setMn(value);
                    } else if ("PW".equals(key)) {
                        dtu212Dto.setPw(value);
                    } else if ("QN".equals(key)) {
                        dtu212Dto.setQn(value);
                    } else if ("ST".equals(key)) {
                        dtu212Dto.setSt(value);
                    } else if ("Flag".equals(key)) {
                        dtu212Dto.setFlag(value);
                    } else if ("PNUM".equals(key)) {
                        dtu212Dto.setPnum(value);
                    } else if ("PNO".equals(key)) {
                        dtu212Dto.setPno(value);
                    } else {
                    }
                }
            }

            String[] cps = cp.split("[,;]");
            Map<String, String> cpMap = new LinkedHashMap<>();
            for (String c : cps) {
                String[] arr = c.split("=");
                if (arr.length == 2) {
                    String key = arr[0];
                    String value = arr[1];
                    cpMap.put(key, value);
                }
            }
            if (cpMap.size() > 0) {
                dtu212Dto.setCp(cpMap);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dtu212Dto;
    }

    @Override
    public void write(Session session) {
        StringBuilder str = new StringBuilder();
        Dtu212Dto dtu212Dto = (Dtu212Dto) session.getData();
        str.append("QN=").append(dtu212Dto.getQn()).append(";");
        str.append("ST=").append("91").append(";");
        str.append("CN=").append("9014").append(";");
        str.append("PW=").append(dtu212Dto.getPw()).append(";");
        str.append("MN=").append(dtu212Dto.getMn()).append(";");
        str.append("Flag=").append("8").append(";");
        str.append("CP=").append("&&&&");
        String content = str.toString();
        byte[] bodyContent = content.getBytes();
        short checkCode = getCheckCode(bodyContent);
        String strCheckCode = String.format("%04x", checkCode);
        String strLen = String.format("%04d", bodyContent.length);
        String response = "##" + strLen + content + strCheckCode + "\r\n";
        // session.getChannel().writeAndFlush(response);
        session.write(response).block();
    }

    @Override
    public SubData subpackage(
            Object data,
            SessionManager sessionManager,
            Channel channel,
            String delimiter,
            ProtocolType protocolType) {
        Dtu212Dto dtu212Dto = (Dtu212Dto) data;
        dtu212Dto.setIp(channel.remoteAddress().toString());
        SubData subData1 = new SubData();
        subData1.setId(dtu212Dto.getMn());
        subData1.setChannelId(channel.id().asLongText());
        Session session = sessionManager.findByClientIdAndId(dtu212Dto.getMn(),channel.id().asLongText());
        if (session == null) {
            session =
                    new Session(
                            channel,
                            channel.remoteAddress(),
                            delimiter,
                            dtu212Dto.getMn(),
                            protocolType,
                            data);
            sessionManager.put(dtu212Dto.getMn(),channel.id().asLongText(),session);
        }
        session.setData(dtu212Dto);
        //分包操作
        if (dtu212Dto.getPnum() != null && dtu212Dto.getPno() != null) {
            int pnum = Integer.parseInt(dtu212Dto.getPnum());
            int pno = Integer.parseInt(dtu212Dto.getPno());
            String cn = dtu212Dto.getCn();
            Map<String, Object> subData = session.getSubData();
            Object obj = subData.get(cn);
            if (obj != null) {
                Dtu212Dto dtu212Dto1 = (Dtu212Dto) obj;
                Map<String, String> subCp = dtu212Dto1.getCp();
                Map<String, String> cp = dtu212Dto.getCp();
                subCp.putAll(cp);
                dtu212Dto1.setCp(subCp);
                subData.put(cn, subCp);
            } else {
                subData.put(cn, dtu212Dto);
            }
            session.setSubData(subData);
            if (pnum == pno) {
                Object o = subData.get(cn);
                subData1.setData(o);
                subData.remove(cn);
                return subData1;
            }
        } else {
            subData1.setData(dtu212Dto);
            return subData1;
        }
        return null;
    }
    @Override
    public void getCommand(Session session) {
        Dtu212Dto dtu212Dto = (Dtu212Dto) session.getData();
        if (session.isCommand()) {
            String command = (String) session.getCommandDown();
            if (command.equals(dtu212Dto.getCn())) {
                CommandSubject subject = session.getCommandSubject();
                if (subject != null) {
                    subject.notify(dtu212Dto);
                }
            }
            if ("9011".equals(dtu212Dto.getCn())) {
                Map<String, String> map = dtu212Dto.getCp();
                if (map.containsKey("QnRtn")) {
                    if (map.get("QnRtn").equals("1")) {
                        CommandSubject subject = session.getCommandSubject();
                        if (subject != null) {
                            subject.notify(dtu212Dto);
                        }
                    }
                }
            }
            if ("9012".equals(dtu212Dto.getCn())) {
                CommandSubject subject = session.getCommandSubject();
                if (subject != null) {
                    subject.notify(dtu212Dto);
                }
            }
        }
    }

    public static short getCheckCode(byte[] msg) {
        int crc = 0xffff;
        int dxs = 0xa001;
        int hibyte;
        int sbit;
        for (int i = 0; i < msg.length; i++) {
            hibyte = crc >> 8;
            crc = hibyte ^ (char) msg[i];
            for (int j = 0; j < 8; j++) {
                sbit = crc & 0x0001;
                crc = crc >> 1;
                if (sbit == 1) {
                    crc ^= dxs;
                }
            }
        }
        return (short) (crc & 0xffff);
    }
}
