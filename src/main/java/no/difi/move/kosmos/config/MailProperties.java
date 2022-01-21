package no.difi.move.kosmos.config;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class MailProperties {

    @NotNull
    @Email
    private String recipient;

    @NotNull
    @Email
    private String from;

    private String appendSubject;
}
