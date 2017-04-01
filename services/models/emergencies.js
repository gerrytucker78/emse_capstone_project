var dbData = require("./db.js");

var Sequelize = require('sequelize');
var config = require('config');
var dbConfig = config.get("db");

/**
 * Defining Database Model
 **/
var Emergency = dbData.db.define('emergencies', {
    emergency_id: {type: Sequelize.INTEGER, primaryKey: true},
    location_id: {type: Sequelize.INTEGER},
    emergency_type: {type: Sequelize.STRING},
    emergency_notes: {type: Sequelize.STRING},
    emergency_start: {type: Sequelize.DATE},
    emergency_last_update: {type: Sequelize.DATE},
    emergency_end: {type: Sequelize.DATE}
}, {timestamps: false});


module.exports = Emergency;