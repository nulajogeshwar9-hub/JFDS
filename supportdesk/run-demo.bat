@echo off
echo ===================================================
echo   SupportDesk - Starting in Standalone Demo Mode
echo ===================================================
echo Using Embedded In-Memory Database (No MySQL Required)
echo Default Admin: admin@supportdesk.com / Admin@123
echo Default Agent: agent@supportdesk.com / Agent@123
echo Default Customer: customer@supportdesk.com / Customer@123
echo Portal URL: http://localhost:8080/login
echo ===================================================

if "%JAVA_HOME%"=="" (
    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
)
set PATH=%JAVA_HOME%\bin;%PATH%

java -jar target\supportdesk-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
pause

