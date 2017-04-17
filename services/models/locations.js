var dbData = require("./db.js");

var Sequelize = require('sequelize');
var config = require('config');
var dbConfig = config.get("db");

/**
 * Defining Database Model
 **/
var Location = dbData.db.define('locations', {
    location_id: {type: Sequelize.INTEGER, primaryKey: true, autoIncrement: true},
    floor: {type: Sequelize.INTEGER},
    name: {type: Sequelize.STRING},
    type: {type: Sequelize.STRING},
    map: {type: Sequelize.STRING},
    pixel_loc_x: {type: Sequelize.INTEGER},
    pixel_loc_y: {type: Sequelize.INTEGER}
}, {timestamps: false});


module.exports = Location;