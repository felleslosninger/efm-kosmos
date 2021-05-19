Feature: Synchronization - Allowlisted

  Background:
    And the "integrasjonspunkt-1.7.93-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the distribution "integrasjonspunkt-1.7.93-SNAPSHOT" is allowlisted
    And the latest integrasjonspunkt version is "1.7.93-SNAPSHOT"
    And the early bird setting is not activated
    And the supported major version is "1"
    And state is "Started" then a "GET" request to "/manage/health" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "status": "UP"
    }
    """
    And a "GET" request to "/manage/info" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
      "build": {
        "artifact": "integrasjonspunkt",
        "name": "Meldingsutveksling Integrasjonspunkt",
        "time": 1620995588.865000000,
        "version": "1.7.90",
        "group": "no.difi.meldingsutveksling"
      }
    }
    """

  Scenario: Allowlisted
    Given the synchronization handler is triggered
    Then the "integrasjonspunkt-1.7.93-SNAPSHOT.jar" is successfully launched
    And an email is sent with subject "Upgrade SUCCESS integrasjonspunkt-1.7.93-SNAPSHOT.jar" and content:
    """
    Started IntegrasjonspunktApplication
    """
