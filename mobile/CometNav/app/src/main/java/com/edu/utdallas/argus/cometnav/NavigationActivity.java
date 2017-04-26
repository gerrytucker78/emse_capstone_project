package com.edu.utdallas.argus.cometnav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Toast;

import com.edu.utdallas.argus.cometnav.dataservices.DataServices;
import com.edu.utdallas.argus.cometnav.dataservices.beacons.BeaconManagerService;
import com.edu.utdallas.argus.cometnav.dataservices.beacons.CometNavBeacon;
import com.edu.utdallas.argus.cometnav.dataservices.emergencies.Emergency;
import com.edu.utdallas.argus.cometnav.dataservices.emergencies.EmergencyService;
import com.edu.utdallas.argus.cometnav.dataservices.locations.ILocationClient;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Location;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Path;
import com.edu.utdallas.argus.cometnav.navigation.CurrentLocation;
import com.edu.utdallas.argus.cometnav.navigation.Navigation;
import com.edu.utdallas.argus.cometnav.navigation.OnRouteChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;

import net.coderodde.graph.pathfinding.support.Point2DF;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static android.graphics.BitmapFactory.*;

public class NavigationActivity extends AppCompatActivity implements ILocationClient {
    private static final String TAG = "NavigationActivity";
    private BroadcastReceiver receiver;
    private BroadcastReceiver emergencyReceiver;
    private CometNavView photoView;
    private DownloadImageTask task;
    private Canvas locDot;
    private Canvas paths;
    private Canvas backgroundCanvas;
    private Canvas beacons;
    private Canvas emergencies;
    private Canvas blockedAreasCanvas;

    private Paint paint;
    private Paint beaconPaint;
    private Paint pathPaint;
    private Paint emergencyPaint;
    private Paint blockedAreasPaint;

    private float[] mPathArray;
    private Bitmap immutableMap;
    private Bitmap mutableMap;
    private float cumulScaleFactor = 1;
    private boolean showLocDot = false;
    private int xPos = 0;
    private int yPos = 0;
    private boolean showLocDotReal = false;
    private boolean shouldShowBeacons = false;
    private int xPosReal = 0;
    private int yPosReal = 0;
    private int mRadius = 0;
    private final int MIN_RADIUS = 10;
    private Navigation navigation = Navigation.getInstance();
    private List<CometNavBeacon> cnBeaconList = new ArrayList<>();
    private List<Emergency> emergencyList = new ArrayList<Emergency>();
    private List<Location> emergencyLocations = new ArrayList<Location>();
    private List<Location> blockedAreas = new ArrayList<Location>();

    private int startLoc = 0;
    private int endLoc = 0;

