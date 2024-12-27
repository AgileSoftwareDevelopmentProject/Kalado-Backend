package com.kalado.gateway.configuration;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.response.ErrorResponse;
import com.kalado.gateway.exception.CustomGatewayException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {
  private final ObjectMapper objectMapper;

  @Override
  public Exception decode(String methodKey, Response response) {
    try {
      String body = getBodyAsString(response);
      log.debug("Received error response: {}", body);

      ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);

      ErrorCode errorCode = findErrorCodeByValue(errorResponse.getErrorCode());

      return new CustomGatewayException(
              errorCode,
              errorResponse.getMessage() != null ?
                      errorResponse.getMessage() :
                      errorCode.getErrorMessageValue()
      );

    } catch (IOException e) {
      log.error("Failed to decode error response", e);
      return createExceptionFromStatus(response.status(), response.reason());
    }
  }

  private ErrorCode findErrorCodeByValue(Integer errorCodeValue) {
    if (errorCodeValue == null) {
      return ErrorCode.INTERNAL_SERVER_ERROR;
    }

    return Arrays.stream(ErrorCode.values())
            .filter(code -> code.getErrorCodeValue() != null
                    && code.getErrorCodeValue().equals(errorCodeValue))
            .findFirst()
            .orElseGet(() -> {
              log.warn("No matching ErrorCode found for value: {}", errorCodeValue);
              return ErrorCode.INTERNAL_SERVER_ERROR;
            });
  }

  private CustomGatewayException createExceptionFromStatus(int httpStatus, String reason) {
    ErrorCode errorCode = Arrays.stream(ErrorCode.values())
            .filter(code -> code.getHttpStatus() != null
                    && code.getHttpStatus().value() == httpStatus)
            .findFirst()
            .orElse(ErrorCode.INTERNAL_SERVER_ERROR);

    return new CustomGatewayException(
            errorCode,
            reason != null ? reason : errorCode.getErrorMessageValue()
    );
  }

  private String getBodyAsString(Response response) throws IOException {
    if (response.body() == null) {
      return "";
    }
    try (var inputStream = response.body().asInputStream()) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}