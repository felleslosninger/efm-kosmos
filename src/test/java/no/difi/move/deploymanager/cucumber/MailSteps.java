package no.difi.move.deploymanager.cucumber;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import lombok.SneakyThrows;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class MailSteps {

    @Autowired
    private JavaMailSenderImpl javaMailSender;
    private SimpleSmtpServer simpleSmtpServer;

    @Before
    @SneakyThrows
    public void before() {
        simpleSmtpServer = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
        javaMailSender.setPort(simpleSmtpServer.getPort());
    }

    @After
    public void after() {
        simpleSmtpServer.stop();
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
