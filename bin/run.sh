#!/usr/bin/env bash
mvn -f ../pom.xml clean package -DskipTests
java -cp  "../target/scala-dbtest-0.0.1-fat-tests.jar" -Dconfig.file="../src/test/resources/application.conf" org.scalatest.tools.Runner  -Dtenantid=TENANT1 -eNDXEHLO -s samples.dbSpec -l DbTestIgnore
java -cp  "../target/scala-dbtest-0.0.1-fat-tests.jar" -Dconfig.file="../src/test/resources/application.conf" org.scalatest.tools.Runner  -Dtenantid=TENANT1 -eNDXEHLO -s samples.dbSpec -n DbTestIgnore

