package com.edu.utdallas.argus.cometnav;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

/**
 * Created by gtucker on 3/24/2017.
 */

public class DataServicesClient {
    AsyncHttpClient client = new AsyncHttpClient();
    public static String LOCATION_URL = "http://52.32.181.216/locations";

    public void getLocations() {
        client.get(LOCATION_URL, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
}
