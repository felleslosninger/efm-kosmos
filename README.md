[![Build Status](https://dev.azure.com/dificloud/eformidling/_apis/build/status/felleslosninger.efm-kosmos?repoName=felleslosninger%2Fefm-kosmos&branchName=feature-MOVE-2110-pipeline)](https://dev.azure.com/dificloud/eformidling/_build/latest?definitionId=27&repoName=felleslosninger%2Fefm-kosmos&branchName=feature-MOVE-2110-pipeline)

#Efm-Kosmos
> Kontinuerlege oppdateringar for sikker meldingsutveksling i offentleg sektor - KOSMOS

## Installation
### Download
Get the KOSMOS JAR from Digdir's artefact repository: TODO
### Verify your download (recommended)
1. Get [GnuPG](https://gnupg.org/download/), if it is not already present in for instance Git Bash.
2. Download the detached signature from Digdir's artefact repository: TODO
3. Download the public key of Digdir's GPG signing certificate from TODO.
4. Verify that the fingerprint of the downloaded public key matches the expected value (published side by side with the key):
#### Fast approach (one line)
```shell
$ gpg --import-options show-only --import --fingerprint <path-to-downloaded-public-key-file>
pub   <algorithm> <creation-date> [<type-of-key>]
      0ABA FD4F AA80 9D6E EC8A  FD98 A30B 684A 308D 8FC8 # This is the fingerprint of the downloaded public key. 
      # Compare the value to the fingerprint published by Digdir.
uid           [ unknown] "eFormidling Name Here <eformidling-email-address-here>"
sub   <algorithm> <creation-date>

```
#### Conventional approach with explanations:
```shell
$ gpg --import <path-to-downloaded-public-key-file> # Import the public key into your local keyring.
# Output that shows successful import:
gpg: key <short-key-identifier-here>: public key "eFormidling Name Here <eformidling-email-address-here>" imported
gpg: Total number processed: 1
gpg:               imported: 1

$ gpg --list-keys # List the keys in your local keyring.
# ... Keys are listed on the following format:
pub   <algorithm> <creation-date> [<type-of-key>]
      0ABAFD4FAA809D6EEC8AFD98A30B684A308D8FC8 # Long key identifier
uid           [ unknown] "eFormidling Name Here <eformidling-email-address-here>"
sub   <algorithm> <creation-date>

$ gpg --fingerprint <put-long-key-identifier-or-email-address-here>
# Output should be similar to the fast approach.

```
#### Manual verification of .jar file
You may also want to manually verify the .jar file using the detached signature you can download above. This is also done automatically by the application.

**Example:**
```shell
gpg --verify "integrasjonspunkt-X.Y.Z-SNAPSHOT.jar.asc" "integrasjonspunkt-X.Y.Z-SNAPSHOT.jar"
gpg: Signature made Tue Mar 16 10:16:16 2021 CET
gpg:                using RSA key 0ABAFD4FAA809D6EEC8AFD98A30B684A308D8FC8
gpg:                issuer "issuerEmail"
gpg: Good signature from "LongName (test2) <issuerEmail>" [ultimate]
``` 

### Running KOSMOS as a Windows service

We are using [WinSW](https://github.com/kohsuke/winsw) as a Windows service wrapper. Please follow the installation instructions using the following configuration:

```xml
<configuration>
  
  <!-- ID of the service. It should be unique accross the Windows system-->
  <id>kosmossvc</id>
  <!-- Display name of the service -->
  <name>Kosmos Service</name>
  <!-- Service description -->
  <description>Keeps the integrasjonspunkt application up-to-date.</description>
  
  <!-- Path to the executable, which should be started -->
  <executable>java</executable>
    <arguments>-jar %BASE%\kosmos-X.Y.Z.jar --spring.profiles.active=production</arguments>
  <logpath>%BASE%\kosmos-logs</logpath>
  
  <log mode="roll-by-size">
    <sizeThreshold>10240</sizeThreshold>
    <keepFiles>8</keepFiles>
  </log>
</configuration>
```
**Please note that you need to use a local administration user when installing the service.**
#### Local property file for KOSMOS

You will need a file named kosmos-local.properties in the same folder as winsw.
Here is an example - Please replace the properties with your information:

```properties
difi.move.org.number=<your-nine-digit-organization-number>

# Replace hosts and ports of URL with the location
# of your integrasjonspunkt.
kosmos.integrasjonspunkt.baseURL=http://localhost:9093

# E-mail is optional. Please specify these properties 
# to receive e-mails when KOSMOS updates the integrasjonspunkt-application.
kosmos.mail.recipient=someone@yourdomain.no
kosmos.mail.from=noreply@yourdomain.no

spring.mail.host=smtp.yourdomain.no
spring.mail.port=<set-your-port-here>
```  
#### Other default assumptions
- KOSMOS and Integrasjonspunkt JARs are located in the same directory.
- The public key of the GPG certificate used to sign Digdir's integrasjonspunkt is present in the same directory. If not, please refer to the [documentation]  (https://docs.digdir.no/) for instructions on how to configure a different path.

#### Running KOSMOS and integrasjonspunkt from different folders
The recommended setup (requires less configuration) is to have both JARs in the same directory. If for some reason you should prefer running the applications from different directories, the following settings have to be added.

Add the following property to kosmos-local.properties:
```INI
kosmos.integrasjonspunkt.home={path-to-where-your-integrasjonspunkt-runs}
```
Modify the arguments tag in kosmos's XML configuration file:
```xml
<arguments>-jar %BASE%\kosmos-X.Y.Z.jar --spring.profiles.active=production --spring.config.additional-location=file:{path-to-where-your-integrasjonspunkt-runs}\integrasjonspunkt-local.properties</arguments>
```

### Running KOSMOS on Linux
You can use the same properties-file as found above. To get it up and running you should for instance have the following files in your directory: 
- kosmos-local.properties
- kosmos.jar
- integrasjonspunkt-local.properties
- integrasjonspunkt-keystore.jks (your own keystore for the integrasjonspunkt to use)
- eformidling-key.asc ([can be found here](https://docs.digdir.no/eformidling_auto_update.html#verifisere-sertifikatet))

You can choose to run in your current shell or as a task by using these commands:

Run in current shell
```java -jar kosmos-x.y.z.jar -Dspring.profiles.active=staging -Dspring.config.additional-location=file:%BASE%\integrasjonspunkt-local.properties```

Run as a task
```java -jar kosmos-x.y.z.jar -Dspring.profiles.active=staging -Dspring.config.additional-location=file:%BASE%\integrasjonspunkt-local.properties &```

When this command is executed the PID for the process will be returned. THis can be used if you need to stop the process. You would also find it by using for instance *htop* or *top* and shut down the Java-process. The integrasjonspunkt will start as its own Java-process.