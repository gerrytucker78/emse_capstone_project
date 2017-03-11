var request = require('supertest');
var config = require('config');
var simulation = config.get('simumlation');

describe('Locations', function () {
    var server;
    beforeEach(function () {
        server = require('../app.js');
    });
    afterEach(function () {
        server.close();
    });

    it('getLocation', function (done) {
        request(server).get('/locations/id/1').expect(simulation.get("location:1")).expect(200, done);
    });

    it('getAll', function (done) {
        request(server).get('/locations').expect(simulation.get("locations")).expect(200, done);
    });


});

