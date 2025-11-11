package com.datarecv.cloud.utils.silt_height_calculation;

import javafx.util.Pair;

import java.util.*;

public class SiltJudgment {
    /**
     * 多值淤堵高度计算
     *
     * @param pipeline    管道
     * @param q           实测流量
     * @param v           实测流速
     * @param liquidLevel 液位
     * @return 状态和淤堵高度
     */
    public Pair<SiltState, Double> judge(Pipeline pipeline, double q, double v, double liquidLevel) {
        Pair<SiltState, Double> result;

        List<List<Double>> siltAndQCurve = pipeline.getCurve(CurveType.Q, liquidLevel); // 淤堵高度和流量关系曲线
        List<List<Double>> siltAndVCurve = pipeline.getCurve(CurveType.V, liquidLevel); // 淤堵高度和流速关系曲线
        List<Double> siltList = siltAndQCurve.get(0); // 淤堵高度
        List<Double> qList = siltAndQCurve.get(1); // 流量
        List<Double> vList = siltAndVCurve.get(1); //  流速

        Double maxQ = Collections.max(qList); // 最大经验流量值
        Double minQ = Collections.min(qList); // 最小经验流量值
        Double maxV = Collections.max(vList); // 最大经验流速值
        Double minV = Collections.min(vList); // 最小经验流速值

        // 如果实测流量值或者实测速度大于经验值， 上报异常
        if (q > maxQ || v > maxV) {
            result = new Pair<>(SiltState.ABNORMAL, 0.0);
            return result;
        }

        // 如果实测流量值或者实测速度小于经验值， 上报淤堵严重异常
        if (q < minQ || v < minV) {
            result = new Pair<>(SiltState.SERIOUS, 0.0);
            return result;
        }

        List<Integer> estimateQ = new ArrayList<>(); // 估算Q对应点位
        List<Integer> estimateV = new ArrayList<>(); // 估算V对应点位
        for (int i = 0; i < siltList.size() - 1; i++) {
            if ((qList.get(i) < q && q <= qList.get(i + 1)) || (qList.get(i + 1) < q && q <= qList.get(i))) {
                estimateQ.add(i + 1);
            }
            if ((vList.get(i) < v && v <= vList.get(i + 1)) || (vList.get(i + 1) < v && v <= vList.get(i))) {
                estimateV.add(i + 1);
            }
        }

        // 如果Q值和V值对应点位均只有一个，选择淤堵高度小的值
        if (estimateQ.size() == 1 && estimateV.size() == 1) {
            double qSiltHeight = siltList.get(estimateQ.get(0));
            double vSiltHeight = siltList.get(estimateV.get(0));
            double siltHeight = Math.min(qSiltHeight, vSiltHeight);
            result = new Pair<>(SiltState.NORMAL, siltHeight);
            return result;
        }
        // 否则选择唯一值
        else if (estimateQ.size() == 2 && estimateV.size() == 1) {
            double vSiltHeight = siltList.get(estimateV.get(0));
            result = new Pair<>(SiltState.NORMAL, vSiltHeight);
            return result;
        } else if (estimateQ.size() == 1 && estimateV.size() == 2) {
            double qSiltHeight = siltList.get(estimateQ.get(0));
            result = new Pair<>(SiltState.NORMAL, qSiltHeight);
            return result;
        }
        // 如果有多值对应，选择距离最近
        else {
            // 添加候选预估淤堵高度
            List<Double> candidateValue = new ArrayList<>();
            for (Integer index : estimateQ) {
                candidateValue.add(siltList.get(index));
            }
            for (Integer index : estimateV) {
                candidateValue.add(siltList.get(index));
            }
            Collections.sort(candidateValue);
            // 计算距离最小索引
            int index = 0;
            double minDistance = Double.MIN_VALUE;
            for (int i = 0; i < candidateValue.size() - 1; i++) {
                if (candidateValue.get(i + 1) - candidateValue.get(i) <= minDistance) {
                    index = i;
                    minDistance = candidateValue.get(i + 1) - candidateValue.get(i);
                }
            }
            result = new Pair<>(SiltState.NORMAL, candidateValue.get(index));
            return result;
        }
    }

    /**
     * 单值淤堵高度计算
     *
     * @param pipeline    管道
     * @param value       实测值
     * @param liquidLevel 液位
     * @return 状态和淤堵高度
     */
    public Pair<SiltState, Double> judge(Pipeline pipeline, CurveType curveType, double value, double liquidLevel) {
        Pair<SiltState, Double> result;

        List<List<Double>> curve = pipeline.getCurve(curveType, liquidLevel); // 淤堵高度和实测值关系曲线
        List<Double> siltList = curve.get(0); // 淤堵高度
        List<Double> experienceValueList = curve.get(1); // 经验值
        Double maxValue = Collections.max(experienceValueList); // 最大经验值
        Double minValue = Collections.min(experienceValueList); // 最小经验值

        // 如果实测值大于经验值， 上报异常
        if (value > maxValue) {
            result = new Pair<>(SiltState.ABNORMAL, 0.0);
            return result;
        }
        // 如果实测值小于经验值， 上报淤堵严重异常
        if (value < minValue) {
            result = new Pair<>(SiltState.SERIOUS, 0.0);
            return result;
        }

        // 估算实测值对应点位
        List<Integer> estimateValue = new ArrayList<>();
        for (int i = 0; i < siltList.size() - 1; i++) {
            if ((experienceValueList.get(i) < value && value <= experienceValueList.get(i + 1))
                    || (experienceValueList.get(i + 1) < value && value <= experienceValueList.get(i))
            ) {
                estimateValue.add(i + 1);
            }
        }

        // 选取淤堵较小值
        List<Double> candidateValue = new ArrayList<>();
        for (int index : estimateValue) {
            candidateValue.add(siltList.get(index));
        }
        result = new Pair<>(SiltState.NORMAL, Collections.min(candidateValue));
        return result;
    }
}

