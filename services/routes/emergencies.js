var express = require('express');
var Emergency = require('../models/emergencies.js');

var config = require('config');
var simulation = config.get('simulation');
var router = express.Router();

/**
 * Simulation Data
 */
var simDataSet = simulation.get("emergencies");

/* GET ALL sensors listing. */
router.get('/', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Emergency.findAll({order: [['emergency_id','ASC']]}).then(function (sensors) {
            return res.send(sensors)
        });
    }
});

/**
 * GET Specific Emergency
 */
router.get('/id/:id', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simulation.get("emergency:" + req.params.id));
    } else {
        Emergency.findOne({where: {emergency_id: req.params.id}}).then(function (sensors) {
            return res.send(sensors)
        });
    }
});

/**
 * POST Add Emergency
 */

router.post('/', function (req, res, next) {
    Emergency.create({
        emergency_id: req.body.emergency_id,
        location_id: req.body.location_id,
        emergency_type: req.body.emergency_type,
        emergency_notes: req.body.emergency_notes,
        emergency_start: req.body.emergency_start,
        emergency_last_update: req.body.emergency_last_update,
        emergency_end: req.body.emergency_end
    }).then(function (loc) {
        res.send(loc)
    });
});

/**
 * DELETE Specific sensor
 */
router.delete('/', function (req, res, next) {
    if (req.body.sensor_id === undefined) {
        Emergency.destroy({truncate: true}).then(res.sendStatus(200));
    } else {
        Emergency.destroy({where: {emergency_id: req.body.emergency_id}}).then(res.sendStatus(200));
    }

});

module.exports = router;