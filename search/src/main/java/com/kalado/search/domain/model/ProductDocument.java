package com.kalado.search.domain.model;

import com.kalado.common.Price;
import com.kalado.common.enums.ProductStatus;
import lombok.*;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import java.time.LocalDateTime;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/settings.json")
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

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @Field(type = FieldType.Object)
    private GeoPoint location;

    public void setLocationFromString(String location) {
        if (location != null && !location.trim().isEmpty()) {
            String[] coordinates = location.split(",");
            if (coordinates.length == 2) {
                try {
                    double lat = Double.parseDouble(coordinates[0]);
                    double lon = Double.parseDouble(coordinates[1]);
                    this.location = new GeoPoint(lat, lon);
                } catch (NumberFormatException e) {
                }
            }
        }
    }
}