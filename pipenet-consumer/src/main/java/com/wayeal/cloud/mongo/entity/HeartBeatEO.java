package com.wayeal.cloud.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/*
 * @author  chensi
 * @date  2022/9/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "heartbeat_info")
public class HeartBeatEO {
    @Id
    private String _id;

    @Field("SiteId")
    private String siteId;

    @Field("UpdateTime")
    private String updateTime;

    @Field("Status")
    private String status;
}
