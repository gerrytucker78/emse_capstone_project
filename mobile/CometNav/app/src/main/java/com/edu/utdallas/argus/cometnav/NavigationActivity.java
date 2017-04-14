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
import android.view.View;

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
    private ImageView img;
    private PhotoView photoView;
    private DownloadImageTask task;
    private LocationPointer pointer;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //photoView = (PhotoView) findViewById(R.id.photo_view);
        //img = (ImageView) findViewById(R.id.imageView2);
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
                Log.d("Navigation", "Received beacon broadcast! " +beaconArrayList.toString() );

                //do something based on the intent's action
            }
        };
        registerReceiver(receiver, filter);



        /*Draw a circle on an existing image - starts here*/
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ecss,myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);


        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);


        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawCircle(200, 250, 10, paint);


        ImageView imageView = (ImageView)findViewById(R.id.photo_view);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);

        /*Draw a circle on an existing image - ends here*/


//        DataServices.getMap(this, file);
        //setContentView(new MyView(this)); //TODO Remove this reference "MyView"
        //img=(ImageView) findViewById(R.id.locator);


        //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_locate);
        //pointer = new LocationPointer(icon, img);
        //new LocationPointer(icon,img);
    }

    //TODO Remove this class "MyView"
    public class MyView extends View
    {
        Paint paint = null;
        public MyView(Context context)
        {
            super(context);
            paint = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            int x = getWidth();
            int y = getHeight();
            int radius;
            radius = 100;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            // Use Color.parseColor to define HTML colors
            paint.setColor(Color.parseColor("#CD5C5C"));
            canvas.drawCircle(x / 2, y / 2, radius, paint);
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
                bmImage.setImageBitmap(result);

            }
            else
                Log.d("Navigation", "bmImage is null?");
        }
    }
}
