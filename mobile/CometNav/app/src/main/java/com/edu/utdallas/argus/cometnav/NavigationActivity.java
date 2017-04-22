package com.edu.utdallas.argus.cometnav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.edu.utdallas.argus.cometnav.dataservices.beacons.BeaconManagerService;
import com.edu.utdallas.argus.cometnav.dataservices.beacons.CometNavBeacon;
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

import static android.graphics.BitmapFactory.*;

public class NavigationActivity extends AppCompatActivity implements ILocationClient
{
    private BroadcastReceiver receiver;
    //private PhotoView photoView;
    private CometNavView photoView;
    private DownloadImageTask task;
    private Canvas locDot;
    private Canvas paths;
    private Canvas backgroundCanvas;
    private Canvas beacons;
    private Paint paint;
    private Paint beaconPaint;
    private Paint pathPaint;

    private float[] mPathArray;
    private Bitmap immutableMap;
    private Bitmap mutableMap;
    private float cumulScaleFactor = 1;
    private boolean showLocDot = false;
    private int xPos = 0;
    private int yPos = 0;
    private boolean showLocDotReal = false;
    private int xPosReal = 0;
    private int yPosReal = 0;
    private int mRadius = 0;
    private final int MIN_RADIUS = 10;
    private Navigation navigation = Navigation.getInstance();
    private List<CometNavBeacon> cnBeaconList = new ArrayList<>();

    private int startLoc = 0;
    private int endLoc = 0;

