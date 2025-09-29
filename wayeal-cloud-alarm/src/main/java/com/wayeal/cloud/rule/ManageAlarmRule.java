package com.wayeal.cloud.rule;

import com.wayeal.cloud.constant.AlarmModeEnum;
import com.wayeal.cloud.constant.SubsystemEnum;
import com.wayeal.cloud.dto.AlarmTypeDto;
import com.wayeal.cloud.dto.PipelineDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageAlarmRule {
    /**
     * 告警集合
     */
    private List<AlarmTypeDto> alarmTypeDtoList;
    /**
     * 因子集合
     */
    private List<AlarmTypeDto> factors;

    private PipelineDto pipelineDto;


    public ManageAlarmRule(List<AlarmTypeDto> factors) {
        this.factors = factors;
        init();
    }

    private void init() {
        if (factors == null) {
            return;
        }
        alarmTypeDtoList=new ArrayList<>();
        pipelineDto=new PipelineDto();
        for (AlarmTypeDto alarmTypeDto:factors){
            //没有告警类型
            if (AlarmModeEnum.wu.name().equals(alarmTypeDto.getStyle())){
                continue;
            }
            if (AlarmModeEnum.liganliuliang.name().equals(alarmTypeDto.getStyle()) ||
                AlarmModeEnum.liganliusu.name().equals(alarmTypeDto.getStyle()) ||
                AlarmModeEnum.liganshuiwei.name().equals(alarmTypeDto.getStyle())){
                //单位转换 m->cm
                if ( AlarmModeEnum.liganshuiwei.name().equals(alarmTypeDto.getStyle())){
                    String val= alarmTypeDto.getValue();
                    if (val!=null){
                        double d =Double.parseDouble(val);
                        double  d1= d*100;
                        alarmTypeDto.setValue(String.valueOf(d1));
                    }
                }
                PoleAlarmRule poleAlarmRule=new PoleAlarmRule(alarmTypeDto);
                alarmTypeDto= poleAlarmRule.doAlarm();
                alarmTypeDtoList.add(alarmTypeDto);
            }else if (AlarmModeEnum.didianya.name().equals(alarmTypeDto.getStyle())){
                PoleAlarmRule poleAlarmRule=new PoleAlarmRule(alarmTypeDto,true);
                alarmTypeDto= poleAlarmRule.doAlarm();
                alarmTypeDtoList.add(alarmTypeDto);
            }else if (AlarmModeEnum.guandaoliuliang.name().equals(alarmTypeDto.getStyle()) ||
                AlarmModeEnum.guandaoliusu.name().equals(alarmTypeDto.getStyle()) ||
                AlarmModeEnum.guandaoyewei.name().equals(alarmTypeDto.getStyle())){
                if (SubsystemEnum.pipeClog.equals(alarmTypeDto.getSubsystemEnum())){
                    if (AlarmModeEnum.guandaoliuliang.name().equals(alarmTypeDto.getStyle())){
                          pipelineDto.setFlow(alarmTypeDto.getValue());
                    }
                    if (AlarmModeEnum.guandaoliusu.name().equals(alarmTypeDto.getStyle())){
                        pipelineDto.setSpeed(alarmTypeDto.getValue());
                    }
                    if (AlarmModeEnum.guandaoyewei.name().equals(alarmTypeDto.getStyle())){
                        pipelineDto.setWaterLevel(alarmTypeDto.getValue());
                        //
                        pipelineDto.setWellType(alarmTypeDto.getWellType());
                        pipelineDto.setPipeline(alarmTypeDto.getPipeline());
                        pipelineDto.setSiteId(alarmTypeDto.getSiteId());
                        pipelineDto.setTime(alarmTypeDto.getTime());
                        pipelineDto.setCloggingAlarmConfDto(alarmTypeDto.getCloggingAlarmConfDto());
                        pipelineDto.setSiteName(alarmTypeDto.getSiteName());
                        pipelineDto.setGisPipeCode(alarmTypeDto.getGisPipeCode());
                        pipelineDto.setGisPipeType(alarmTypeDto.getGisPipeType());
                    }
                }
                PipelineAlarmRule pipelineAlarmRule=new PipelineAlarmRule(alarmTypeDto);
                alarmTypeDto= pipelineAlarmRule.doAlarm();
                alarmTypeDtoList.add(alarmTypeDto);
            }else if (AlarmModeEnum.yijingyewei.name().equals(alarmTypeDto.getStyle())){
                UndergroundAlarmRule undergroundAlarmRule=new UndergroundAlarmRule(alarmTypeDto);
                alarmTypeDto= undergroundAlarmRule.doAlarm();
                alarmTypeDtoList.add(alarmTypeDto);
            }else if (alarmTypeDto.getSubsystemEnum().equals(SubsystemEnum.drainage)){
                DrainageAlarmRule drainageAlarmRule=new DrainageAlarmRule(alarmTypeDto);
                alarmTypeDto=drainageAlarmRule.doAlarm();
                alarmTypeDtoList.add(alarmTypeDto);
            }
        }
    }

    public List<AlarmTypeDto> getAlarmTypeDtoList() {
        if (alarmTypeDtoList!=null){
           List<AlarmTypeDto> list= alarmTypeDtoList.stream().filter(item->{
                if (item.isAlarm()|| item.isLevelOne() || item.isLevelTwo()){
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
           return list;
        }
        return null;
    }

    public PipelineDto getPipelineDto() {
        return pipelineDto;
    }
}
