var request = require('supertest');
var config = require('config');
var simulation = config.get('simulation');

describe('Sensors', function () {
    this.timeout(10000);
    var server;
    var testSensors = [];

    before(function (done) {
        server = require('../app.js');

        var simLocations = simulation.get("sensors");
        var totalCount = 0;

        request(server).delete('/sensors').send().end(function (err, res) {
            for (var i = 0; i < simLocations.length; i++) {
                var simLoc = simLocations[i];

                request(server).post('/sensors').send(simLoc).end(function (err, res) {
                    totalCount++;
                    if (totalCount == testSensors.length) {
                        done();
                    }
                });

                testSensors.push(simLoc);
            }
        });


    });
    after(function (done) {

        request(server).delete('/sensors').send().end(function (err, res) {
            done();
            server.close();
        });


    });


    it('getAll', function (done) {
        request(server).get('/sensors').expect(testSensors).expect(200, done);
    });

    it('getSensor', function (done) {
        request(server).get('/sensors/id/' + testSensors[0].sensor_id).expect(testSensors[0]).expect(200, done);
    });

    it('Bulk Update By Id', function (done) {
        var testSens = [];
        testSensors[0].floor = 99;
        testSensors[1].floor = 99;
       ;

        testSens.push(testSensors[0]);
        testSens.push(testSensors[1]);


        request(server).put('/sensors/update/').send(testSens).expect(testSens).expect(200,done);
    });

    it('completeReplace', function(done) {
        request(server).put('/sensors').send(testSensors).expect(testSensors).expect(200,done);
    })

    it('Delete By Id', function (done) {
        request(server).delete('/sensors').send(testSensors[0]).expect(200).then(function () {
            request(server).get('/sensors/id/' + testSensors[0].sensor_id).expect('').expect(200, done);
        });
    });

});

