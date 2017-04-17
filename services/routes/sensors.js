var express = require('express');
var Sensor = require('../models/sensors.js');

var config = require('config');
var simulation = config.get('simulation');
var router = express.Router();

/**
 * Simulation Data
 */
var simDataSet = simulation.get("sensors");

/* GET ALL sensors listing. */
router.get('/', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Sensor.findAll({order: [['sensor_id','ASC']]}).then(function (sensors) {
            return res.send(sensors)
        });
    }
});

/**
 * GET Specific Sensor
 */
router.get('/id/:id', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simulation.get("sensor:" + req.params.id));
    } else {
        Sensor.findOne({where: {sensor_id: req.params.id}}).then(function (sensors) {
            return res.send(sensors)
        });
    }
});

/**
 * POST Add Sensor
 */

router.post('/', function (req, res, next) {
    Sensor.create({
        sensor_id: req.body.sensor_id,
        name: req.body.name,
        pixel_loc_x: req.body.pixel_loc_x,
        pixel_loc_y: req.body.pixel_loc_y,
        floor: req.body.floor
    }).then(function (loc) {
        res.send(loc)
    });
});

/**
 * DELETE Specific sensor
 */
router.delete('/', function (req, res, next) {
    if (req.body.sensor_id === undefined) {
        Sensor.destroy({truncate: true}).then(res.sendStatus(200));
    } else {
        Sensor.destroy({where: {sensor_id: req.body.sensor_id}}).then(res.sendStatus(200));
    }

});

/**
 * PUT Complete drop and replace of data with provide array of JSON objects
 */
router.put('/', function (req, res, next) {
    var sensors = [];
    sensors = req.body;
    Sensor.destroy({truncate: true}).then(function () {
        Sensor.bulkCreate(sensors).then(function (sens) {
            res.send(sens)
        });
    });

});

/**
 * PUT Complete drop and replace of data with provide array of JSON objects
 */
router.put('/update', function (req, res, next) {
    var sensors = [];
    sensors = req.body;

    var ids = [];

    for (var i = 0; i < sensors.length; i++) {
        ids.push(sensors[i].sensor_id);
    }



    Sensor.destroy({where: {sensor_id: {in: ids}}}).then(function () {
        Sensor.bulkCreate(sensors).then(function (sens) {
            res.send(sens)
        });
    });

});

module.exports = router;