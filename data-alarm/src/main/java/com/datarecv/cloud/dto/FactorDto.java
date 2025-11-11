package com.datarecv.cloud.dto;

import com.datarecv.cloud.constant.FactorType;

public class FactorDto {

    private FactorType factorType;

    private String name;

    private String unit;

    public FactorType getFactorType() {
        return factorType;
    }

    public void setFactorType(FactorType factorType) {
        this.factorType = factorType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
