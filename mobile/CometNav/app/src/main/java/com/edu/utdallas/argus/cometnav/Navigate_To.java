package com.edu.utdallas.argus.cometnav;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.edu.utdallas.argus.cometnav.dataservices.DataServices;
import com.edu.utdallas.argus.cometnav.dataservices.locations.ILocationClient;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Location;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Path;
import com.edu.utdallas.argus.cometnav.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Navigate_To extends AppCompatActivity implements ILocationClient {
    private Map<String, Integer> locNameToId = new HashMap<String,Integer>();
    private String startLoc = "";
    private String endLoc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate__to);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        DataServices.getNavigableLocations(this);


    }

    @Override
    public void receiveNavigableLocations(List<Location> locations) {
        Spinner startLocation = (Spinner) findViewById(R.id.startLocation);
        Spinner endLocation = (Spinner) findViewById(R.id.endLocation);

        ArrayList<String> locationNames = new ArrayList<String>();

        locationNames.add(Navigation.CURRENT_LOCATION);
        locNameToId.put(Navigation.CURRENT_LOCATION,0);

        for (Location loc : locations) {
            if (loc.getType() == Location.Type.ROOM)
            locationNames.add(loc.getName());
            locNameToId.put(loc.getName(),loc.getLocationId());
        }

        ArrayAdapter<String> adp = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_dropdown_item,locationNames);
        startLocation.setAdapter(adp);

        startLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // Notify the selected item text
                startLoc = selectedItemText;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                startLoc = "";
            }
        });

        endLocation.setAdapter(adp);

        endLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // Notify the selected item text
                endLoc = selectedItemText;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                endLoc = "";
            }
        });
    }

    @Override
    public void receiveBlockedAreas(List<Location> locations) {

    }

    @Override
    public void receivePaths(List<Path> paths) {

    }

    @Override
    public void receiveEmergencyLocations(List<Location> locations) {

    }

    /** Called when the user taps the Send button */
    public void startNavigation(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, NavigationActivity.class);
        int startLocId = 0;
        int endLocId = 0;


        if (this.locNameToId.get(this.startLoc) != null && this.locNameToId.get(this.endLoc) != null) {
            startLocId = this.locNameToId.get(this.startLoc);
            endLocId = this.locNameToId.get(this.endLoc);

            intent.putExtra(NavigationActivity.START_LOCATION_ID, startLocId);
            intent.putExtra(NavigationActivity.END_LOCATION_ID, endLocId);
        }

        this.startActivity(intent);
    }
}
