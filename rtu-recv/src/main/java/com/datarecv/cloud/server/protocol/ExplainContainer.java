package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.enums.IdentifierPilotEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @des 对于要素信息进行解释
 */
public class ExplainContainer {
    /** 因子名称 */
    private String factorName;
    /** 值 */
    private String value;
    /** 精度 */
    private Integer decimal;
    /** 步长值 */
    private Integer interval;

    public ExplainContainer(String factorName, String value) {
        this.factorName = factorName;
        this.value = value;
    }

    public ExplainContainer(String factorName, String value, Integer interval) {
        this.factorName = factorName;
        this.value = value;
        this.interval = interval;
    }

    public ExplainContainer(String factorName, String value, Integer interval, Integer decimal) {
        this.factorName = factorName;
        this.value = value;
        this.decimal = decimal;
        this.interval = interval;
    }

    public List<String> getExplainValList() {
        List<String> list;
        IdentifierPilotEnum identifierPilotEnum = IdentifierPilotEnum.getDataByValue(factorName);

        //
        if (identifierPilotEnum == null && interval == null) {
            List<String> strings = new ArrayList<>();
            strings.add(value);
            list = getVal(strings, null);
            return list;
        }
        // 如果字符串长度和定义的一样
        if (identifierPilotEnum != null && value.length() == identifierPilotEnum.getByteLength() * 2) {
            List<String> strings = new ArrayList<>();
            strings.add(value);
            list = getVal(strings, identifierPilotEnum);
            return list;
        }
        //
        if (interval != null) {
            int step = readTimeStep(interval);
            List<String> strings = stringToStringArray(value, value.length() / step);
            list = getVal(strings, identifierPilotEnum);
        } else {
            int byteLen = identifierPilotEnum.getByteLength() * 2;
            List<String> strings = stringToStringArray(value, byteLen);
            list = getVal(strings, identifierPilotEnum);
        }
        return list;
    }

    public List<String> getVal(List<String> list, IdentifierPilotEnum identifierPilotEnum) {
        List<String> res = new ArrayList<>();
        for (String val : list) {
            long data;
            if (identifierPilotEnum != null && identifierPilotEnum.isHex()) {
                data = Long.parseLong(val, 16);
            } else {
                data = Long.parseLong(val);
            }
            if (identifierPilotEnum != null) {
                int low3 = identifierPilotEnum.getDecimals();
                double d = 1;
                if (low3 > 0) {
                    for (int i = 0; i < low3; i++) {
                        d = d * 10;
                    }
                    res.add(String.valueOf(data / d));
                } else {
                    res.add(String.valueOf(data));
                }

            } else if (decimal != null) {
                double d = 1;
                if (decimal > 0) {
                    for (int i = 0; i < decimal; i++) {
                        d = d * 10;
                    }
                }
                res.add(String.valueOf(data / d));
            } else {
                res.add(String.valueOf(data));
            }
        }
        return res;
    }

    public static int readTimeStep(int timeStep) {
        if (timeStep <= 100) {
            int s = 60 / timeStep;
            return s;
        }
        if (timeStep <= 100 * 100) {
            int s = timeStep / 100;
            return 24 / s;
        }
        if (timeStep <= 100 * 100 * 100) {
            int s = timeStep / 100 * 100;
            return 365 / s;
        }
        return timeStep;
    }

    public static List<String> stringToStringArray(String src, int length) {
        // 检查参数是否合法
        if (null == src || src.equals("")) {
            return null;
        }
        if (length <= 0) {
            return null;
        }
        // 获取整个字符串可以被切割成字符子串的个数
        int n = (src.length() + length - 1) / length;
        List<String> split = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (i < (n - 1)) {
                split.add(src.substring(i * length, (i + 1) * length));
            } else {
                split.add(src.substring(i * length));
            }
        }
        return split;
    }

    public static  boolean isInvalid (String obj){

       return isInvalid(obj,obj.length());
    }

    public static  boolean isInvalid (String obj,int length){

        StringBuilder stringBuilder=new StringBuilder();
        for (int i = 0; i <length ; i++) {
            stringBuilder.append("f");
        }
        String ff= stringBuilder.toString();

        if (obj.equals(ff)){
            return true;
        }
        return false;
    }
}
