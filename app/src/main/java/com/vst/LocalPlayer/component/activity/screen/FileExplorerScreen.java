package com.vst.LocalPlayer.component.activity.screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.activity.MainActivity;
import com.vst.LocalPlayer.component.activity.Player;

import java.io.File;
import java.io.FileFilter;

public class FileExplorerScreen extends BaseScreen implements MainActivity.IBackPressedListener {

    private Context ctx = null;
    private String mBaseDirPath = null;
    private String mExplorerRootPath = null;
    private boolean mDeviceExists = false;
    private ListView mListView = null;
    private TextView rootPathView = null;
    private View emptyView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = activity.getApplication();
        mBaseDirPath = getArguments().getString("RootPath", "");
        File file = new File(mBaseDirPath);
        if (file.exists()) {
            mDeviceExists = true;
            if (file.isDirectory()) {
                mExplorerRootPath = mBaseDirPath;
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ctx = null;
    }

    @Override
    protected View makeAttachUI() {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        rootPathView = new TextView(ctx);
        rootPathView.setText(mExplorerRootPath);
        layout.addView(rootPathView);
        emptyView = new TextView(ctx);
        ((TextView) emptyView).setText("设备已移除或不存在");
        mListView = new ListView(ctx);
        mListView.setEmptyView(emptyView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = (File) parent.getAdapter().getItem(position);
                Utils.FileCategory category = Utils.getFileCategory(file);
                switch (category) {
                    case Dir:
                        mExplorerRootPath = file.getAbsolutePath();
                        updateUI();
                        break;
                    case Apk:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        ctx.startActivity(intent);
                        break;
                    case Music:
                        Utils.pleyAudioFile(ctx, file);
                        break;
                    case Video:
                        intent = new Intent(ctx, Player.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                        Bundle args = new Bundle();
                        args.putString("pushUrl", "file://" + file.getAbsolutePath());
                        intent.putExtras(args);
                        ctx.startActivity(intent);
                        //Utils.pleyMediaFile(ctx, file);
                        break;
                    default:
                        break;
                }
            }
        });
        layout.addView(mListView, -1, -1);
        layout.addView(emptyView);
        return layout;
    }


    @Override
    protected void updateUI() {
        if (mDeviceExists) {
            mListView.setEmptyView(null);
            rootPathView.setText(mExplorerRootPath);
            mListView.setAdapter(new FileArrayAdapter(ctx, new File(mExplorerRootPath).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !(pathname.isHidden() || pathname.getName().contains("$RECYCLE.BIN"));
                }
            })));
        } else {
            mListView.setEmptyView(emptyView);
            emptyUI();
        }
    }

    @Override
    public boolean onBack() {
        if (!mExplorerRootPath.equals(mBaseDirPath)) {
            mExplorerRootPath = new File(mExplorerRootPath).getParentFile().getAbsolutePath();
            updateUI();
            return true;
        }
        return false;
    }

    private class FileArrayAdapter extends ArrayAdapter<File> {

        public FileArrayAdapter(Context context, File[] objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            File file = getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                LinearLayout layout = new LinearLayout(ctx);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setPadding(60, 10, 60, 10);
                layout.setGravity(Gravity.CENTER_VERTICAL);
                ImageView iconView = new ImageView(ctx);
                TextView nameView = new TextView(ctx);
                nameView.setGravity(Gravity.CENTER_VERTICAL);
                nameView.setPadding(30, 0, 0, 0);
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
            holder.fileNameView.setText(file.getName());
            holder.fileIconView.setImageResource(Utils.getFileCategoryIcon(file));
            return convertView;
        }
    }


    private class ViewHolder {
        ImageView fileIconView;
        TextView fileNameView;
    }

    private void emptyUI() {
        mExplorerRootPath = mBaseDirPath;
        rootPathView.setText(mBaseDirPath);
        mListView.setAdapter(new FileArrayAdapter(ctx, new File[0]));
    }
}
