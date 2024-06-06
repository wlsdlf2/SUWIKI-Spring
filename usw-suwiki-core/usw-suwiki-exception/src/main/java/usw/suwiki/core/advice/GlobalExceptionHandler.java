package usw.suwiki.core.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import usw.suwiki.core.exception.BaseException;
import usw.suwiki.core.exception.MailException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception) {
    log.warn("[Base exception] code : {}, message : {}", exception.getCode(), exception.getMessage());

    return ResponseEntity.status(exception.getHttpStatus()).body(ErrorResponse.from(exception));
  }

  @ExceptionHandler({
    Exception.class,
    MailException.class
  })
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleException(Exception exception) {
    log.error("[Unexpected Exception] exception : {}, message : {}", exception.getClass().getName(), exception.getMessage());
    // todo: webhook 추가하기
    return ErrorResponse.internal();
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleRequestValidationFailException(HttpServletRequest request) {
    log.warn("[Parameter validation fail] on : {}", request.getRequestURI());

    return ErrorResponse.parameter();
  }
}
