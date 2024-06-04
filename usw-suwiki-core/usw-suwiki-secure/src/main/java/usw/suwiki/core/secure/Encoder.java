package usw.suwiki.core.secure;

public interface Encoder {
  String encode(String input);

  boolean matches(CharSequence rawInput, String encoded);
}
