package no.difi.move.kosmos.service.actuator.dto;

import lombok.Data;

@Data
public class InfoResource {

    private BuildResource build;

    @Data
    public static class BuildResource{
        private String version;
        private String artifact;
        private String name;
        private String group;
        private String time;
    }
}
