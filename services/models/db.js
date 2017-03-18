var config = require('config');

var Sequelize = require('sequelize');
var dbConfig = config.get('db');


var dbUrl = "postgres://";

if (dbConfig.get("authRequired") == "true") {
    dbUrl = dbUrl + dbConfig.get("user") + ":" + dbConfig.get("password") + "@";
}

dbUrl = dbUrl + dbConfig.get("host")+ ":" + dbConfig.get("port") + "/" + dbConfig.get("db");

var db = new Sequelize(dbUrl);
db.authenticate();

module.exports = {db: db, sync: dbConfig.get("sync")};
