#!/bin/bash

curl -X POST -H "Content-Type: application/json" -d@data/locations.json http://localhost/locations
