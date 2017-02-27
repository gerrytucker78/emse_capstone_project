/**
 * Created by gtucker on 2/26/17.
 */
var request = require('nodeunit-express');
var app = require('../app.js');

exports.testSomething = function(test)
{
    test.expect(1);
    test.ok(true,"This should pass");
    test.done();
}

exports.testSomethingElse = function(test) {
    var express = request(app);

    express.get('/').expect(function (response) {
        test.equal(response.body, "ok");
        test.equal(response.code, 200);
        test.done();
    });

    express.close();
}