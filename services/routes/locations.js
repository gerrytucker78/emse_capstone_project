var express = require('express');
var Location = require('../models/locations.js');
var Path = require('../models/paths.js');

var config = require('config');
var simulation = config.get('simulation');
var router = express.Router();

var multer = require('multer');
var upload = multer({ storage: multer.memoryStorage()});


/**
 * Simulation Data
 */
var simDataSet = simulation.get("locations");

/* GET ALL locations listing. */
router.get('/', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Location.findAll({order: [['location_id', 'ASC']]}).then(function (locations) {
            return res.send(locations)
        });
    }
});

/* GET ALL location paths listing. */
router.get('/paths', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Path.findAll({order: [['start_id', 'ASC']]}).then(function (paths) {
            return res.send(paths)
        });
    }
});

/* GET ALL locations listing. */
router.get('/floors', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Location.findAll({where: {type: "FLOOR"}, order: [['location_id', 'ASC']]}).then(function (locations) {
            return res.send(locations)
        });
    }
});


/**
 * GET Specific Location
 */
router.get('/id/:id', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simulation.get("location:" + req.params.id));
    } else {
        Location.findOne({where: {location_id: req.params.id}}).then(function (location) {
            return res.send(location)
        });
    }
});

/**
 * POST Add Location
 */

router.post('/', function (req, res, next) {

    Location.create({
        location_id: req.body.location_id,
        name: req.body.name,
        floor: req.body.floor,
        type: req.body.type,
        latlong: req.body.latlong,
        map: req.body.map
    }).then(function (loc) {
        res.send(loc)
    });
});

/**
 * POST Add Path
 */
router.post('/path', function (req, res, next) {

    Path.create({
        start_id: req.body.start_id,
        end_id: req.body.end_id,
        weight: req.body.weight
    }).then(function (loc) {
        res.send(loc)
    });
});

/**
 * DELETE Specific location OR All
 */
router.delete('/', function (req, res, next) {
    if (req.body.location_id === undefined) {
        Location.destroy({truncate: true}).then(res.sendStatus(200));
    } else {
        Location.destroy({where: {location_id: req.body.location_id}}).then(res.sendStatus(200));
    }

});

/**
 * DELETE Specific path OR All
 */
router.delete('/paths', function (req, res, next) {
    if (req.body.start_id === undefined && req.body.end_id === undefined) {
        Path.destroy({truncate: true}).then(res.sendStatus(200));
    } else {
        Path.destroy({where: {start_id: req.body.start_id} & {end_id: req.body.end_id}}).then(res.sendStatus(200));
    }

});

module.exports = router;
