Feature: Synchronization - Blacklisted

  Background:
    Given the info URL is "http://localhost:9092/manage/info"
    And a "GET" request to "http://localhost:9092/manage/info" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "build": {
                      "version": "1.7.92-SNAPSHOT",
                      "artifact": "integrasjonspunkt",
                      "name": "Meldingsutveksling Integrasjonspunkt",
                      "group": "no.difi.meldingsutveksling",
                      "time": "2021-02-23T14:23:09.751Z"
                  }
    }
    """
    And the "integrasjonspunkt-1.7.92-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the "integrasjonspunkt-1.7.93-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the distribution "integrasjonspunkt-1.7.93-SNAPSHOT" is blacklisted
    And the latest integrasjonspunkt version is "1.7.93-SNAPSHOT"
    And the early bird setting is not activated
    And the supported major version is unset

  Scenario: Blacklisted
    Given the synchronization handler is triggered
    Then no JAR is launched
    And no emails are sent
