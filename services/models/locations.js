var config = require('config');
var simulation = config.get('simumlation');


/**
 * @todo Need to populate with real database functionality
 */



/**
 * Simulation Data
 */
var simDataSet = simulation.get("locations");


/**
 * Export Methods
 */
if (simulation.get("enabled") == "true") {
    module.exports = {
        getLocations: function () {
            return simDataSet;
        }
    };
} else {
    module.exports = {getLocations: function() {
        // @todo Need to populate with real data.
        return {};
    }};
}
