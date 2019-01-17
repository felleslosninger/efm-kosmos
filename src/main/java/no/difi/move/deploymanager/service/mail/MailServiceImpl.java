package no.difi.move.deploymanager.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
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
    private final DeployManagerProperties properties;

    @Override
    public void sendMail(String subject, String content) {
        DeployManagerProperties.MailProperties mail = properties.getMail();

        if (mail == null) {
            return;
        }

        Objects.requireNonNull(mail.getRecipient(), "deploymanager.mail.recipient must be set");
        Objects.requireNonNull(mail.getFrom(), "deploymanager.mail.from must be set");

        Optional.ofNullable(mailSenderProvider.getIfAvailable()).ifPresent(mailSender ->
                mailSender.send(mimeMessage -> {
                    log.info("Sending mail");
                    mimeMessage.setRecipient(Message.RecipientType.TO,
                            new InternetAddress(mail.getRecipient()));
                    mimeMessage.setFrom(new InternetAddress(mail.getFrom()));
                    mimeMessage.setSubject(subject);
                    mimeMessage.setText(content);
                }));
    }
}
