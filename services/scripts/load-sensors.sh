#!/bin/bash

curl -X PUT -H "Content-Type: application/json" -d@data/sensors.json http://localhost/sensors
