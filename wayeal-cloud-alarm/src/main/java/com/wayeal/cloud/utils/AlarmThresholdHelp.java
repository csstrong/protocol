package com.wayeal.cloud.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AlarmThresholdHelp {


    public static String alarmThreshold(JSONObject siteJson) {
        String sub = siteJson.getString("Subsystem");
        if ("pipeClog".equals(sub) || "pipeEnvironment".equals(sub) || "riverSystem".equals(sub)) {
            calculation(siteJson);
        }

        return siteJson.toJSONString();
    }

    public static void calculation(JSONObject siteJson) {

        //管径(mm)
        String pipeWidth = siteJson.getString("PipeWidth");
        //井深(m)
        String wellDepth = siteJson.getString("WellDepth");
        //井径(mm)
        String wellWidth = siteJson.getString("WellWidth");
        //管道坡度
        Double pipeSlope = siteJson.getDouble("PipeSlope");
        //管道材质
        String pipeMaterial = siteJson.getString("PipeType");

        int width = Integer.parseInt(pipeWidth);

        double maxFull = getMaxFull(width);

        double n = getRs(pipeMaterial);
        //弧度
        double radian = calculationRadian(maxFull);
        //断面面积
        double A = calculationSection(radian, width);
        //水利半径
        double R = calculationHydraulicRadius(radian, width);
        double one = 1;
        double two = 2;
        double three = 3;
        //最大流速
        double maxV = (1 / n) * Math.pow(R, two / three) * Math.pow(pipeSlope, one / two);
        //最大流量
        double maxQ = (1 / n) * A * Math.pow(R, two / three) * Math.pow(pipeSlope, one / two);

        siteJson.put("MaxV", maxV);

        siteJson.put("MaxQ", maxQ);

    }


    /**
     * 获取最大充满度
     *
     * @param width
     * @return
     */
    public static double getMaxFull(int width) {
        if (width <= 350) {
            return 0.5;
        } else if (width <= 500) {
            return 0.65;
        } else if (width <= 950) {
            return 0.7;
        } else {
            return 0.75;
        }
    }

    /**
     * 粗糙系数
     *
     * @param type
     * @return
     */
    public static double getRs(String type) {
        double res = 1;
        if ("1".equals(type)) {
            res = 0.014;
        } else if ("2".equals(type)) {
            res = 0.012;
        } else if ("3".equals(type)) {
            res = 0.012;
        } else if ("4".equals(type)) {
            res = 0.01;
        } else if ("5".equals(type)) {
            res = 0.03;
        } else if ("6".equals(type)) {
            res = 0.025;
        } else if ("7".equals(type)) {
            res = 0.017;
        } else if ("8".equals(type)) {
            res = 0.015;
        }

        return res;
    }

    /**
     * 计算幅度
     *
     * @return
     */
    public static double calculationRadian(double maxFull) {

        return Math.acos(1 - (2 * maxFull));
    }

    /**
     * 计算断面面积
     *
     * @return
     */
    public static double calculationSection(double radian, double width) {

        return (width * width) * (radian - 0.5 * Math.sin(2 * radian));
    }

    public static double calculationHydraulicRadius(double radian, double width) {

        return (0.5 * width) * (1 - Math.sin(2 * radian) / (2 * radian));
    }


}
