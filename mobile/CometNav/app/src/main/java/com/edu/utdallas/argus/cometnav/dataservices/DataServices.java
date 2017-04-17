package com.edu.utdallas.argus.cometnav.dataservices;

import android.util.Log;

//import java.io.
import org.json.*;

import com.edu.utdallas.argus.cometnav.dataservices.emergencies.EmergencyClientInterface;
import com.edu.utdallas.argus.cometnav.navigation.Navigation;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Daniel on 4/1/2017.
 */
public class DataServices {
    private static final String TAG="DataServices";

    public static void getLocations(final Navigation nav)
    {
        DataServicesClient.get("locations", null, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Warning! Locations returned as object and not array");
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
                Log.d(TAG, "Warning! paths returned as object and not array");
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
                Log.d(TAG, "Warning! blocked areas returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray blockedAreas)
            {
                //Update the navigation with locations
                nav.updateBlockedAreas(blockedAreas);
            }
        });
    }

    public static void getEmergencies(final EmergencyClientInterface emergencyClient) {
        DataServicesClient.get("emergencies", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Warning! emergencies returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray emergencies) {
                //Update the navigation with locations
                emergencyClient.receiveEmergencies(emergencies);

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
