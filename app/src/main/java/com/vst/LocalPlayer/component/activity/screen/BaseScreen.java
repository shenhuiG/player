package com.vst.LocalPlayer.component.activity.screen;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vst.LocalPlayer.component.activity.IFragmentJump;


/**
 * listen the broadcast Media_Mounted and Media_REMOVE,notify the devices changed;
 */
public abstract class BaseScreen extends Fragment {
    protected IFragmentJump ifm = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
        try {
            ifm = (IFragmentJump) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentJump");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ifm = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mUI = makeAttachUI();
        if (mUI != null) {
            return makeAttachUI();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUI();
    }

    protected abstract View makeAttachUI();

    protected abstract void updateUI();
}
