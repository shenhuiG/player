package com.vst.LocalPlayer.component.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.vst.LocalPlayer.model.IMDBApi;
import com.vst.LocalPlayer.model.NFOApi;
import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.component.provider.MediaStoreHelper;

import java.io.File;
import java.io.FileFilter;

public class MyIntentService extends IntentService {
    private static final String ACTION_SCANNER = "com.vst.LocalPlayer.component.service.action.SCANNER";
    private static final String ACTION_SCANNER_DEVICE_ID = "deviceId";
    private static final String ACTION_SCANNER_DEVICE_PATH = "devicePath";
    private static final String ACTION_UPDATE_VALID = "com.vst.LocalPlayer.component.service.action.UpdateDeviceAndMediaValid";
    private static final String ACTION_ENTRY_INFO = "com.vst.LocalPlayer.component.service.action.EntryNfo";
    private static final String ACTION_ENTRY_INFO_PATH = "path";
    private static final String ACTION_ENTRY_INFO_META_TITLE = "metaTitle";
    private static final String ACTION_ENTRY_INFO_MEDIA_ID = "mediaId";
    private static final String ACTION_ADD_MEDIA = "com.vst.LocalPlayer.component.service.action.AddMedia";
    private static final String ACTION_ADD_MEDIA_DEVICE_ID = "deviceId";
    private static final String ACTION_ADD_MEDIA_DEVICE_PATH = "devicePath";
    private static final String ACTION_ADD_MEDIA_MEDIA_PATH = "mediaPath";

