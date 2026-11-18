#!/usr/bin/env bash
set -e
mkdir -p out
echo "Compiling 12 Java Backend Classes..."
javac -d out src/*.java
echo "Starting Java Backend Server on http://localhost:8080 ..."
java -cp out Main
