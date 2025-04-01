package no.difi.move.kosmos.cucumber;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RequiredArgsConstructor
public class MailSteps {

    private final GreenMail testSmtpServer;
    private final JavaMailSenderImpl javaMailSender;

    @Before
    public void before() {
        javaMailSender.setPort(testSmtpServer.getSmtp().getPort());
    }

    @After
    public void after() {
        testSmtpServer.reset();
    }

    @Then("^an email is sent with subject \"([^\"]*)\" and content:$")
    public void anEmailIsSentWithSubjectAndContent(String subject, String content) {
        assertThat(testSmtpServer.getReceivedMessages())
                .extracting(this::getSubject, GreenMailUtil::getBody)
                .contains(tuple(subject, content.trim()));
    }

    @SneakyThrows
    private String getSubject(MimeMessage p) {
        return p.getSubject();
    }

    @Then("^no emails are sent$")
    public void noEmailsAreSent() {
        assertThat(testSmtpServer.getReceivedMessages()).isEmpty();
    }

}