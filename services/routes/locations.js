var express = require('express');
var Location = require('../models/locations.js');

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
            locations[0].map
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
 * DELETE Specific location
 */
router.delete('/', function (req, res, next) {
    if (req.body.location_id === undefined) {
        Location.destroy({truncate: true}).then(res.sendStatus(200));
    } else {
        Location.destroy({where: {location_id: req.body.location_id}}).then(res.sendStatus(200));
    }

});

module.exports = router;
