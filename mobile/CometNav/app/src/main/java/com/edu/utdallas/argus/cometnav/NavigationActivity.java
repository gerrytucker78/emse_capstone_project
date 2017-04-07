package com.edu.utdallas.argus.cometnav;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


import java.io.File;

public class NavigationActivity extends AppCompatActivity
{

    File file = new File("./mapFile");
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        image = (ImageView) findViewById(R.id.imageView2);

        DataServices.getMap(this, file);
    }

    public void updateMap(File map)
    {
        Log.d("NavigationActivity", "Got image callback");
        Bitmap bitmap = BitmapFactory.decodeFile(map.getAbsolutePath());
        image.setImageBitmap(bitmap);
    }
}
