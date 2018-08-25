package com.mzom.meteoroute;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.directions.service.models.Waypoint;

class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private String[] mResults;
    private Waypoint[] mResultsWaypoints;

    static class ViewHolder extends RecyclerView.ViewHolder{

        final ConstraintLayout mResultView;
        final TextView mResultText;

        ViewHolder(ConstraintLayout resultView) {
            super(resultView);

            mResultView = resultView;
            mResultText = resultView.findViewById(R.id.search_result_text);
        }
    }


    interface OnResultClickedListener{
        void onResultClicked(int position);
    }

    private OnResultClickedListener onResultClickedListener;


    SearchResultsAdapter(OnResultClickedListener onResultClickedListener){
        this.mResults = new String[0];
        this.mResultsWaypoints = new Waypoint[0];
        this.onResultClickedListener = onResultClickedListener;
    }

    void setResults(String[] results, Waypoint[] waypoints){
        this.mResults = results;
        this.mResultsWaypoints = waypoints;
    }

    @NonNull
    @Override
    public SearchResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ConstraintLayout resultView = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.template_search_result,parent,false);

        return new ViewHolder(resultView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.mResultText.setText(mResults[position]);
        holder.itemView.setOnClickListener(v -> onResultClickedListener.onResultClicked(position));

    }

    @Override
    public int getItemCount() {

        return mResults.length;
    }


}
