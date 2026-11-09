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

## Tryggleik i heimekatalogen
KOSMOS lastar ned og startar integrasjonspunktet frå `kosmos.integrasjonspunkt.home` (standard er katalogen KOSMOS
blir starta frå). Den offentlege nøkkelen blir lesen frå `kosmos.verification.publicKeyPaths` (standard
`eformidling-key.asc` i same katalog). Kode i denne katalogen blir køyrd under tenestekontoen til KOSMOS, så:

- Katalogen, filene i han og den offentlege nøkkelen skal **berre vere skrivbare for tenestekontoen** som køyrer KOSMOS.
  På Linux til dømes med `chown -R <tenestekonto> <katalog>` og `chmod -R go-w <katalog>`.
  På Windows: fjern skrivetilgang for andre enn tenestekontoen og administratorar.

KOSMOS stolar likevel ikkje på filene i katalogen åleine:

- Før kvar oppstart, også ved rollback, blir JAR-fila verifisert mot signaturen for versjonen. Signaturen blir lasta ned
  frå GitHub kvar gong og blir ikkje lagra i katalogen. Ei JAR-fil som ikkje kan verifiserast, blir ikkje starta.
  Det betyr at KOSMOS må nå GitHub for å kunne starte integrasjonspunktet.
- Etter oppstart blir SHA-256 for JAR-fila samanlikna med innhaldet som vart verifisert. Er fila endra, blir
  integrasjonspunktet stoppa.
- `.allowlisted`-filene seier berre kva versjon som sist vart starta. Versjonen blir berre brukt viss JAR-fila
  for han kan verifiserast.
- Ligg det alt ei JAR-fil for siste versjon i katalogen som ikkje kan verifiserast, blir versjonen lasta ned på nytt.

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
   - Lastar ned den publiserte offentlege nøkkelen frå [docs.digdir.no](https://docs.digdir.no/resources/eformidling/public_keys/eformidling-key.asc)
     og verifiserer signaturen mot han, slik at brukarane får same resultat. Krev gyldig (ikkje utgått) nøkkel med rett fingeravtrykk
   - Lastar opp `kosmos-<tag>.jar` og `kosmos-<tag>.jar.asc` til GitHub-releasen

### Secrets som trengst
Signeringsjobben brukar desse secrets (org- eller repo-secrets):

| Secret | Innhald |
|---|---|
| `EFM_GPG_PRIVATE_KEY` | Privat kodesigneringsnøkkel, ASCII-armert (innhaldet i `kodesignering-eformidling-private.key`) |
| `EFM_GPG_PASSPHRASE` | Passordet til den private nøkkelen |

Fingeravtrykket til nøkkelen (`AEF27AA6948A3856932AF98ECA5643393753ECE3`) er hardkoda i workflowen, sidan det ikkje er hemmeleg.

Workflowen treng i tillegg `GH_PACKAGES_READ_USER` / `GH_PACKAGES_READ_PAT` (via `secrets: inherit`) for å lese interne Maven-avhengigheiter.

Skript for manuell signering, verifisering og forlenging av kodesigneringsnøkkelen finst i
[efm-integrasjonspunkt/signering](https://github.com/felleslosninger/efm-integrasjonspunkt/tree/main/signering).
Når nøkkelen blir forlenga med `extend.sh`, må både `EFM_GPG_PRIVATE_KEY` og den publiserte offentlege nøkkelen oppdaterast.

Signaturen kan verifiserast lokalt med `verify.sh` derifrå, eller direkte med gpg:
```bash
gpg --import kodesignering-eformidling-public.key
gpg --verify kosmos-v4.0.0.jar.asc kosmos-v4.0.0.jar
```

## License

This project is licensed under the BSD 3-Clause License. See the [LICENSE](LICENSE) file for details.
