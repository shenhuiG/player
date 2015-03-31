package com.vst.LocalPlayer.component.activity.screen;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.component.provider.MediaStore;

public class MainScreen extends BaseScreen implements MediaStoreNotifier.CallBack {

    private Context ctx = null;
    public static final String TAG = "main";
    private TextView mVideoNumTxtView = null;
    private TextView mDeviceNumTxtView = null;
    private int mVideoCount = 0;
    private int mDeviceCount = 0;
    private MediaStoreNotifier notifier;


    public static MainScreen newInstance(Bundle args) {
        MainScreen fragment = new MainScreen();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = activity.getApplication();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=?", new String[]{"1"}, null);
        notifier.registQueryContentUri(MediaStore.MediaDevice.CONTENT_URI, null,
                MediaStore.MediaDevice.FIELD_VALID + "=?", new String[]{"1"}, null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        notifier.release();
        ctx = null;
    }

    @Override
    protected View makeAttachUI() {
        FrameLayout layout = new FrameLayout(ctx);
        layout.setPadding(60, 20, 60, 20);
        TextView titleTxt = new TextView(ctx);
        titleTxt.setText("VST播放器");
        titleTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30);
        layout.addView(titleTxt);
        LinearLayout center = new LinearLayout(ctx);
        center.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout videoDir = new LinearLayout(ctx);
        videoDir.setOrientation(LinearLayout.VERTICAL);
        videoDir.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        videoDir.setFocusable(true);
        videoDir.setBackgroundResource(R.drawable.focus);
        videoDir.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ifm != null) {
                    ifm.fragmentJump("video", null);
                }
            }
        });
        ImageView videoIcon = new ImageView(ctx);
        videoIcon.setBackgroundColor(0xFF378BFB);
        videoIcon.setImageResource(R.drawable.ic_video_nor);
        videoIcon.setScaleType(ScaleType.CENTER);
        videoDir.addView(videoIcon, -1, 200);
        mVideoNumTxtView = new TextView(ctx);
        mVideoNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mVideoNumTxtView.setPadding(32, 0, 0, 0);
        mVideoNumTxtView.setBackgroundColor(0xff1352A2);
        videoDir.addView(mVideoNumTxtView, -1, 108);
        center.addView(
                videoDir,
                new LinearLayout.LayoutParams(400, 348));
        LinearLayout deviceDir = new LinearLayout(ctx);
        deviceDir.setFocusable(true);
        deviceDir.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        deviceDir.setBackgroundResource(R.drawable.focus);
        deviceDir.setOrientation(LinearLayout.VERTICAL);
        deviceDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ifm != null) {
                    ifm.fragmentJump("device", null);
                }
            }
        });
        ImageView deviceIcon = new ImageView(ctx);
        deviceIcon.setBackgroundColor(0xFFFF910C);
        deviceIcon.setImageResource(R.drawable.ic_folder_nor);
        deviceIcon.setScaleType(ScaleType.CENTER);
        deviceDir.addView(deviceIcon, -1, 200);
        mDeviceNumTxtView = new TextView(ctx);
        mDeviceNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mDeviceNumTxtView.setPadding(32, 0, 0, 0);
        mDeviceNumTxtView.setBackgroundColor(0xffB25704);
        deviceDir.addView(mDeviceNumTxtView, -1, 108);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(300,
                348);
        lp.leftMargin = 30;
        center.addView(deviceDir, lp);
        layout.addView(center, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        return layout;
    }

    @Override
    protected void updateUI() {
        if (mDeviceNumTxtView != null) {
            mDeviceNumTxtView.setText(getString(R.string.deviceFomart, mDeviceCount));
        }
        if (mVideoNumTxtView != null) {
            mVideoNumTxtView.setText(getString(R.string.videoFomart, mVideoCount));
        }
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaDevice.CONTENT_URI)) {
            mDeviceCount = cursor.getCount();
        }
        if (uri.equals(MediaStore.MediaBase.CONTENT_URI)) {
            mVideoCount = cursor.getCount();
        }
        System.out.println("mDeviceCount=" + mDeviceCount + ",mVideoCount=" + mVideoCount);
        updateUI();
    }
}
