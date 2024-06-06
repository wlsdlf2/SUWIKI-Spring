package usw.suwiki.core.secure;

/**
 * 암호화를 담당하는 인터페이스입니다.
 *
 * @author hejow
 */
public interface Encoder {
  String encode(String input);

  boolean matches(CharSequence rawInput, String encoded);

  boolean nonMatches(CharSequence rawInput, String encoded);
}
