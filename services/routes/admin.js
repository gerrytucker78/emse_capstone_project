var express = require('express');
var router = express.Router();
var config = require('config');

router.get('/maintain/locations', function(req, res, next) {
    res.render('maintainLocations', {});
});

router.get('/maintain/emergencies', function(req, res, next) {
    res.render('maintainEmergencies', {});
});

module.exports = router;