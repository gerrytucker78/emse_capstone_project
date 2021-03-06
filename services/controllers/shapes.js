// The following code has been heavily edited and derived from the following source.  Thanks to Simon for the great
// base functionality!
//***************************
// By Simon Sarris
// www.simonsarris.com
// sarris@acm.org
//
// Last update December 2011
//
// Free to use and distribute at will
// So long as you are nice to people, etc

/*****************************************************************************************************
 * LocationShape Class Definition
 * *****************************************************************************************************
 */


/**
 * Constructor for wrapper object LocationShape that allows for drawing / moving location and eventual
 * save to the database later.
 * @param location
 * @param w
 * @param h
 * @param fill
 * @constructor
 */
function LocationShape(location, w, h, fill) {
    // This is a very simple and unsafe constructor. All we're doing is checking if the values exist.
    // "x || 0" just means "if there is a value for x, use that. Otherwise use 0."
    // But we aren't checking anything else! We could put "Lalala" for the value of x
    this.x = location.pixel_loc_x || 0;
    this.y = location.pixel_loc_y || 0;
    this.w = w || 1;
    this.h = h || 1;
    this.fill = fill || '#AAAAAA';
    this.location = location;
    this.visible = true;
    this.valid = true;
    this.floor = this.location.floor;
    this.emergency = {emergency_id: "", notes: "", emergency_type: "", emergency_state: "",
        location_id: this.location.location_id, start: "", last_update: "", end: ""};
};

/**
 * Method called to load the edit form with the appropriate data
 */
LocationShape.prototype.edit = function () {
    document.getElementById("location_id").value = this.location.location_id;
    document.getElementById("location_name").value = this.location.name;
    document.getElementById("location_type").value = this.location.type;

    if (this.emergency != undefined && this.emergency.emergency_id != "") {
        document.getElementById("emergency_id").value = this.emergency.emergency_id;
        document.getElementById("emergency_notes").value = this.emergency.emergency_notes;
        document.getElementById("emergency_type").value = this.emergency.emergency_type;
        document.getElementById("emergency_start").value = this.emergency.emergency_start;
        document.getElementById("emergency_last_update").value = this.emergency.emergency_last_update;
        document.getElementById("emergency_end").value = this.emergency.emergency_end;

        if (this.emergency.emergency_start == null) {
            document.getElementById("emergency_state").value = "NEW";
        } else if (this.emergency.emergency_end != null) {
            document.getElementById("emergency_state").value = "ENDED";
        } else {
            document.getElementById("emergency_state").value = "UPDATE";
        }
    } else if (document.getElementById("emergency_id")) {
        document.getElementById("emergency_id").value = "";
        document.getElementById("emergency_notes").value = "";
        document.getElementById("emergency_type").value = "";
        document.getElementById("emergency_start").value = "";
        document.getElementById("emergency_last_update").value = "";
        document.getElementById("emergency_end").value = "";

    }
};

LocationShape.prototype.unselect = function () {
    document.getElementById("location_id").value = "";
    document.getElementById("location_name").value = "";
    document.getElementById("location_type").value = "";
};

/**
 * Draw the location shape
 * @param ctx
 */
LocationShape.prototype.draw = function (ctx) {
    ctx.fillStyle = this.fill;
    ctx.fillRect(this.x, this.y, this.w, this.h);
};

/**
 * Determine if shape contains the provided point
 * @param mx
 * @param my
 * @returns {boolean}
 */
LocationShape.prototype.contains = function (mx, my, selType) {
    // All we have to do is make sure the Mouse X,Y fall in the area between
    // the shape's X and (X + Width) and its Y and (Y + Height)
    return (this.x <= mx) && (this.x + this.w >= mx) &&
        (this.y <= my) && (this.y + this.h >= my) && (selType == "LOCATION");
};

/*****************************************************************************************************
 * End LocationShape Class Definition
 * *****************************************************************************************************
 */


/*****************************************************************************************************
 * BeaconShape Class Definition
 * *****************************************************************************************************
 */


/**
 * Constructor for wrapper object BeaconShape that allows for drawing / moving sensor and eventual
 * save to the database later.
 * @param sensor
 * @param w
 * @param h
 * @param fill
 * @constructor
 */
