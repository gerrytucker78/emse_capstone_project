package com.edu.utdallas.argus.cometnav;

import android.util.Log;

import org.json.*;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Daniel on 4/1/2017.
 */
class DataServices {

    public void updateGraph(Navigation nav)
    {
        getLocations(nav);
    }

    private void getLocations(final Navigation nav) {
        DataServicesClient.get("locations", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("DataServices", "Warning! Locations returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray locations) {
                //Update the navigation with locations
                nav.updateNodes(locations);
                //Paths has to happen after locations is done.
                getPaths(nav);
            }
        });
    }

    private void getPaths(final Navigation nav) {
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

}
