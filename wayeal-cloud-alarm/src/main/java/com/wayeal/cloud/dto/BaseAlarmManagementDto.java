package com.wayeal.cloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "base_alarm_management")
public class BaseAlarmManagementDto implements Cloneable, Serializable {

    @Id
    private String _id;

    @Field("Id")
    private String id;

    @Field("Content")
    private String content;

    @Field("CreateTime")
    private String createTime;

    @Field("Duration")
    private String duration;

    @Field("EndTime")
    private String endTime;

    @Field("IsHandle")
    private String isHandle;

    @Field("Reason")
    private String reason;

    @Field("StartTime")
    private String startTime;

    @Field("UpdateTime")
    private String updateTime;

    @Field("Type")
    private String type;

    @Field("ComponentId")
    private String componentId;

    @Field("HandleType")
    private String handleType;

    @Field("IsRecall")
    private String isRecall;

    @Field("RecallNum")
    private String recallNum;

    @Field("AlarmOff")
    private String alarmOff;

    @Field("IsCloggingAlarm")
    private boolean isCloggingAlarm;

    @Field("IssueFlag")
    private String issueFlag;

    @Field("Value")
    private String value;

    @Field("LoadRate")
    private String loadRate;

    @Field("Unit")
    private String unit;

    @Field("SiteId")
    private String siteId;

    @Field("SiteName")
    private String siteName;

    @Field("Imei")
    private String imei;

    //报警角度
    @Field("AlarmAngle")
    private String alarmAngle;

    //报警类型：0 报警恢复；1 倾斜报警；2 翻开报警
    @Field("AlarmType")
    private String alarmType;
    // 备注
    @Field("Remarks")
    private String remarks;

}
