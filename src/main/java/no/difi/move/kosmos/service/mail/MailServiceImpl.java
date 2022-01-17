package no.difi.move.kosmos.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.config.MailProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final KosmosProperties properties;

    @Override
    public void sendMail(String subject, String content) {
        MailProperties mail = properties.getMail();
        if (mail == null) {
            return;
        }
        Objects.requireNonNull(mail.getRecipient(), "kosmos.mail.recipient must be set");
        Objects.requireNonNull(mail.getFrom(), "kosmos.mail.from must be set");
        String appendSubject = properties.getMail().getAppendSubject();
        if (appendSubject != null && !appendSubject.isEmpty()) {
            subject += " - " + appendSubject;
        }
        log.trace("Sending email from {} to {} with subject '{}'", mail.getFrom(), mail.getRecipient(), subject);
        String finalSubject = subject;
        Optional.ofNullable(mailSenderProvider.getIfAvailable()).ifPresent(mailSender ->
                mailSender.send(mimeMessage -> {
                    log.info("Sending mail");
                    mimeMessage.setRecipient(Message.RecipientType.TO,
                            new InternetAddress(mail.getRecipient()));
                    mimeMessage.setFrom(new InternetAddress(mail.getFrom()));
                    mimeMessage.setSubject(finalSubject);
                    mimeMessage.setText(content);
                }));
    }
}
