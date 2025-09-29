package com.wayeal.cloud.mongo.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_minute")
public class MinuteMonitorData implements Cloneable, Serializable {

    @Id
    private String _id;

    @Field("Id")
    private String id;

    @Field("Ip")
    private String ip;

    @Field("MonitorTime")
    private String monitorTime;

    @Field("StroageTime")
    private String stroageTime;

    @Field("ComponentVal")
    private JSONObject componentVal;

    public MinuteMonitorData deepCopy(Object object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (MinuteMonitorData) ois.readObject();
        } catch (Exception e) {
            throw new Error("deepCopy:" + e.getMessage());
        }
    }
}
