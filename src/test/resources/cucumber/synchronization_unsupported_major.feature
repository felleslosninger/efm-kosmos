Feature: Synchronization - Major version not supported

  Background:
    Given the metadata.properties contains:
    """
    version=1.9.0
    filename=integrasjonspunkt-1.9.0.jar
    repositoryId=staging
    sha1=39ba01879f7ededa62f7e5129f140089795e05bc
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
    repositoryId=staging
    sha1=39ba01879f7ededa62f7e5129f140089795e05bc
    """
    And no emails are sent