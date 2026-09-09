# Teststrategi

Som eit ledd i oppfølgingsoppgåver i kjølvatnet av Testing Spring Boot Applications Demystified-workshop 
skal det gjennomføres noen testendringar (ref JIRA oppgåver MOVE-4904, MOVE-4910, MOVE-4917).

- Målet bør vera å halda test-fasen rask.
- Innføra maven-failsafe-plugin for integrasjonstestar
- Parallellisera unit-testar
- Slice testar som er raske går som unit-testar
- Dersom noko kjem i konflikt med parallellisering skal dei testane redirigerast til Failsafe
- Surefire som tommelfingerregel, men dersom profilering (t.d. vha. Spring Test Profiler) tilseier noko anna, kan dei omdøypast til ...IT, og dermed handsamast av Failsafe
- For hybridtestar som @WebMvcTest og @DataJpaTest, bruk fornuft og se på tidsbruken (maven `test` fasen skal være rask)

## Oppsett for tweaking

Benyttet følgande oppsettet på med MacBook M3 Pro og kjørte alle testar fra terminalen.
```bash
> java --version
openjdk 21.0.9 2025-10-21 LTS

> mvn --version
Apache Maven 3.9.13 (39d686bd50d8e054301e3a68ad44781df6f80dda)
```
Bruker `mvn clean verify` for å sjekke fremgang mellom forsøkene.

## Etter gjennomgang av tester (default surefire + failsafe with no junit tweaks)
```bash
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  58.104 s
[INFO] Finished at: 2026-03-13T12:12:49+01:00
[INFO] ------------------------------------------------------------------------
```

## Parallel testing konfigurert i junit-platform.properties
Fikk trøbbel med testen `PublicKeyVerifierImplTest.java` igjen, den sliter med mulig
forurensing av classpath / security provider og feiler om den kjøres sammen med andre
tester (kan tvinges gjennom med `reuseForks=false` og `forkCount=1`).

Renamet testen til `PublicKeyVerifierImplIT.java` slik at den kjøres med failsafe
i stede for å unngå problemet (kunne antagelig laget en egen surefire target for kun
den testen, men prioriterte å holde pom.xml enkel).

Resultatet av kjøring etter denne endringe var uendret, rett under ett minutt pr
kjøring.

## Enablet parallel testing i junit-platform.properties
Parallel, same thread og concurrent classes aktivert i
[junit-platform.properties](src/test/resources/junit-platform.properties).

Drøyt 20 sekunder raskere.
```bash
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  36.917 s
[INFO] Finished at: 2026-03-13T13:38:22+01:00
[INFO] ------------------------------------------------------------------------
```
