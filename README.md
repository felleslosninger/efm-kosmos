[![Build Status](https://dev.azure.com/dificloud/eformidling/_apis/build/status/felleslosninger.efm-deploy-manager?repoName=felleslosninger%2Fefm-deploy-manager&branchName=feature-MOVE-2110-pipeline)](https://dev.azure.com/dificloud/eformidling/_build/latest?definitionId=27&repoName=felleslosninger%2Fefm-deploy-manager&branchName=feature-MOVE-2110-pipeline)

#Efm-Deploy-Manager

## Installation
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
  <executable>java</executable>
    <arguments>-jar %BASE%\deploymanager-X.Y.Z.jar --spring.profiles.active=production --spring.config.additional-location=file:%BASE%\integrasjonspunkt-local.properties</arguments>
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
# Replace hosts and ports of URL with the location
# of your integrasjonspunkt.
deploymanager.integrasjonspunkt.baseURL=http://localhost:9093

# E-mail is optional. Please specify these properties 
# to receive e-mails when the deploy-manager updates the integrasjonspunkt-application.
deploymanager.mail.recipient=someone@yourdomain.no
deploymanager.mail.from=noreply@yourdomain.no

spring.mail.host=smtp.yourdomain.no
spring.mail.port=<set-your-port-here>
```  

#### Running deploymanager and integrasjonspunkt from different folders
The recommended setup (requires less configuration) is to have both JARs in the same directory. If for some reason you should prefer running the applications from different directories, the following settings have to be added.

Add the following property to deploymanager-local.properties:
```properties
deploymanager.integrasjonspunkt.home={path-to-where-your-integrasjonspunkt-runs}
```
Modify the arguments tag in deploymanager's XML configuration file:
```xml
<arguments>-jar %BASE%\deploymanager-X.Y.Z.jar --spring.profiles.active=production --spring.config.additional-location=file:{path-to-where-your-integrasjonspunkt-runs}\integrasjonspunkt-local.properties</arguments>
```