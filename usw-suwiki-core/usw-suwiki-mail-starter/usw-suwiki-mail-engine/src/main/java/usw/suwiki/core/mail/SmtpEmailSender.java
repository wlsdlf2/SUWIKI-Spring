package usw.suwiki.core.mail;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import usw.suwiki.core.exception.MailException;

import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static usw.suwiki.core.exception.ExceptionCode.BAD_MAIL_REQUEST;
import static usw.suwiki.core.exception.ExceptionCode.SEND_MAIL_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
class SmtpEmailSender implements EmailSender {
  private static final String SUBJECT = "수원대학교 강의평가 플랫폼 SUWIKI 입니다.";
  private static final String FROM = "no-reply@suwiki.kr";

  private final ServerProperties serverProperties;
  private final TemplateEngine templateEngine;
  private final JavaMailSender mailSender;

  @Async
  @Override
  public void send(String to, MailType mailType, String param) {
    validateParameter(param);
    String value = mailType.isEmailAuth() ? serverProperties.redirectUrl(param) : param;
    send(to, mailType, context -> context.setVariable(mailType.key(), value));
  }

  @Async
  @Override
  public void send(String to, MailType mailType) {
    send(to, mailType, context -> {});
  }

  private void send(String to, MailType mailType, Consumer<Context> option) {
    var context = new Context();
    option.accept(context);

    var message = mailSender.createMimeMessage();
    var messageHelper = new MimeMessageHelper(message, UTF_8.name());
    setContents(messageHelper, to, templateEngine.process(mailType.template(), context));

    try {
      mailSender.send(message);
      log.info("[Send Email] to : {}, type : {}", to, mailType.name());
    } catch (org.springframework.mail.MailException e) {
      throw new MailException(SEND_MAIL_FAILED);
    }
  }

  private void setContents(MimeMessageHelper messageHelper, String to, String text) {
    try {
      messageHelper.setTo(to);
      messageHelper.setFrom(FROM);
      messageHelper.setSubject(SUBJECT);
      messageHelper.setText(text, true);
    } catch (MessagingException e) {
      throw new MailException(SEND_MAIL_FAILED);
    }
  }

  private void validateParameter(String parameter) {
    if (parameter == null || parameter.isBlank()) {
      throw new MailException(BAD_MAIL_REQUEST);
    }
  }
}
