package usw.suwiki.common.event;

import lombok.RequiredArgsConstructor;

import java.util.Map;

public sealed interface Event permits Event.Webhook {

  @RequiredArgsConstructor
  sealed class Webhook implements Event {
    private final String message;

    public Map<String, String> getMessage() {
      return Map.of("content", this.message);
    }
  }

  non-sealed class Error extends Webhook {
    public Error(String message) {
      super(message);
    }
  }
}
