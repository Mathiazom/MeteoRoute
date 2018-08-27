package com.mzom.meteoroute;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PermissionsFragment extends Fragment {

    private View view;

    private PermissionsFragmentListener mCallback;

    interface PermissionsFragmentListener{
        void onPermissionsDenied();
    }

    public static PermissionsFragment newInstance(PermissionsFragmentListener callback) {

        PermissionsFragment fragment = new PermissionsFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        this.view = inflater.inflate(R.layout.fragment_permissions,container,false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initButtonListeners();

    }

    private void initButtonListeners(){

        view.findViewById(R.id.request_permission_gps_location_button).setOnClickListener(v -> {
            requestRequiredPermissions();
        });

        view.findViewById(R.id.request_permission_gps_location_exit_button).setOnClickListener(v -> {
            if(mCallback != null){
                mCallback.onPermissionsDenied();
            }
        });

    }

    private static final int PERMISSION_REQUEST_CODE = 1600;

    private void requestRequiredPermissions() {

        if(getActivity() == null) return;

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }


}
