var vertx = require('vertx');

var stopAreaName = {};

var stations = {
    northwest: [9710, 9711, 9700, 9701, 9702, 9703, 9704, 9325],
    northeast: [9502, 9503, 9504, 9505, 9506, 9507, 9508, 9509],
    central: [9510, 9000, 9530, 9531, 9529],
    southwest: [9528, 9527, 9526, 9525, 9524, 9523, 9522, 9521, 9520, 9543],
    southeast: [9180, 9732, 9731, 9730, 9729, 9728, 9727, 9726, 9725, 9724]
};

function put(message) {
    for (var id in message) {
        stopAreaName[id] = message[id];
    }
}

function get(message, replier) {
    var directions = ['northwest', 'northeast', 'central', 'southwest', 'southeast'];

    var reply = {};

    directions.forEach(function (direction) {
        reply[direction] = stations[direction].map(wrapInObject);
    });

    replier(reply);

    function wrapInObject(siteId) {
        var id = siteId.toString();

        return {
            SiteId: siteId,
            StopAreaName: stopAreaName[id] || id
        };
    }
}

vertx.eventBus.registerHandler('store.put', put);
vertx.eventBus.registerHandler('store.stations', get);
