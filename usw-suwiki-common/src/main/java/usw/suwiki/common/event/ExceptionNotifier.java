package usw.suwiki.common.event;

public interface ExceptionNotifier { // todo: (06.12) common 모듈 처우 고민 후 이후 삭제
  void notify(Throwable throwable);
}
