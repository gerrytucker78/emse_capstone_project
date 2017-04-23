package com.edu.utdallas.argus.cometnav;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;


import com.edu.utdallas.argus.cometnav.dataservices.emergencies.Emergency;
import com.edu.utdallas.argus.cometnav.dataservices.emergencies.EmergencyService;
import com.edu.utdallas.argus.cometnav.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //Doesn't really matter what number, as long as they don't match eachother
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;
    private static final int BLUETOOTH_ENABLE_REQUEST_ID = 300;

    private static final String TAG = MainActivity.class.toString();

    private BroadcastReceiver emergencyReceiver;
    private List<Emergency> emergencyList = new ArrayList<>();

    private boolean hasLocationPermissions = false;
    private Navigation navigation = Navigation.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //ButterKnife.inject(this);

        IntentFilter filterz = new IntentFilter();
        filterz.addAction("EMERGENCY_ACTION");
        emergencyReceiver = new EmergencyBroadcastReceiver();
        registerReceiver(emergencyReceiver, filterz);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                startActivity(new Intent(MainActivity.this, FindARoomMapsActivity.class));


            }


        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Request Permissions for coarse location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)){
                Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        }

        //Request Permissions for fine location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

        //Request the user enable bluetooth... to find the beacons!
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST_ID);

        // Start Emergency Service - Run until the app is killed
        Intent intent = new Intent(this, EmergencyService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);


        //This triggers the Singleton and creates the Navigation object.
        //nav.beginNavigation(4, 6);
        //double[][] positions = new double[][] { { 0, 0 }, { 10, 0 }, { 21.0, -3.0 }, { 12.4, -21.2 } };
        //double[] distances = new double[] { 8.06, 13.97, 23.32, 15.31 };
        //Log.d("Location", LocationFinder.getLocationMatrix(positions, distances).toString());
        try {
            //TODO Delete this later
            //LocationFinder.trilateration1DExact1();
            //LocationFinder.trilateration2DExact1();
            //LocationFinder.trilateration3DExact();
            //LocationFinder.trilateration3DInexact();
            //LocationFinder.trilateration2DInexact1();
            //LocationFinder.trilateration2DExact3();
        }
        catch (Exception e)
        {
            Log.d("LocTest", e.toString());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_emergency) {
            //Toast.makeText(this, "Emergency Navigation Clicked", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, NavigationActivity.class);
            
            this.startActivity(intent);
            return true;

        } else if (id == R.id.nav_manage) {
            Toast.makeText(this, "Manage Account Clicked", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.nav_navigation__to) {
            Intent intent = new Intent(this, Navigate_To.class);
            this.startActivity(intent);
            return true;

        /*} else if (id == R.id.fab) {
            Intent intent = new Intent(this,FindARoomMapsActivity.class);
            this.startActivity(intent);
            return true;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {*/

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ENABLE_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                // Request granted - bluetooth is turning on...
            }
            if (resultCode == RESULT_CANCELED) {
                // Request denied by user, or an error was encountered while
                // attempting to enable bluetooth
                //TODO Change app to indicate bluetooth is turned off?
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    hasLocationPermissions = true;
                }else{
                    //PERMISSION NOT GRANTED
                    //TODO Decide what the app will do without coarse location
                }
            }
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    hasLocationPermissions = true;
                }else{
                    //PERMISSION NOT GRANTED
                    //TODO Decide what the app will do without coarse location
                }
            }
        }
    }


    //@OnClick(R.id.button)
    public void sendNotification(View view) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/maps"));
        Intent intent = new Intent(this, NavigationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("CometNav Emergency!!!");
        builder.setContentText("Fire Incident Reported.");
        builder.setSubText("Tap to get to a Safe Zone.");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }

   // @OnClick(R.id.button2)
    public void cancelNotification(View view) {

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(1);


    }

    private class EmergencyBroadcastReceiver extends BroadcastReceiver {
        private void sendAlert(Context context, Emergency e){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            String emergClass;

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
            emergencyList = intent.getParcelableArrayListExtra("EMERGENCY_LIST");

            Log.d(TAG, "List of emergencies received from broadcast: " + emergencyList.toString());

            //Send out an alert for all the emergencies in the list
            for (Emergency e : emergencyList){
                sendAlert(context, e);
            }

        }
    }
}
