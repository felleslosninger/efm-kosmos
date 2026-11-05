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
Testet og bygget med OpenJDK 21.0.9 og Maven 3.9.15.

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
- Velg tag (ny eller eksisterande) `Bruk semantisk versjonering, Github best practice anbefaler v-prefix, slik "v4.0.0"`
- Fyll inn tittel og beskrivelse
- Klikk på "Publish release"

### Kva release-workflowen gjer
Når releasen er oppretta, køyrer [call-release.yaml](.github/workflows/call-release.yaml) to jobbar etter kvarandre:

1. **Bygg og publiser** – den felles workflowen `felleslosninger/github-workflows/.github/workflows/ci-build-publish-lib.yml`
   set versjonen frå taggen, byggjer og testar, køyrer Trivy-skanning, publiserer JAR-fila til GitHub Packages og lagar SBOM.
2. **Signer og legg ved JAR** (`sign-and-attach-jar`):
   - Sjekkar at signerings-secrets er sette (jobben feilar viss nokon av dei er tomme)
   - Lastar ned nøyaktig den JAR-fila som vart publisert til GitHub Packages (`kosmos-<tag>.jar`)
   - Signerer ho med kodesigneringsnøkkelen for eFormidling, som gjev ein frittståande ASCII-armert signatur (`kosmos-<tag>.jar.asc`)
   - Verifiserer signaturen mot den offentlege nøkkelen, og krev at han er laga av nøkkelen med rett fingeravtrykk
   - Lastar opp `kosmos-<tag>.jar` og `kosmos-<tag>.jar.asc` til GitHub-releasen

### Secrets som trengst
Signeringsjobben brukar desse secrets (org- eller repo-secrets):

| Secret | Innhald |
|---|---|
| `EFM_GPG_PRIVATE_KEY` | Privat kodesigneringsnøkkel, ASCII-armert (innhaldet i `kodesignering-eformidling-private.key`) |
| `EFM_GPG_PASSPHRASE` | Passordet til den private nøkkelen |
| `EFM_GPG_PUBLIC_KEY` | Offentleg kodesigneringsnøkkel, ASCII-armert (innhaldet i `kodesignering-eformidling-public.key`) |
| `EFM_GPG_FINGERPRINT` | Fingeravtrykket til nøkkelen, no `AEF27AA6948A3856932AF98ECA5643393753ECE3` |

Workflowen treng i tillegg `GH_PACKAGES_READ_USER` / `GH_PACKAGES_READ_PAT` (via `secrets: inherit`) for å lese interne Maven-avhengigheiter.

Signaturen kan verifiserast lokalt med:
```bash
gpg --import kodesignering-eformidling-public.key
gpg --verify kosmos-v4.0.0.jar.asc kosmos-v4.0.0.jar
```

## License

This project is licensed under the BSD 3-Clause License. See the [LICENSE](LICENSE) file for details.
