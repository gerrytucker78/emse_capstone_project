var beacons = true;
var halls = false;
var rooms = false;
var showPaths = true;
var exits = true;
var stairs = true;
var labels = true;
var floor = 2;

var locations = {};
var paths = [];
var sensors = [];


var maintainLocations = angular.module('maintainLocations', ['ngSanitize']);
maintainLocations.controller('locationController', ['$scope', '$http', function ($scope, $http) {
    $scope.message = 'Initial Load';

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
        halls = !halls;
        drawData();
    };

    $scope.toggleHalls = function () {
        labels = !labels;
        drawData();
    };

    $scope.toggleRooms = function () {
        rooms = !rooms;
        drawData();
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
                    drawData();
                }).error(function (data, status, headers, config) {
                    // TO-DO: Need to fill in.
                });
            }).error(function (data, status, headers, config) {
                // TO-DO: Need to fill in.
            });

        }).error(function (data, status, headers, config) {
            // TO-DO: Need to fill in.
        });

    }

}]);

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