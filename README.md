# scala-dbtest

Use scalikeJDBC, scalatest, json4s to perform data verification on an ETL run 

# How to run?
Clone this project, update the config files and then run ./bin/run.sh


# run.sh - Run the fat jar from command line
## mvn -f ../pom.xml clean package -DskipTests
Compile and create fat jar
## java -cp  "../target/scala-dbtest-0.0.1-fat-tests.jar" -Dconfig.file="../src/test/resources/application.conf" org.scalatest.tools.Runner  -Dtenantid=TENANT1 -eNDXEHLO -s samples.dbSpec -l DbTestIgnore
Run test cases excluding the tag 'DbTestIgnore'
## java -cp  "../target/scala-dbtest-0.0.1-fat-tests.jar" -Dconfig.file="../src/test/resources/application.conf" org.scalatest.tools.Runner  -Dtenantid=TENANT1 -eNDXEHLO -s samples.dbSpec -n DbTestIgnore
Run test cases including the tag 'DbTestIgnore'


