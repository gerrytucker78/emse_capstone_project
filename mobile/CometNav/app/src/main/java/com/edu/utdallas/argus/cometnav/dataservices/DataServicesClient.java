package com.edu.utdallas.argus.cometnav.dataservices;

import android.util.Log;

import com.loopj.android.http.*;

/**
 * Created by gtucker on 3/24/2017.
 */

public class DataServicesClient {
    private static final String TAG="DataServicesClient";
    private static final String BASE_URL = "http://52.32.181.216/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
        Log.d(TAG, "URL:" + getAbsoluteUrl(url).toString());
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
