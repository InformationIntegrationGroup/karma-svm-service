Karma DataMining Service
========================

## About
This project is a rest based implementation for various machine learning services like SVM, Decision Trees, etc. The machine learning services are implmented in R and the webservice is implemented using Jersey framework in java.

## Installation and Setup ##
System Requirements: **Java 1.6, Maven 3.0** and above.

To compile the code run this from the folder of web_service/svm-service/:
`mvn clean install`

Now, to run the web service:
```
cd web_service/svm-service/
mvn jetty:run
```

Once the server has started point your browser to **http://localhost:8080/**. To start it on a port other than 8080 (e.g. Port number 9999) 
`mvn -Djetty.port=9999 jetty:run`


## Source Code Directories ##
The root directory contains the following:
1. web_service : This directory contains the web service code
2. Rscripts : This directory contains the R scripts that are invoked from Java
3. services_db.sqlite : The SQLite database file that is used to store various information reqired for each service


