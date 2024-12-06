package com.kalado.common.exception;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    ErrorResponse errorResponse =
        new ErrorResponse(errorCode.getErrorCodeValue(), errorCode.name());
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }
}
