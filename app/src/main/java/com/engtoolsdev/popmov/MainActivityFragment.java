package com.engtoolsdev.popmov;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    public boolean isRestoringState = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        isRestoringState = savedInstanceState != null;

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!isRestoringState){
            //run AsyncTask
            isRestoringState = false;
        }else{
            //restoreState ??
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
