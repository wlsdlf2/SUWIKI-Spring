package usw.suwiki.core.mail;

/**
 * Email 발송을 관리하는 인터페이스입니다.
 *
 * @author hejow
 */
public interface EmailSender {
  /**
   * 이메일 발송에 필요한 부가 정보 담아서 전송합니다.
   *
   * @param to email address
   */
  void send(String to, MailType mailType, String param);

  /**
   * 부가 정보 없이 발송합니다.
   *
   * @param to       email address
   * @param mailType
   */
  void send(String to, MailType mailType);
}