function BeaconShape(sensor, w, h, fill) {
    // This is a very simple and unsafe constructor. All we're doing is checking if the values exist.
    // "x || 0" just means "if there is a value for x, use that. Otherwise use 0."
    // But we aren't checking anything else! We could put "Lalala" for the value of x
    this.x = sensor.pixel_loc_x || 0;
    this.y = sensor.pixel_loc_y || 0;
    this.w = w || 1;
    this.h = h || 1;
    this.fill = fill || '#AAAAAA';
    this.sensor = sensor;
    this.visible = true;
    this.floor = this.sensor.floor;
    this.valid = true;
};

/**
 * Method called to load the edit form with the appropriate data
 */
BeaconShape.prototype.edit = function () {
    document.getElementById("beacon_name").value = this.sensor.name;
};

BeaconShape.prototype.unselect = function () {
    document.getElementById("beacon_name").value = "";
};

/**
 * Draw the beacon shape
 * @param ctx
 */
BeaconShape.prototype.draw = function (ctx) {
    ctx.fillStyle = this.fill;
    ctx.fillRect(this.x, this.y, this.w, this.h);
};

/**
 * Determine if shape contains the provided point
 * @param mx
 * @param my
 * @returns {boolean}
 */
BeaconShape.prototype.contains = function (mx, my, selType) {
    // All we have to do is make sure the Mouse X,Y fall in the area between
    // the shape's X and (X + Width) and its Y and (Y + Height)
    return (this.x <= mx) && (this.x + this.w >= mx) &&
        (this.y <= my) && (this.y + this.h >= my) &&  (selType == "BEACON");
};

/*****************************************************************************************************
 * End BeaconShape Class Definition
 * *****************************************************************************************************
 */


/*****************************************************************************************************
 * PathShape Class Definition
 * *****************************************************************************************************
 */


/**
 * Constructor for wrapper object BeaconShape that allows for drawing / moving sensor and eventual
 * save to the database later.
 * @param sensor
 * @param w
 * @param h
 * @param fill
 * @constructor
 */
function PathShape(startLoc, endLoc, w, h, fill) {
    // This is a very simple and unsafe constructor. All we're doing is checking if the values exist.
    // "x || 0" just means "if there is a value for x, use that. Otherwise use 0."
    // But we aren't checking anything else! We could put "Lalala" for the value of x
    this.startLocation = startLoc
    this.endLocation = endLoc
    this.w = w || 1;
    this.h = h || 1;
    this.fill = fill || '#AAAAAA';
    this.visible = true;
    this.valid = true;
};

/**
 * Method called to load the edit form with the appropriate data
 */
PathShape.prototype.edit = function () {
    document.getElementById("start_loc_name").value = this.startLocation.name;
    document.getElementById("end_loc_name").value = this.endLocation.name;
};

PathShape.prototype.unselect = function () {
    document.getElementById("start_loc_name").value = "";
    document.getElementById("end_loc_name").value = "";
};

/**
 * Draw the beacon shape
 * @param ctx
 */
PathShape.prototype.draw = function (ctx) {
    ctx.fillStyle = this.fill;
    ctx.beginPath();
    ctx.moveTo(this.startLocation.pixel_loc_x + this.w/2, this.startLocation.pixel_loc_y + this.h/2);
    ctx.lineTo(this.endLocation.pixel_loc_x + this.w/2, this.endLocation.pixel_loc_y + this.h/2);
    ctx.stroke();
};

/**
 * Determine if shape contains the provided point
 * @param mx
 * @param my
 * @returns {boolean}
 */
PathShape.prototype.contains = function (mx, my, selType) {
    // All we have to do is make sure the Mouse X,Y fall in the area between
    // the shape's X and (X + Width) and its Y and (Y + Height)
    return false
};

/*****************************************************************************************************
 * End PathShape Class Definition
 * *****************************************************************************************************
 */




/*******************************************************************************************************
 * TypeVisible Class Definition
 * *****************************************************************************************************
 */

/**
 * Enumeration for TypeVisible class interactions
 * @type {{ROOM: string, HALL: string, STAIRS: string, EXIT: string}}
 */
var TypeVisibleEnum = {
    ROOM: "ROOM",
    HALL: "HALL",
    STAIRS: "STAIRS",
    EXIT: "EXIT"
};

/**
 * Constructor for TypeVisible.  All types will be visible by default.
 * @constructor
 */
