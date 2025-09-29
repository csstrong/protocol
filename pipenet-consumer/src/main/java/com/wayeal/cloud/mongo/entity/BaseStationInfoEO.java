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
 * @date  2022/8/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "base_station_info")
public class BaseStationInfoEO {
    @Id
    private String _id;

    @Field("Id")
    private String id;

    @Field("Name")
    private String name;

}
