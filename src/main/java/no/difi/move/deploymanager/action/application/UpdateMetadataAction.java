package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.service.launcher.dto.LaunchStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateMetadataAction implements ApplicationAction {

    private final DeployDirectoryRepo directoryRepo;

    @Override
    public Application apply(Application application) {
        log.debug("Running UpdateMetadataAction");

        if (application.getLaunchResult() == null || application.getLaunchResult().getStatus() != LaunchStatus.SUCCESS) {
            log.info("No successful launch. Skipping update of metadata.");
            return application;
        }

        try {
            ApplicationMetadata applicationMetadata = getApplicationMetadata(application);
            Properties directoryProperties = directoryRepo.getMetadata();
            directoryProperties.setProperty("version", applicationMetadata.getVersion());
            directoryProperties.setProperty("repositoryId", applicationMetadata.getRepositoryId());
            directoryProperties.setProperty("filename", applicationMetadata.getFile().getName());
            directoryRepo.setMetadata(directoryProperties);

            return application;
        } catch (IOException e) {
            throw new DeployActionException("Could not update metadata.", e);
        }
    }

    private ApplicationMetadata getApplicationMetadata(Application application) {
        return Objects.requireNonNull(application.getLatest(), "Invalid application metadata encountered.");
    }
}
