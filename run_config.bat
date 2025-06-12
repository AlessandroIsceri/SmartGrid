@REM call mvn clean
call mvn compile
@REM call mvn test
@REM call mvn package
@REM call mvn install
call mvn -Pjade-main exec:java
call mvn -Pjade-start-owner exec:java