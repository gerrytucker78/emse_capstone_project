var request = require('supertest');
var config = require('config');
var simulation = config.get('simulation');
var fs = require('fs');

describe('Locations', function () {
    this.timeout(10000);
    var server;
    var testLocations = [];
    var testFloors = [];
    var testBlockedAreas = [];
    var testPaths = [];
    var testNavigable = [];

    before(function (done) {
        server = require('../app.js');

        var simLocations = simulation.get("locations");
        var simPaths = simulation.get('paths')
        var totalLocCount = 0;
        var totalPathCount = 0;

        var locationsSetupComplete = false;
        var pathSetupComplete = false;

        request(server).delete('/locations').send().end(function (err, res) {
            for (var i = 0; i < simLocations.length; i++) {
                var simLoc = simLocations[i];

                testLocations.push(simLoc);

                if (simLoc.type == "FLOOR") {
                    testFloors.push(simLoc);
                } else if (simLoc.type == "BLOCKED_AREA") {
                    testBlockedAreas.push(simLoc);
                } else {
                    testNavigable.push(simLoc);
                }

                request(server).post('/locations').send(simLoc).end(function (err, res) {
                    totalLocCount++;
                    if (totalLocCount == testLocations.length) {
                        locationsSetupComplete = true;


                        if (pathSetupComplete && locationsSetupComplete) {
                            done()
                        }
                    }
                });
            }
        });

        request(server).delete('/locations/paths').send().end(function (err, res) {
            for (var i = 0; i < simPaths.length; i++) {
                var simPath = simPaths[i];

                testPaths.push(simPath);


                request(server).post('/locations/path').send(simPath).end(function (err, res) {
                    totalPathCount++;
                    if (totalPathCount == testPaths.length) {
                        pathSetupComplete = true;

                        if (pathSetupComplete && locationsSetupComplete) {
                            done()
                        }
                    }
                });
            }

        })


    });


    after(function (done) {

        request(server).delete('/locations').send().end(function (err, res) {

            request(server).delete('/locations/paths').send().end(function (err, res) {
                server.close();
                done();
            });
        });
    });


    it('getAll', function (done) {
        request(server).get('/locations').expect(testLocations).expect(200, done);
    });

    it('getLocation', function (done) {
        request(server).get('/locations/id/' + testLocations[0].location_id).expect(testLocations[0]).expect(200, done);
    });

    it('getFloors', function (done) {
        request(server).get('/locations/floors').expect(testFloors).expect(200, done);
    });

    it('getPaths', function(done) {
        request(server).get('/locations/paths').expect(testPaths).expect(200,done);
    })

    it('getBlockedAreas', function(done) {
        request(server).get('/locations/blockedAreas').expect(testBlockedAreas).expect(200,done);
    })

    it('getNearbyHalls', function (done) {
        var nearbyHalls = [testLocations[1],testLocations[8]]
        request(server).get('/locations/halls/nearby/2,190,180,30').expect(nearbyHalls).expect(200, done);
    });

    it('getAllNavigable', function (done) {
        request(server).get('/locations/navigable').expect(testNavigable).expect(200, done);
    });

    it('completeReplaceFloors', function(done) {
        request(server).put('/locations').send(testLocations).expect(testLocations).expect(200,done);
    })

    it('completeReplacePaths', function(done) {
        request(server).put('/locations/paths').send(testPaths).expect(testPaths).expect(200,done);
    })

    it('Delete By Id', function (done) {
        request(server).delete('/locations').send(testLocations[0]).expect(200).then(function () {
            request(server).get('/locations/id/' + testLocations[0].location_id).expect('').expect(200, done);
        });
    });

    it('Update By Id', function (done) {
        var testLoc = testLocations[0];
        testLoc.type = "TEST";
        request(server).put('/locations/id/' + testLoc.location_id).send(testLocations[0]).expect(200).then(function () {
            request(server).get('/locations/id/' + testLocations[0].location_id).expect(testLoc).expect(200, done);
        });
    });

    it('Bulk Update By Id', function (done) {
        var testLocs = [];
        testLocations[0].type = "TEST";
        testLocations[1].type = "TEST";
        testLocations[2].type = "TEST";

        testLocs.push(testLocations[0]);
        testLocs.push(testLocations[1]);
        testLocs.push(testLocations[2]);

        request(server).put('/locations/update/').send(testLocs).expect(testLocs).expect(200,done);
    });

});

