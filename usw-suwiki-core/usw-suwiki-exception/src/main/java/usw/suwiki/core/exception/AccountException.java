package usw.suwiki.core.exception;

public class AccountException extends BaseException {

  public AccountException(ExceptionType exceptionType) {
    super(exceptionType);
  }

  public AccountException(ExceptionType exceptionType, String message) {
    super(exceptionType, message);
  }
}
