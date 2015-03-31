package com.vst.LocalPlayer.component.activity.screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.LocalPlayer.IMDBApi;
import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.component.activity.Player;
import com.vst.LocalPlayer.component.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideosScreen extends BaseScreen implements MediaStoreNotifier.CallBack {

    private Context ctx;
    private MediaStoreNotifier notifier = null;
    private ListView mListView;
    private TextView mTextView;
    private ArrayAdapter<MediaBaseInfo> mAdapter;
    private ArrayList<MediaBaseInfo> mArray = new ArrayList<MediaBaseInfo>();
    private HashMap<Long, String> mDevicePath = new HashMap<Long, String>();


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = activity.getApplicationContext();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=?", new String[]{"1"}, null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ctx = null;
        notifier.release();
    }


    @Override
    protected View makeAttachUI() {
        FrameLayout root = new FrameLayout(ctx);
        mTextView = new TextView(ctx);
        mTextView.setText("videoList");
        root.addView(mTextView);
        mListView = new ListView(ctx);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaBaseInfo info = (MediaBaseInfo) parent.getAdapter().getItem(position);
                //Utils.pleyMediaFile(ctx, new File(info.path));
                String path = info.devicePath + info.path;
                if (new File(path).exists()) {
                    Bundle args = new Bundle();
                    args.putString("pushUrl", "file://" + path);
                /*ifm.fragmentJump("play", args);*/
                    Intent intent = new Intent(ctx, Player.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtras(args);
                    ctx.startActivity(intent);
                } else {
                    Toast.makeText(ctx, "该影片不存在 路径：" + "盘:" + info.path, Toast.LENGTH_LONG).show();
                }
            }
        });
        registerForContextMenu(mListView);
        root.addView(mListView);
        return root;
    }


    @Override
    protected void updateUI() {
        if (mListView != null) {
            if (mAdapter == null) {
                mAdapter = new ArrayAdapter<MediaBaseInfo>(ctx, android.R.layout.simple_list_item_1, mArray);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaBase.CONTENT_URI)) {
            mArray.clear();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_RELATIVE_PATH));
                long deviceId = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_DEVICE_ID));
                String devicePath = "";
                if (mDevicePath.containsKey(deviceId)) {
                    devicePath = mDevicePath.get(deviceId);
                } else {
                    Cursor deviceC = ctx.getContentResolver().query(MediaStore.getContentUri(
                            MediaStore.MediaDevice.TABLE_NAME, deviceId), null, null, null, null);
                    if (deviceC.moveToFirst()) {
                        devicePath = deviceC.getString(deviceC.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                        mDevicePath.put(deviceId, devicePath);
                    }
                    deviceC.close();
                }
                mArray.add(new MediaBaseInfo(path, name, devicePath));
            }
            updateUI();
        }
    }

    /* 每次长按ContextMenu被绑定的View的子控件，都会调用此方法*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        System.out.println("onCreateContextMenu------>");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        System.out.println("onContextItemSelected------>" + item.getItemId());
        return true;
    }

    private class Adapter extends ArrayAdapter<MediaBaseInfo> {

        public Adapter(Context context, List<MediaBaseInfo> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            return super.getView(position, convertView, parent);
        }
    }

    private static class MediaBaseInfo {
        public String path;
        public String name;
        public String devicePath;

        public MediaBaseInfo(String path, String name, String devicePath) {
            this.path = path;
            this.name = name;
            this.devicePath = devicePath;
        }

        public String toString() {
            return name + "/n" + IMDBApi.smartMediaName(name);
        }
    }


}