function TypeVisible() {
    this.typesVisible = {};
    this.typesVisible[TypeVisibleEnum.ROOM] = true;
    this.typesVisible[TypeVisibleEnum.HALL] = true;
    this.typesVisible[TypeVisibleEnum.STAIRS] = true;
    this.typesVisible[TypeVisibleEnum.EXIT] = true;
}

/**
 * Toggle the visibility of the provided type
 */
TypeVisible.prototype.toggleTypeVisible = function (typeVisibleEnumToToggle) {
    this.typesVisible[typeVisibleEnumToToggle] = !this.typesVisible[typeVisibleEnumToToggle];
};

/*******************************************************************************************************
 * End TypeVisible Class Definition
 * *****************************************************************************************************
 */

/*******************************************************************************************************
 * CanvasSate Class Definition
 * *****************************************************************************************************
 */
function CanvasState(canvas, image, floor) {
    // **** First some setup! ****
    this.image = image;
    this.canvas = canvas;
    this.width = canvas.width;
    this.height = canvas.height;
    this.ctx = canvas.getContext('2d');
    this.floor = floor;

    // This complicates things a little but but fixes mouse co-ordinate problems
    // when there's a border or padding. See getMouse for more detail
    var stylePaddingLeft, stylePaddingTop, styleBorderLeft, styleBorderTop;
    if (document.defaultView && document.defaultView.getComputedStyle) {
        this.stylePaddingLeft = parseInt(document.defaultView.getComputedStyle(canvas, null)['paddingLeft'], 10) || 0;
        this.stylePaddingTop = parseInt(document.defaultView.getComputedStyle(canvas, null)['paddingTop'], 10) || 0;
        this.styleBorderLeft = parseInt(document.defaultView.getComputedStyle(canvas, null)['borderLeftWidth'], 10) || 0;
        this.styleBorderTop = parseInt(document.defaultView.getComputedStyle(canvas, null)['borderTopWidth'], 10) || 0;
    }
    // Some pages have fixed-position bars (like the stumbleupon bar) at the top or left of the page
    // They will mess up mouse coordinates and this fixes that
    var html = document.body.parentNode;
    this.htmlTop = html.offsetTop;
    this.htmlLeft = html.offsetLeft;

    // **** Keep track of state! ****

    this.valid = false; // when set to false, the canvas will redraw everything
    this.shapes = [];  // the collection of things to be drawn


    this.dragging = false; // Keep track of when we are dragging
    // the current selected object. In the future we could turn this into an array for multiple selection
    this.selection = null;
    this.dragoffx = 0; // See mousedown and mousemove events for explanation
    this.dragoffy = 0;

    this.shapesAdded = [];


    // **** Then events! ****

    // This is an example of a closure!
    // Right here "this" means the CanvasState. But we are making events on the Canvas itself,
    // and when the events are fired on the canvas the variable "this" is going to mean the canvas!
    // Since we still want to use this particular CanvasState in the events we have to save a reference to it.
    // This is our reference!
    var myState;
    myState = this;

    //fixes a problem where double clicking causes text to get selected on the canvas
    canvas.addEventListener('selectstart', function (e) {
        e.preventDefault();
        return false;
    }, false);
    // Up, down, and move are for dragging
    canvas.addEventListener('mousedown', function (e) {
        var mouse = myState.getMouse(e);
        var mx = mouse.x;
        var my = mouse.y;
        var shapes = myState.shapes;
        var l = shapes.length;
        var selectionType = document.getElementById("selectionType").value;
        for (var i = l - 1; i >= 0; i--) {
            if (shapes[i].contains(mx, my, selectionType)) {
                var mySel = shapes[i];
                mySel.valid = false;
                // Keep track of where in the object we clicked
                // so we can move it smoothly (see mousemove)
                myState.dragoffx = mx - mySel.x;
                myState.dragoffy = my - mySel.y;
                myState.dragging = true;
                myState.selection = mySel;
                myState.valid = false;
                mySel.edit();
                return;
            }
        }
        // havent returned means we have failed to select anything.
        // If there was an object selected, we deselect it
        if (myState.selection) {
            myState.selection.unselect();

            myState.selection = null;
            myState.valid = false; // Need to clear the old selection border

        }
    }, true);
    canvas.addEventListener('mousemove', function (e) {
        if (myState.dragging) {
            var mouse = myState.getMouse(e);
            // We don't want to drag the object by its top-left corner, we want to drag it
            // from where we clicked. Thats why we saved the offset and use it here
            myState.selection.x = mouse.x - myState.dragoffx;
            myState.selection.y = mouse.y - myState.dragoffy;
            myState.valid = false; // Something's dragging so we must redraw
        }
    }, true);
    canvas.addEventListener('mouseup', function (e) {
        myState.dragging = false;
    }, true);
    // double click for making new shapes
    canvas.addEventListener('dblclick', function (e) {
        var mouse = myState.getMouse(e);
        var addType = document.getElementById("addType").value;

        if (addType == "LOCATION") {
            var newShape = new LocationShape({
                name: "New Shape",
                type: "UNKNOWN",
                pixel_loc_x: mouse.x - 5,
                pixel_loc_y: mouse.y - 5,
                floor: myState.floor
            }, 5, 5, "#000000");
            newShape.valid = false;
            myState.shapesAdded.push(newShape);
            myState.addShape(newShape);
            myState.valid = false;
        } else if (addType == "BEACON") {
            var newShape = new BeaconShape({
                name: "New Beacon",
                pixel_loc_x: mouse.x - 5,
                pixel_loc_y: mouse.y - 5,
                floor: myState.floor
            }, 5, 5, "#FF0000");
            newShape.valid = false;
            myState.shapesAdded.push(newShape);
            myState.addShape(newShape);
            myState.valid = false;
        }
    }, true);

    // **** Options! ****

    this.selectionColor = '#CC0000';
    this.selectionWidth = 2;
    this.interval = 30;
    setInterval(function () {
        myState.draw();
    }, myState.interval);
}

