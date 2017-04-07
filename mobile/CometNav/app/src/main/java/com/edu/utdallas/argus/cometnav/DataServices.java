package com.edu.utdallas.argus.cometnav;

import android.provider.ContactsContract;
import android.util.Log;

//import java.io.
import org.json.*;
import com.loopj.android.http.*;

import java.io.File;

import cz.msebera.android.httpclient.Header;

import static java.security.AccessController.getContext;

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
                Log.d("DataServices", "Warning! paths returned as object and not array");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray blockedAreas)
            {
                //Update the navigation with locations
                nav.updateBlockedAreas(blockedAreas);
            }
        });
    }

    public static void getMap(final NavigationActivity activity, File file)
    {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get("https://s3-us-west-2.amazonaws.com/got150030/capstone/ECSS2.png", new FileAsyncHttpResponseHandler(file)
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response)
            {
                // Do something with the file `response`
                activity.updateMap(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, File response)
            {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                activity.updateMap(response);
            }
        });
    }
}
