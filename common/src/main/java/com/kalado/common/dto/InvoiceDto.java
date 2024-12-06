package com.kalado.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Time;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDto {
  private String id;

  @JsonProperty("user_id")
  private long userId;

  @JsonProperty("order_id")
  private long orderId;

  @JsonProperty("create_time")
  private Time creationTime;
}
