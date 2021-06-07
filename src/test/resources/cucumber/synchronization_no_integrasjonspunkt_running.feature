Feature: Synchronization - No integrasjonspunkt running on client

  Background:
    Given the latest integrasjonspunkt version is "2.2.1"
    And the early bird setting is not activated
    And the supported major version is unset
    And a "GET" request to "/maven2/no/difi/meldingsutveksling/integrasjonspunkt/2.2.1" will respond with status "200" and the following "application/java-archive" in "/cucumber/success.jar"
    And a "GET" request to "/maven2/no/difi/meldingsutveksling/integrasjonspunkt/2.2.1.jar.sha1" will respond with status "200" and the following "application/octet-stream"
    """
    39ba01879f7ededa62f7e5129f140089795e05bc
    """
    And a "GET" request to "/maven2/no/difi/meldingsutveksling/integrasjonspunkt/2.2.1.jar.md5" will respond with status "200" and the following "application/octet-stream"
    """
    e343ab4e4151f822331e7f5998b26ecc
    """
    And a "GET" request to "/maven2/no/difi/meldingsutveksling/integrasjonspunkt/2.2.1.jar.asc" will respond with status "200" and the following "text/plain" in "/cucumber/success.jar.asc"
    And a "GET" request to "/manage/health" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "status": "UP"
    }
    """

  Scenario: No integrasjonspunkt is running
    Given the synchronization handler is triggered
    Then the "integrasjonspunkt-2.2.1.jar" is successfully launched
    And an email is sent with subject "Upgrade SUCCESS integrasjonspunkt-2.2.1.jar" and content:
    """
    Started IntegrasjonspunktApplication
    """
