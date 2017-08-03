#! /bin/bash

# java -jar ../target/jersey-service-1.0.jar ../Config/jersey-all.ini
java -jar ../target/jersey-service-1.0.jar ../Config/algr-input.ini &
java -jar ../target/jersey-service-1.0.jar ../Config/data-input.ini &
java -jar ../target/jersey-service-1.0.jar ../Config/training.ini &
java -jar ../target/jersey-service-1.0.jar ../Config/execution.ini &