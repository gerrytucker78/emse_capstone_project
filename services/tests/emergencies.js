var request = require('supertest');
var config = require('config');
var simulation = config.get('simulation');

describe('Emergencies', function () {
    var server;
    this.timeout(10000);
    var testEmergencies = [];

    before(function (done) {
        server = require('../app.js');

        var simEmergencies = simulation.get("emergencies");
        var totalCount = 0;

        request(server).delete('/emergencies').send().end(function (err, res) {
            for (var i = 0; i < simEmergencies.length; i++) {
                var simEmergency = simEmergencies[i];

                request(server).post('/emergencies').send(simEmergency).end(function (err, res) {
                    totalCount++;
                    if (totalCount == testEmergencies.length) {
                        done();
                    }
                });

                testEmergencies.push(simEmergency);
            }
        });


    });
    after(function (done) {

        request(server).delete('/emergencies').send().end(function (err, res) {
            done();
            server.close();
        });


    });


    it('getAll', function (done) {
        request(server).get('/emergencies').expect(testEmergencies).expect(200, done);
    });

    it('getEmergency', function (done) {
        request(server).get('/emergencies/id/' + testEmergencies[0].emergency_id).expect(testEmergencies[0]).expect(200, done);
    });

    it('Update By Id', function (done) {
        var testEmerg = testEmergencies[0];
        testEmerg.emergency_type= "TEST";
        request(server).put('/emergencies/id/' + testEmerg.emergency_id).send(testEmergencies[0]).expect(200).then(function () {
            request(server).get('/emergencies/id/' + testEmergencies[0].emergency_id).expect(testEmerg).expect(200, done);
        });
    });


    it('completeReplace', function(done) {
        request(server).put('/emergencies').send(testEmergencies).expect(testEmergencies).expect(200,done);
    })


    it('Delete By Id', function (done) {
        request(server).delete('/emergencies').send(testEmergencies[0]).expect(200).then(function () {
            request(server).get('/emergencies/id/' + testEmergencies[0].emergency_id).expect('').expect(200, done);
        });
    });

});

