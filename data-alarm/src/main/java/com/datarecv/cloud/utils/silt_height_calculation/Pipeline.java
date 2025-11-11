package com.datarecv.cloud.utils.silt_height_calculation;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import java.util.ArrayList;
import java.util.List;

public class Pipeline {
    private double d; // 直径 (m)
    private double roughness; // 粗糙度系数
    private double slope;
    private double interval; // 增量间隔 (m) 默认0.01

    public Pipeline(double d, double roughness, double slope, double interval) {
        this.d = d;
        this.roughness = roughness;
        this.slope = slope;
        this.interval = interval;
    }

    /**
     * 计算淤堵和流量(流速)关系曲线
     *
     * @param curveType   曲线类型
     * @param liquidLevel 液位
     * @return 淤堵和流量(流速)关系曲线
     */
    public List<List<Double>> getCurve(CurveType curveType, double liquidLevel) {
        double siltHeight = 0; // 淤堵高度（初始为0）
        List<List<Double>> curve = new ArrayList<>(); // 淤堵和流量(流速)关系曲线
        List<Double> siltList = new ArrayList<>(); // 淤堵高度递增数组
        List<Double> experienceValueList = new ArrayList<>(); // 经验值变化数组
        double phi = 2 * Math.asin(Math.pow(liquidLevel / this.d, 0.5)); // 液位充满半角弧度
        double experienceValue; // 经验公式计算值
        // 当淤堵高度小于液位时， 迭代计算经验流速值
        while (siltHeight < liquidLevel) {
            double theta = 2 * Math.asin(Math.pow(siltHeight / this.d, 0.5)); // 淤堵对应半角弧度
            switch (curveType) {
                case Q:
                    experienceValue = this.calculateQ(phi, theta);
                    break;
                case V:
                    experienceValue = this.calculateV(phi, theta);
                    break;
                default:
                    throw new ValueException("Curve Type Error");
            }
            siltList.add(siltHeight);
            experienceValueList.add(experienceValue);
            siltHeight += this.interval;
        }
        curve.add(siltList);
        curve.add(experienceValueList);
        return curve;
    }

    /**
     * 计算流量
     *
     * @param phi   # 液位充满半角弧度
     * @param theta # 淤堵对应半角弧度
     * @return 流量
     */
    public double calculateQ(double phi, double theta) {
        double area = this.calculateArea(phi, theta); // 水流横截面积
        double hydraulicRadius = calculateHydraulicRadius(phi, theta); // 水力半径
        return 1 / this.roughness * area * Math.pow(hydraulicRadius, 2.0 / 3) * Math.pow(slope, 0.5);
    }

    /**
     * 计算流速
     *
     * @param phi   # 液位充满半角弧度
     * @param theta # 淤堵对应半角弧度
     * @return 流速
     */
    public double calculateV(double phi, double theta) {
        double hydraulicRadius = calculateHydraulicRadius(phi, theta); // 水力半径
        return 1 / this.roughness * Math.pow(hydraulicRadius, 2.0 / 3) * Math.pow(slope, 0.5);
    }

    /**
     * 计算水流横截面积
     *
     * @param phi   # 液位充满半角弧度
     * @param theta # 淤堵对应半角弧度
     * @return 流速
     */
    public double calculateArea(double phi, double theta) {
        double x = (2 * phi - 2 * theta + Math.sin(2 * theta) - Math.sin(2 * phi));
        return Math.pow(this.d, 2.0) / 8 * (2 * phi - 2 * theta + Math.sin(2 * theta) - Math.sin(2 * phi));
    }

    /**
     * 计算水力半径
     *
     * @param phi   # 液位充满半角弧度
     * @param theta # 淤堵对应半角弧度
     * @return 水力半径
     */
    public double calculateHydraulicRadius(double phi, double theta) {
        double hydraulicRadius;
        if (theta == 0) {
            hydraulicRadius = this.d / 4 * (1 - Math.sin(2 * phi) / 2 / phi);
        } else {
            hydraulicRadius = this.d / 4 * (Math.sin(2 * theta) / 2 / theta - Math.sin(2 * phi) / 2 / phi) + 2 * this.d * Math.sin(theta);
        }
        return hydraulicRadius;
    }


    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getRoughness() {
        return roughness;
    }

    public void setRoughness(double roughness) {
        this.roughness = roughness;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }
}
