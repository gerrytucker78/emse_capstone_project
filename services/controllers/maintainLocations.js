var beacons = true;
var halls = false;
var rooms = false;
var showPaths = true;
var exits = true;
var stairs = true;
var labels = true;


var maintainLocations = angular.module('maintainLocations', ['ngSanitize']);
maintainLocations.controller('locationController', ['$scope','$http', function($scope,$http) {
    $scope.message = 'Initial Load';

    $scope.togglePaths = function() {
        showPaths = !showPaths;
        drawData();
    };

    $scope.toggleBeacons = function() {
        beacons = !beacons;
        drawData();
    };

    $scope.toggleHalls = function() {
        halls = !halls;
        drawData();
    };

    $scope.toggleRooms = function() {
        rooms = !rooms;
        drawData();
    };

}]);

function drawData() {
    var c = document.getElementById("myCanvas");
    var ctx = c.getContext("2d");
    var img = document.getElementById("mapImage");
    ctx.drawImage(img, 0, 0);

    var markerSize = 5;
    var markerOffset = markerSize/2;

    var c=document.getElementById("myCanvas");
    var ctx=c.getContext("2d");

    for (var i = 0; i < locations.length; i++) {
        var loc = locations[i];
        ctx.font="10px Arial";


        if ((locations[i].type == "BEACON" && beacons) || (locations[i].type == "HALL" && halls) || (locations[i].type == "ROOM" && rooms) || (locations[i].type == "EXIT" && exits) || (locations[i].type == "STAIRS" && stairs)) {
            if (labels) {
                ctx.fillText(i,locations[i].pixel_loc_x-markerOffset-10,locations[i].pixel_loc_y+10);
            }

            if (locations[i].type == "BEACON") {
                ctx.fillStyle="#FF0000"
            } else {
                ctx.fillStyle="#000000"
            }

            ctx.fillRect((loc.pixel_loc_x + markerOffset),(loc.pixel_loc_y + markerOffset),5,5);
        }
    }

    for (var i = 0; i < paths.length; i++) {
        var cPath = paths[i];

        if (showPaths) {
            ctx.beginPath();
            ctx.moveTo(locations[cPath.start_id].pixel_loc_x+markerSize,locations[cPath.start_id].pixel_loc_y+markerSize);
            ctx.lineTo(locations[cPath.end_id].pixel_loc_x+markerSize,locations[cPath.end_id].pixel_loc_y+markerSize);
            ctx.stroke();
        }
    }
}