    public static void startActionScanner(Context context, String devicePath, long deviceId) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_SCANNER);
        intent.putExtra(ACTION_SCANNER_DEVICE_PATH, devicePath);
        intent.putExtra(ACTION_SCANNER_DEVICE_ID, deviceId);
        context.startService(intent);
    }


    public static void startActionUpdateValid(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_UPDATE_VALID);
        context.startService(intent);
    }


    public static void startActionEntryInfo(Context context, String path, String metaTitle, long mediaId) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_ENTRY_INFO);
        intent.putExtra(ACTION_ENTRY_INFO_MEDIA_ID, mediaId);
        intent.putExtra(ACTION_ENTRY_INFO_PATH, path);
        intent.putExtra(ACTION_ENTRY_INFO_META_TITLE, metaTitle);
        context.startService(intent);
    }


    public static void startActionAddMedia(Context context, String filePath, String devicePath, String deviceId) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_ADD_MEDIA);
        intent.putExtra(ACTION_ADD_MEDIA_DEVICE_ID, deviceId);
        intent.putExtra(ACTION_ADD_MEDIA_DEVICE_PATH, devicePath);
        intent.putExtra(ACTION_ADD_MEDIA_MEDIA_PATH, devicePath);
        context.startService(intent);
    }

    public MyIntentService() {
        super("ScannerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SCANNER.equals(action)) {
                final String devicePath = intent.getStringExtra(ACTION_SCANNER_DEVICE_PATH);
                final long deviceId = intent.getLongExtra(ACTION_SCANNER_DEVICE_ID, -1);
                if (deviceId < 0 || devicePath == null || "".equals(devicePath)) {
                    throw new IllegalArgumentException("the device info is null");
                } else {
                    handleActionScanner(getContentResolver(), devicePath, deviceId);
                }
            } else if (ACTION_UPDATE_VALID.equals(action)) {
                handleActionUpdateValid(getContentResolver());
            } else if (ACTION_ENTRY_INFO.equals(action)) {
                String path = intent.getStringExtra(ACTION_ENTRY_INFO_PATH);
                long id = intent.getLongExtra(ACTION_ENTRY_INFO_MEDIA_ID, -1);
                String mataTitle = intent.getStringExtra(ACTION_ENTRY_INFO_META_TITLE);
                handlerActionMediaInfo(getContentResolver(), path, mataTitle, id);
            } else if (ACTION_ADD_MEDIA.equals(action)) {
                final String devicePath = intent.getStringExtra(ACTION_ADD_MEDIA_DEVICE_PATH);
                final String mediaPath = intent.getStringExtra(ACTION_ADD_MEDIA_MEDIA_PATH);
                final long deviceId = intent.getLongExtra(ACTION_ADD_MEDIA_DEVICE_ID, -1);
                if (deviceId < 0 || !new File(mediaPath).exists()) {
                    throw new IllegalArgumentException("the media is not exists");
                } else {
                    handlerActionAddMedia(getContentResolver(), mediaPath, devicePath, deviceId);
                }
            }
        }
    }

    private void handlerActionAddMedia(ContentResolver cr, String path, String devicePath, long deviceId) {
        int width = 0;
        int height = 0;
        String title = null;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(path);
            width = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            height = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().getSimpleName(), "MediaMetadataRetriever error path=" + path);
        } finally {
            mmr.release();
        }
        Uri uri = MediaStoreHelper.addNewMediaBase(cr, path, devicePath, deviceId, width, height, title);
        long mediaId = ContentUris.parseId(uri);
        startActionEntryInfo(this, path, title, mediaId);
    }


    private void handleActionUpdateValid(ContentResolver cr) {
        Cursor c = getContentResolver().query(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.CONTENT_URI, null,
                null, null, null);
        while (c.moveToNext()) {
            String devicePath = c.getString(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.FIELD_DEVICE_PATH));
            String deviceUUID = c.getString(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.FIELD_DEVICE_UUID));
            long deviceId = c.getLong(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice._ID));
            boolean valid = MediaStoreHelper.checkDeviceValid(devicePath, deviceUUID);
            MediaStoreHelper.updateMediaDeviceValid(cr, devicePath, deviceUUID, valid);
            MediaStoreHelper.updateMediaValidByDevice(cr, deviceId, valid);
        }
        c.close();
    }

    private void handleActionScanner(ContentResolver cr, String devicePath, long deviceId) {
        long start = System.currentTimeMillis();
        scannerVideoFiles(cr, devicePath, devicePath, deviceId);
        long end = System.currentTimeMillis();
        Log.e("handleActionScanner", "" + (end - start) / 1000f);
    }


    private void handlerActionMediaInfo(ContentResolver cr, String mediaPath, String metaTitle, long mediaBaseId) {
        String sourceID;
        //Log.e("Info", "mediaPath=" + mediaPath + ",mediaBaseId=" + mediaBaseId);
        if (mediaBaseId >= 0) {
            File mediaFile = new File(mediaPath);
            //nfo api
            //本地文件寻找imdbId
            sourceID = NFOApi.getImdbIdFromNFOFile(mediaPath);
            Log.w("Info", "FromNFO imdb=" + sourceID);
            if (sourceID == null) {
                if (metaTitle != null) {
                    sourceID = IMDBApi.getImdbIdFromSearch(metaTitle, null);
                } else {
                    String name = IMDBApi.smartMediaName(mediaFile.getName());
                    sourceID = IMDBApi.getImdbIdFromSearch(name, null);
                }
            }
            Log.w("Info", "FromIMDB imdb=" + sourceID);
            if (sourceID != null) {
                //exsist  get sourceID
                boolean sourceExist = false;
                Cursor c = cr.query(MediaStore.MediaInfo.CONTENT_URI, null, MediaStore.MediaInfo.FIELD_SOURCE_ID + "=?",
                        new String[]{sourceID}, null);
                if (c.getCount() > 0) {
                    sourceExist = true;
                }
                c.close();
                if (!sourceExist) {
                    String json = IMDBApi.imdbById(sourceID, null);
                    Uri uri = MediaStoreHelper.insertMediaInfo(cr, json, sourceID);
                }
            }
            //douban api
            Log.w("Info", "mediaSourceId=" + sourceID);
            //媒体信息关联到MediaBaseTable
            if (sourceID != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaBase.FIELD_MEDIA_INFO_SOURCEID, sourceID);
                cr.update(MediaStore.getContentUri(MediaStore.MediaBase.TABLE_NAME, mediaBaseId), values, null, null);
            }
        }
    }


    /*非递归*/
    /*private void scannerVideoFiles(String path) {
        File rootfile = new File(path);
        LinkedList<String> dirs = new LinkedList<String>();
        LinkedList<String> dirsBuffer = new LinkedList<String>();
        //init
        if (rootfile.isDirectory()) {
            dirs.add(rootfile.getAbsolutePath());
        } else {
            scannerFileToStore(rootfile.getAbsolutePath());
        }
        while (!dirs.isEmpty()) {
            File rootDir = new File(dirs.remove(0));
            File[] files = rootDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return true;
                    }
                    String filename = pathname.getName();
                    if (filename.endsWith(".ts") || filename.endsWith(".avi") || filename.endsWith(".mp4")
                            || filename.endsWith(".rmvb") || filename.endsWith(".mkv") || filename.endsWith(".flv")) {
                        return true;
                        //scannerFileToStore(pathname.getAbsolutePath());
                    }
                    return false;
                }
            });
            if (files != null && files.length > 0) {
                int length = files.length;
                for (int i = 0; i < length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        dirsBuffer.add(file.getAbsolutePath());
                    } else {
                        scannerFileToStore(file.getAbsolutePath());
                    }
                }
            }
            dirs.addAll(dirsBuffer);
            dirsBuffer.clear();
        }
    }*/

    /*递归*/
    private void scannerVideoFiles(ContentResolver cr, String dir, String devicePath, long deviceId) {
        File file = new File(dir);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (!pathname.isHidden()) {
                        String filename = pathname.getName();
                        if (filename.contains("$REC")) {
                            return false;
                        }
                        if (Utils.fileIsVideo(pathname)) {
                            return true;
                        }
                        if (pathname.isDirectory()) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (files != null && files.length > 0) {
                for (File f : files) {
                    scannerVideoFiles(cr, f.getAbsolutePath(), devicePath, deviceId);
                }
            }
        } else {
            String path = file.getAbsolutePath();
            handlerActionAddMedia(cr, path, devicePath, deviceId);
        }
    }
}

