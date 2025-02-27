# KOSMOS

<img style="float:right" width="100" height="100" src="docs/EF.png" alt="KOSMOS - ein komponent i eFormidling">

## Føremål
KOSMOS er [eFormidling](https://docs.digdir.no/docs/eFormidling/Introduksjon/) sitt verktøy for å halda installasjonar av [Integrasjonspunktet](https://github.com/felleslosninger/efm-integrasjonspunkt/) oppdaterte.

## Teknologiar i bruk
- Spring Boot

## Føresetnadar
- Java 21
- Maven 3

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
