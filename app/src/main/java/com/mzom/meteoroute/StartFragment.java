package com.mzom.meteoroute;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class StartFragment extends Fragment {

    private static final String TAG = "MRU-StartFragment";

    private View view;

    private LoadSearchFromStartCallback mCallback;

    interface LoadSearchFromStartCallback{

        void loadSearchFragmentFromStart();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        view = inflater.inflate(R.layout.fragment_start, container, false);

        setListeners();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{

            mCallback = (LoadSearchFromStartCallback) context;

        }catch (ClassCastException e){

            throw new ClassCastException(context.toString() + " must implement LoadSearchFromStartCallback");

        }

    }

    private void setListeners() {

        final EditText destinationEdit = view.findViewById(R.id.start_destination_edit);
        destinationEdit.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:

                    mCallback.loadSearchFragmentFromStart();

                    v.performClick();
            }

            return false;
        });

    }
}
