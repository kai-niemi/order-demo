#!/bin/bash

app_jarfile=target/order.jar

if [ ! -f "$app_jarfile" ]; then
    ./mvnw clean install
fi

java -jar $app_jarfile $*
