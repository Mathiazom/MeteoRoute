package com.mzom.meteoroute;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

class WeatherNodesUtils {

    // Returns double array representing min and max temperature values as [min,max]
    static double[] getTemperatureBounds(@NonNull final ArrayList<WeatherNode> weatherNodes){

        double temperatureBounds[] = new double[2];


        // Iterates weather nodes data set to find highest temperature value. Returns -1 if no such value was found

        double maxTemp = -1;

        for(WeatherNode node : weatherNodes){

            final double nodeTemp = node.getTemperature();

            if(nodeTemp > maxTemp){
                maxTemp = nodeTemp;
            }

        }

        temperatureBounds[1] = maxTemp;


        // Iterates weather nodes data set to find lowest temperature value. Returns -1 if no such value was found

        double minTemp = maxTemp;

        for(WeatherNode node : weatherNodes){

            final double nodeTemp = node.getTemperature();

            if(nodeTemp < minTemp){
                minTemp = nodeTemp;
            }

        }

        temperatureBounds[0] = minTemp;

        return temperatureBounds;

    }

    static double getTotalPrecipitation(@NonNull final ArrayList<WeatherNode> weatherNodes){

        double totalPrecipitation = 0;

        Log.i("MRU-WeatherNodeUtils","Weathernodes size: " + String.valueOf(weatherNodes.size()));

        for(int i = 1;i<weatherNodes.size();i++){



            final WeatherNode weatherNode = weatherNodes.get(i);
            long nodeDuration = weatherNode.getTime() - weatherNodes.get(i-1).getTime();

            float portion = nodeDuration/ForecastConstants.FORECAST_INTERVAL_MILLIS;

            Log.i("MRU-WeatherNodeUtils","Node portion: " + String.valueOf(portion));

            double rainFall = weatherNode.getRainFall()*portion;
            double snowFall = weatherNode.getRainFall()*portion;

            Log.i("MRU-WeatherNodeUtils","Node rain: " + String.valueOf(weatherNode.getRainFall()));

            double nodePrecipitation = rainFall + snowFall;

            Log.i("MRU-WeatherNodeUtils","Node precip: " + String.valueOf(nodePrecipitation));

            totalPrecipitation += nodePrecipitation;
        }

        return totalPrecipitation;
    }

    static ArrayList<WeatherNode> getMarkerWeatherNodes(ArrayList<WeatherNode> weatherNodes){

        final ArrayList<WeatherNode> markerNodes = new ArrayList<>();

        int currentWeatherType = weatherNodes.get(0).getWeatherType();
        int currentTypeCount = 0;

        for(int i = 0;i<weatherNodes.size();i++){

            final WeatherNode node = weatherNodes.get(i);

            if(node.getWeatherType() != currentWeatherType || i == weatherNodes.size()-1){

                int midIndex = i-(currentTypeCount/2);
                markerNodes.add(weatherNodes.get(midIndex));

                currentWeatherType = node.getWeatherType();
                currentTypeCount = 0;
            }

            currentTypeCount++;

        }

        return markerNodes;

    }

}
