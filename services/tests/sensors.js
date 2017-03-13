var request = require('supertest');
var config = require('config');
var simulation = config.get('simumlation');

describe('Sensors', function () {
    var server;
    beforeEach(function () {
        server = require('../app.js');
    });
    afterEach(function () {
        server.close();
    });

    it('getSensor', function (done) {
        request(server).get('/sensors/id/1').expect(simulation.get("sensor:1")).expect(200, done);
    });

    it('getAll', function (done) {
        request(server).get('/sensors').expect(simulation.get("sensors")).expect(200, done);
    });


});

