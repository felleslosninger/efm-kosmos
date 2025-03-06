# KOSMOS

<img style="float:right" width="100" height="100" src="docs/EF.png" alt="KOSMOS - ein komponent i eFormidling">

## Føremål
KOSMOS er [eFormidling](https://docs.digdir.no/docs/eFormidling/Introduksjon/) sitt verktøy for å halda installasjonar av [Integrasjonspunktet](https://github.com/felleslosninger/efm-integrasjonspunkt/) oppdaterte.

## Teknologiar i bruk
- Spring Boot

## Føresetnadar
- Java 21
- Maven 3

## Breaking changes fra Kosmos v1.x til v2.x
-  Fjernet bruk av Spring Cloud Config (properties) for styring av latest/earlybird versjoner :
    - Property `kosmos.integrasjonspunkt.latest-version` er ikke lenger i bruk (kan fjernes lokalt)
    - Property `kosmos.integrasjonspunkt.earlybird-version` er ikke lenger i bruk (kan fjernes lokalt)
    - Property `kosmos.integrasjonspunkt.versionsURL` peker på URL hvor siste versjoner lastes ned (default er [latest-version.yml](https://raw.githubusercontent.com/felleslosninger/efm-integrasjonspunkt/refs/heads/main/latest-versions.yml))

Eksempel på hvordan en [latest-versions.yml](src/test/resources/versions/latest-versions.yml) ser ut. 

## Bygging og testing
Testet og bygget med OpenJDK 21.0.5 og Maven 3.9.9.

```bash
# bygge og kjøre unit-tester (surefire only, i praksis uten Cucumber)
mvn clean package

# bygge og kjøre alt av unit- & integrasjons-tester (surefire + failsafe)
mvn clean verify
```

## Dokumentasjon
- Sjå [Digdir Docs](https://docs.digdir.no/docs/eFormidling/installasjon/automatisk_oppgradering)

## Release
See documentation for the [maven-release-plugin](https://maven.apache.org/maven-release/maven-release-plugin/) and [guide for maven-release-plugin](https://maven.apache.org/guides/mini/guide-releasing.html).

```bash
# local repo must be in sync with origin/GitHub
git push

mvn release:prepare
# answer three questions (set the tag equal to the release version)
# What is the release version for "Kosmos"? (no.difi.move:kosmos) 1.0: : 1.0.0
# What is SCM release tag or label for "Kosmos"? (no.difi.move:kosmos) 1.0.0: :
# What is the new development version for "Kosmos"? (no.difi.move:kosmos) 1.0.1-SNAPSHOT: :

mvn release:perform
```