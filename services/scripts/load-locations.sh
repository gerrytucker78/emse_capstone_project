#!/bin/bash

curl -X PUT -H "Content-Type: application/json" -d@data/locations.json http://localhost/locations
