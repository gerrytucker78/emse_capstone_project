var floor;

var locations = {};
var locationsByName = {};
var paths = [];
var sensors = [];
var changedLocations = {};
var emergencies = {};

var canvasState;
var typesVisible = new TypeVisible();

var maintainEmergencies = false;


var maintainLocations = angular.module('maintainLocations', ['ngSanitize']);
maintainLocations.controller('locationController', ['$scope', '$http', '$location', function ($scope, $http, $location) {
    $scope.message = 'Initial Load';
    $scope.locationName = 'Select a location...';
    $scope.floor;
    $scope.maintainEmergencies = maintainEmergencies;

    $scope.togglePaths = function () {
        showPaths = !showPaths;
        $scope.message = 'Toggling paths: ' + showPaths

        drawData();
    };

    $scope.toggleBeacons = function () {
        beacons = !beacons;
        drawData();
    };

    $scope.toggleHalls = function () {
        typesVisible.toggleTypeVisible(TypeVisibleEnum.HALL)
    };

    $scope.toggleLabels = function () {
        labels = !labels;
        drawData();
    };

    $scope.toggleRooms = function () {
        typesVisible.toggleTypeVisible(TypeVisibleEnum.ROOM)
    };

    $scope.loadData = function () {
        $scope.message = "Loading /locations";

        $http.get('/locations').success(function (data, status, headers, config) {
            $scope.message = "Results" + data;
            locations = {};
            locationsByName = {};

            for (i = 0; i < data.length; i++) {
                // Only add locations on this floor
                if (data[i].floor == floor) {
                    locations[data[i].location_id] = data[i];
                    locationsByName[data[i].name] = data[i];
                }
            }


            $http.get('/locations/paths').success(function (data, status, headers, config) {
                paths = data;
                $scope.message = "Paths Results" + data;

                $http.get('/sensors').success(function (data, status, headers, config) {
                    sensors = data;

                    if ($scope.maintainEmergencies) {
                        $http.get('/emergencies').success(function (data, status, headers, config) {
                            emergencies = {};

                            for (i = 0; i < data.length; i++) {
                                if (locations[data[i].location_id] != undefined) {
                                    emergencies[data[i].location_id] = data[i];
                                }
                            }

                            $scope.message = "Emergencies Results" + data;

                            loadData(floor);
                        }).error(function (data, status, headers, config) {
                            // TO-DO: Need to fill in.
                        });
                    } else {
                        loadData(floor);
                    }

                }).error(function (data, status, headers, config) {
                    // TO-DO: Need to fill in.
                });
            }).error(function (data, status, headers, config) {
                // TO-DO: Need to fill in.
            });

        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });

    };

    $scope.saveLocationData = function () {
        var changedLocations = [];
        var shapes = canvasState.shapes;

        $scope.updateLocationData;

        // Loop through all locations and pull out changed ones
        for (var i = 0; i < shapes.length; i++) {
            if (!shapes[i].valid) {
                if (shapes[i] instanceof LocationShape) {
                    var location = shapes[i].location;
                    location.pixel_loc_x = shapes[i].x;
                    location.pixel_loc_y = shapes[i].y;
                    shapes[i].valid = true;
                    changedLocations.push(location);
                }
            }
        }

        // Put updates
        $http.put('/locations/update', changedLocations).success(function (data, status, headers, config) {
            $scope.loadData();
        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });

        canvasState.selection = null;

        document.getElementById("location_id").value = "";
        document.getElementById("location_name").value = "";
        document.getElementById("location_type").value = null;

        $scope.locationType = null;
        $scope.location = null;

        if ($scope.maintainEmergencies) {
            document.getElementById("emergency_notes").value = "";
            document.getElementById("emergency_type").value = null;

            $scope.emergency_notes = "";
            $scope.emergency_type = null;
        }



    };

    $scope.updateLocationData = function () {
        canvasState.selection.location.name = document.getElementById("location_name").value
        canvasState.selection.location.type = document.getElementById("location_type").value
    };

    $scope.deleteLocationData = function () {
        var selectedLocation = canvasState.selection;

        var selectedEmergency = selectedLocation.emergency;

        // Delete Location
        $http.delete('/locations/id/' + selectedLocation.location.location_id).success(function (data, status, headers, config) {
            $scope.loadData();
        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });

        // Delete Emergency
        if ($scope.maintainEmergencies && selectedEmergency != null && selectedEmergency != undefined) {
            $http.delete('/emergencies/id/' + selectedEmergency.emergency_id).success(function (data, status, headers, config) {
                $scope.loadData();
            }).error(function (data, status, headers, config) {
                // TO-DO: Need to fill in.
            });
        }

        canvasState.selection = null;

        document.getElementById("location_id").value = "";
        document.getElementById("location_name").value = "";
        document.getElementById("location_type").value = null;

        if ($scope.maintainEmergencies) {
            document.getElementById("emergency_notes").value = "";
            document.getElementById("emergency_type").value = null;
        }
    };


    $scope.savePath = function () {
        // Put updates
        $http.post('/locations/paths',{start_id: $scope.start_node_id, end_id: $scope.end_node_id, weight: 1}).success(function (data, status, headers, config) {
            $http.post('/locations/paths',{start_id: $scope.end_node_id, end_id: $scope.start_node_id, weight: 1}).success(function (data, status, headers, config) {

            $scope.loadData();
            }).error(function (data, status, headers, config) {
                // TO-DO: Need to fill in.
            });
        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });
    };

    $scope.deletePath = function () {
        // Put updates
        $http.delete('/locations/paths/ids/' + $scope.start_node_id + "," + $scope.end_node_id).success(function (data, status, headers, config) {
            $http.delete('/locations/paths/ids/' + $scope.end_node_id + "," + $scope.start_node_id).success(function (data, status, headers, config) {
            $scope.loadData();
            }).error(function (data, status, headers, config) {
                // TO-DO: Need to fill in.
            });

        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });
    };

    $scope.updateEmergencyData = function () {
        if (canvasState.selection.emergency == undefined) {
            canvasState.selection.emergency = {emergency_id: "", notes: "", emergency_type: "", emergency_state: "",
                location_id: canvasState.selection.location.location_id, start: "", last_update: "", end: ""};

            document.getElementById("emergency_notes").value = "";
            document.getElementById("emergency_type").value = null;
        }

        $scope.emergency_state = document.getElementById("emergency_state").value;

        canvasState.selection.emergency.emergency_notes = document.getElementById("emergency_notes").value;
        canvasState.selection.emergency.emergency_type = document.getElementById("emergency_type").value;
    };


    $scope.saveEmergencyData = function() {
        var selectedLocation = canvasState.selection.location;
        var selectedEmergency = canvasState.selection.emergency;

        if (selectedEmergency.emergency_id == "") {
            selectedEmergency.emergency_start = Date.now();
        } else if ($scope.emergency_state == "UPDATE") {
            selectedEmergency.emergency_last_update = Date.now();
        } else if ($scope.emergency_state == "END") {
            selectedEmergency.emergency_last_update = Date.now();
            selectedEmergency.emergency_end = selectedEmergency.emergency_last_update;
        }

        // Put updates
        if (selectedEmergency.emergency_id == "") {
            selectedEmergency.emergency_id = null;
            $http.post('/emergencies', selectedEmergency).success(function (data, status, headers, config) {
                $scope.loadData();
            }).error(function (data, status, headers, config) {
                // TO-DO: Need to fill in.
            });
        } else {
            $http.put('/emergencies/id/' + selectedEmergency.emergency_id, selectedEmergency).success(function (data, status, headers, config) {
                $scope.loadData();
            }).error(function (data, status, headers, config) {
                // TO-DO: Need to fill in.
            });
        }

        document.getElementById("location_id").value = "";
        document.getElementById("location_name").value = "";
        document.getElementById("location_type").value = null;

        document.getElementById("emergency_notes").value = "";
        document.getElementById("emergency_type").value = null;
        canvasState.selection = null;

    }

    /**
     * Function to update backend database for beacons
     */
    $scope.saveBeaconData = function () {
        var changedBeacons = [];
        var shapes = canvasState.shapes;

        // Loop through all locations and pull out changed ones
        for (var i = 0; i < shapes.length; i++) {
            if (!shapes[i].valid) {
                if (shapes[i] instanceof BeaconShape) {
                    var beacon = shapes[i].sensor;
                    beacon.pixel_loc_x = shapes[i].x;
                    beacon.pixel_loc_y = shapes[i].y;
                    changedBeacons.push(beacon);
                }
            }
        }

        // Put updates
        $http.put('/sensors/update', changedBeacons).success(function (data, status, headers, config) {
            $scope.loadData();
        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });
    };

    $scope.deleteBeaconData = function () {
        var selectedBeacon = canvasState.selection;

        // Put updates
        $http.delete('/sensors/id/' + selectedBeacon.sensor.sensor_id).success(function (data, status, headers, config) {
            $scope.loadData();
        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });
    };


    $scope.updateBeaconData = function () {
        canvasState.selection.sensor.name = document.getElementById("beacon_name").value
    };

    $scope.init = function() {
        var parms = $location.search();

        if ($scope.floor == undefined) {
            if (parms.floor != undefined) {
                $scope.floor = parms.floor;
            } else {
                $scope.floor = 2;
            }
        }

        $scope.maintainEmergencies = maintainEmergencies;


        floor = $scope.floor;
        initDrawing(floor);

        $scope.loadData()
    }


    $scope.init();
}]);

function initDrawing(currentFloor) {
    var c = document.getElementById("myCanvas");
    var img = document.getElementById("mapImage" + currentFloor);

    if (canvasState == undefined) {
        canvasState = new CanvasState(c, img, currentFloor);
    } else {
        canvasState.image = img;
        canvasState.floor = currentFloor;
    }


}

function loadData(currentFloor) {
    canvasState.shapes = []

    for (var i in locations) {

        canvasState.addShape(new LocationShape(locations[i], 5, 5, "#000000"));
        canvasState.shapes[canvasState.shapes.length-1].emergency = emergencies[i];
    }

    for (var i = 0; i < sensors.length; i++) {
        canvasState.addShape(new BeaconShape(sensors[i], 5, 5, "#FF0000"));
    }

    for (var i = 0; i < paths.length; i++) {
        var startNode;
        var endNode;

        startNode = locations[paths[i].start_id];
        endNode = locations[paths[i].end_id];

        if (startNode && endNode) {
            canvasState.addShape(new PathShape(startNode, endNode, 5, 5, "#228B22"));
        }
    }
}