package no.difi.move.kosmos.config;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class BlocklistProperties {

    private boolean enabled;

    @NotNull
    @Positive
    private Integer durationInHours;
}
