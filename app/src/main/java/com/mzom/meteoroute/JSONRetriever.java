package com.mzom.meteoroute;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.mzom.meteoroute.ForecastConstants.getForecastRequestURL;
import static com.mzom.meteoroute.RouteConstants.getRouteRequestURL;

public class JSONRetriever extends AsyncTask<JSONRetriever.JSONRetrieverInterface,Void,String> {

    private static final String TAG = "MRU-JSONRetriever";

    private String requestURL;

    private JSONRetrieverInterface callback;

    interface JSONRetrieverInterface{

        void onRetrieved(String json);
    }

    static void sendRouteRequest(Context context, Waypoint origin, Waypoint destination, JSONRetriever.JSONRetrieverInterface callback) {

        new JSONRetriever(getRouteRequestURL(origin, destination, context)).execute(callback);

    }

    static void sendForecastRequest(Context context, LatLng latLng, JSONRetriever.JSONRetrieverInterface callback) {

        new JSONRetriever(getForecastRequestURL(context,latLng)).execute(callback);

    }

    JSONRetriever(String requestURL){

        this.requestURL = requestURL;
    }

    @Override
    protected String doInBackground(JSONRetrieverInterface... retrieverInterfaces) {

        callback = retrieverInterfaces[0];

        URL url;
        try {

            url = new URL(requestURL);

            HttpURLConnection urlConnection = null;
            try {

                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {

                e.printStackTrace();
            }

            InputStream stream = null;
            if (urlConnection != null) {

                stream = urlConnection.getInputStream();
            }

            BufferedReader reader = null;
            if (stream != null) {

                reader = new BufferedReader(new InputStreamReader(stream));
            }

            StringBuilder buffer = new StringBuilder();
            String line;
            if (reader != null) {
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
            }



            return buffer.toString();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {

        super.onPostExecute(s);

        callback.onRetrieved(s);
    }
}
