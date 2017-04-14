package com.edu.utdallas.argus.cometnav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;

import org.altbeacon.beacon.Beacon;

import java.io.InputStream;
import java.util.List;

import static android.graphics.BitmapFactory.*;

public class NavigationActivity extends AppCompatActivity
{

//    File file = new File("./mapFile");
//    ImageView image;

    private BroadcastReceiver receiver;

    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    private PhotoView photoView;
    private DownloadImageTask task;

    private Canvas locDot;
    private Paint paint;
    private Bitmap immutableMap;
    private Bitmap mutableMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        //mCustomDrawableView = new CustomDrawableView(this);
        //setContentView(mCustomDrawableView);
        //setContentView(new MyView(this));

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
               // Log.d("Navigation", "Received beacon broadcast! " +beaconArrayList.toString() );

                //do something based on the intent's action
            }
        };
        registerReceiver(receiver, filter);

        photoView.setAdjustViewBounds(true);
        photoView.setOnPhotoTapListener(new PhotoTapListener());
        photoView.setOnScaleChangeListener(new ScaleChangeListener());
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

    private float cumulScaleFactor = 1;
    private int mX = 200;
    private int mY = 250;
    private class ScaleChangeListener implements OnScaleChangedListener {

        public void onScaleChange(float scaleFactor, float focusX, float focusY) {
            cumulScaleFactor = cumulScaleFactor * scaleFactor;
            //Log.d("Navigation", "Scale changed: " + cumulScaleFactor);

            locDot.save();
            //TODO! This clears the entire map! need a way to not do that
            //locDot.drawColor(0, PorterDuff.Mode.CLEAR);

            locDot.translate(mX+=10, mY);
            float scaleVal = (1/cumulScaleFactor);
            Log.d("Navigation", "scaleVal: " + scaleVal);
            locDot.scale(scaleVal, scaleVal);
            locDot.drawCircle(0, 0, 10, paint);
            locDot.restore();
        }
    }

    //TODO Remove this class "MyView"
    public class MyView extends View
    {
        private Paint   mBitmapPaint;
        private Bitmap  mBitmap;
        private Canvas  mCanvas;

        public MyView(Context context)
        {
            super(context);
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            BitmapFactory.Options myOptions = new BitmapFactory.Options();
            myOptions.inDither = true;
            myOptions.inScaled = false;
            myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
            myOptions.inPurgeable = true;
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ecss,myOptions);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            //canvas.drawColor(0xFFAAAAAA);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawCircle(200, 250, 10, mBitmapPaint);
            //canvas.drawPath(mPath, mPaint);
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

                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.id.image_view,myOptions);
                immutableMap = result;
                mutableMap = immutableMap.copy(Bitmap.Config.ARGB_8888, true);

                locDot = new Canvas(mutableMap);
                locDot.drawCircle(200, 250, 10, paint);

                bmImage.setImageBitmap(mutableMap);
            }
            else
                Log.d("Navigation", "bmImage is null?");
        }
    }
}
