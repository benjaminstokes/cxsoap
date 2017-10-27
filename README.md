#Checkmarx Portal SOAP API Utility/Test App

Spring Boot application, uses Spring WebServiceGatewaySupport for SOAP client functionality.

###Release Notes
#####v0.1.0
* Dumps a list of Cx users along with their permissions to users.csv file
* Includes several other SOAP api requests testable via the CxPortalClientTests

#####Usage:

```
java -jar cxsoap-0.1.0-SNAPSHOT.jar <CxHost> <user> <password>
```
For example: 

```
java -jar cxsoap-0.1.0-SNAPSHOT.jar http://cxlocal admin@cx P@ssw0rd
```