    public static final String START_LOCATION_ID = "START_LOCATION_ID";
    public static final String END_LOCATION_ID = "END_LOCATION_ID";

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
    private void drawCurrentLoc()
    {
        if (locDot == null)
            return;
        if (showLocDotReal)
        {
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

    private void drawPath()
    {
        if (paths == null || mPathArray == null)
            return;
        paths.save();
        if (mPathArray.length > 0)
        {
            paths.drawLines(mPathArray, pathPaint);
        }
        paths.restore();
    }

    private void drawBeacons()
    {
        if (beacons == null) {
            return;
        }

        for (int i = 0; i < this.cnBeaconList.size(); i++) {
            beacons.save();
            CometNavBeacon cnb = this.cnBeaconList.get(i);
            beacons.translate(cnb.getxLoc(), cnb.getyLoc());
            float scaleVal = (1 / cumulScaleFactor);
            beacons.scale(scaleVal, scaleVal);
            beacons.drawCircle(0, 0, 5, beaconPaint);
            beacons.drawText(Integer.toHexString(cnb.getName()) + " - " + String.format ("%.2f", cnb.getDistance()),(float)(0), (float)(0), beaconPaint);
            beacons.restore();
        }
    }

    private void updateDraw()
    {
        if (backgroundCanvas != null)
            backgroundCanvas.drawBitmap(immutableMap, 0, 0, paint);
        drawCurrentLoc();
        drawBeacons();
        drawPath();
        //Forces a redraw
        //photoView.invalidate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent origIntent = getIntent();
        this.startLoc = origIntent.getIntExtra(NavigationActivity.START_LOCATION_ID, 0);
        this.endLoc = origIntent.getIntExtra(NavigationActivity.END_LOCATION_ID, 0);

        CometNavView.setNavActivity(this);

        photoView = (CometNavView) findViewById(R.id.photo_view);
        task = new DownloadImageTask(photoView);
        task.execute("https://s3-us-west-2.amazonaws.com/got150030/capstone/ECSS2.png");

        //TODO Find Beacons on create. Move to only find beacons when ready to navigate
        Intent intent = new Intent(this, BeaconManagerService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);

        // your oncreate code should be
        Log.d("Navigation", "Creating NavigationActivity!");
        IntentFilter filter = new IntentFilter();
        filter.addAction("BEACON_ACTION");

        receiver = new BeaconBroadcastReceiver();

        registerReceiver(receiver, filter);

        photoView.setAdjustViewBounds(true);
        //photoView.setOnPhotoTapListener(new PhotoTapListener());
        photoView.setOnScaleChangeListener(new ScaleChangeListener());


        navigation.setOnRouteChangedListener(new OnRouteChangedListener()
        {
            boolean shouldAlt = false;

            @Override
            public void onRouteChange(int[] routeArcs) {
                Log.d("Navigation", "Updating route: " + Arrays.toString(routeArcs));
                //Each intermediate node will be present twice
                mPathArray = new float[(routeArcs.length * 4) - 4];
                int pathArrayCounter = 0;
                for (int i = 0; i < routeArcs.length; i++)
                {
                    int[] coords = navigation.getNodePos(routeArcs[i]);
                    mPathArray[pathArrayCounter] = coords[0];
                    mPathArray[pathArrayCounter + 1] = coords[1];
                    if (pathArrayCounter > 0  && i != routeArcs.length-1)
                    {
                        mPathArray[pathArrayCounter + 2] = coords[0];
                        mPathArray[pathArrayCounter + 3] = coords[1];
                        pathArrayCounter+=2;
                    }
                    pathArrayCounter+=2;
                }
                //updateDraw();
                photoView.invalidate();

            }
        });

        /**
         * @// TODO: 4/16/2017 Need to integrate with list selection
         */
        navigation.beginNavigation(this.startLoc, this.endLoc);
    }

    private void updateNavBeaconList(List<CometNavBeacon> beaconArrayList) {

        this.cnBeaconList = new ArrayList<>();
        Map<Integer, CometNavBeacon> beaconData = navigation.getBeaconMap();
        //Log.d("Navigation", "Nav beacons: " + navigation.getBeaconMap().toString());
        //Log.d("Navigation", "Found beacons: " + beaconArrayList.toString());

        for (CometNavBeacon cnBeacon : beaconArrayList)
        {
            CometNavBeacon refBeacon = beaconData.get(cnBeacon.getName());
            if (refBeacon != null) {
                cnBeacon.setxLoc(refBeacon.getxLoc());
                cnBeacon.setyLoc(refBeacon.getyLoc());
                cnBeacon.setFloor(refBeacon.getFloor());
                cnBeaconList.add(cnBeacon);
                //Log.d("Navigation", "Newly created beacon! " + cnBeacon.toString());
            }
        }
        Log.d("Navigation", "Received beacon broadcast! " +beaconArrayList.toString() );
    }

    @Override
    public void receiveNavigableLocations(List<Location> locations) {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    @Override
    public void receiveBlockedAreas(List<Location> locations) {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    @Override
    public void receivePaths(List<Path> paths) {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    private void snapToPath(CurrentLocation loc)
    {
        float minDist = Float.MAX_VALUE;
        Point2DF destCoords = new Point2DF();
        for (int x = 0, y = 1; x < mPathArray.length - 2 && y < mPathArray.length - 1; x += 2, y += 2)
        {
            Point2DF point = nearestPointOnLine(mPathArray[x], mPathArray[y], mPathArray[x+2], mPathArray[y+2], loc.getxLoc(), loc.getyLoc());
            float distance = distBetweenPoints(point.getX(), point.getY(), loc.getxLoc(), loc.getyLoc());
            if (distance < minDist) {
                minDist = distance;
                destCoords = point;
            }
        }

        loc.setxLoc(Math.round(destCoords.getX()));
        loc.setyLoc(Math.round(destCoords.getY()));
    }

    private float distBetweenPoints(float x1, float y1, float x2, float y2)
    {
        return (float) Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
    }

    private static Point2DF nearestPointOnLine(float ax, float ay, float bx, float by, float px, float py)
    {
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

        public static void setNavActivity(NavigationActivity activity)
        {
            navActivity = activity;
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            //Log.d("Test", "Draw works!");
            if (navActivity != null)
                navActivity.updateDraw();
        }
    }

    private class BeaconBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Update Beacons
            updateNavBeaconList(intent.getParcelableArrayListExtra("BEACON_LIST"));

            // Determine Current Location
            CurrentLocation loc = navigation.calculateCurrentPos(cnBeaconList);

            showLocDotReal = true;
            xPosReal = loc.getxLoc();
            yPosReal = loc.getyLoc();
            snapToPath(loc);

            // If we have a radius, that means there's only 1 beacon
            if (loc.getRadius() != 0)
            {
                showLocDot = true;
                //Draw circle at location
                xPos = loc.getxLoc();
                yPos = loc.getyLoc();
                //right now I'm assuming we're on the right floor
                mRadius = (int)Math.round(loc.getRadius());
                if (mRadius < MIN_RADIUS)
                    mRadius = MIN_RADIUS;
                Log.d("Navigation", "Found a position! " + loc.toString());
            }
            //otherwise we'll have x y and floor
            else if (loc.getxLoc() != 0)
            {
                showLocDot = true;
                xPos = loc.getxLoc();
                yPos = loc.getyLoc();
                mRadius = MIN_RADIUS;
                Log.d("Navigation", "Found a position! " + loc.toString());
            }
            //If we don't have x, that means we don't have a location. Hide our current location.
            else
            {
                showLocDot = false;
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
            //updateDraw();
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
                beaconPaint.setColor(Color.RED);

                pathPaint = new Paint();
                pathPaint.setAntiAlias(true);
                pathPaint.setColor(Color.GREEN);

                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.id.image_view,myOptions);
                immutableMap = result;
                mutableMap = immutableMap.copy(Bitmap.Config.ARGB_8888, true);

                backgroundCanvas = new Canvas(mutableMap);
                locDot = new Canvas(mutableMap);
                paths = new Canvas(mutableMap);
                beacons = new Canvas(mutableMap);

                bmImage.setImageBitmap(mutableMap);

                //updateDraw();
                photoView.invalidate();

            }
            else
                Log.d("Navigation", "bmImage is null?");
        }
    }
}
