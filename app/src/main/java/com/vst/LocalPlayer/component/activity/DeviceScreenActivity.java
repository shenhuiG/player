package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;

public class DeviceScreenActivity extends Activity implements MediaStoreNotifier.CallBack {

    private Context ctx = null;
    private ArrayList<DeviceInfo> devicePaths = new ArrayList<DeviceInfo>();
    private LinearLayout mCenterLayout = null;
    private MediaStoreNotifier notifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplication();
        setContentView(makeAttachUI());
        updateUI();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
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
        FrameLayout root = new FrameLayout(ctx);
        root.setBackgroundResource(R.drawable.wallpaper_1);
        mCenterLayout = new LinearLayout(ctx);
        mCenterLayout.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(mCenterLayout, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        return root;
    }

    protected void updateUI() {
        mCenterLayout.removeAllViews();
        if (devicePaths != null && devicePaths.size() > 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    com.vst.dev.common.util.Utils.getFitSize(ctx, 240),
                    com.vst.dev.common.util.Utils.getFitSize(ctx, 308));
            lp.rightMargin = 30;
            for (int i = 0; i < devicePaths.size(); i++) {
                DeviceInfo device = devicePaths.get(i);
                View v = makeDeviceView(device);
                if (v != null) {
                    mCenterLayout.addView(v, lp);
                }
            }
        }
        mCenterLayout.requestFocus();
    }


    private View makeDeviceView(final DeviceInfo deviceInfo) {
        LinearLayout fileDeviceView = new LinearLayout(ctx);
        fileDeviceView.setOrientation(LinearLayout.VERTICAL);
        fileDeviceView.setFocusable(true);
        fileDeviceView.setBackgroundResource(R.drawable.fffffffff);
        fileDeviceView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(ctx, FileExplorerScreenActivity.class);
                i.putExtra(FileExplorerScreenActivity.PARAMS_DEVICE_UUID, deviceInfo.uuid);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
            }
        });
        ImageView iconView = new ImageView(ctx);
        iconView.setBackgroundColor(getResources().getColor(R.color.yellow_1));
        iconView.setImageResource(R.drawable.ic_usb_nor);
        iconView.setScaleType(ImageView.ScaleType.CENTER);
        fileDeviceView.addView(iconView, -1, com.vst.dev.common.util.Utils.getFitSize(ctx, 200));
        TextView tv = new TextView(ctx);
        tv.setText(Utils.fileSizeFormat(deviceInfo.fsSize));
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding(com.vst.dev.common.util.Utils.getFitSize(ctx,
                com.vst.dev.common.util.Utils.getFitSize(ctx, 32)), 0, 0, 0);
        tv.setBackgroundColor(getResources().getColor(R.color.yellow_2));
        fileDeviceView.addView(tv, -1, com.vst.dev.common.util.Utils.getFitSize(ctx, 108));
        return fileDeviceView;
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaDevice.CONTENT_URI)) {
            devicePaths.clear();
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                String uuid = cursor.getString(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_UUID));
                long fsSize = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_FS_SIZE));
                devicePaths.add(new DeviceInfo(uuid, path, fsSize));
            }
            updateUI();
        }
    }


    private static class DeviceInfo {
        public String path;
        public long fsSize;
        public String uuid;

        private DeviceInfo(String uuid, String path, long fsSize) {
            this.uuid = uuid;
            this.path = path;
            this.fsSize = fsSize;
        }
    }
}
