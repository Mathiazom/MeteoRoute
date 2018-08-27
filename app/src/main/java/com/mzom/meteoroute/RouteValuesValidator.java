package com.mzom.meteoroute;

import com.mapbox.directions.service.models.Waypoint;

class RouteValuesValidator {

    // Method indicating if user inputs can be used as basis for route creation
    static boolean validateInputs(final Waypoint origin, final String originPlaceName, final Waypoint destination, final String destinationPlaceName, long startTime){

        return origin != null
                && originPlaceName != null
                && destination != null
                && destinationPlaceName != null
                && startTime > 0;

    }

    // Method indicating if RouteBuilder possesses the required data to build a complete route
    static boolean validateBuilder(RouteBuilder builder){
        return builder.getOrigin() != null
                && builder.getOriginPlaceName() != null
                && builder.getDestination() != null
                && builder.getDestinationPlaceName() != null
                && builder.getStartTime() > 0
                && builder.getCoordinates() != null
                && builder.getCoordinates().size() > 0
                && builder.getDistance() > 0
                && builder.getDuration() > 0
                && builder.getWeatherNodes() != null
                && builder.getWeatherNodes().size() > 0;
    }

}
