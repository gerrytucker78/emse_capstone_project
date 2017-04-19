var express = require('express');
var Location = require('../models/locations.js');
var Path = require('../models/paths.js');

var config = require('config');
var simulation = config.get('simulation');
var router = express.Router();

var multer = require('multer');
var upload = multer({storage: multer.memoryStorage()});


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

/* GET ALL Navigable locations listing. */
router.get('/navigable', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Location.findAll({
            where: {$or: [{type: "HALL"}, {type: "STAIRS"}, {type: "ROOM"}, {type: "EXIT"}]},
            order: [['location_id', 'ASC']]
        }).then(function (locations) {
            return res.send(locations)
        });
    }
});

/* GET ALL location paths listing. */
router.get('/paths', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Path.findAll({order: [['start_id', 'ASC'], ['end_id', 'ASC']]}).then(function (paths) {
            return res.send(paths)
        });
    }
});

/* GET Path by ids */
router.get('/paths/:start_id,:end_id', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Path.findAll({where: {start_id: req.params.start_id, end_id: req.params.end_id}, order: [['start_id', 'ASC'], ['end_id', 'ASC']]}).then(function (paths) {
            return res.send(paths)
        });
    }
});

/* GET Nearby HALLs. */
router.get('/halls/nearby/:floor,:x,:y,:dist', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {

        minX = parseInt(req.params.x) - parseInt(req.params.dist);
        maxX = parseInt(req.params.x) + parseInt(req.params.dist);
        minY = parseInt(req.params.y) - parseInt(req.params.dist);
        maxY = parseInt(req.params.y) + parseInt(req.params.dist);

        Location.findAll({
            where: {
                floor: req.params.floor,
                type: "HALL",
                pixel_loc_x: {$gte: minX, $lte: maxX},
                pixel_loc_y: {$gte: minY, $lte: maxY}
            }, order: [['location_id', 'ASC']]
        }).then(function (locations) {
            return res.send(locations)
        });
    }
});

/* GET ALL Floors listing. */
router.get('/floors', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Location.findAll({where: {type: "FLOOR"}, order: [['location_id', 'ASC']]}).then(function (locations) {
            return res.send(locations)
        });
    }
});

/* GET ALL Blocked Areas listing. */
router.get('/blockedAreas', function (req, res, next) {
    if (simulation.get("enabled") == "true") {
        res.send(simDataSet);
    } else {
        Location.findAll({where: {type: "BLOCKED_AREA"}, order: [['location_id', 'ASC']]}).then(function (locations) {
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

    Location.create(req.body).then(function (loc) {
        res.send(loc)
    });
});

/**
 * POST Add Path
 */
router.post('/paths', function (req, res, next) {

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
 * DELETE Specific location by id
 */
router.delete('/id/:id', function (req, res, next) {
    Location.destroy({where: {location_id: req.params.id}}).then(res.sendStatus(200));
});

/**
 * DELETE Specific path OR All
 */
router.delete('/paths', function (req, res, next) {
    if (req.body.start_id === undefined && req.body.end_id === undefined) {
        Path.destroy({truncate: true}).then(res.sendStatus(200));
    } else {
        Path.destroy({where: {start_id: req.body.start_id, end_id: req.body.end_id}}).then(res.sendStatus(200));
    }

});

/**
 * DELETE Specific path by Ids
 */
router.delete('/paths/ids/:start_id,:end_id', function (req, res, next) {
    Path.destroy({where: {start_id: req.params.start_id, end_id: req.params.end_id}}).then(res.sendStatus(200));

});

/**
 * PUT Complete drop and replace of data with provide array of JSON objects
 */
router.put('/', function (req, res, next) {
    var locations = [];
    locations = req.body;
    console.log("Request Body: " + req.body.length)
    Location.destroy({truncate: true}).then(function () {
        Location.bulkCreate(locations).then(function (locs) {
            res.send(locs)
        });
    });

});

/**
 * PUT Do a replace of a particular item
 */
router.put('/id/:id', function (req, res, next) {
    Location.destroy({where: {location_id: req.body.location_id}}).then(function () {
        Location.create(req.body).then(function (loc) {
            res.send(loc)
        });
    });
});


/**
 * PUT Complete drop and replace of data with provide array of JSON objects
 */
router.put('/update', function (req, res, next) {
    var locations = [];
    locations = req.body;

    var ids = [];

    for (var i = 0; i < locations.length; i++) {
        ids.push(locations[i].location_id);
    }



    Location.destroy({where: {location_id: {in: ids}}}).then(function () {
        Location.bulkCreate(locations).then(function (locs) {
            res.send(locs)
        });
    });

});

/**
 * PUT Complete drop and replace of data with provide array of JSON objects
 */
router.put('/paths', function (req, res, next) {
    var paths = [];
    paths = req.body;

    Path.destroy({truncate: true}).then(function () {
        Path.bulkCreate(paths).then(function (pths) {
            res.send(pths)
        });
    });

});


module.exports = router;
