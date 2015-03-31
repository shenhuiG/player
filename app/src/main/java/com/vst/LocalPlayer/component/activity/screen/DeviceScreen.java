package com.vst.LocalPlayer.component.activity.screen;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

public class DeviceScreen extends BaseScreen implements MediaStoreNotifier.CallBack {

    private Context ctx = null;
    private ArrayList<DeviceInfo> devicePaths = new ArrayList<DeviceInfo>();
    private LinearLayout mCenterLayout = null;
    MediaStoreNotifier notifier;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = activity.getApplication();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
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
        FrameLayout root = new FrameLayout(ctx);
        mCenterLayout = new LinearLayout(ctx);
        mCenterLayout.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(mCenterLayout, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        return root;
    }

    @Override
    protected void updateUI() {
        mCenterLayout.removeAllViews();
        if (devicePaths != null && devicePaths.size() > 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(240, 308);
            lp.rightMargin = 30;
            for (int i = 0; i < devicePaths.size(); i++) {
                DeviceInfo device = devicePaths.get(i);
                View v = makeDeviceView(device.path, device.fsSize);
                if (v != null) {
                    mCenterLayout.addView(v, lp);
                }
            }
        }
    }


    private View makeDeviceView(final String path, long fsSize) {
        File usb = new File(path);
        if (usb.exists()) {
            LinearLayout fileDeviceView = new LinearLayout(ctx);
            fileDeviceView.setOrientation(LinearLayout.VERTICAL);
            fileDeviceView.setFocusable(true);
            fileDeviceView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ifm != null) {
                        Bundle args = new Bundle();
                        args.putString("RootPath", path);
                        ifm.fragmentJump("file", args);
                    }
                }
            });
            ImageView iconView = new ImageView(ctx);
            iconView.setBackgroundColor(0xFFFF910C);
            iconView.setImageResource(R.drawable.ic_usb_nor);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            fileDeviceView.addView(iconView, -1, 200);
            TextView tv = new TextView(ctx);
            tv.setText(Utils.fileSizeFormat(fsSize));
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setPadding(32, 0, 0, 0);
            tv.setBackgroundColor(0xffB25704);
            fileDeviceView.addView(tv, -1, 108);
            return fileDeviceView;
        }
        return null;
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaDevice.CONTENT_URI)) {
            devicePaths.clear();
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                long fsSize = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_FS_SIZE));
                devicePaths.add(new DeviceInfo(path, fsSize));
            }
            updateUI();
        }
    }


    private static class DeviceInfo {
        public String path;
        public long fsSize;

        private DeviceInfo(String path, long fsSize) {
            this.path = path;
            this.fsSize = fsSize;
        }
    }
}
