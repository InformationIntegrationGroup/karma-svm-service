Karma DataMining Service
========================

## About
This project is a rest based implementation for various machine learning services like SVM, Decision Trees, etc. The machine learning services are implmented in R and the webservice is implemented using Jersey framework in java.

## Installation and Setup ##
System Requirements: **Java 1.6, Maven 3.0** and above.

To compile the code run this from the folder of web_service/svm-service/
```
cd web_service/svm-service
mvn clean package
```

Now deploy the webservice to tomcat / jetty / etc
```
cp web_service/svm-service/target/dm-service.war /location/of/the/tomcat/webapps/directory
```
Copy the Rscript directory to the same level/location as webapps
```
cp -r Rscripts/ /location/of/the/tomcat/
```
Copy the sqlite database file to the same location as webapps
```
cp services_db.sqlite /location/of/the/tomcat/
```
Now start the server. Once the server has started (assuming port 8080 ) point your browser to **http://localhost:8080/dm-service.

## Source Code Directories ##
The root directory contains the following:

1. web_service : This directory contains the web service code
2. Rscripts : This directory contains the R scripts that are invoked from Java
3. services_db.sqlite : The SQLite database file that is used to store various information reqired for each service


## Using the Webservice ##

### /dm-service/service ###
This endpoint serves meta information about all the available services.
```
http://localhost/dm-service/service/all
```
The /all will fetch a list of all the available services along with their parameters
```
{
    "models": [
        {
            "Name": "SVM Training",
            "Description": "The SVM classification service. It assumes the last column of the service to be the class",
            "Url": "/svm/train",
            "Id": 1,
            "params": [
                {
                    "Name": "kernel_type",
                    "Description": "The kernel type for the service. Options are : linear, polynomial, radial basis, sigmoid",
                    "default_value": "linear",
                    "Id": 1
                },
                {
                    "Name": "c_type",
                    "Description": "The classificaion Type for SVM. Options: C-classification, nu-classification, one-classification, eps-regression, nu-regression",
                    "default_value": "C-classification",
                    "Id": 2
                },
                {
                    "Name": "model_name",
                    "Description": "User defined model name for the SVM",
                    "default_value": "system generated unique name",
                    "Id": 3
                }
            ]
        },
        {
            "Name": "SVM Testing",
            "Description": "This service accepts data in the POST payload, along with a model name that was generated from the training phase",
            "Url": "/svm/test",
            "Id": 2,
            "params": [
                {
                    "Name": "model_name",
                    "Description": "The trained model name that need to be loaded for execution. This is a mandatory param",
                    "default_value": "This is mandatory",
                    "Id": 4
                }
            ]
        }
    ]
}
```




