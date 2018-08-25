package com.mzom.meteoroute;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public class WeatherIconsRetriever extends AsyncTask<Resources,Void,Bitmap[]> {

    private WeatherIconsRetrievedCallback mCallback;

    interface WeatherIconsRetrievedCallback{
        void iconsRetrieved(Bitmap[] weatherIcons);
    }

    WeatherIconsRetriever(WeatherIconsRetrievedCallback callback){
        this.mCallback = callback;
    }

    @Override
    protected Bitmap[] doInBackground(Resources... resources) {
        return WeatherIcons.getAllIcons(resources[0]);
    }

    @Override
    protected void onPostExecute(Bitmap[] bitmaps) {
        super.onPostExecute(bitmaps);

        mCallback.iconsRetrieved(bitmaps);


    }
}
