package com.taken.riceutils;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HappeningNow extends Fragment {


    public HappeningNow() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HappeningNow newInstance() {
        HappeningNow fragment = new HappeningNow();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_happening_now, container, false);
    }

}
