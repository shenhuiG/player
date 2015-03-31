package com.vst.LocalPlayer.component.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.component.activity.screen.DeviceScreen;
import com.vst.LocalPlayer.component.activity.screen.FileExplorerScreen;
import com.vst.LocalPlayer.component.activity.screen.MainScreen;
import com.vst.LocalPlayer.component.activity.screen.VideosScreen;

import java.util.List;

public class MainActivity extends FragmentActivity implements IFragmentJump {

    public interface IBackPressedListener {
        boolean onBack();
    }

    private FragmentManager fm;
    final static int CONTAINER_ID = android.R.id.home;
    private String mCurrentFragmentTag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm = getSupportFragmentManager();
        View root = getWindow().getDecorView();
        root.setId(CONTAINER_ID);
        root.setBackgroundResource(R.drawable.wallpaper_1);
        fragmentJump(MainScreen.TAG, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = fm.findFragmentByTag(mCurrentFragmentTag);
        if (fragment instanceof IBackPressedListener) {
            if (!((IBackPressedListener) fragment).onBack()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, "重新扫描");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == 1) {
            //MyIntentService.startActionSCANNER(this);
        }
        return true;
    }


    @Override
    public void fragmentJump(String tag, Bundle args) {
        Fragment fragment = null;
        if ("device".equals(tag)) {
            fragment = new DeviceScreen();
        } else if ("file".equals(tag)) {
            fragment = new FileExplorerScreen();
            fragment.setArguments(args);
        } else if ("video".equals(tag)) {
            fragment = new VideosScreen();
        } else if (MainScreen.TAG.equals(tag)) {
            fragment = new MainScreen();
        } else if ("play".equals(tag)) {
            fragment = PlayFragment.newInstance(args);
        }
        if (fragment != null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(CONTAINER_ID, fragment, tag);
            List<Fragment> fragmentList = fm.getFragments();
            if (fragmentList != null && !fragmentList.isEmpty()) {
                ft.addToBackStack(null);
            }
            ft.commitAllowingStateLoss();
            mCurrentFragmentTag = tag;
        }
    }
}
