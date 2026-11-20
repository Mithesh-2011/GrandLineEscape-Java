@echo off
title Grand Line Escape - Spring Boot
mvn spring-boot:run
if %errorlevel% neq 0 (
    echo Startup failed. Verify Java 17+ and Maven are installed.
    pause
    exit /b %errorlevel%
)
pause
