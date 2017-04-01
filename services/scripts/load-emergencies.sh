#!/bin/bash

curl -X PUT -H "Content-Type: application/json" -d@data/emergencies.json http://localhost/emergencies
