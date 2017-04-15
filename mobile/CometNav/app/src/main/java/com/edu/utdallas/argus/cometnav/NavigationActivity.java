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
import android.util.Log;
import android.widget.ImageView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;

import org.altbeacon.beacon.Beacon;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.graphics.BitmapFactory.*;

public class NavigationActivity extends AppCompatActivity
{
    private BroadcastReceiver receiver;
    private PhotoView photoView;
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
    private int mRadius = 0;
    private final int MIN_RADIUS = 10;
    private Navigation navigation = Navigation.getInstance();
    private List<CometNavBeacon> cnBeaconList = new ArrayList<>();

    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
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
        locDot.save();
        //locDot.drawBitmap(immutableMap, 0, 0, paint);
        if (showLocDot) {
            locDot.translate(xPos, yPos);
            float scaleVal = (1 / cumulScaleFactor);
            locDot.scale(scaleVal, scaleVal);
            locDot.drawCircle(0, 0, mRadius, paint);
        }
        locDot.restore();
    }

    private void drawPath()
    {
        if (paths == null)
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
        photoView.invalidate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        photoView = (PhotoView) findViewById(R.id.photo_view);
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

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<Beacon> beaconArrayList = intent.getParcelableArrayListExtra("BEACON_LIST");
                updateNavBeaconList(beaconArrayList);
                CurrentLocation loc = navigation.calculateCurrentPos(cnBeaconList);
                //If we have a radius, that means there's only 1 beacon
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
                //We always want to redraw
                updateDraw();
            }
        };
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
                    //Log.d("Navigation", "Found path: ")
                    pathArrayCounter+=2;
                }

                updateDraw();
            }
        });

        navigation.beginNavigation(52, 45);
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
            updateDraw();
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

                updateDraw();
            }
            else
                Log.d("Navigation", "bmImage is null?");
        }
    }

    private void updateNavBeaconList(List<Beacon> beaconArrayList) {
        this.cnBeaconList = new ArrayList<>();
        Map<Integer, CometNavBeacon> beaconData = navigation.getBeaconMap();

        for (Beacon beacon : beaconArrayList)
        {
            CometNavBeacon cnBeacon = new CometNavBeacon(beacon);
            CometNavBeacon refBeacon = beaconData.get(cnBeacon.getName());
            cnBeacon.setxLoc(refBeacon.getxLoc());
            cnBeacon.setyLoc(refBeacon.getyLoc());
            cnBeacon.setFloor(refBeacon.getFloor());

            cnBeaconList.add(cnBeacon);
            Log.d("Navigation", "Newly created beacon! " + cnBeacon.toString());
        }
        Log.d("Navigation", "Received beacon broadcast! " +beaconArrayList.toString() );
    }
}
