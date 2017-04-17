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
};

LocationShape.prototype.edit = function() {
    document.getElementById("location_name").value = this.location.name;
    document.getElementById("location_type").value = this.location.type;
};

/**
 * Draw the shape
 * @param ctx
 */
LocationShape.prototype.draw = function(ctx) {
//    if (this.visible) {
        ctx.fillStyle = this.fill;
        ctx.fillRect(this.x, this.y, this.w, this.h);
//    }
};


// Determine if a point is inside the shape's bounds
LocationShape.prototype.contains = function(mx, my) {
    // All we have to do is make sure the Mouse X,Y fall in the area between
    // the shape's X and (X + Width) and its Y and (Y + Height)
    return  (this.x <= mx) && (this.x + this.w >= mx) &&
        (this.y <= my) && (this.y + this.h >= my);
};

/*****************************************************************************************************
 * End LocationShape Class Definition
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
TypeVisible.prototype.toggleTypeVisible = function(typeVisibleEnumToToggle) {
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
        this.stylePaddingLeft = parseInt(document.defaultView.getComputedStyle(canvas, null)['paddingLeft'], 10)      || 0;
        this.stylePaddingTop  = parseInt(document.defaultView.getComputedStyle(canvas, null)['paddingTop'], 10)       || 0;
        this.styleBorderLeft  = parseInt(document.defaultView.getComputedStyle(canvas, null)['borderLeftWidth'], 10)  || 0;
        this.styleBorderTop   = parseInt(document.defaultView.getComputedStyle(canvas, null)['borderTopWidth'], 10)   || 0;
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
    canvas.addEventListener('selectstart', function(e) { e.preventDefault(); return false; }, false);
    // Up, down, and move are for dragging
    canvas.addEventListener('mousedown', function(e) {
        var mouse = myState.getMouse(e);
        var mx = mouse.x;
        var my = mouse.y;
        var shapes = myState.shapes;
        var l = shapes.length;
        for (var i = l-1; i >= 0; i--) {
            if (shapes[i].contains(mx, my)) {
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
            myState.selection = null;
            myState.valid = false; // Need to clear the old selection border
        }
    }, true);
    canvas.addEventListener('mousemove', function(e) {
        if (myState.dragging){
            var mouse = myState.getMouse(e);
            // We don't want to drag the object by its top-left corner, we want to drag it
            // from where we clicked. Thats why we saved the offset and use it here
            myState.selection.x = mouse.x - myState.dragoffx;
            myState.selection.y = mouse.y - myState.dragoffy;
            myState.valid = false; // Something's dragging so we must redraw
        }
    }, true);
    canvas.addEventListener('mouseup', function(e) {
        myState.dragging = false;
    }, true);
    // double click for making new shapes
    canvas.addEventListener('dblclick', function(e) {
        var mouse = myState.getMouse(e);
        var addType = document.getElementById("addType").value;

        if (addType == "LOCATION") {
            var newShape = new LocationShape({name: "New Shape", type: "UNKNOWN", pixel_loc_x: mouse.x - 5, pixel_loc_y: mouse.y - 5, floor: myState.floor}, 5, 5, "#000000");
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
    setInterval(function() { myState.draw(); }, myState.interval);
}

CanvasState.prototype.addShape = function(shape) {
    this.shapes.push(shape);
    this.valid = false;
};

CanvasState.prototype.clear = function() {
    this.ctx.clearRect(0, 0, this.width, this.height);
};

// While draw is called as often as the INTERVAL variable demands,
// It only ever does something if the canvas gets invalidated by our code
CanvasState.prototype.draw = function() {
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

            if (shape.location.floor == this.floor) {
                shapes[i].draw(ctx);
            }
        }

        // draw selection
        // right now this is just a stroke along the edge of the selected Shape
        if (this.selection != null) {
            ctx.strokeStyle = this.selectionColor;
            ctx.lineWidth = this.selectionWidth;
            var mySel = this.selection;
            ctx.strokeRect(mySel.x,mySel.y,mySel.w,mySel.h);
        }

        // ** Add stuff you want drawn on top all the time here **

        this.valid = true;
    }
};


// Creates an object with x and y defined, set to the mouse position relative to the state's canvas
// If you wanna be super-correct this can be tricky, we have to worry about padding and borders
CanvasState.prototype.getMouse = function(e) {
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

// If you dont want to use <body onLoad='init()'>
// You could uncomment this init() reference and place the script reference inside the body tag
//init();
/**
function init() {
    var s = new CanvasState(document.getElementById('canvas1'));
    s.addShape(new Shape(40,40,50,50)); // The default is gray
    s.addShape(new Shape(60,140,40,60, 'lightskyblue'));
    // Lets make some partially transparent
    s.addShape(new Shape(80,150,60,30, 'rgba(127, 255, 212, .5)'));
    s.addShape(new Shape(125,80,30,80, 'rgba(245, 222, 179, .7)'));
}
 **/
