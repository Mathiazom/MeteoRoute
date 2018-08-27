package com.mzom.meteoroute;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingFragment extends Fragment {

    private View view;

    private String loadingMessage;

    public static LoadingFragment newInstance(String loadingMessage) {

        LoadingFragment fragment = new LoadingFragment();
        fragment.loadingMessage = loadingMessage;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_loading,container,false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setLoadingMessage(loadingMessage);

        final TextView loadingText = view.findViewById(R.id.overlay_building_route_text);
        final Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.slide_left_to_right);
        loadingText.startAnimation(animation);

        final ProgressBar progressBar = view.findViewById(R.id.overlay_building_route_progress);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
    }

    void setLoadingMessage(@NonNull String message){

       loadingMessage = message;
       if(view != null){
           final TextView loadingText = view.findViewById(R.id.overlay_building_route_text);
           if(loadingText != null){
               loadingText.setText(message);
           }
       }
    }
}