    public static final String START_LOCATION_ID = "START_LOCATION_ID";
    public static final String END_LOCATION_ID = "END_LOCATION_ID";
    public static final String EMERGENCIES = "EMERGENCIES";
    public static final String EMERGENCY_LOCATIONS = "EMERGENCY_LOCATIONS";

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadIntentData();
        DataServices.getBlockedAreas(this);

    }

    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
            navigation.stopNavigation();
        }
        super.onDestroy();
    }

    /**
     * Draws our current location
     */
    private void drawCurrentLoc() {
        if (locDot == null)
            return;
        if (showLocDotReal) {
            locDot.save();
            locDot.translate(xPosReal, yPosReal);
            float scaleVal = (1 / cumulScaleFactor);
            locDot.scale(scaleVal, scaleVal);
            Paint tempPaint = new Paint();
            tempPaint.setAntiAlias(true);
            tempPaint.setColor(Color.YELLOW);
            locDot.drawCircle(0, 0, mRadius, tempPaint);
            locDot.restore();
        }
        if (showLocDot) {
            locDot.save();
            locDot.translate(xPos, yPos);
            float scaleVal = (1 / cumulScaleFactor);
            locDot.scale(scaleVal, scaleVal);
            locDot.drawCircle(0, 0, mRadius, paint);
            locDot.restore();
        }
    }

    private void drawPath() {
        if (paths == null || mPathArray == null)
            return;
        paths.save();
        if (mPathArray.length > 0) {
            paths.drawLines(mPathArray, pathPaint);
        }
        paths.restore();
    }

    private void drawEmergencies() {
        if (emergencies == null) {
            return;
        }

        if (!this.emergencyList.isEmpty()) {
            for (Location emLoc : this.emergencyLocations) {
                emergencies.save();
                emergencies.translate(emLoc.getPixelLocX(), emLoc.getPixelLocY());
                float scaleVal = (1 / cumulScaleFactor);
                emergencies.scale(scaleVal, scaleVal);
                //emergencies.drawRect(0, 0, 5, 5, emergencyPaint);
                emergencies.drawCircle(0, 0, 5, emergencyPaint);
                emergencies.restore();
                emergencies.save();
                emergencies.translate(emLoc.getPixelLocX(), emLoc.getPixelLocY() + 25);
                emergencies.drawText(emLoc.getName(), (float) (0), (float) (0), emergencyPaint);
                emergencies.restore();
            }
        }
    }

    private void drawBeacons() {
        if (beacons == null || !shouldShowBeacons) {
            return;
        }

        for (int i = 0; i < this.cnBeaconList.size(); i++) {
            beacons.save();
            CometNavBeacon cnb = this.cnBeaconList.get(i);
            beacons.translate(cnb.getxLoc(), cnb.getyLoc());
            float scaleVal = (1 / cumulScaleFactor);
            beacons.scale(scaleVal, scaleVal);
            beacons.drawCircle(0, 0, 5, beaconPaint);
            beacons.restore();
            beacons.save();
            beacons.translate(cnb.getxLoc(), cnb.getyLoc());
            beacons.drawText(Integer.toHexString(cnb.getName()) + " - " + String.format("%.2f", cnb.getDistMtr()), (float) (0), (float) (0), beaconPaint);
            beacons.restore();
        }
    }

    private void drawBlockedAreas() {
        if (blockedAreasCanvas == null) {
            return;
        }

        for (int i = 0; i < this.blockedAreas.size(); i++) {
            blockedAreasCanvas.save();
            Location loc = this.blockedAreas.get(i);
            blockedAreasCanvas.translate(loc.getPixelLocX(), loc.getPixelLocY());
            float scaleVal = (1 / cumulScaleFactor);
            blockedAreasCanvas.scale(scaleVal, scaleVal);
            blockedAreasCanvas.drawCircle(0, 0, 10, blockedAreasPaint);
            blockedAreasCanvas.restore();
            blockedAreasCanvas.save();
        }
    }
    public void toggleDebug(View view) {
        showLocDotReal = !showLocDotReal;
        shouldShowBeacons = !shouldShowBeacons;
        photoView.invalidate();
    }

    private void updateDraw() {
        if (backgroundCanvas != null)
            backgroundCanvas.drawBitmap(immutableMap, 0, 0, paint);
        drawCurrentLoc();
        drawBeacons();
        drawPath();
        drawEmergencies();
        drawBlockedAreas();
        //Forces a redraw
        //photoView.invalidate();
    }

    private void loadIntentData() {
        Intent origIntent = getIntent();
        this.startLoc = origIntent.getIntExtra(NavigationActivity.START_LOCATION_ID, 0);
        this.endLoc = origIntent.getIntExtra(NavigationActivity.END_LOCATION_ID, 0);

        if (origIntent.hasExtra(EMERGENCIES)) {

            Object[] emergArray = (Object[]) origIntent.getExtras().get(EMERGENCIES);

            this.emergencyList = new ArrayList<Emergency>();
            for (Object newEmerg : emergArray) {
                this.emergencyList.add((Emergency) newEmerg);
            }
        }

        if (origIntent.hasExtra(EMERGENCY_LOCATIONS)) {

            Object[] emergLocArray = (Object[]) origIntent.getExtras().get(EMERGENCY_LOCATIONS);

            this.emergencyLocations = new ArrayList<Location>();
            for (Object newLoc : emergLocArray) {
                this.emergencyLocations.add((Location) newLoc);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        loadIntentData();
        DataServices.getBlockedAreas(this);

        CometNavView.setNavActivity(this);

        photoView = (CometNavView) findViewById(R.id.photo_view);
        task = new DownloadImageTask(photoView);
        task.execute("https://s3-us-west-2.amazonaws.com/got150030/capstone/ECSS4.png");

        // your oncreate code should be
        Log.d("Navigation", "Creating NavigationActivity!");
        IntentFilter filter = new IntentFilter();
        filter.addAction("BEACON_ACTION");


        receiver = new BeaconBroadcastReceiver();
        registerReceiver(receiver, filter);

        IntentFilter filterz = new IntentFilter();
        filterz.addAction("EMERGENCY_ACTION");
        emergencyReceiver = new EmergencyBroadcastReceiver();
        registerReceiver(emergencyReceiver, filterz);

        photoView.setAdjustViewBounds(true);
        //photoView.setOnPhotoTapListener(new PhotoTapListener());
        photoView.setOnScaleChangeListener(new ScaleChangeListener());


        navigation.setOnRouteChangedListener(new OnRouteChangedListener() {
            boolean shouldAlt = false;

            @Override
            public void onRouteChange(int[] routeArcs) {
                Log.d("Navigation", "Updating route: " + Arrays.toString(routeArcs));
                if (routeArcs.length < 2) {
                    mPathArray = null;
                } else {
                    //Each intermediate node will be present twice
                    mPathArray = new float[(routeArcs.length * 4) - 4];
                    int pathArrayCounter = 0;
                    for (int i = 0; i < routeArcs.length; i++) {
                        int[] coords = navigation.getNodePos(routeArcs[i]);
                        mPathArray[pathArrayCounter] = coords[0];
                        mPathArray[pathArrayCounter + 1] = coords[1];
                        if (pathArrayCounter > 0 && i != routeArcs.length - 1) {
                            mPathArray[pathArrayCounter + 2] = coords[0];
                            mPathArray[pathArrayCounter + 3] = coords[1];
                            pathArrayCounter += 2;
                        }
                        pathArrayCounter += 2;
                    }
                }
                //updateDraw();
                photoView.invalidate();
            }
        });

        Log.d("Test", startLoc + " " + endLoc);
        if (this.startLoc != 0 && this.endLoc != 0) {
            navigation.beginNavigation(this.startLoc, this.endLoc);
        } else if (this.startLoc == 0 && this.endLoc != 0) {
            navigation.beginNavigation(this.endLoc);
        }
    }

    private void updateNavBeaconList(List<CometNavBeacon> beaconArrayList) {

        this.cnBeaconList = new ArrayList<>();
        Map<Integer, CometNavBeacon> beaconData = navigation.getBeaconMap();

        for (CometNavBeacon cnBeacon : beaconArrayList) {
            CometNavBeacon refBeacon = beaconData.get(cnBeacon.getName());
            if (refBeacon != null) {
                cnBeacon.setxLoc(refBeacon.getxLoc());
                cnBeacon.setyLoc(refBeacon.getyLoc());
                cnBeacon.setFloor(refBeacon.getFloor());
                cnBeaconList.add(cnBeacon);
            }
        }
        Log.d("Navigation", "Received beacon broadcast! " + beaconArrayList.toString());
    }

    @Override
    public void receiveNavigableLocations(List<Location> locations) {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    @Override
    public void receiveBlockedAreas(List<Location> locations) {
        this.blockedAreas = locations;
    }

    @Override
    public void receivePaths(List<Path> paths) {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    @Override
    public void receiveEmergencyLocations(List<Location> locations) {
        throw new UnsupportedOperationException("Method not implemented for this class");

    }

    private void snapToPath(CurrentLocation loc) {
        if (mPathArray == null || mPathArray.length < 4)
            return;
        float minDist = Float.MAX_VALUE;
        Point2DF destCoords = new Point2DF();
        for (int x = 0, y = 1; x < mPathArray.length - 2 && y < mPathArray.length - 1; x += 2, y += 2) {
            Point2DF point = nearestPointOnLine(mPathArray[x], mPathArray[y], mPathArray[x + 2], mPathArray[y + 2], loc.getxLoc(), loc.getyLoc());
            float distance = distBetweenPoints(point.getX(), point.getY(), loc.getxLoc(), loc.getyLoc());
            if (distance < minDist) {
                minDist = distance;
                destCoords = point;
            }
        }

        loc.setxLoc(Math.round(destCoords.getX()));
        loc.setyLoc(Math.round(destCoords.getY()));
    }

    private float distBetweenPoints(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    private static Point2DF nearestPointOnLine(float ax, float ay, float bx, float by, float px, float py) {
        float apx = px - ax;
        float apy = py - ay;
        float abx = bx - ax;
        float aby = by - ay;

        float ab2 = abx * abx + aby * aby;
        float ap_ab = apx * abx + apy * aby;
        float t = ap_ab / ab2;
        if (t < 0)
            t = 0;
        else if (t > 1)
            t = 1;
        Point2DF dest = new Point2DF(ax + abx * t, ay + aby * t);
        return dest;
    }

    public static class CometNavView extends PhotoView {

        private static NavigationActivity navActivity;

        public CometNavView(Context context) {
            super(context);
        }

        public CometNavView(android.content.Context context, android.util.AttributeSet attrs) {
            super(context, attrs);
        }

        public CometNavView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public static void setNavActivity(NavigationActivity activity) {
            navActivity = activity;
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            //Log.d("Test", "Draw works!");
            if (navActivity != null)
                navActivity.updateDraw();
        }
    }

    private class EmergencyBroadcastReceiver extends BroadcastReceiver {


        private void sendAlert(Context context, Emergency e) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            String emergClass = "";

            if (e.getUpdate() == null) {
                emergClass = "NEW";
            } else if (e.getEnd() == null) {
                emergClass = "UPDATED";
            } else {
                emergClass = "ENDED";
            }

            builder1.setMessage("There is a(n) " + emergClass + " " + e.getType() + " emergency with the following notes:\n*****\n" + e.getNotes() + "\n*****\nDo you wish to navigate to safety?");

            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Log.d("Test", "Clicked?");
                            navigation.beginEmergencyNavigation();
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Grab the new/updated emergencies
            emergencyList = intent.getParcelableArrayListExtra(EmergencyService.EMERGENCY_LIST);
            emergencyLocations = intent.getParcelableArrayListExtra(EmergencyService.EMERGENCY_LOCATIONS);

            Log.d(TAG, "List of emergencies received from broadcast: " + emergencyList.toString());

            //Send out an alert for all the emergencies in the list
            for (Emergency e : emergencyList) {
                sendAlert(context, e);
            }
        }
    }

    private class BeaconBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Update Beacons
            updateNavBeaconList(intent.getParcelableArrayListExtra("BEACON_LIST"));

            // Determine Current Location
            CurrentLocation loc = navigation.calculateCurrentPos(cnBeaconList);

            xPosReal = loc.getxLoc();
            yPosReal = loc.getyLoc();
            snapToPath(loc);

            // If we have a radius, that means there's only 1 beacon
            if (loc.getRadius() != 0) {
                showLocDot = true;
                //Draw circle at location
                xPos = loc.getxLoc();
                yPos = loc.getyLoc();
                //right now I'm assuming we're on the right floor
                mRadius = (int) Math.round(loc.getRadius());
                if (mRadius < MIN_RADIUS)
                    mRadius = MIN_RADIUS;
                Log.d("Navigation", "Found a position with 1 node: " + loc.toString());
            }
            //otherwise we'll have x y and floor
            else if (loc.getxLoc() != 0) {
                showLocDot = true;
                xPos = loc.getxLoc();
                yPos = loc.getyLoc();
                mRadius = MIN_RADIUS;
                Log.d("Navigation", "Found a position! " + loc.toString());
            }
            //If we don't have x, that means we don't have a location. Hide our current location.
            else {
                showLocDot = false;
            }
            //This forces a redraw
            photoView.invalidate();

            //If we've arrived at our destination. Determine that by our closest node being the dest
            //node
            Log.d("Test", endLoc + " " + navigation.findNearestNode());
            if (endLoc != 0 && (endLoc == navigation.findNearestNode() || navigation.getDistanceToNode(endLoc) < 30 )) {
                Toast.makeText(NavigationActivity.this, "You have arrived", Toast.LENGTH_LONG).show();
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.arrival);
                mp.setVolume(1f, 1f); //to play sound when volume is 0
                mp.start();

                navigation.stopNavigation();
                mPathArray = null; //Clear the path
            }
        }
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;

            String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
            Log.d("Navigation", (String.format(PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId())));
        }
    }

    private class ScaleChangeListener implements OnScaleChangedListener {

        //This resizes the current location dot
        public void onScaleChange(float scaleFactor, float focusX, float focusY) {
            cumulScaleFactor = cumulScaleFactor * scaleFactor;
            photoView.invalidate();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        PhotoView bmImage;

        public DownloadImageTask(PhotoView bmImage) {

            this.bmImage = bmImage;
            if (bmImage == null)
                Log.d("Navigation", "bmImage is set to null on create?");
            else
                Log.d("Navigation", "bmImage is not null");
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (bmImage != null) {
                 /*Draw a circle on an existing image - starts here*/
                BitmapFactory.Options myOptions = new BitmapFactory.Options();
                myOptions.inDither = true;
                myOptions.inScaled = false;
                myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
                myOptions.inPurgeable = true;

                paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.BLUE);

                beaconPaint = new Paint();
                beaconPaint.setAntiAlias(true);
                beaconPaint.setColor(Color.GRAY);

                pathPaint = new Paint();
                pathPaint.setAntiAlias(true);
                pathPaint.setColor(Color.GREEN);

                emergencyPaint = new Paint();
                emergencyPaint.setAntiAlias(true);
                emergencyPaint.setColor(Color.RED);

                blockedAreasPaint = new Paint();
                blockedAreasPaint.setAntiAlias(true);
                blockedAreasPaint.setColor(Color.RED);

                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.id.image_view,myOptions);
                immutableMap = result;
                mutableMap = immutableMap.copy(Bitmap.Config.ARGB_8888, true);

                backgroundCanvas = new Canvas(mutableMap);
                locDot = new Canvas(mutableMap);
                paths = new Canvas(mutableMap);
                beacons = new Canvas(mutableMap);
                emergencies = new Canvas(mutableMap);
                blockedAreasCanvas = new Canvas(mutableMap);
                bmImage.setImageBitmap(mutableMap);

                //updateDraw();
                photoView.invalidate();

            } else
                Log.d("Navigation", "bmImage is null?");
        }
    }
}
