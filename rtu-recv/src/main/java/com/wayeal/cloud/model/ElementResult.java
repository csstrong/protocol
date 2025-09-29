package com.wayeal.cloud.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @des 每一组要素值格式
 * @author jian
 * @version 2022-07-12 14:57
 */
public class ElementResult {
    /**
     * 时间
     */
    private String    f0;
    /**
     * 是否包含时间步长
     */
    private boolean stepFlag=false;
    /**
     * 步长值
     */
    private int   step=1;
    /**
     * 要素值
     */
    private Map<String,Object> value=new LinkedHashMap<>();


    public String getF0() {
        return f0;
    }

    public void setF0(String f0) {
        this.f0 = f0;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Map<String, Object> getValue() {
        return value;
    }

    public void setValue(Map<String, Object> value) {
        this.value = value;
    }

    public boolean isStepFlag() {
        return stepFlag;
    }

    public void setStepFlag(boolean stepFlag) {
        this.stepFlag = stepFlag;
    }
}
