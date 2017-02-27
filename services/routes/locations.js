var express = require('express');
var location = require('../models/locations.js');

var router = express.Router();

/* GET locations listing. */
router.get('/', function(req, res, next) {
    res.send(location.getLocations());
});

module.exports = router;
