Hello!!

This app will take some configuration to get working
1. Create a Maven project in eclipse with at least Java 11 (use the orginal project name "maven-vision-jar" as shown here -> "package com.vision.maven.maven_vision_jar;") 
2. All java files need to me within the same package
3. key.json must always be accessible to the program (please don't do anything crazy with it, it is linked to my google billing account and I pay for the queries - Andrew :) )
   3.1 no key.json file anymore, grab ur own from google lmao
5. Replace the premade pom.xml with the provided pom.xml (it contains the project dependancies)
6. Docker is required for the project to function, it serves as the SQL database backend
	5.1: to configure docker container use this in the command line (with docker cli installed)
		--> docker run --name project_database -e MYSQL_ROOT_PASSWORD=***** -e MYSQL_DATABASE=groupproject -p 3306:3306 -d mysql:8.0 {make ur own SQL database lmao}
		* note, if you have something running on 3306 this command will fail, the docker container needs to listen on this port  
7. Use the "Main" java file to run the application once everything is configured :)

I believe that is all of the configuration needed to run this project on your machine, If you encounter any problems please reach out to me at any time :)

- Andrew Boyer, email: *******@psu.edu, phone: ******
