package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.enums.IdentifierPilotEnum;
import com.datarecv.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SL651_34 implements ContentDecoderAbstract {

    private static final Logger log = LoggerFactory.getLogger(SL651_34.class);

    private List<ElementResult> list = new ArrayList<>();

    private Map<String, String> lastElement = new LinkedHashMap<>();

    @Override
    public void read(ByteBuf buf) {
        // 小时报 --元素读取
        ElementResult elementResult = new ElementResult();
        while (buf.readableBytes() - 3 > 0) {
            byte[] bytes = new byte[1];
            buf.readBytes(bytes);
            String hex = ByteBufUtil.hexDump(bytes).toUpperCase(Locale.ROOT);
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
                //log.debug(f0Hex);
                // -------------  ---------------
                if (elementResult.getValue().size() > 0) {
                    list.add(elementResult);
                    elementResult = new ElementResult();
                }
                elementResult.setF0(f0Hex);
            } else if (hex.equals("04")) {
                // 时间步长码--------------
                // 舍去数据标识符
                buf.readByte();
                // 读取步长码 固定搭配3个字节
                byte[] bytes1 = new byte[3];
                buf.readBytes(bytes1);
                String o4Hex = ByteBufUtil.hexDump(bytes1);
                elementResult.setStep(Integer.parseInt(o4Hex, 16));
                elementResult.setStepFlag(true);
                //log.debug(o4Hex);
            } else {
                // 数据读取 在时间步长下的数据
                byte bb = buf.readByte();
                // 读取数据标识符
                int high5 = bb >> 3 & 0x1f;
                int low3 = bb & 0x07;
                IdentifierPilotEnum identifierPilotEnum = IdentifierPilotEnum.getDataByValue(hex);
                if (high5 == 15 || high5 == 31 || high5==0) {
                    if (identifierPilotEnum != null) {
                        int aa = identifierPilotEnum.getByteLength();
                        if (elementResult.isStepFlag()) {
                            high5 = ExplainContainer.readTimeStep(elementResult.getStep()) * aa;
                        } else {
                            high5 = identifierPilotEnum.getByteLength() * 12;
                        }
                    }
                }else {
                    if (elementResult.isStepFlag()){
                        if (high5==12 || high5==24 || (identifierPilotEnum!=null && identifierPilotEnum.isCommonFactor())){

                        }else {
                            high5=high5*ExplainContainer.readTimeStep(elementResult.getStep());
                        }
                    }
                }
                // 读取数据
                byte[] data = new byte[high5];
                buf.readBytes(data);
                String hexData = ByteBufUtil.hexDump(data);
                ExplainContainer explainContainer;
                if (elementResult.isStepFlag()) {
                    explainContainer = new ExplainContainer(hex, hexData, elementResult.getStep(),low3);
                } else {
                    explainContainer = new ExplainContainer(hex, hexData,null,low3);
                }
                List<String> strings = explainContainer.getExplainValList();
                if (identifierPilotEnum != null && identifierPilotEnum.isCommonFactor()) {
                    lastElement.put(hex, strings.get(0));
                    if (elementResult.getValue().size() > 0) {
                        list.add(elementResult);
                        elementResult = new ElementResult();
                    }
                } else {
                    Map<String, Object> valueMap = elementResult.getValue();
                    valueMap.put(hex, strings);
                }
            }
        }
    }

    @Override
    public List<ElementResult> getList() {
        return list;
    }

    @Override
    public Map<String, String> getLastElement() {
        return lastElement;
    }

    @Override
    public void write() {}
}
