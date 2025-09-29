package com.wayeal.cloud.server.protocol;

import com.wayeal.cloud.enums.IdentifierPilotEnum;
import com.wayeal.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author jian
 * @version 2023-02-14 14:03
 */
public class SL651_32 implements ContentDecoderAbstract {

    private static final Logger log = LoggerFactory.getLogger(SL651_32.class);

    private List<ElementResult> list = new ArrayList<>();

    private Map<String, String> lastElement = new LinkedHashMap<>();

    public void read(ByteBuf buf) {
       // log.info("--------------{}", ByteBufUtil.hexDump(buf));
        // 定时报 --元素读取
       // boolean readFlag = false;
        ElementResult elementResult = new ElementResult();
        while (buf.readableBytes() - 3 > 0) {
            byte[] bytes = new byte[1];
            buf.readBytes(bytes);
            String hex = ByteBufUtil.hexDump(bytes).toUpperCase(Locale.ROOT);
           // log.debug(hex);
            //如果引导符为FF 那就跳过读取下一个标识符
            if("FF".equals(hex)){
                byte[] bytes1 = new byte[1];
                buf.readBytes(bytes1);
                String hex1 = ByteBufUtil.hexDump(bytes1).toUpperCase(Locale.ROOT);
                hex=hex+hex1;
            }
            if (hex.equals("F0")) {
                // 时间引导符-------------
                // 舍去数据标识符
                buf.readByte();
                // 读取时间 固定搭配5个字节
                byte[] bytes1 = new byte[5];
                buf.readBytes(bytes1);
                String f0Hex = ByteBufUtil.hexDump(bytes1);
               // log.debug(f0Hex);
                elementResult.setF0(f0Hex);
            } else if (hex.equals("04")) {
                // 时间步长码--------------
               // readFlag = true;
                // 舍去数据标识符
                buf.readByte();
                // 读取步长码 固定搭配3个字节
                byte[] bytes1 = new byte[3];
                buf.readBytes(bytes1);
                String o4Hex = ByteBufUtil.hexDump(bytes1);
                elementResult.setStep(Integer.parseInt(o4Hex));
                elementResult.setStepFlag(true);
              //  log.debug(o4Hex);
            } else {
                // 数据读取 在时间步长下的数据
                if (elementResult.isStepFlag()) {
                    byte[] bytes1 = new byte[1];
                    buf.readBytes(bytes1);
                    // 读取数据标识符
                    String id = ByteBufUtil.hexDump(bytes1);
                    int data10 = Integer.parseInt(id, 16);
                   // log.debug(String.valueOf(data10));
                    // 读取数据
                    byte[] data = new byte[data10];
                    buf.readBytes(data);
                    String hexData = ByteBufUtil.hexDump(data);
                    //log.debug(hexData);
                    Map<String,Object> valueMap= elementResult.getValue();
                    ExplainContainer explainContainer;
                    if (elementResult.isStepFlag()){
                        explainContainer= new ExplainContainer(hex,hexData,elementResult.getStep());
                    }else {
                        explainContainer= new ExplainContainer(hex,hexData);
                    }
                    List<String> strings= explainContainer.getExplainValList();
                    valueMap.put(hex,strings);
                    //-------------  ---------------
                    if (elementResult.getValue().size()>0) {
                        list.add(elementResult);
                        elementResult = new ElementResult();
                    }
                   // readFlag = false;
                } else {
                    // 读取非时间步长下的数据
                    byte bb       = buf.readByte();
                    int  high5    = bb >> 3 & 0x1f;
                    int  low3     = bb & 0x07;
                    byte[] bytes1 = new byte[high5];
                    buf.readBytes(bytes1);
                    String res="";
                    String hexData = ByteBufUtil.hexDump(bytes1);
                    if (ExplainContainer.isInvalid(hexData)){
                       res = hexData;
                    }else {
                        long data = Long.parseLong(hexData,10);
                        double d = 1;
                        if (low3 > 0) {
                            for (int i = 0; i < low3; i++) {
                                d = d * 10;
                            }
                        }
                        double d1 = data / d;
                        res = String.valueOf(d1);
                    }
                    IdentifierPilotEnum identifierPilotEnum = IdentifierPilotEnum.getDataByValue(hex);
                    if (identifierPilotEnum!=null && identifierPilotEnum.isCommonFactor()){
                        lastElement.put(hex, res);
                        if (elementResult.getValue().size() > 0) {
                            list.add(elementResult);
                            elementResult = new ElementResult();
                        }
                    }else {
                        if (elementResult.getF0()!=null) {
                            Map<String,Object> valueMap= elementResult.getValue();
                            List<String> arr=new ArrayList<>();
                            arr.add(res);
                            valueMap.put(hex,arr);
                        }
                    }

                   // log.debug(hexData);
                }
            }
        }
    }

    public List<ElementResult> getList() {
        return list;
    }

    public Map<String, String> getLastElement() {
        return lastElement;
    }

    @Override
    public void write() {}
}
