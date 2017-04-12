package com.edu.utdallas.argus.cometnav;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


import java.io.File;
import java.io.InputStream;

import static android.graphics.BitmapFactory.*;

public class NavigationActivity extends AppCompatActivity
{

//    File file = new File("./mapFile");
//    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
//        image = (ImageView) findViewById(R.id.imageView2);
        new DownloadImageTask((ImageView) findViewById(R.id.imageView2))
                .execute("https://s3-us-west-2.amazonaws.com/got150030/capstone/ECSS2.png");

//        DataServices.getMap(this, file);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
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
            bmImage.setImageBitmap(result);
        }
    }
}
