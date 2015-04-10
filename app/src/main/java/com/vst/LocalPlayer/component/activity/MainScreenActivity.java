package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import com.vst.dev.common.util.Utils;

public class MainScreenActivity extends Activity implements MediaStoreNotifier.CallBack {

    private Context ctx = null;
    public static final String TAG = "main";
    private TextView mVideoNumTxtView = null;
    private TextView mDeviceNumTxtView = null;
    private int mVideoCount = 0;
    private int mDeviceCount = 0;
    private MediaStoreNotifier notifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplication();
        setContentView(makeAttachUI());
        updateUI();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=?", new String[]{"1"}, null);
        notifier.registQueryContentUri(MediaStore.MediaDevice.CONTENT_URI, null,
                MediaStore.MediaDevice.FIELD_VALID + "=?", new String[]{"1"}, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifier.release();
        ctx = null;
    }

    protected View makeAttachUI() {
        FrameLayout layout = new FrameLayout(ctx);
        layout.setBackgroundResource(R.drawable.wallpaper_1);
        layout.setPadding(Utils.getFitSize(ctx, 60),
                Utils.getFitSize(ctx, 20),
                Utils.getFitSize(ctx, 60),
                Utils.getFitSize(ctx, 20));
        TextView titleTxt = new TextView(ctx);
        titleTxt.setText("VST播放器");
        titleTxt.setTextColor(Color.WHITE);
        titleTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(ctx, 30));
        layout.addView(titleTxt);
        LinearLayout center = new LinearLayout(ctx);
        center.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout videoDir = new LinearLayout(ctx);
        videoDir.setOrientation(LinearLayout.VERTICAL);
        videoDir.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        videoDir.setFocusable(true);
        videoDir.setBackgroundResource(R.drawable.fffffffff);
        videoDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ctx, VideosScreenActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
            }
        });
        ImageView videoIcon = new ImageView(ctx);
        videoIcon.setBackgroundColor(getResources().getColor(R.color.blue_1));
        videoIcon.setImageResource(R.drawable.ic_video_nor);
        videoIcon.setScaleType(ScaleType.CENTER);
        videoDir.addView(videoIcon, -1, Utils.getFitSize(ctx, 200));
        mVideoNumTxtView = new TextView(ctx);
        mVideoNumTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(ctx, 28));
        mVideoNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mVideoNumTxtView.setPadding(Utils.getFitSize(ctx, 32), 0, 0, 0);
        mVideoNumTxtView.setBackgroundColor(0xff1352A2);
        videoDir.addView(mVideoNumTxtView, -1, Utils.getFitSize(ctx, 108));
        center.addView(
                videoDir,
                new LinearLayout.LayoutParams(Utils.getFitSize(ctx, 400), Utils.getFitSize(ctx, 348)));
        LinearLayout deviceDir = new LinearLayout(ctx);
        deviceDir.setFocusable(true);
        deviceDir.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        deviceDir.setBackgroundResource(R.drawable.fffffffff);
        deviceDir.setOrientation(LinearLayout.VERTICAL);
        deviceDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ctx, DeviceScreenActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
            }
        });
        ImageView deviceIcon = new ImageView(ctx);
        deviceIcon.setBackgroundColor(getResources().getColor(R.color.yellow_1));
        deviceIcon.setImageResource(R.drawable.ic_folder_nor);
        deviceIcon.setScaleType(ScaleType.CENTER);
        deviceDir.addView(deviceIcon, -1, Utils.getFitSize(ctx, 200));
        mDeviceNumTxtView = new TextView(ctx);
        mDeviceNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mDeviceNumTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(ctx, 28));
        mDeviceNumTxtView.setPadding(Utils.getFitSize(ctx, 32), 0, 0, 0);
        mDeviceNumTxtView.setBackgroundColor(getResources().getColor(R.color.yellow_2));
        deviceDir.addView(mDeviceNumTxtView, -1, Utils.getFitSize(ctx, 108));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.getFitSize(ctx, 300),
                Utils.getFitSize(ctx, 348));
        lp.leftMargin = 30;
        center.addView(deviceDir, lp);
        layout.addView(center, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        return layout;
    }

    private void updateUI() {
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
        updateUI();
    }
}
