var express = require('express');
var sensor = require('../models/sensors.js');

var router = express.Router();

/* GET ALL sensors listing. */
router.get('/', function(req, res, next) {
    res.send(sensor.getSensors());
});

/**
 * GET Specific Location
 */
router.get('/id/:id', function(req, res, next) {
    res.send(sensor.getSensor(req.params.id));
});

module.exports = router;
