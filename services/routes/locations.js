var express = require('express');
var location = require('../models/locations.js');

var router = express.Router();

/* GET ALL locations listing. */
router.get('/', function(req, res, next) {
    res.send(location.getLocations());
});

/**
 * GET Specific Location
 */
router.get('/id/:id', function(req, res, next) {
    res.send(location.getLocation(req.params.id));
});

module.exports = router;
