Karma DataMining Service
========================

## About
This project is a rest based implementation for various machine learning services like SVM, Decision Trees, etc. The machine learning services are implmented in R and the webservice is implemented using Jersey framework in java.

## Installation and Setup ##
System Requirements: **Java 1.6, Maven 3.0** and above.

To compile the code run this from the folder of web_service/svm-service/:
`mvn clean package`

Now deploy the webservice to tomcat / jetty / etc
```
cp web_service/svm-service/target/dm-service.war /location/of/the/tomcat/webapps/directory
```
Copy the Rscript directory to the same level/location as webapps
```
cp -r cp -r Rscripts/ /location/of/the/tomcat/
```
Copy the sqlite database file to the same location as webapps
```
cp services_db.sqlite /location/of/the/tomcat/
```
Now start the server. Once the server has started (assuming post) point your browser to **http://localhost:8080/dm-service.

## Source Code Directories ##
The root directory contains the following:

1. web_service : This directory contains the web service code
2. Rscripts : This directory contains the R scripts that are invoked from Java
3. services_db.sqlite : The SQLite database file that is used to store various information reqired for each service


