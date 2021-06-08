package no.difi.move.kosmos.cucumber;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RequiredArgsConstructor
public class MailSteps {

    private final SimpleSmtpServer simpleSmtpServer;
    private final JavaMailSenderImpl javaMailSender;

    @Before
    public void before() {
        javaMailSender.setPort(simpleSmtpServer.getPort());
    }

    @After
    public void after() {
        simpleSmtpServer.reset();
    }

    @Then("^an email is sent with subject \"([^\"]*)\" and content:$")
    public void anEmailIsSentWithSubjectAndContent(String subject, String content) {
        assertThat(simpleSmtpServer.getReceivedEmails())
                .extracting(p -> p.getHeaderValue("Subject"), SmtpMessage::getBody)
                .contains(tuple(subject, content.trim()));
    }

    @Then("^no emails are sent$")
    public void noEmailsAreSent() {
        assertThat(simpleSmtpServer.getReceivedEmails()).isEmpty();
    }
}
