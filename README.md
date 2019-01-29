#Move-Deploy-Manager

## Installation

### Obtain the DIFI JAR signer public key certificate

Obtain the JAR signer public key certificate from Direktoratet for forvaltning og IKT (DIFI). 
 
### Import the Certificate as a Trusted Certificate

Suppose that you have received from DIFI

* The latest deploymanager JAR file.
* The file dif_certsign_2019_public.cer containing the public key certificate for code signing 

First you need to create a keystore named deploymananger.jks and import the certificate into an entry with an alias of difi.

```bash
keytool -import -alias difi -file dif_certsign_2019_public.cer -keystore deploymanager.jks
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

#### Local property file for Move-Deploy-Manager

You will need a file named deploymanager-local.properties in the same folder as winsw.
Here is an example - Please replace the properties with your information:

```properties
spring.profiles.active=production

app.logger.enableSSL=true
app.logger.jks=file:c:/jks/logstash.jks
app.logger.password=logstash

# Replace with your organization
deploymanager.orgnumber=900000000

deploymanager.root=c:/apps/integrasjonspunkt

deploymanager.keystore.path=file:c:/jks/deploymanager.jks
deploymanager.keystore.password=xxx
deploymanager.keystore.alias=difi

# E-mail is optional. Please specify these properties 
# to receive e-mails when the deploy-manager updates the integrasjonspunkt-application.
deploymanager.mail.recipient=someone@yourdomain.no
deploymanager.mail.from=noreply@yourdomain.no

spring.mail.host=smtp.yourdomain.no
```  





