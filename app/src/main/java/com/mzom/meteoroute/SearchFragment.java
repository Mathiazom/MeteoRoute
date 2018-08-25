package com.mzom.meteoroute;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mapbox.directions.service.models.Waypoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SearchFragment extends Fragment {

    private static final String TAG = "MRU-SearchFragment";

    private View view;

    private EditText searchEdit;

    private int geoCodingRequests = 0;

    private RecyclerView mResultsRecycler;
    private SearchResultsAdapter mResultsAdapter;

    private String[] placeNames;
    private Waypoint[] waypoints;

    private OnWaypointSelectedCallback onWaypointSelectedCallback;

    interface OnWaypointSelectedCallback {
        void onWaypointSelected(Waypoint waypoint, String placeName);
    }

    public static SearchFragment newInstance(OnWaypointSelectedCallback onWaypointSelectedCallback) {

        SearchFragment fragment = new SearchFragment();

        fragment.onWaypointSelectedCallback = onWaypointSelectedCallback;

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_search, container, false);

        initResultsRecycler();

        initSearchListener();

        return view;
    }

    private void initResultsRecycler() {

        mResultsAdapter = new SearchResultsAdapter(position -> {

            Log.i(TAG, "Result selected: " + String.valueOf(position) + " , Waypoint: " + String.valueOf(waypoints[position]) + " , " + String.valueOf(placeNames[position]));

            onWaypointSelectedCallback.onWaypointSelected(waypoints[position], placeNames[position]);
            hideSoftKeyboard(searchEdit);
        });

        mResultsRecycler = view.findViewById(R.id.search_results_recycler);

        mResultsRecycler.setHasFixedSize(true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mResultsRecycler.setLayoutManager(mLayoutManager);

        mResultsRecycler.setAdapter(mResultsAdapter);

        mResultsRecycler.startLayoutAnimation();
    }

    private void updateSearchRecycler(String[] results, Waypoint[] waypoints) {

        this.placeNames = results;

        this.waypoints = waypoints;

        mResultsAdapter.setResults(results, waypoints);

        mResultsAdapter.notifyDataSetChanged();

        mResultsRecycler.startLayoutAnimation();

        Log.i(TAG, "Geocoding Requests: " + String.valueOf(geoCodingRequests));
    }

    private void showSoftKeyboard(EditText editText) {
        editText.postDelayed(() -> {
            if (getContext() == null) return;

            final InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager == null) return;

            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);

        }, 10);
    }

    private void hideSoftKeyboard(EditText editText) {

        if (getContext() == null) return;

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if(imm == null) return;

        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    private void initSearchListener() {

        searchEdit = view.findViewById(R.id.search_edit);

        searchEdit.requestFocus();

        showSoftKeyboard(searchEdit);

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().length() == 0) {

                    updateSearchRecycler(new String[0], new Waypoint[0]);

                    return;
                }

                sendGeoCodingRequest(s.toString(), geoCodingJson -> {

                    try {

                        JSONArray features = new JSONObject(geoCodingJson).getJSONArray("features");
                        String[] results = new String[features.length()];
                        Waypoint[] waypoints = new Waypoint[features.length()];

                        for (int i = 0; i < features.length(); i++) {
                            JSONObject object = features.getJSONObject(i);

                            String placeName = object.getString("place_name");
                            results[i] = placeName;

                            Waypoint waypoint = new Waypoint(object.getJSONArray("center").getDouble(0), object.getJSONArray("center").getDouble(1));
                            waypoints[i] = waypoint;

                        }

                        updateSearchRecycler(results, waypoints);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Select top result on enter click
        searchEdit.setOnEditorActionListener(onEnterEditorActionListener(() -> {
            if (waypoints != null && waypoints[0] != null && placeNames != null && placeNames[0] != null) {
                onWaypointSelectedCallback.onWaypointSelected(waypoints[0], placeNames[0]);
                hideSoftKeyboard(searchEdit);
            }
        }));


    }

    public static TextView.OnEditorActionListener onEnterEditorActionListener(final Runnable doOnEnter) {
        return (__, actionId, event) -> {
            if (event == null) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Capture soft enters in a singleLine EditText that is the last EditText.
                    doOnEnter.run();
                    return true;
                } else if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    // Capture soft enters in other singleLine EditTexts
                    doOnEnter.run();
                    return true;
                } else {
                    return false;  // Let system handle all other null KeyEvents
                }
            } else if (actionId == EditorInfo.IME_NULL) {
                // Capture most soft enters in multi-line EditTexts and all hard enters.
                // They supply a zero actionId and a valid KeyEvent rather than
                // a non-zero actionId and a null event like the previous cases.
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // We capture the event when key is first pressed.
                    return true;
                } else {
                    doOnEnter.run();
                    return true;   // We consume the event when the key is released.
                }
            } else {
                // We let the system handle it when the listener
                // is triggered by something that wasn't an enter.
                return false;
            }
        };
    }

    private void sendGeoCodingRequest(String query, JSONRetriever.JSONRetrieverInterface callback) {

        new JSONRetriever(getGeoCodingRequestURL(query)).execute(callback);

        geoCodingRequests++;

    }

    private String getGeoCodingRequestURL(String query) {

        return "https://api.mapbox.com/geocoding/v5/"
                + "mapbox.places" + "/"
                + query + ".json?access_token="
                + getString(R.string.mapbox_api_key);

    }

}
