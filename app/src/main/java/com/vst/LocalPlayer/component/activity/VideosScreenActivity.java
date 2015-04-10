package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.model.MediaBaseModel;
import com.yixia.zi.utils.ImageCache;
import com.yixia.zi.utils.ImageFetcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideosScreenActivity extends Activity implements MediaStoreNotifier.CallBack {

    public static final String TAG = "VideosScreen";
    private Context ctx;
    private MediaStoreNotifier notifier = null;
    private GridView mListView;
    private TextView mTextView;
    private ArrayAdapter<MediaBaseModel> mAdapter;
    private ArrayList<MediaBaseModel> mArray = new ArrayList<MediaBaseModel>();
    private HashMap<Long, String> mDevicePath = new HashMap<Long, String>();
    private ImageFetcher fetcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        setContentView(makeAttachUI());
        updateUI();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=?", new String[]{"1"}, null);
        fetcher = new ImageFetcher(ctx);
        fetcher.setImageCache(new ImageCache(ctx, new ImageCache.ImageCacheParams(".cache")));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ctx = null;
        notifier.release();
        fetcher.setExitTasksEarly(true);
        fetcher = null;
    }


    protected View makeAttachUI() {
        LinearLayout root = new LinearLayout(ctx);
        root.setBackgroundResource(R.drawable.wallpaper_1);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                com.vst.dev.common.util.Utils.getFitSize(ctx, 35),
                com.vst.dev.common.util.Utils.getFitSize(ctx, 0),
                com.vst.dev.common.util.Utils.getFitSize(ctx, 35),
                com.vst.dev.common.util.Utils.getFitSize(ctx, 0));
        mTextView = new TextView(ctx);
        mTextView.setPadding(
                com.vst.dev.common.util.Utils.getFitSize(ctx, 0),
                com.vst.dev.common.util.Utils.getFitSize(ctx, 35),
                com.vst.dev.common.util.Utils.getFitSize(ctx, 0),
                com.vst.dev.common.util.Utils.getFitSize(ctx, 35));
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(ctx, 35));
        root.addView(mTextView);
        mListView = new GridView(ctx);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaBaseModel info = (MediaBaseModel) parent.getAdapter().getItem(position);
                String path = info.devicePath + info.relativePath;
                if (new File(path).exists()) {
                    Utils.playMediaFile(ctx, new File(path), info.id);
                } else {
                    Toast.makeText(ctx, "该影片不存在 路径：" + "盘:" + info.relativePath, Toast.LENGTH_LONG).show();
                }
            }
        });
        mListView.setNumColumns(5);
        mListView.setColumnWidth(250);
        mListView.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
        mListView.setHorizontalSpacing(com.vst.dev.common.util.Utils.getFitSize(ctx, 30));
        mListView.setVerticalSpacing(com.vst.dev.common.util.Utils.getFitSize(ctx, 35));
        root.addView(mListView);
        return root;
    }

    protected void updateUI() {
        if (mListView != null) {
            if (mAdapter == null) {
                mAdapter = new Adapter(ctx, mArray);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
        if (mTextView != null) {
            mTextView.setText("视频 " + mArray.size() + " 部");
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
                long _id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaBase._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_TITLE));
                String poster = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_POSTER));
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
                mArray.add(new MediaBaseModel(_id, path, name, title, poster, devicePath));
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

    private class Adapter extends ArrayAdapter<MediaBaseModel> {

        public Adapter(Context context, List<MediaBaseModel> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                holder = new ViewHolder();
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                TextView text = new TextView(getContext());
                text.setSingleLine(true);
                text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                text.setMarqueeRepeatLimit(Integer.MAX_VALUE);
                layout.addView(image, -1, 300);
                layout.addView(text, -1, 60);
                convertView = layout;
                holder.poster = image;
                holder.name = text;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MediaBaseModel info = getItem(position);
            holder.name.setText(null == info.title ? info.name : info.title);
            if (info.poster != null) {
                fetcher.loadImage(info.poster, holder.poster, R.drawable.poster_default);
            } else {
                holder.poster.setImageResource(R.drawable.poster_default);
            }
            return convertView;
        }


        class ViewHolder {
            ImageView poster;
            TextView name;
        }
    }


}
