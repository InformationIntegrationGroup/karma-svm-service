#/bin/bash

mvn clean package
cp ./target/svm-service.war /Users/shaarif/Desktop/Tomcat/webapps/
sh /Users/shaarif/Desktop/Tomcat/bin/shutdown.sh
tail -f /Users/shaarif/Desktop/Tomcat/logs/catalina.out

