package no.difi.move.deploymanager.config;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class BlacklistProperties {

    @NotNull
    @Positive
    private Integer durationInHours;
}
