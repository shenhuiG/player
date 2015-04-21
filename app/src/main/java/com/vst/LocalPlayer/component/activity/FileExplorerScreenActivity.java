package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStore;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileExplorerScreenActivity extends Activity implements MediaStoreNotifier.CallBack {

    public static final String PARAMS_DEVICE_UUID = "deviceUUid";
    public static final String TAG = "FileExplorerScreen";
    private Context ctx = null;
    private String mExplorerRootPath = null;
    private boolean mDeviceExists = false;
    private ListView mListView = null;
    private TextView rootPathView = null;
    private View emptyView;
    private MediaStoreNotifier notifier;
    private String deviceUUID = null;
    private String mDevicePath = null;
    private List<String> childFiles = new ArrayList<String>();
    private FileArrayAdapter mAdapter;
    private int selection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplication();
        setContentView(makeAttachUI());
        updateUI();
        Intent i = getIntent();
        deviceUUID = i.getStringExtra(PARAMS_DEVICE_UUID);
        if (deviceUUID != null) {
            notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
            notifier.registQueryContentUri(MediaStore.MediaDevice.CONTENT_URI, null,
                    MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=? AND " + MediaStore.MediaDevice.FIELD_VALID + "=?", new String[]{deviceUUID, "1"}, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ctx = null;
        notifier.release();
        notifier = null;
    }

    protected View makeAttachUI() {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setBackgroundResource(R.drawable.wallpaper_1);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(com.vst.dev.common.util.Utils.getFitSize(ctx, 70),
                com.vst.dev.common.util.Utils.getFitSize(ctx, 40), 0, com.vst.dev.common.util.Utils.getFitSize(ctx, 70));
        rootPathView = new TextView(ctx);
        rootPathView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(ctx, 35));
        rootPathView.setSingleLine(true);
        rootPathView.setEllipsize(TextUtils.TruncateAt.START);
        layout.addView(rootPathView, -1, -2);
        emptyView = new TextView(ctx);
        ((TextView) emptyView).setText(R.string.deviceEmpty);
        ((TextView) emptyView).setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(ctx, 30));
        mListView = new ListView(ctx);
        mListView.setEmptyView(emptyView);
        mListView.setPadding(
                com.vst.dev.common.util.Utils.getFitSize(ctx, 120),
                0,
                com.vst.dev.common.util.Utils.getFitSize(ctx, 120),
                0);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = (String) parent.getAdapter().getItem(position);
                File file = new File(mExplorerRootPath, fileName);
                Utils.FileCategory category = Utils.getFileCategory(file);
                if (category == null) {
                    return;
                }
                switch (category) {
                    case Dir:
                        selection = -1;
                        updateDate(file, null);
                        updateUI();
                        break;
                    case Apk:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        ctx.startActivity(intent);
                        break;
                    case Music:
                        Utils.playAudioFile(ctx, file);
                        break;
                    case Video:
                        String relativePath = file.getAbsolutePath().replace(mDevicePath, "");
                        long mediaId = -1;
                        Cursor c = ctx.getContentResolver().query(MediaStore.MediaBase.CONTENT_URI, null,
                                MediaStore.MediaBase.FIELD_RELATIVE_PATH + "=?", new String[]{relativePath}, null);
                        if (c.moveToFirst()) {
                            mediaId = c.getLong(c.getColumnIndex(MediaStore.MediaBase._ID));
                        }
                        c.close();
                        Utils.playMediaFile(ctx, file, mediaId);
                        break;
                    case BDMV:
                        String target = Utils.findBDMVMediaPath(file);
                        System.out.println("target" + target);
                        if (target != null) {
                            Utils.playMediaFile(ctx, new File(target), -1);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        mListView.setSelector(R.drawable.explorer_item_selector_bg);
        mListView.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        lp.leftMargin = com.vst.dev.common.util.Utils.getFitSize(ctx, 90);
        lp.rightMargin = com.vst.dev.common.util.Utils.getFitSize(ctx, 90);
        lp.topMargin = com.vst.dev.common.util.Utils.getFitSize(ctx, 45);
        layout.addView(mListView, lp);
        layout.addView(emptyView, lp);
        return layout;
    }


    protected void updateUI() {
        if (mListView != null) {
            if (mAdapter == null) {
                mAdapter = new FileArrayAdapter(ctx, childFiles);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
            if (selection >= 0) {
                mListView.setSelection(selection);
            }
            if (mDeviceExists) {
                mListView.setEmptyView(null);
            } else {
                mListView.setEmptyView(emptyView);
            }
            mListView.requestFocus();
        }
        if (rootPathView != null) {
            rootPathView.setText(mExplorerRootPath);
        }
    }

    @Override
    public void onBackPressed() {
        if (onBack()) {
            return;
        }
        super.onBackPressed();
    }

    public boolean onBack() {
        if (!mExplorerRootPath.equals(mDevicePath)) {
            updateDate(new File(mExplorerRootPath).getParentFile(), new File(mExplorerRootPath));
            updateUI();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (MediaStore.MediaDevice.CONTENT_URI.equals(uri)) {
            if (cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                File file = new File(path);
                if (file.exists()) {
                    mDevicePath = path;
                    mDeviceExists = true;
                    updateDate(file, null);
                    updateUI();
                }
            }
        }
    }


    private void updateDate(File file, File child) {
        mExplorerRootPath = file.getAbsolutePath();
        String[] files = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File f = new File(dir, filename);
                if (!f.isHidden() && !filename.contains("$")) {
                    return true;
                }
                return false;
            }
        });
        childFiles.clear();
        if (files != null && files.length > 0) {
            childFiles.addAll(Arrays.asList(files));
        }
        if (child != null && child.exists()) {
            String name = child.getName();
            selection = childFiles.indexOf(name);
        }
    }

    private class FileArrayAdapter extends ArrayAdapter<String> {

        public FileArrayAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String filePath = getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                LinearLayout layout = new LinearLayout(ctx);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setPadding(
                        com.vst.dev.common.util.Utils.getFitSize(ctx, 60),
                        com.vst.dev.common.util.Utils.getFitSize(ctx, 40),
                        com.vst.dev.common.util.Utils.getFitSize(ctx, 60),
                        com.vst.dev.common.util.Utils.getFitSize(ctx, 40));
                layout.setGravity(Gravity.CENTER_VERTICAL);
                ImageView iconView = new ImageView(ctx);
                TextView nameView = new TextView(ctx);
                nameView.setGravity(Gravity.CENTER_VERTICAL);
                nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25);
                nameView.setSingleLine(true);
                nameView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                nameView.setMarqueeRepeatLimit(Integer.MAX_VALUE);
                nameView.setPadding(com.vst.dev.common.util.Utils.getFitSize(ctx, 30), 0, 0, 0);
                layout.addView(iconView);
                layout.addView(nameView);
                holder = new ViewHolder();
                holder.fileIconView = iconView;
                holder.fileNameView = nameView;
                convertView = layout;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            File file = new File(mExplorerRootPath, filePath);
            holder.fileNameView.setText(file.getName());

            holder.fileIconView.setImageResource(Utils.getFileCategoryIcon(file));
            return convertView;
        }

        private class ViewHolder {
            ImageView fileIconView;
            TextView fileNameView;
            TextView tagTextView;
        }
    }

}
