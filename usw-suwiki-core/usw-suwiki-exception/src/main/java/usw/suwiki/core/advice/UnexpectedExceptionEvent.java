package usw.suwiki.core.advice;

import usw.suwiki.common.event.Event;
import usw.suwiki.core.exception.Exceptions;

public class UnexpectedExceptionEvent extends Event.Error {

  public UnexpectedExceptionEvent(Throwable throwable) {
    super("@everyone " + Exceptions.simplify(throwable));
  }
}
