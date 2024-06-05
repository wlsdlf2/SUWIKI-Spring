package usw.suwiki.core.exception;

public class AccountException extends BaseException {

  public AccountException(ExceptionCode exceptionCode) {
    super(exceptionCode);
  }

  public AccountException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode, message);
  }
}
