package usw.suwiki.core.exception;

public class BaseException extends RuntimeException {
  private final ExceptionCode exceptionCode;

  public BaseException(ExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
  }

  public BaseException(ExceptionCode exceptionCode, String message) {
    super(message);
    this.exceptionCode = exceptionCode;
  }

  public String getCode() {
    return this.exceptionCode.getCode();
  }

  public int getHttpStatus() {
    return this.exceptionCode.getStatus();
  }
}
