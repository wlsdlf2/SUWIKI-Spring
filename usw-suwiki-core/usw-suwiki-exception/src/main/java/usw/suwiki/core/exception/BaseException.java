package usw.suwiki.core.exception;

public class BaseException extends RuntimeException {
  private final ExceptionType exceptionType;

  public BaseException(ExceptionType exceptionType) {
    super(exceptionType.getMessage());
    this.exceptionType = exceptionType;
  }

  public BaseException(ExceptionType exceptionType, String message) {
    super(message);
    this.exceptionType = exceptionType;
  }

  public String getCode() {
    return this.exceptionType.getCode();
  }

  public int getHttpStatus() {
    return this.exceptionType.getStatus();
  }
}
