var beacons = true;
var halls = false;
var rooms = false;
var showPaths = true;
var exits = true;
var stairs = true;
var labels = true;
var floor;

var locations = {};
var paths = [];
var sensors = [];
var changedLocations = {};

var canvasState;
var typesVisible = new TypeVisible();


var maintainLocations = angular.module('maintainLocations', ['ngSanitize']);
maintainLocations.controller('locationController', ['$scope', '$http', function ($scope, $http) {
    $scope.message = 'Initial Load';
    $scope.locationName = 'Select a location...';

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
        $scope.message = "Loading /locations"
        $http.get('/locations').success(function (data, status, headers, config) {
            $scope.message = "Results" + data;

            for (i = 0; i < data.length; i++) {
                locations[data[i].location_id] = data[i];
            }


            $http.get('/locations/paths').success(function (data, status, headers, config) {
                paths = data;
                $scope.message = "Paths Results" + data;

                $http.get('/sensors').success(function (data, status, headers, config) {
                    sensors = data;
                    loadData();
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

    $scope.saveData = function() {
      var changedLocations = [];
        var locationShapes = canvasState.shapes;

        // Loop through all locations and pull out changed ones
        for (var i = 0; i < locationShapes.length; i++) {
            if (!locationShapes[i].valid) {
                var location = locationShapes[i].location;
                location.pixel_loc_x = locationShapes[i].x;
                location.pixel_loc_y = locationShapes[i].y;
                locationShapes[i].valid = true;
                changedLocations.push(location);
            }
        }

        // Put updates
        $http.put('/locations/update',changedLocations).success(function (data, status, headers, config) {
            $scope.loadData();
        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });
    };

    $scope.updateLocationData = function() {
        canvasState.selection.location.name = document.getElementById("location_name").value
        canvasState.selection.location.type = document.getElementById("location_type").value
    };

}]);

function initDrawing(currentFloor) {
    var c = document.getElementById("myCanvas");
    var img = document.getElementById("mapImage");

    canvasState = new CanvasState(c, img, currentFloor);

}

function loadData(currentFloor) {
    canvasState.shapes = []

    for (var i in locations) {

        canvasState.addShape(new LocationShape(locations[i], 5, 5, "#000000"));
    }
}


function drawData() {
    var c = document.getElementById("myCanvas");
    var ctx = c.getContext("2d");
    var img = document.getElementById("mapImage");
    ctx.drawImage(img, 0, 0);

    var markerSize = 5;
    var markerOffset = markerSize / 2;

    var c = document.getElementById("myCanvas");
    var ctx = c.getContext("2d");

    for (var i in locations) {
        var loc = locations[i];
        ctx.font = "10px Arial";

        if (locations[i].floor == floor) {
            ctx.fillStyle = "#000000"
            if ((locations[i].type == "BEACON" && beacons) || (locations[i].type == "HALL" && halls) || (locations[i].type == "ROOM" && rooms) || (locations[i].type == "EXIT" && exits) || (locations[i].type == "STAIRS" && stairs)) {
                if (labels) {
                    ctx.fillText(i, locations[i].pixel_loc_x - markerOffset - 10, locations[i].pixel_loc_y + 10);
                }

                if (locations[i].type == "BEACON") {
                    ctx.fillStyle = "#FF0000"
                } else {
                    ctx.fillStyle = "#000000"
                }

                ctx.fillRect((loc.pixel_loc_x + markerOffset), (loc.pixel_loc_y + markerOffset), 5, 5);
            }
        }
    }

    for (var i = 0; i < sensors.length; i++) {
        var loc = sensors[i];
        ctx.font = "10px Arial";

        if (sensors[i].floor == floor) {
            if (beacons) {

                ctx.fillStyle = "#FF0000"
                if (labels) {
                    ctx.fillText(i, sensors[i].pixel_loc_x - markerOffset - 10, sensors[i].pixel_loc_y + 10);
                }
                ctx.fillRect((loc.pixel_loc_x + markerOffset), (loc.pixel_loc_y + markerOffset), 5, 5);
            }
        }
    }


    for (var i = 0; i < paths.length; i++) {
        var cPath = paths[i];

        if (showPaths && locations[cPath.start_id].floor == floor) {
            ctx.beginPath();
            ctx.moveTo(locations[cPath.start_id].pixel_loc_x + markerSize, locations[cPath.start_id].pixel_loc_y + markerSize);
            ctx.lineTo(locations[cPath.end_id].pixel_loc_x + markerSize, locations[cPath.end_id].pixel_loc_y + markerSize);
            ctx.stroke();
        }
    }
}