package com.vst.LocalPlayer.component.activity.screen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.vst.dev.common.media.VideoView;

public class PlayScreen extends BaseScreen {

    private String mediaUri;
    private Context ctx;
    private FragmentManager mFm;
    private VideoView player;

    public static final String MEDIA_URI = "mediaUri";

    public static PlayScreen newInstance(Bundle args) {
        PlayScreen fragment = new PlayScreen();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    public PlayScreen() {
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = activity.getApplicationContext();
        mFm = getChildFragmentManager();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ctx = null;
        mFm = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mediaUri = getArguments().getString(MEDIA_URI);
        }
    }

    @Override
    protected View makeAttachUI() {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void updateUI() {
    }
}
