var dbData = require("./db.js");

var Sequelize = require('sequelize');
var config = require('config');
var dbConfig = config.get("db");

/**
 * Defining Database Model
 **/
var Path = dbData.db.define('paths', {
    start_id: {type: Sequelize.INTEGER, primaryKey: true},
    end_id: {type: Sequelize.INTEGER, primaryKey: true},
    weight: {type: Sequelize.REAL}
}, {timestamps: false});


module.exports = Path;