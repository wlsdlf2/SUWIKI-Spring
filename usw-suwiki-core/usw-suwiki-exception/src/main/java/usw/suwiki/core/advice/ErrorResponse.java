package usw.suwiki.core.advice;

import usw.suwiki.core.exception.BaseException;
import usw.suwiki.core.exception.ExceptionCode;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static usw.suwiki.core.exception.ExceptionCode.PARAMETER_VALIDATION_FAIL;

public record ErrorResponse(String code, String message, Integer status) {

  static ErrorResponse from(BaseException baseException) {
    return new ErrorResponse(baseException.getCode(), baseException.getMessage(), baseException.getHttpStatus());
  }

  static ErrorResponse parameter() {
    return new ErrorResponse(PARAMETER_VALIDATION_FAIL.getCode(), PARAMETER_VALIDATION_FAIL.getMessage(), PARAMETER_VALIDATION_FAIL.getStatus());
  }

  static ErrorResponse internal() {
    return new ErrorResponse(INTERNAL_SERVER_ERROR.name(), ExceptionCode.SERVER_ERROR.getMessage(), INTERNAL_SERVER_ERROR.value());
  }
}
