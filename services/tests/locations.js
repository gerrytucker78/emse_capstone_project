var request = require('supertest');
var config = require('config');
var simulation = config.get('simulation');
var fs = require('fs');

describe('Locations', function () {
    this.timeout(10000);
    var server;
    var testLocations = [];
    var testFloors = [];

    before(function (done) {
        server = require('../app.js');

        var simLocations = simulation.get("locations");
        var totalCount = 0;


        request(server).delete('/locations').send().end(function (err, res) {




            for (var i = 0; i < simLocations.length; i++) {
                var simLoc = simLocations[i];

                testLocations.push(simLoc);

                if (simLoc.type == "FLOOR") {
                    testFloors.push(simLoc);
                }

                request(server).post('/locations').send(simLoc).end(function (err, res) {
                    totalCount++;
                    if (totalCount == testLocations.length) {
                        done();
                    }
                });



            }
        });


    });
    after(function (done) {

        request(server).delete('/locations').send().end(function (err, res) {
            done();
            server.close();
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

    it('Delete By Id', function (done) {
        request(server).delete('/locations').send(testLocations[0]).expect(200).then(function () {
            request(server).get('/locations/id/' + testLocations[0].location_id).expect('').expect(200, done);
        });
    });

});

