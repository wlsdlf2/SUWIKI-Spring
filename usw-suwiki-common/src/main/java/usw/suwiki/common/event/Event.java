package usw.suwiki.common.event;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import static usw.suwiki.common.event.Event.Webhook;

public sealed interface Event permits Webhook {

  @RequiredArgsConstructor
  non-sealed class Webhook implements Event {
    private final String message;

    public Map<String, String> getMessage() {
      return Map.of("content", this.message);
    }
  }
}
