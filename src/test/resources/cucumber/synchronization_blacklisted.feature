Feature: Synchronization - Blacklisted

  Background:
    Given the metadata.properties contains:
    """
    version=1.7.92-SNAPSHOT
    filename=integrasjonspunkt-1.7.92-SNAPSHOT.jar
    repositoryId=staging
    sha1=39ba01879f7ededa62f7e5129f140089795e05bc
    """
    And the "integrasjonspunkt-1.7.92-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the "integrasjonspunkt-1.7.93-SNAPSHOT.jar" exists as a copy of "/cucumber/success.jar"
    And the "integrasjonspunkt-1.7.93-SNAPSHOT.blacklisted" exists
    And the latest integrasjonspunkt version is "1.7.93-SNAPSHOT"
    And the supported major version is unset

  Scenario: Blacklisted
    Given the synchronization handler is triggered
    Then no JAR is launched
    And the metadata.properties is updated with:
    """
    version=1.7.92-SNAPSHOT
    filename=integrasjonspunkt-1.7.92-SNAPSHOT.jar
    repositoryId=staging
    sha1=39ba01879f7ededa62f7e5129f140089795e05bc
    """
    And no emails are sent
