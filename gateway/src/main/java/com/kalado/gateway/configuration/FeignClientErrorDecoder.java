package com.kalado.gateway.configuration;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.response.ErrorResponse;
import com.kalado.gateway.exception.CustomGatewayException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class FeignClientErrorDecoder implements ErrorDecoder {
  private final ObjectMapper objectMapper;
  @Override
  public Exception decode(String methodKey, Response response) {
    String body = getBodyAsString(response);
    ErrorResponse errorResponse;
      try {
           errorResponse = objectMapper.readValue(body, ErrorResponse.class);
      } catch (JsonProcessingException e) {
          return new CustomGatewayException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
      ErrorCode errorCode = mapResponseBodyToErrorCode(errorResponse.getMessage());

    if (errorCode != null) {
      return new CustomGatewayException(errorCode);
    }

    return new Exception("Unexpected error: " + response.reason());
  }

  private String getBodyAsString(Response response) {
    try {
      if (response.body() != null) {
        return new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
      }
      return "No response body available";
    } catch (IOException e) {
      return "Unable to read response body";
    }
  }

  private ErrorCode mapResponseBodyToErrorCode(String body) {
    for (ErrorCode errorCode : ErrorCode.values()) {
      if (body.equals(errorCode.name())) {
        return errorCode;
      }
    }
    return null;
  }
}
