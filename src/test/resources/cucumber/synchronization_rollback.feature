Feature: Synchronization - Rollback

  Background:
    Given the metadata.properties contains:
    """
    version=1.7.92-SNAPSHOT
    filename=integrasjonspunkt-1.7.92-SNAPSHOT.jar
    repositoryId=staging
    sha1=83e1532b48e95cdce524972d397e5460e9529c97
    """
    And the "integrasjonspunkt-1.7.92-SNAPSHOT.jar" exists
    And a "GET" request to "http://staging-move-app02.dmz.local:8084/latest?env=staging" will respond with status "200" and the following "application/json; charset=utf-8"
    """
    {
        "baseVersion": "1.7.93-SNAPSHOT",
        "version": "1.7.93-20181012.140228-1",
        "sha1": "83e1532b48e95cdce524972d397e5460e9529c97",
        "downloadUri": "https://beta-meldingsutveksling.difi.no/service/local/artifact/maven/redirect?r=staging&g=no.difi.meldingsutveksling&a=integrasjonspunkt&v=1.7.93-SNAPSHOT"
    }
    """
    And a "GET" request to "https://beta-meldingsutveksling.difi.no/service/local/artifact/maven/content?r=staging&g=no.difi.meldingsutveksling&a=integrasjonspunkt&v=1.7.93-SNAPSHOT" will respond with status "200" and the following "application/java-archive" in "/cucumber/failure.jar"
    And a "GET" request to "https://beta-meldingsutveksling.difi.no/service/local/artifact/maven/content?r=staging&g=no.difi.meldingsutveksling&a=integrasjonspunkt&v=1.7.93-SNAPSHOT&e=jar.sha1" will respond with status "200" and the following "application/octet-stream"
    """
    06d4cfb40c1bfeb3ef8d4ccfd222defe7225d425
    """
    And a "GET" request to "https://beta-meldingsutveksling.difi.no/service/local/artifact/maven/content?r=staging&g=no.difi.meldingsutveksling&a=integrasjonspunkt&v=1.7.93-SNAPSHOT&e=jar.md5" will respond with status "200" and the following "application/octet-stream"
    """
    d45f066d54edf0c99ead70d3305865a5
    """
    And a "GET" request to "http://localhost:9092/manage/health" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "status": "UP"
    }
    """
    And a "POST" request to "http://localhost:9092/manage/shutdown" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "message": "Shutting down, bye..."
    }
    """
    And a "GET" request to "http://localhost:9092/manage/health" will respond with connection refused
    And a "GET" request to "http://localhost:9092/manage/health" will respond with connection refused
    And a "GET" request to "http://localhost:9092/manage/health" will respond with connection refused
    And a "GET" request to "http://localhost:9092/manage/health" will respond with connection refused
    And a "GET" request to "http://localhost:9092/manage/health" will respond with status "200" and the following "application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"
    """
    {
        "status": "UP"
    }
    """

  Scenario: Rollback
    Given the synchronization handler is triggered
    Then the "integrasjonspunkt-1.7.92-SNAPSHOT.jar" is successfully launched
    And the metadata.properties is updated with:
    """
    version=1.7.92-SNAPSHOT
    filename=integrasjonspunkt-1.7.92-SNAPSHOT.jar
    repositoryId=staging
    sha1=83e1532b48e95cdce524972d397e5460e9529c97
    """
