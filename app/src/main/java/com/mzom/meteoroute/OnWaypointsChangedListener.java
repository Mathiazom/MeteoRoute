package com.mzom.meteoroute;

import com.mapbox.directions.service.models.Waypoint;

interface OnWaypointsChangedListener {

    void onDestinationWaypointChanged(Waypoint destination, String placeName);
}
