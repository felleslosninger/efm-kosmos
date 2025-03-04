Feature: Synchronization - EarlyBird major version not supported

  Background:
    Given a "GET" request to "/manage/info" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "build": {
                      "version": "1.9.0",
                      "artifact": "integrasjonspunkt",
                      "name": "Meldingsutveksling Integrasjonspunkt",
                      "group": "no.difi.meldingsutveksling",
                      "time": "2021-02-23T14:23:09.751Z"
                  }
    }
    """
    And the "integrasjonspunkt-1.9.0.jar" exists as a copy of "/cucumber/success.jar"
    And the supported major version is "2"
    And the early bird setting is activated with version set to "3.0.0-SNAPSHOT"

  Scenario: Major version not supported
    Given the synchronization handler is triggered
    Then no JAR is launched
    And no emails are sent