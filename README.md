#Move-Deploy-Manager

## Installation

### Obtain the DIFI JAR signer public key certificate

Obtain the JAR signer public key certificate from Direktoratet for forvaltning og IKT (DIFI). 
 
### Import the Certificate as a Trusted Certificate

Suppose that you have received from DIFI

* The latest deploymanager JAR file.
* The file dif_certsign_2019_withchain.crt containing the public key certificate for code signing 

First you need to create a keystore named deploymananger.jks and import the certificate into an entry with an alias of difi.

```bash
keytool -import -alias difi -file dif_certsign_2019_withchain.crt -keystore deploymanager.jks
```
The keytool prints the certificate information and asks you to verify it; For example, by comparing the displayed certificate fingerprints with those obtained from another (trusted) source of information. (Each fingerprint is a relatively short number that uniquely and reliably identifies the certificate.) For example, in the real world you can phone Stan and ask him what the fingerprints should be. He can get the fingerprints of the StanSmith.cer file he created by executing the command

```bash
keytool -printcert -file dif_certsign_2019_withchain.crt
```

If the fingerprints he sees are the same as the ones reported to you by keytool, then you both can assume that the certificate has not been modified in transit. You can safely let keytool procede to place a "trusted certificate" entry into your keystore. This entry contains the public key certificate data from the file dif_certsign_2019_withchain.crt. keytool assigns the alias stan to this new entry.

### Running Move-Deploy-Manager as a Windows service

We are using [https://github.com/kohsuke/winsw] as a Windows service wrapper. Please follow the installation instructions using the following configuration:

```xml
<configuration>
  
  <!-- ID of the service. It should be unique accross the Windows system-->
  <id>deploymanagersvc</id>
  <!-- Display name of the service -->
  <name>Deploymanager Service</name>
  <!-- Service description -->
  <description>Keeps the integrasjonspunkt application up-to-date.</description>
  
  <!-- Path to the executable, which should be started -->
  <executable>javaw</executable>
  <startargument>-jar</startargument>
  
  <!-- Start profile -->    
  <startargument>-Dspring.profiles.active=production</startargument>
  
  <startargument>-Ddeploymanager.shutdownURL=http://localhost:9092/manage/shutdown</startargument>  
  <startargument>-Ddeploymanager.healthURL=http://localhost:9092/manage/health</startargument>
  <startargument>-Ddeploymanager.verbose=false</startargument>

  <!-- The directory that will be used as the root-directory for the integrasjonspunkt application. -->  
  <startargument>-Ddeploymanager.root=C:\\temp\root</startargument>

  <!-- Keystore information. Please replace with correct path and password --> 
  <startargument>-Ddeploymanager.keystore.path=C:\\jks\deploymanager.jks</startargument>
  <startargument>-Ddeploymanager.keystore.password=xxxxxx</startargument>
  <startargument>-Ddeploymanager.keystore.alias=difi</startargument>
  
  <!-- Optional notification by email --> 
  <startargument>-Dspring.mail.host=mail.yourdomain.no</startargument>
  <startargument>-Ddeploymanager.mail.recipient=someone@yourdomain.no</startargument>
  <startargument>-Ddeploymanager.mail.from=noreply@yourdomain.no</startargument>

  <!-- Path to the Move-Deploy-Manager JAR file -->
  <startargument>%BASE%\deploymanager-X.X.X.jar</startargument>   
             
  <logpath>%BASE%\deploymanager-logs</logpath>
  
  <log mode="roll-by-size">
    <sizeThreshold>10240</sizeThreshold>
    <keepFiles>8</keepFiles>
  </log>
</configuration>
```

Please note that you need to use a local administration user when installing the service.




