# KOSMOS

<img style="float:right" width="100" height="100" src="docs/EF.png" alt="KOSMOS - ein komponent i eFormidling">

## Føremål
KOSMOS er [eFormidling](https://docs.digdir.no/docs/eFormidling/Introduksjon/) sitt verktøy for å halda installasjonar av [Integrasjonspunktet](https://github.com/felleslosninger/efm-integrasjonspunkt/) oppdaterte.

## Teknologiar i bruk
- Spring Boot 4 med WebFlux (`WebClient` for nedlasting)
- BouncyCastle (`bcpg`) for GPG-verifisering av JAR-filene
- logstash-logback-encoder for logging til ELK
- JUnit 6, Cucumber, WireMock og GreenMail i testane

## Føresetnadar
- Java 21
- Maven 3.9

## Breaking changes frå KOSMOS v1.x til v2.x
Desse endringane kom i v2 og gjeld framleis:

- Spring Cloud Config (properties) blir ikkje lenger brukt til å styre latest- og earlybird-versjonar:
    - Property `kosmos.integrasjonspunkt.latest-version` er ikkje lenger i bruk (kan fjernast lokalt)
    - Property `kosmos.integrasjonspunkt.earlybird-version` er ikkje lenger i bruk (kan fjernast lokalt)
    - Property `kosmos.integrasjonspunkt.versionsURL` peikar på URL-en der siste versjonar blir lasta ned frå (standard er [latest-versions.yml](https://raw.githubusercontent.com/felleslosninger/efm-integrasjonspunkt/refs/heads/main/latest-versions.yml))

Eksempel på korleis ei [latest-versions.yml](src/test/resources/versions/latest-versions.yml) ser ut.

## Bygging og testing
Sist testa med OpenJDK 21.0.9 og Maven 3.9.13. CI byggjer med Java 21.

```bash
# byggje og køyre unit-testane (surefire). Cucumber og *IT-testane blir ikkje køyrde
mvn clean package

# byggje og køyre alle testane: unit-testar (surefire) og integrasjonstestar (failsafe),
# det vil seie alle *IT-klassane, også Cucumber-scenaria via RunCucumberIT
mvn clean verify
```

Unit-testane køyrer parallelt, medan failsafe køyrer integrasjonstestane éin om gongen.
Sjå [TESTING.md](TESTING.md) for teststrategien og kvifor nokre testar er lagde under failsafe.

## Dokumentasjon
- Sjå [Digdir Docs](https://docs.digdir.no/docs/eFormidling/installasjon/automatisk_oppgradering)

## Konfigurasjon
KOSMOS blir konfigurert med Spring Boot-properties. Standardverdiane står i
[application.properties](src/main/resources/application.properties).

Lokale innstillingar legg du i `kosmos-local.properties` i katalogen KOSMOS blir starta frå. Fila blir lesen ved
oppstart viss ho finst, og verdiane i ho går føre alle andre kjelder, også argument på kommandolinja.

| Property | Standard | Forklaring |
|---|---|---|
| `kosmos.orgnumber` | (ingen) | **Påkravd.** Organisasjonsnummeret KOSMOS køyrer for. Må vere nøyaktig 9 siffer med rett kontrollsiffer (mod11). Utan gyldig verdi startar ikkje KOSMOS. Blir henta frå `difi.move.org.number` viss han ikkje er sett. |
| `kosmos.integrasjonspunkt.profile` | `staging` | Lovlege verdiar er `dev`, `itest`, `staging` og `production`. Vel miljøet i versjonsfila og blir sendt vidare til integrasjonspunktet som `spring.profiles.active`. Startar du KOSMOS med ein av desse Spring-profilane, blir verdien sett tilsvarande. |
| `kosmos.integrasjonspunkt.home` | `${user.dir}` | Katalogen der integrasjonspunktet blir lasta ned og starta. Sjå [Tryggleik i heimekatalogen](#tryggleik-i-heimekatalogen). |
| `kosmos.integrasjonspunkt.autoRefresh` | `true` | Hentar versjonsfila frå `versionsURL` ved kvar synkronisering. Kan fila ikkje hentast eller lesast, blir den sist gyldige versjonsfila brukt. Med `false` blir fila ikkje henta, og KOSMOS oppgraderer ikkje: han held fram med versjonen som køyrer, eller startar den sist brukte versjonen (`.allowlisted`) viss JAR-fila for han kan verifiserast. Slik gjer KOSMOS også når versjonsfila ikkje kan hentast og han ikkje har henta nokon gyldig versjonsfil sidan oppstart. Finst det ingen versjon å halde fram med, stoppar synkroniseringa med ein ERROR. |
| `kosmos.integrasjonspunkt.versionsURL` | [latest-versions.yml](https://raw.githubusercontent.com/felleslosninger/efm-integrasjonspunkt/refs/heads/main/latest-versions.yml) | Kvar versjonsfila blir henta frå. |
| `kosmos.integrasjonspunkt.earlybird` | `false` | Bruk earlybird-versjonen frå versjonsfila i staden for latest. Er det ingen earlybird-versjon, blir latest brukt. |
| `kosmos.schedulerCronExpression` | `0 30 5,19,21 * * MON-FRI` | Når synkroniseringa køyrer (tidssone Europe/Oslo): kl. 05:30, 19:30 og 21:30 måndag–fredag. Ho køyrer i tillegg éin gong når KOSMOS har starta. |
| `kosmos.blocklist.enabled` | `false` | Blokklista er av som standard. Når ho er på, blir versjonar sperra i `kosmos.blocklist.duration-in-hours` timar (standard 2) når signaturkontrollen av ei ny nedlasting returnerer `false`, eller oppstarten feilar. Unntak under validering av ei ny nedlasting, til dømes utgått nøkkel, avvist hash-algoritme eller feil sjekksum, utløyser ikkje blokklisting. |
| `kosmos.java-archive-repository-url` | `https://github.com` | Basis-URL for nedlasting av JAR-fila og signaturen, sjå [Signaturverifisering](#signaturverifisering). |
| `kosmos.verification.publicKeyPaths[0]` | `file:${user.dir}/eformidling-key.asc` | Offentlege nøklar som signaturen blir verifisert mot. Property-en er ei liste, så fleire nøklar blir sette som `publicKeyPaths[1]` osv. |

## Tryggleik i heimekatalogen
KOSMOS lastar ned og startar integrasjonspunktet frå `kosmos.integrasjonspunkt.home` (standard er katalogen KOSMOS
blir starta frå, `user.dir`).

- Manglar katalogen, lagar KOSMOS han, saman med foreldrekatalogar som manglar.
- Er stien ikkje ein lesbar katalog (han finst, men er ei fil, eller katalogen kan ikkje lagast eller lesast),
  stoppar synkroniseringa med ein ERROR i loggen. Integrasjonspunktet blir då verken lasta ned eller starta.
- Integrasjonspunktet blir starta med heimekatalogen som arbeidskatalog. Filer som integrasjonspunktet les med
  relative stiar, til dømes `integrasjonspunkt-local.properties`, må derfor liggje der.

Den offentlege nøkkelen blir lesen frå `kosmos.verification.publicKeyPaths`. Standard er `eformidling-key.asc` i
startkatalogen til KOSMOS (`user.dir`), **også når `home` er endra**. Skal nøkkelen liggje i heimekatalogen eller
ein annan stad, må du setje `kosmos.verification.publicKeyPaths[0]` sjølv.

Kode i heimekatalogen blir køyrd under tenestekontoen til KOSMOS, så:

- Katalogen, filene i han og den offentlege nøkkelen skal **berre vere skrivbare for tenestekontoen** som køyrer KOSMOS.
  På Linux til dømes med `chown -R <tenestekonto> <katalog>` og `chmod -R go-w <katalog>`.
  På Windows: fjern skrivetilgang for andre enn tenestekontoen og administratorar.
- KOSMOS sjekkar **ikkje** filrettane sjølv. Det er opp til den som driftar KOSMOS å setje dei rett.

### Signaturverifisering
KOSMOS stolar likevel ikkje på filene i katalogen åleine:

- Før kvar oppstart, også ved rollback, blir JAR-fila verifisert mot signaturen for versjonen. Signaturen blir lasta ned
  kvar gong og blir ikkje lagra i katalogen. Ei JAR-fil som ikkje kan verifiserast, blir ikkje starta.
  Det betyr at KOSMOS må nå nedlastingsadressa for å kunne starte integrasjonspunktet.
- Etter oppstart blir SHA-256 for JAR-fila samanlikna med innhaldet som vart verifisert. Er fila endra, blir
  integrasjonspunktet stoppa.
- `.allowlisted`-filene seier berre kva versjon som sist vart starta. Versjonen blir berre brukt viss JAR-fila
  for han kan verifiserast.
- Ligg det alt ei JAR-fil for siste versjon i katalogen som ikkje kan verifiserast, blir versjonen lasta ned på nytt.

JAR-fila og signaturen blir lasta ned frå
`<kosmos.java-archive-repository-url>/felleslosninger/efm-integrasjonspunkt/releases/download/<versjon>/integrasjonspunkt-<versjon>.jar`
(og same adresse med `.asc` til slutt for signaturen). Ved nye nedlastingar blir også `.sha1` henta frå same adresse
og kontrollert før signaturen. Standard basis-URL er `https://github.com`. Han kan setjast til ein intern spegel
med same struktur, men spegelen må tilby alle tre filene: `.jar`, `.jar.asc` og `.jar.sha1`.

Krav til signaturen og nøkkelen:

- Signaturen må vere ein frittståande (detached) GPG-signatur av typen binærdokument.
- Hash-algoritmen må vere SHA-256, SHA-384, SHA-512, SHA3-256 eller SHA3-512. SHA-1, MD5, RIPEMD-160 og SHA-224
  blir avviste.
- Nøkkelen som har signert, må finnast i ei av nøkkelfilene i `kosmos.verification.publicKeyPaths`.
- Primærnøkkelen kan ikkje vere utgått eller tilbakekalla. Er signaturen laga med ein undernøkkel, gjeld det same
  for undernøkkelen.
- Den nyaste sjølvsignaturen på nøkkelen som har signert, må ha nøkkelflagget for å signere data. Manglar
  nøkkelflagga i den nyaste sjølvsignaturen, blir nøkkelen avvist, sjølv om ein eldre sjølvsignatur tillét signering.

Er den offentlege signeringsnøkkelen utgått eller tilbakekalla, kan JAR-filer signerte med han ikkje verifiserast,
heller ikkje ved rollback. Ved utløp må nøkkelfila oppdaterast med den forlengde nøkkelen frå
[docs.digdir.no](https://docs.digdir.no/resources/eformidling/public_keys/eformidling-key.asc). Ved tilbakekalling må ein ta i bruk ein ny,
ikkje-tilbakekalla nøkkel og JAR-signaturar laga med han.

## Release (for interne)
Release av ny versjon gjerast via GitHub GUI
- Gå til "Releases" i GitHub repo
- Klikk på "Draft a new release"
- Vel tagg (ny eller eksisterande). Bruk semantisk versjonering med v-prefiks, slik GitHub tilrår, til dømes `v4.0.0`.
  Taggen blir brukt som han er, med prefikset: Maven-versjonen blir `v4.0.0` og JAR-fila `kosmos-v4.0.0.jar`.
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

Byggjejobben sender alle secrets vidare til den felles workflowen `ci-build-publish-lib.yml` (`secrets: inherit`).
Han treng `GH_PACKAGES_READ_USER` / `GH_PACKAGES_READ_PAT` for å lese frå GitHub Packages. KOSMOS har sjølv ingen
interne Maven-avhengigheiter, så secretane trengst berre fordi den felles workflowen brukar dei.

Med [test-signing-secrets.yaml](.github/workflows/test-signing-secrets.yaml) kan du sjekke signerings-secretane utan å
lage ein release: at nøkkelen har rett fingeravtrykk, at passordet er rett, og at signaturen verifiserer mot den
publiserte nøkkelen. Workflowen blir starta manuelt (Actions → "Test signing secrets" → "Run workflow"). Køyr han etter
at nøkkelen er forlenga og secretane oppdaterte, eller når signeringa i release-workflowen feilar.

Skript for manuell signering, verifisering og forlenging av kodesigneringsnøkkelen finst i
[efm-integrasjonspunkt/signering](https://github.com/felleslosninger/efm-integrasjonspunkt/tree/main/signering).
Når nøkkelen blir forlenga med `extend.sh`, må både `EFM_GPG_PRIVATE_KEY` og den publiserte offentlege nøkkelen oppdaterast.

Signaturen kan verifiserast lokalt med `verify.sh` derifrå, eller direkte med gpg mot den publiserte nøkkelen:
```bash
curl -fsSLO https://docs.digdir.no/resources/eformidling/public_keys/eformidling-key.asc
# fingeravtrykket skal vere AEF2 7AA6 948A 3856 932A  F98E CA56 4339 3753 ECE3
gpg --show-keys --with-fingerprint eformidling-key.asc
gpg --import eformidling-key.asc
gpg --verify kosmos-v4.0.0.jar.asc kosmos-v4.0.0.jar
```
`gpg --verify` skal svare «Good signature» og vise same fingeravtrykk.

## License

This project is licensed under the BSD 3-Clause License. See the [LICENSE](LICENSE) file for details.
