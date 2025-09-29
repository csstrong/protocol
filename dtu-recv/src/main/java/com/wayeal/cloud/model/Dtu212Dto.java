package com.wayeal.cloud.model;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class Dtu212Dto extends Message implements Serializable {

    private String qn;

    private String pnum;

    private String pno;

    private String st;

    private String cn;

    private String pw;

    private String mn;

    private String flag;

    private Map<String,String> cp;

    @Override
    public String toString() {
        StringBuilder stringBuilder=new StringBuilder();
        // "QN=" + qn + "ST=" + st +"CN=" + cn+ "PW=" + pw + "MN=" + mn + "Flag="+ flag
        if (qn!=null){
            stringBuilder.append("QN=").append(qn).append(";");
        }
        if (st!=null){
            stringBuilder.append("ST=").append(st).append(";");
        }
        if (cn!=null){
            stringBuilder.append("CN=").append(cn).append(";");
        }
        if (pw!=null){
            stringBuilder.append("PW=").append(pw).append(";");
        }
        if (flag!=null){
            stringBuilder.append("Flag=").append(flag).append(";");
        }
        if (pno!=null){
            stringBuilder.append("PNO=").append(pno).append(";");
        }
        if (pnum!=null){
            stringBuilder.append("PNUM=").append(pnum).append(";");
        }
        if (cp!=null){
            StringBuilder cps=new StringBuilder();
            for (Map.Entry<String,String> entry: cp.entrySet()){
                String key =entry.getKey();
                String val =entry.getValue();
                cps.append(key+"=").append(val).append(",");
            }
            String cpStr= cps.toString();
            cpStr.substring(0, cpStr.length() - 1);
            stringBuilder.append("CP=").append(cpStr);
        }
        return stringBuilder.toString();
    }
}
