var dbData = require("./db.js");

var Sequelize = require('sequelize');
var config = require('config');
var dbConfig = config.get("db");

/**
 * Defining Database Model
 **/
var Location = dbData.db.define('locations', {
    location_id: {type: Sequelize.INTEGER, primaryKey: true},
    floor: {type: Sequelize.INTEGER},
    latlong: {type: Sequelize.GEOMETRY},
    name: {type: Sequelize.STRING},
    type: {type: Sequelize.STRING},
    map: {type: Sequelize.STRING}
}, {timestamps: false});


module.exports = Location;