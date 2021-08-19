#!/bin/bash

pwd

ls -alh

java -Dserver.port=8083 -jar /home/root/searchengine/docker/app.jar
