package com.edu.utdallas.argus.cometnav.dataservices;

import android.util.Log;

//import java.io.
import org.json.*;

import com.edu.utdallas.argus.cometnav.dataservices.emergencies.IEmergencyClient;
import com.edu.utdallas.argus.cometnav.dataservices.locations.ILocationClient;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Location;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Path;
import com.edu.utdallas.argus.cometnav.navigation.Navigation;
import com.loopj.android.http.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Daniel on 4/1/2017.
 */
public class DataServices {
    private static final String TAG = "DataServices";

    private static final String LOCATIONS_NAVIGABLE = "locations/navigable";
    private static final String LOCATIONS_BLOCKED_AREAS = "locations/blockedAreas";
    private static final String LOCATIONS_PATHS = "locations/paths";
    private static final String LOCATIONS_EMERGENCIES = "locations/emergencies";

    public static void getNavigableLocations(final ILocationClient client) {
        DataServicesClient.get(LOCATIONS_NAVIGABLE, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Warning! Locations returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray locations) {
                client.receiveNavigableLocations(Location.createLocations(locations));

            }
        });
    }

    public static void getPaths(final ILocationClient client) {
        DataServicesClient.get(LOCATIONS_PATHS, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Warning! paths returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray paths) {
                client.receivePaths(Path.createPaths(paths));
            }
        });
    }

    public static void getBlockedAreas(final ILocationClient client) {
        DataServicesClient.get(LOCATIONS_BLOCKED_AREAS, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Warning! blocked areas returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray blockedAreas) {
                client.receiveBlockedAreas(Location.createLocations(blockedAreas));
            }
        });
    }

    public static void getEmergencies(final IEmergencyClient emergencyClient) {
        DataServicesClient.get("emergencies", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Warning! emergencies returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray emergencies) {


                DataServicesClient.get(LOCATIONS_EMERGENCIES, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        Log.d(TAG, "Warning! blocked areas returned as object and not array");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray emergencyLocations) {
                        //Update the navigation with locations
                        emergencyClient.receiveEmergencies(emergencies, emergencyLocations);
                    }
                });
                Log.i(TAG, "Emergencies received from callback: " + emergencies.toString());
            }
        });
    }

    public static void getBeacons(final Navigation nav) {
        DataServicesClient.get("sensors", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Warning! beacons returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray beacons) {
                //Update the navigation with locations
                nav.updateBeacons(beacons);
            }
        });
    }

}
