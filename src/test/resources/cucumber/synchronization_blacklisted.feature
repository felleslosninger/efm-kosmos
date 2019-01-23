Feature: Synchronization - Blacklisted

  Background:
    Given the metadata.properties contains:
    """
    version=1.7.92-SNAPSHOT
    filename=integrasjonspunkt-1.7.92-SNAPSHOT.jar
    repositoryId=staging
    sha1=83e1532b48e95cdce524972d397e5460e9529c97
    """
    And the "integrasjonspunkt-1.7.92-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the "integrasjonspunkt-1.7.93-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the "integrasjonspunkt-1.7.93-SNAPSHOT.blacklisted" exists
    And a "GET" request to "http://staging-move-app02.dmz.local:8084/latest?env=staging" will respond with status "200" and the following "application/json; charset=utf-8"
    """
    {
        "baseVersion": "1.7.93-SNAPSHOT",
        "version": "1.7.93-20181012.140228-1",
        "sha1": "83e1532b48e95cdce524972d397e5460e9529c97",
        "downloadUri": "https://beta-meldingsutveksling.difi.no/service/local/artifact/maven/redirect?r=staging&g=no.difi.meldingsutveksling&a=integrasjonspunkt&v=1.7.93-SNAPSHOT"
    }
    """

  Scenario: Blacklisted
    Given the synchronization handler is triggered
    Then no JAR is launched
    And the metadata.properties is updated with:
    """
    version=1.7.92-SNAPSHOT
    filename=integrasjonspunkt-1.7.92-SNAPSHOT.jar
    repositoryId=staging
    sha1=83e1532b48e95cdce524972d397e5460e9529c97
    """
    And no emails are sent
