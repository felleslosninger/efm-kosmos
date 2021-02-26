Feature: Synchronization - Major version not supported

  Background:
    Given the metadata.properties contains:
    """
    version=1.9.0
    filename=integrasjonspunkt-1.9.0.jar
    """
    And the info URL is "http://localhost:9092/manage/info"
    And a "GET" request to "http://localhost:9092/manage/info" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
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
    And the "integrasjonspunkt-2.0.0.jar" exists as a copy of "/cucumber/success.jar"
    And the latest integrasjonspunkt version is "2.0.0"
    And the supported major version is "1"

  Scenario: Major version not supported
    Given the synchronization handler is triggered
    Then no JAR is launched
    And the metadata.properties is updated with:
    """
    version=1.9.0
    filename=integrasjonspunkt-1.9.0.jar
    """
    And no emails are sent