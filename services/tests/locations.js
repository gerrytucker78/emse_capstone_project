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

    it('getAll', function (done) {
        request(server).get('/locations').expect(simulation.get("locations")).expect(200, done);
    });
});

