Feature: Synchronization - No Change

  Background:
    Given a "GET" request to "/manage/info" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "build": {
                      "version": "1.7.93-SNAPSHOT",
                      "artifact": "integrasjonspunkt",
                      "name": "Meldingsutveksling Integrasjonspunkt",
                      "group": "no.difi.meldingsutveksling",
                      "time": "2021-02-23T14:23:09.751Z"
                  }
    }
    """
    And the "integrasjonspunkt-1.7.93-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the latest integrasjonspunkt version is "1.7.93-SNAPSHOT"
    And the early bird setting is not activated
    And the supported major version is unset
    And a "GET" request to "/felleslosninger/efm-integrasjonspunkt/releases/download/1.7.93-SNAPSHOT/integrasjonspunkt-1.7.93-SNAPSHOT.jar.asc" will respond with status "200" and the following "text/plain" in "/cucumber/success.jar.asc"
    And a "GET" request to "/manage/health" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "status": "UP"
    }
    """

  Scenario: No change
    Given the synchronization handler is triggered
    Then no JAR is launched
    And no emails are sent
