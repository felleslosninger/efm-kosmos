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
    And the latest integrasjonspunkt version is "1.7.93-SNAPSHOT" with SHA-1 "83e1532b48e95cdce524972d397e5460e9529c97"

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
