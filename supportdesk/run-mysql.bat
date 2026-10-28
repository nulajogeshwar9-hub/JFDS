@echo off
echo ===================================================
echo   SupportDesk - Starting in MySQL Production Mode
echo ===================================================
echo Connecting to MySQL on localhost:3306 (database: supportdesk)
echo Flyway migrations will automatically execute on startup.
echo ===================================================

if "%JAVA_HOME%"=="" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
)
set PATH=%JAVA_HOME%\bin;%PATH%

java -jar target\supportdesk-0.0.1-SNAPSHOT.jar
pause

