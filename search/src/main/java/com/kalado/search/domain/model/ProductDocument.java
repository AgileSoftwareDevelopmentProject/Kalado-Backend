package com.kalado.search.domain.model;

import com.kalado.common.Price;
import com.kalado.common.enums.ProductStatus;
import lombok.*;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/settings.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {
    @Id
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "persian"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english")
            }
    )
    private String title;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "persian"),
            otherFields = {
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english")
            }
    )
    private String description;

    @Field(type = FieldType.Object)
    private Price price;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Integer)
    private Integer productionYear;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Keyword)
    private ProductStatus status;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Long)
    private Long sellerId;
}