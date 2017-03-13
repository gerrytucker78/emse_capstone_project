var config = require('config');
var simulation = config.get('simumlation');


/**
 * @todo Need to populate with real database functionality
 */



/**
 * Simulation Data
 */
var simDataSet = simulation.get("sensors");


/**
 * Export Methods
 */
if (simulation.get("enabled") == "true") {
    module.exports = {
        getSensors: function () {
            return simDataSet;
        },

        getSensor: function (id) {
            var result = simulation.get("sensor:" + id);
            return result;
        }
    };

} else {
    module.exports = {
        getSensors: function() {
        // @todo Need to populate with real data.
        return {};
    }};
}

