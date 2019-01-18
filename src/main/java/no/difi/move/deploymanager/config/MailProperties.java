package no.difi.move.deploymanager.config;

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
}
