package com.datarecv.cloud.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.datarecv.cloud.constant.Constant;
import com.datarecv.cloud.constant.FactorType;
import com.datarecv.cloud.constant.MonitoringType;
import com.datarecv.cloud.constant.SubsystemEnum;
import com.datarecv.cloud.dao.*;
import com.datarecv.cloud.dto.*;
import com.datarecv.cloud.rabbit.RabbitMqSendUtil;
import com.datarecv.cloud.rule.ManageAlarmRule;
import com.datarecv.cloud.utils.AlarmThresholdHelp;
import com.datarecv.cloud.utils.silt_height_calculation.CurveType;
import com.datarecv.cloud.utils.silt_height_calculation.Pipeline;
import com.datarecv.cloud.utils.silt_height_calculation.SiltJudgment;
import com.datarecv.cloud.utils.silt_height_calculation.SiltState;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @des 数据处理
 */
@Service
@Slf4j
public class DataHandle {

    public static final String format = "当前%s为%s已超过%s级水质限值（%s），请及时处置！";

    public static final String clogging_format="%s淤积厚度已达%scm，管道负荷率为%s，%s";

    public static final String hydrology_format = "当前%s为%s已超过限值（%s），请注意！";

    private static ExecutorService threadPool = Executors.newFixedThreadPool(4);

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private FactorRepository factorRepository;
    @Autowired
    private StandardRepository standardRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private AlarmManagementRepository alarmManagementRepository;
    @Autowired
    private CloggingAlarmConfRepository cloggingAlarmConfRepository;
    @Autowired
    private DictionaryInfoRepository dictionaryInfoRepository;

