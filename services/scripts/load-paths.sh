#!/bin/bash

curl -X PUT -H "Content-Type: application/json" -d@data/paths.json http://localhost/locations/paths
