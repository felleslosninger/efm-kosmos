# KOSMOS

<img style="float:right" width="100" height="100" src="docs/EF.png" alt="KOSMOS - ein komponent i eFormidling">

## Føremål
KOSMOS er [eFormidling](https://docs.digdir.no/docs/eFormidling/Introduksjon/) sitt verktøy for å halda installasjonar av [Integrasjonspunktet](https://github.com/felleslosninger/efm-integrasjonspunkt/) oppdaterte.

## Teknologiar i bruk
- Spring Boot 3

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
Testet og bygget med OpenJDK 21.0.9 og Maven 3.9.12.

```bash
# bygge og kjøre unit-tester (surefire only, i praksis uten Cucumber)
mvn clean package

# bygge og kjøre alt av unit- & integrasjons-tester (surefire + failsafe)
mvn clean verify
```

## Dokumentasjon
- Sjå [Digdir Docs](https://docs.digdir.no/docs/eFormidling/installasjon/automatisk_oppgradering)

## Release (for interne)
Release av ny versjon gjerast via GitHub GUI
- Gå til "Releases" i GitHub repo
- Klikk på "Draft a new release"
- Velg tag (ny eller eksisterande)
- Fyll inn tittel og beskrivelse
- Klikk på "Publish release"
