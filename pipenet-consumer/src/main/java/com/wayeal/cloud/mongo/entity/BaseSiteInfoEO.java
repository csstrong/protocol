package com.wayeal.cloud.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "base_site_info")
public class BaseSiteInfoEO {
    @Id
    private String _id;

    @Field("Id")
    private String id;

    @Field("Name")
    private String name;
}