    public void init(String s) {
        try {
            JSONObject json = JSON.parseObject(s);
            RecvDataDto recvDataDto = dataConvert(json);
            List<AlarmTypeDto> alarmTypeDtoList = doComponentVal(recvDataDto);
            Map<String, BaseAlarmManagementDto> baseAlarmManagementDtoMap = alarmManagementRepository.findById(
                recvDataDto.getSiteId());
            List<BaseAlarmManagementDto> alarmInfo = doAlarm(recvDataDto, alarmTypeDtoList, baseAlarmManagementDtoMap);
            //send alarm info to mq (queueName:cloud_notify)
            if (alarmInfo != null && alarmInfo.size() > 0) {
                for (BaseAlarmManagementDto baseAlarmManagementDto : alarmInfo) {
                    //消警的信息无需向App推送。0:未消警,1:已消警
                    String alarmOff = baseAlarmManagementDto.getAlarmOff();
                    if ("1".equals(alarmOff)) {
                        continue;
                    }
                    String info = JSONObject.toJSON(baseAlarmManagementDto).toString();
                    JSONObject jsonObject = JSONObject.parseObject(info);
                    RabbitMqSendUtil.sendMessage(jsonObject.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RecvDataDto dataConvert(JSONObject json) {

        RecvDataDto recvDataDto = new RecvDataDto();

        String id = json.getString("id");

        Map<String, JSONObject> allSiteInfo = siteRepository.getAllSiteInfo();
        if (!allSiteInfo.containsKey(id)) {
            return recvDataDto;
        }

        String time = json.getString("monitorTime");
        recvDataDto.setTime(time);
        recvDataDto.setSiteId(id);
        JSONObject siteJson = allSiteInfo.get(id);
        String subSystem = siteJson.getString(Constant.SUBSYSTEM);
        SubsystemEnum subsystemEnum = SubsystemEnum.valueOf(subSystem);
        recvDataDto.setSubsystemEnum(subsystemEnum);
        //
        MonitoringType monitoringType = executiveStandard(subsystemEnum);
        recvDataDto.setMonitoringType(monitoringType);
        //
        JSONObject comVal = json.getJSONObject("componentVal");
        recvDataDto.setComponentVal(comVal);
        //
        String waterClass = siteJson.getString(Constant.WATER_CLASS);
        recvDataDto.setWaterClass(waterClass);

        recvDataDto.setSiteName(siteJson.getString("Name"));

        recvDataDto.setMaxV(Optional.ofNullable(siteJson.getString("MaxV")).orElse(""));
        recvDataDto.setMaxQ(Optional.ofNullable(siteJson.getString("MaxQ")).orElse(""));
        recvDataDto.setMaxFull(Optional.ofNullable(siteJson.getString("MaxFull")).orElse(""));
        recvDataDto.setWellDepth(Optional.ofNullable(siteJson.getString("WellDepth")).orElse(""));
        //管径(mm)
        Double pipeWidth = Optional.ofNullable(siteJson.getDouble("PipeWidth")).orElse(0d) * 0.001;
        //管道坡度
        Double pipeSlope = Optional.ofNullable(siteJson.getDouble("PipeSlope")).orElse(0d);
        //管道材质
        String pipeMaterial = Optional.ofNullable(siteJson.getString("PipeType")).orElse("");

        JSONObject gis = siteJson.getJSONObject("GisInfoLine");
        if (gis != null) {
            recvDataDto.setGisPipeCode(gis.getString("NAME"));
            String LAYER = gis.getString("LAYER");
            if ("WSLINE".equals(LAYER)) {
                recvDataDto.setGisPipeType("污水管道");
            }
            if ("YSLINE".equals(LAYER)) {
                recvDataDto.setGisPipeType("雨水管道");
            }
        }
        recvDataDto.setWellType(Optional.ofNullable(siteJson.getString("WellType")).orElse(""));
        double n = AlarmThresholdHelp.getRs(pipeMaterial);
        Pipeline pipeline = new Pipeline(pipeWidth, n, pipeSlope, 0.01);
        recvDataDto.setPipeline(pipeline);

        return recvDataDto;
    }

    private List<AlarmTypeDto> doComponentVal(RecvDataDto recvDataDto) {
        List<AlarmTypeDto> alarmList = new ArrayList<>();
        if (!StringUtils.hasText(recvDataDto.getSiteId())) {
            return alarmList;
        }
        JSONObject jsonObject = recvDataDto.getComponentVal();
        List<AlarmTypeDto> hydrologyList = new ArrayList<>();
        Map<String, FactorDto> factors = factorRepository.findAll();
        Map<String, Map<String, List<StandardDto>>> standard = standardRepository.findQuality();
        JSONObject factorJson = equipmentRepository.findById(recvDataDto.getSiteId());
        // key : 因子编码 value:值
        for (String key : jsonObject.keySet()) {
            if (factors.containsKey(key)) {
                FactorDto factorType = factors.get(key);
                String value = jsonObject.getString(key);
                // 水质类
                if (factorType.getFactorType().equals(FactorType.quality)) {
                    // 水质类更具标准项限值产生报警，可产生多条报警
                    if (recvDataDto.getSubsystemEnum().equals(SubsystemEnum.pipeEnvironment)
                        || recvDataDto.getSubsystemEnum().equals(SubsystemEnum.sewagePlantSite)
                        || recvDataDto.getSubsystemEnum().equals(SubsystemEnum.drainage)
                        || recvDataDto.getSubsystemEnum().equals(SubsystemEnum.sewagePlant)
                        ||recvDataDto.getSubsystemEnum().equals(SubsystemEnum.riverSystem)) {
                        String[] isAlarm = doQualityByStand(key, value, recvDataDto.getWaterClass(),
                            recvDataDto.getMonitoringType(), standard);
                        if ("true".equals(isAlarm[0])) {
                            AlarmTypeDto alarmTypeDto = new AlarmTypeDto();
                            alarmTypeDto.setAlarm(true);
                            alarmTypeDto.setAlarmType("2");
                            alarmTypeDto.setValue(value);
                            alarmTypeDto.setFactorName(factorType.getName());
                            alarmTypeDto.setFactor(key);
                            alarmTypeDto.setSiteId(recvDataDto.getSiteId());
                            alarmTypeDto.setTime(recvDataDto.getTime());
                            String content = String.format(format, factorType.getName(), value + factorType.getUnit(),
                                recvDataDto.getWaterClass(),isAlarm[1]+ factorType.getUnit());
                            alarmTypeDto.setContent(content);
                            alarmList.add(alarmTypeDto);
                        }
                    } else {
                        // 水质类不根据标准项限值

                    }
                }
                // 水文类 或者通用类
                if (factorType.getFactorType().equals(FactorType.hydrology) || factorType.getFactorType().equals(
                    FactorType.universal)) {
                    AlarmTypeDto alarmTypeDto = new AlarmTypeDto();
                    alarmTypeDto.setFactor(key);
                    alarmTypeDto.setValue(value);
                    JSONObject jsonObject1 = factorJson.getJSONObject(key);
                    if (jsonObject1 != null) {
                        String alarmValue = jsonObject1.getString("alarmValue");
                        String levelOne = jsonObject1.getString("levelOne");
                        String levelTwo = jsonObject1.getString("levelTwo");
                        if (!StringUtils.hasText(alarmValue) && !StringUtils.hasText(levelOne) && !StringUtils.hasText(
                            levelTwo)) {
                            continue;
                        }
                        alarmTypeDto.setAlarmVal(alarmValue);
                        alarmTypeDto.setLevelOneVal(levelOne);
                        alarmTypeDto.setLevelTwoVal(levelTwo);
                        alarmTypeDto.setAlarm(true);
                        alarmTypeDto.setLevelOne(true);
                        alarmTypeDto.setLevelTwo(true);
                        alarmTypeDto.setStyle(jsonObject1.getString("AlarmType"));
                    }
                    alarmTypeDto.setSubsystemEnum(recvDataDto.getSubsystemEnum());
                    alarmTypeDto.setFactorName(factorType.getName());
                    alarmTypeDto.setSiteId(recvDataDto.getSiteId());
                    alarmTypeDto.setMaxQ(recvDataDto.getMaxQ());
                    alarmTypeDto.setMaxV(recvDataDto.getMaxV());
                    alarmTypeDto.setMaxFull(recvDataDto.getMaxFull());
                    alarmTypeDto.setWellDepth(recvDataDto.getWellDepth());
                    alarmTypeDto.setTime(recvDataDto.getTime());
                    alarmTypeDto.setUnit(factorType.getUnit());
                    if (SubsystemEnum.pipeClog.equals(alarmTypeDto.getSubsystemEnum())){
                        alarmTypeDto.setPipeline(recvDataDto.getPipeline());
                        CloggingAlarmConfDto cloggingAlarmConfDto= cloggingAlarmConfRepository.findAll();
                        alarmTypeDto.setCloggingAlarmConfDto(cloggingAlarmConfDto);
                        alarmTypeDto.setWellType(recvDataDto.getWellType());
                        alarmTypeDto.setSiteName(recvDataDto.getSiteName());
                        alarmTypeDto.setGisPipeType(recvDataDto.getGisPipeType());
                        alarmTypeDto.setGisPipeCode(recvDataDto.getGisPipeCode());
                    }
                    String alarmSuffix = "";
                    String alarmV = "";
                    if (value.compareTo(alarmTypeDto.getLevelOneVal()) > 0 && value.compareTo(alarmTypeDto.getLevelTwoVal()) < 0) {
                        alarmSuffix = "一级预警值";
                        alarmV = alarmTypeDto.getLevelOneVal();
                        alarmTypeDto.setAlarmType("0");
                    } else if (value.compareTo(alarmTypeDto.getLevelTwoVal()) > 0 && value.compareTo(alarmTypeDto.getAlarmVal()) < 0) {
                        alarmSuffix = "二级预警值";
                        alarmV = alarmTypeDto.getLevelTwoVal();
                        alarmTypeDto.setAlarmType("1");
                    } else if (value.compareTo(alarmTypeDto.getAlarmVal()) > 0) {
                        alarmSuffix = "报警值";
                        alarmV = alarmTypeDto.getAlarmVal();
                        alarmTypeDto.setAlarmType("2");
                    }
                    String content = String.format(hydrology_format, factorType.getName(), value + factorType.getUnit(),
                        alarmSuffix + alarmV + factorType.getUnit());
                    alarmTypeDto.setContent(content);
                    hydrologyList.add(alarmTypeDto);
                }
            }
        }
        if (hydrologyList.size() > 0) {
            ManageAlarmRule manageAlarmRule = new ManageAlarmRule(hydrologyList);
            List<AlarmTypeDto> hyAlarmList = manageAlarmRule.getAlarmTypeDtoList();
            executePipeClog(manageAlarmRule.getPipelineDto());
            if (hyAlarmList != null) {
                alarmList.addAll(hyAlarmList);
            }
        }
        return alarmList;
    }

    private  List<BaseAlarmManagementDto> doAlarm(RecvDataDto recvDataDto, List<AlarmTypeDto> alarmTypeDtoList, Map<String, BaseAlarmManagementDto> alarmManagementDtoMap) throws ParseException {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = sdf.format(date);
        List<BaseAlarmManagementDto> old = new ArrayList<>();
        List<BaseAlarmManagementDto> newAlarm = new ArrayList<>();
        Date eDate = sdf.parse(recvDataDto.getTime());
        List<String> factors=new ArrayList<>();
        for (AlarmTypeDto alarmTypeDto : alarmTypeDtoList) {
            String factor = alarmTypeDto.getFactor();
            if (alarmManagementDtoMap.containsKey(factor)) {
                factors.add(factor);
                // 处理旧的报警
                BaseAlarmManagementDto b = alarmManagementDtoMap.get(factor);
                String sTime = b.getStartTime();
                Date sDate = sdf.parse(sTime);
                //比较时间 --防止有补传数据
                if (eDate.before(sDate)) {
                    continue;
                }
                // 折算成小时
                double dur = (eDate.getTime() - sDate.getTime()) / ((double) 1000 * 60 * 60);
                if (dur == 0d) {
                    continue;
                }
                // 产生一条新报警，然后关闭旧的报警
                BaseAlarmManagementDto alarmManagementDto = createNewAlarm(alarmTypeDto, time, factor);
                newAlarm.add(alarmManagementDto);

                b.setDuration(String.valueOf(dur));
                // 关闭告警
                b.setEndTime(alarmTypeDto.getTime());
                b.setAlarmOff("1");
                b.setUpdateTime(time);
                old.add(b);
                continue;
            }
            BaseAlarmManagementDto alarmManagementDto = createNewAlarm(alarmTypeDto, time, factor);
            newAlarm.add(alarmManagementDto);
        }
        for (String key:alarmManagementDtoMap.keySet()){
            boolean flag= factors.stream().anyMatch(key::equals);
            if (!flag){
                // 处理旧的报警
                BaseAlarmManagementDto b = alarmManagementDtoMap.get(key);
                String sTime = b.getStartTime();
                Date sDate = sdf.parse(sTime);
                //比较时间 --防止有补传数据
                if (eDate.before(sDate)) {
                    continue;
                }
                // 折算成小时
                double dur = (eDate.getTime() - sDate.getTime()) / ((double) 1000 * 60 * 60);
                if (dur == 0d) {
                    continue;
                }
                b.setDuration(String.valueOf(dur));
                // 关闭告警
                b.setEndTime(recvDataDto.getTime());
                b.setAlarmOff("1");
                b.setUpdateTime(time);
                old.add(b);
            }
        }
        if (old.size() > 0) {
            alarmManagementRepository.update(old);
        }
        if (newAlarm.size() > 0) {
            alarmManagementRepository.save(newAlarm);
            return newAlarm;
        }
        return null;
    }

    // 新产生报警
    private BaseAlarmManagementDto createNewAlarm(AlarmTypeDto alarmTypeDto, String time, String factor) {

        BaseAlarmManagementDto alarmManagementDto = new BaseAlarmManagementDto();
        String id = alarmTypeDto.getSiteId() + "_" + alarmTypeDto.getTime() + "_" + alarmTypeDto.getFactor() + "_"
            + alarmTypeDto.getAlarmType();
        alarmManagementDto.setId(id);
        alarmManagementDto.setSiteId(alarmTypeDto.getSiteId());
        alarmManagementDto.setAlarmOff("0");
        alarmManagementDto.setCreateTime(time);
        alarmManagementDto.setComponentId(factor);
        alarmManagementDto.setStartTime(alarmTypeDto.getTime());
        alarmManagementDto.setReason("监测自动报警");
        //
        alarmManagementDto.setType(alarmTypeDto.getAlarmType());
        alarmManagementDto.setContent(alarmTypeDto.getContent());
        alarmManagementDto.setCloggingAlarm(false);
        return alarmManagementDto;
    }
    // 新产生报警
    public  BaseAlarmManagementDto createNewCloggingAlarm(PipelineDto pipelineDto, String level, double value) {

        BaseAlarmManagementDto alarmManagementDto = new BaseAlarmManagementDto();
        String id = pipelineDto.getSiteId() + "_" + pipelineDto.getTime() + "_" +"yd" + "_" + level;
        alarmManagementDto.setId(id);
        alarmManagementDto.setSiteId(pipelineDto.getSiteId());
        alarmManagementDto.setAlarmOff("0");
        alarmManagementDto.setCreateTime(DateTime.now().toString("yyyy/MM/dd HH:mm:ss"));
        alarmManagementDto.setComponentId("yd");
        alarmManagementDto.setStartTime(pipelineDto.getTime());
        alarmManagementDto.setReason("淤堵自动报警");
        alarmManagementDto.setSiteName(pipelineDto.getSiteName());
        //
        alarmManagementDto.setType(level);
        alarmManagementDto.setCloggingAlarm(true);
        alarmManagementDto.setHandleType("incomplete");
        alarmManagementDto.setIsRecall("false");
        alarmManagementDto.setRecallNum("0");

        double m= value*pipelineDto.getPipeline().getD()*100;
        String m1=String.format("%.2f", m);
        String v1=String.format("%.2f",value*100);
        String warn="";
        if ("0".equals(level)){
            warn="请注意！";
        }else if ("1".equals(level)){
            warn="请及时清淤！";
        }else if ("2".equals(level)){
            warn="请及时清淤！";
        }
        String content=String.format(clogging_format,pipelineDto.getGisPipeCode()+pipelineDto.getGisPipeType(),m1,v1+"%",warn);
        alarmManagementDto.setValue(m1);
        alarmManagementDto.setUnit("cm");
        alarmManagementDto.setLoadRate(v1+"%");
        alarmManagementDto.setContent(content);
        return alarmManagementDto;
    }
    /**
     * 执行水质类型 根据标准项限值
     */
    private  String[] doQualityByStand(String key, String value, String waterClass, MonitoringType monitoringType, Map<String, Map<String, List<StandardDto>>> standard) {
        String[] res={"false",""};
        if (standard.containsKey(monitoringType.name())) {
            Map<String, List<StandardDto>> standById = standard.get(monitoringType.name());
            if (standById.containsKey(key)) {
                List<StandardDto> standList = standById.get(key);
                List<StandardDto> oneStand = standList.stream().filter(
                    item -> waterClass.equals(item.getLevel())).collect(Collectors.toList());

                for (StandardDto standardDto : oneStand) {
                    double val = Double.parseDouble(value);
                    if (val > standardDto.getValue()) {
                        // 产生报警
                        res[0]="true";
                        res[1]= String.valueOf(standardDto.getValue());
                        return res;
                    }
                }
            }
        }
        return res;
    }

    private MonitoringType executiveStandard(SubsystemEnum subsystemEnum) {
        MonitoringType monitoringType = null;
        switch (subsystemEnum) {
            case pipeClog:
            case cityProne:
            case cityManhole:
            case riverSystem:
            case pipeEnvironment:
            case sewagePlant:
            case drainage:
                monitoringType = MonitoringType.StandarLimit;
                break;
            case sewagePlantSite:
                monitoringType = MonitoringType.StandarLimitStation;
                break;
        }
        return monitoringType;
    }
    public  void executePipeClog(PipelineDto pipelineDto) {
        threadPool.execute(() -> {
            log.info("=====================执行开始============================");
            SiltJudgment siltJudgment = new SiltJudgment();
            Pipeline pipeline = pipelineDto.getPipeline();
            String flow= pipelineDto.getFlow();
            String speed=pipelineDto.getSpeed();
            String level=pipelineDto.getWaterLevel();
            Pair<SiltState, Double> result=null;
            if (StringUtils.hasText(flow) && StringUtils.hasText(speed)){
                double flow1 =Double.parseDouble(flow);
                double speed1 = Double.parseDouble(speed);
                double level1 = Double.parseDouble(level);
                result= siltJudgment.judge(pipeline,flow1,speed1,level1);
            }else if (StringUtils.hasText(flow)){
                double flow1 =Double.parseDouble(flow);
                double level1 = Double.parseDouble(level);
                result= siltJudgment.judge(pipeline, CurveType.V,flow1,level1);
            }else if (StringUtils.hasText(speed)){
                double speed1 = Double.parseDouble(speed);
                double level1 = Double.parseDouble(level);
                result=siltJudgment.judge(pipeline, CurveType.Q,speed1,level1);
            }
            if (result!=null){
                SiltState siltState = result.getKey();
                if (siltState.equals(SiltState.NORMAL)){
                    double value= result.getValue();
                    CloggingAlarmConfDto cloggingAlarmConfDto= pipelineDto.getCloggingAlarmConfDto();
                    BaseAlarmManagementDto baseAlarmManagementDto=null;
                    if (cloggingAlarmConfDto!=null){
                        if (value>cloggingAlarmConfDto.getAlarmValue()){
                            baseAlarmManagementDto=createNewCloggingAlarm(pipelineDto,"2",value);
                        }else if (value>cloggingAlarmConfDto.getLevelTwo()){
                            baseAlarmManagementDto=createNewCloggingAlarm(pipelineDto,"1",value);
                        }else if (value>cloggingAlarmConfDto.getLevelOne()){
                            baseAlarmManagementDto= createNewCloggingAlarm(pipelineDto,"0",value);
                        }
                    }
                    if (baseAlarmManagementDto!=null){
                         alarmManagementRepository.replace(baseAlarmManagementDto);
                    }
                }
                log.info("=====================执行结束============================");
            }
            log.info("result==============={}",result);
        });
    }
}
