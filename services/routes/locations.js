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

router.post('/', upload.single('map'), function (req, res, next) {
    var location = JSON.parse(req.body.location);
    var mapArray = req.file.buffer



    Location.create({
        location_id: location.location_id,
        name: location.name,
        floor: location.floor,
        type: location.type,
        latlong: location.latlong,
        map: mapArray
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
