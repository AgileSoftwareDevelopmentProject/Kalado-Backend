package com.kalado.gateway.exception;

import com.kalado.common.enums.ErrorCode;
import com.kalado.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerAdvice {

  @ExceptionHandler(CustomGatewayException.class)
  public ResponseEntity<ErrorResponse> handleCustomGatewayException(CustomGatewayException ex) {
    log.error("Gateway exception occurred: {}", ex.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode().getErrorCodeValue(),
            ex.getMessage()
    );

    return new ResponseEntity<>(
            errorResponse,
            ex.getErrorCode().getClientHttpStatus()
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unexpected error occurred", ex);

    ErrorResponse errorResponse = new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR.getErrorCodeValue(),
            ErrorCode.INTERNAL_SERVER_ERROR.getErrorMessageValue()
    );

    return new ResponseEntity<>(
            errorResponse,
            ErrorCode.INTERNAL_SERVER_ERROR.getClientHttpStatus()
    );
  }
}