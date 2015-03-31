package com.vst.LocalPlayer.component.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStoreHelper;

import java.io.File;
import java.io.FileFilter;

public class MyIntentService extends IntentService {
    private static final String ACTION_SCANNER = "com.vst.LocalPlayer.component.service.action.SCANNER";
    private static final String ACTION_SCANNER_DEVICE_ID = "deviceId";
    private static final String ACTION_SCANNER_DEVICE_PATH = "devicePath";
    private static final String ACTION_UPDATE_VALID = "com.vst.LocalPlayer.component.service.action.UpdateDeviceAndMediaValid";
    private static final String ACTION_ENTRY_NFO = "com.vst.LocalPlayer.component.service.action.EntryNfo";
    private static final String ACTION_ENTRY_NFO_NFOFILE = "nfoFile";
    private static final String ACTION_ENTRY_NFO_MEDIA_ID = "mediaId";

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


    public static void startActionEntryNFO(Context context, File nfoFile, long mediaId) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_ENTRY_NFO);
        intent.putExtra(ACTION_ENTRY_NFO_MEDIA_ID, mediaId);
        intent.putExtra(ACTION_ENTRY_NFO_NFOFILE, nfoFile);
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
                    handleActionScannel(getContentResolver(), devicePath, deviceId);
                }
            } else if (ACTION_UPDATE_VALID.equals(action)) {
                handleActionUpdateValid(getContentResolver());
            } else if (ACTION_ENTRY_NFO.equals(action)) {
                File nfoFile = (File) intent.getSerializableExtra(ACTION_ENTRY_NFO_NFOFILE);
                long id = intent.getLongExtra(ACTION_ENTRY_NFO_MEDIA_ID, -1);
                handlerActionMediaInfo(getContentResolver(), nfoFile, id);
            }
        }
    }

    private void handleActionUpdateValid(ContentResolver cr) {
        Cursor c = getContentResolver().query(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.CONTENT_URI, null,
                null, null, null);
        while (c.moveToNext()) {
            String devicePath = c.getString(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.FIELD_DEVICE_PATH));
            String deviceUUID = c.getString(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.FIELD_DEVICE_UUID));
            long deviceId = c.getLong(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice._ID));
            System.out.println("devicePath=" + devicePath + ",deviceUUID=" + deviceUUID);
            boolean valid = MediaStoreHelper.checkDeviceValid(devicePath, deviceUUID);
            System.out.println("valid=" + valid);
            MediaStoreHelper.updateMediaDeviceValid(cr, devicePath, deviceUUID, valid);
            MediaStoreHelper.updateMediaValidByDevice(cr, deviceId, valid);
        }
        c.close();
    }

    private void handleActionScannel(ContentResolver cr, String devicePath, long deviceId) {
        long start = System.currentTimeMillis();
        scannerVideoFiles(cr, devicePath, devicePath, deviceId);
        long end = System.currentTimeMillis();
        Log.e("~~", "" + (end - start) / 1000f);
    }


    private void handlerActionMediaInfo(ContentResolver cr, File nfoFile, long mediaBaseId) {
        //录入本地媒体信息


        //录入网络媒体信息


        //媒体信息关联到MediaBaseTable

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


   /* private void scannerMediaToStore(String path, String devicePath, long deviceId) {
        boolean storeExist = false;
        Cursor cursor = this.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.DATA}, MediaStore.Video.Media.DATA + "=?", new String[]{path}, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                storeExist = true;
            }
            cursor.close();
        }
        if (!storeExist) {
            MediaScannerConnection.scanFile(this.getApplicationContext(), new String[]{path}, new String[1], null);
        } else {
            Log.e("SCANNER", "-----scannerFiles--exist");
        }

        ContentResolver cr = getContentResolver();

        MediaStoreHelper.addNewMediaBase(cr, path, devicePath, deviceId);


    }*/


    /*递归*/
    private void scannerVideoFiles(ContentResolver cr, String path, String devicePath, long deviceId) {
        File root = new File(path);
        if (!root.exists()) {
            return;
        }
        if (root.isDirectory()) {
            File[] files = root.listFiles(new FileFilter() {
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
                for (File file : files) {
                    scannerVideoFiles(cr, file.getAbsolutePath(), devicePath, deviceId);
                }
            }
        } else {
            Uri uri = MediaStoreHelper.addNewMediaBase(cr, path, devicePath, deviceId);
            long mediaId = ContentUris.parseId(uri);
            if (mediaId > 0) {
                //nfo api

                //imdb api

                //douban api
            }
        }
    }
}

