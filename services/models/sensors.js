var dbData = require("./db.js");

var Sequelize = require('sequelize');
var config = require('config');
var dbConfig = config.get("db");

/**
 * Defining Database Model
 **/
var Location = dbData.db.define('sensors', {
    sensor_id: {type: Sequelize.STRING, primaryKey: true},
    latlong: {type: Sequelize.GEOMETRY},
    name: {type: Sequelize.STRING}
}, {timestamps: false});


module.exports = Location;