CanvasState.prototype.addShape = function (shape) {
    this.shapes.push(shape);
    this.valid = false;
};

CanvasState.prototype.clear = function () {
    this.ctx.clearRect(0, 0, this.width, this.height);
};

// While draw is called as often as the INTERVAL variable demands,
// It only ever does something if the canvas gets invalidated by our code
CanvasState.prototype.draw = function () {
    // if our state is invalid, redraw and validate!
    if (!this.valid) {
        var ctx = this.ctx;
        var shapes = this.shapes;
        var image = this.image;


        this.clear();

        // ** Add stuff you want drawn in the background all the time here **

        // Draw Background image
        ctx.drawImage(image, 0, 0);

        // draw all shapes
        var l = shapes.length;
        for (var i = 0; i < l; i++) {
            var shape = shapes[i];
            // We can skip the drawing of elements that have moved off the screen:
            if (shape.x > this.width || shape.y > this.height ||
                shape.x + shape.w < 0 || shape.y + shape.h < 0) continue;

            if (shape.floor == this.floor || shape instanceof PathShape) {
                shapes[i].draw(ctx);
            }
        }

        // draw selection
        // right now this is just a stroke along the edge of the selected Shape
        if (this.selection != null) {
            ctx.strokeStyle = this.selectionColor;
            ctx.lineWidth = this.selectionWidth;
            var mySel = this.selection;
            ctx.strokeRect(mySel.x, mySel.y, mySel.w, mySel.h);
        }

        // ** Add stuff you want drawn on top all the time here **

        this.valid = true;
    }
};


// Creates an object with x and y defined, set to the mouse position relative to the state's canvas
// If you wanna be super-correct this can be tricky, we have to worry about padding and borders
CanvasState.prototype.getMouse = function (e) {
    var element = this.canvas, offsetX = 0, offsetY = 0, mx, my;

    // Compute the total offset
    if (element.offsetParent !== undefined) {
        do {
            offsetX += element.offsetLeft;
            offsetY += element.offsetTop;
        } while ((element = element.offsetParent));
    }

    // Add padding and border style widths to offset
    // Also add the <html> offsets in case there's a position:fixed bar
    offsetX += this.stylePaddingLeft + this.styleBorderLeft + this.htmlLeft;
    offsetY += this.stylePaddingTop + this.styleBorderTop + this.htmlTop;

    mx = e.pageX - offsetX;
    my = e.pageY - offsetY;

    // We return a simple javascript object (a hash) with x and y defined
    return {x: mx, y: my};
};

