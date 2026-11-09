Feature: Synchronization - JAR in the home folder does not match the signature

  Background:
    Given the "integrasjonspunkt-2.2.1.jar" exists as a copy of "/cucumber/failure.jar"
    And the distribution "integrasjonspunkt-2.2.1" is allowlisted
    And the latest integrasjonspunkt version is "2.2.1"
    And the early bird setting is not activated
    And the supported major version is unset
    And a "GET" request to "/felleslosninger/efm-integrasjonspunkt/releases/download/2.2.1/integrasjonspunkt-2.2.1.jar.asc" will respond with status "200" and the following "text/plain" in "/cucumber/success.jar.asc"
    And a "GET" request to "/manage/health" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "status": "UP"
    }
    """

  Scenario: The JAR is not launched
    Given the synchronization handler is triggered
    Then no JAR is launched
    And no emails are sent
    And the "integrasjonspunkt-2.2.1.jar.download" does not exist

  Scenario: The JAR is replaced by a verified download
    Given a "GET" request to "/felleslosninger/efm-integrasjonspunkt/releases/download/2.2.1/integrasjonspunkt-2.2.1.jar" will respond with status "200" and the following "application/java-archive" in "/cucumber/success.jar"
    And a "GET" request to "/felleslosninger/efm-integrasjonspunkt/releases/download/2.2.1/integrasjonspunkt-2.2.1.jar.sha1" will respond with status "200" and the following "application/octet-stream"
    """
    39ba01879f7ededa62f7e5129f140089795e05bc
    """
    When the synchronization handler is triggered
    Then the "integrasjonspunkt-2.2.1.jar" is successfully launched
    And the "integrasjonspunkt-2.2.1.jar" has the same content as "/cucumber/success.jar"
    And an email is sent with subject "Upgrade SUCCESS integrasjonspunkt-2.2.1.jar" and content:
    """
    Started IntegrasjonspunktApplication
    """
