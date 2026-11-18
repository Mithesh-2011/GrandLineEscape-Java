@echo off
title Grand Line Escape - Java Backend
if not exist out mkdir out
echo Compiling 12 Java Backend Classes...
javac -d out src\*.java
if %errorlevel% neq 0 (
    echo Compilation failed! Verify Java JDK 11+ is installed.
    pause
    exit /b %errorlevel%
)
echo Starting Java Backend Server on http://localhost:8080 ...
java -cp out Main
pause
