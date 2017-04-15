package com.edu.utdallas.argus.cometnav;

import android.provider.ContactsContract;
import android.util.Log;

//import java.io.
import org.json.*;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Daniel on 4/1/2017.
 */
class DataServices {


    public static void getLocations(final Navigation nav)
    {
        DataServicesClient.get("locations", null, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("DataServices", "Warning! Locations returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray locations)
            {
                //Update the navigation with locations
                nav.updateNodes(locations);
            }
        });
    }

    public static void getPaths(final Navigation nav) {
        DataServicesClient.get("locations/paths", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("DataServices", "Warning! paths returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray paths) {
                //Update the navigation with locations
                nav.updatePaths(paths);
            }
        });
    }

    public static void getBlockedAreas(final Navigation nav) {
        DataServicesClient.get("locations", null, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("DataServices", "Warning! blocked areas returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray blockedAreas)
            {
                //Update the navigation with locations
                nav.updateBlockedAreas(blockedAreas);
            }
        });
    }

    public static void getEmergencies(final EmergencyClient emergencyClient) {
        DataServicesClient.get("emergencies", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("DataServices", "Warning! emergencies returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray emergencies) {
                //Update the navigation with locations
                emergencyClient.receiveEmergencies(emergencies);
            }
        });
    }

    public static void getBeacons(final Navigation nav) {
        DataServicesClient.get("sensors", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("DataServices", "Warning! beacons returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray beacons) {
                //Update the navigation with locations
                nav.updateBeacons(beacons);
            }
        });
    }

}
