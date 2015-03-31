package com.vst.LocalPlayer.component.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.StatFs;
import android.util.Log;

import com.vst.LocalPlayer.Utils;

import java.io.File;

public class MediaStoreHelper {

    static final String TAG = "MediaStoreHelper";

    public static void updateMediaDeviceValid(ContentResolver cr, String path, String uuid, boolean valid) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaDevice.FIELD_VALID, valid);
        String where = "";
        String[] selectionArgs = null;
        if (path != null) {
            where = MediaStore.MediaDevice.FIELD_DEVICE_PATH + "=?";
            selectionArgs = new String[]{path};
        }
        if (uuid != null) {
            if ("".equals(where)) {
                where = MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=?";
                selectionArgs = new String[]{uuid};
            } else {
                where = where + " AND " + MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=?";
                selectionArgs = new String[]{path, uuid};
            }
        }
        if (selectionArgs != null) {
            int i = cr.update(MediaStore.MediaDevice.CONTENT_URI, values, where, selectionArgs);
            Log.d(TAG, "updateMediaDeviceValid  i=" + i);
        }
    }

    public static void updateMediaDeviceInfo() {

    }


    public static boolean deviceInStore(ContentResolver cr, String uuid) {
        boolean inStore = false;
        Cursor c = cr.query(MediaStore.MediaDevice.CONTENT_URI, null, MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=?",
                new String[]{uuid}, null);
        System.out.println("Cursor=" + c);
        if (c != null) {
            System.out.println("Cursor=" + c.getCount());
            if (c.getCount() > 0) {
                inStore = true;
            }
            c.close();
        }
        return inStore;
    }


    public static Uri addNewMediaDevice(ContentResolver cr, String uuid, String path) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaDevice.FIELD_DEVICE_UUID, uuid);
        values.put(MediaStore.MediaDevice.FIELD_DEVICE_PATH, path);
        values.put(MediaStore.MediaDevice.FIELD_LAST_MODIFY_TIME, Utils.getDeviceLaseTime(path));
        values.put(MediaStore.MediaDevice.FIELD_FS_SIZE, calculatSFSize(path));
        values.put(MediaStore.MediaDevice.FIELD_VALID, 1);
        Uri uri = cr.insert(MediaStore.MediaDevice.CONTENT_URI, values);
        Log.d(TAG, "addNewMediaDevice  uri=" + uri);
        return uri;
    }


    public static Uri addNewMediaBase(ContentResolver cr, String mediaPath, String devicePath, long deviceId) {
        Uri uri = null;
        File mediaFile = new File(mediaPath);
        if (mediaFile.exists() && mediaFile.isFile()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaBase.FIELD_NAME, mediaFile.getName());
            values.put(MediaStore.MediaBase.FIELD_FILE_SIZE, mediaFile.getTotalSpace());
            values.put(MediaStore.MediaBase.FIELD_RELATIVE_PATH, mediaPath.replace(devicePath, ""));
            values.put(MediaStore.MediaBase.FIELD_DEVICE_ID, deviceId);
            values.put(MediaStore.MediaBase.FIELD_ADD_DATE, System.currentTimeMillis());
            values.put(MediaStore.MediaBase.FIELD_VALID, 1);
            uri = cr.insert(MediaStore.MediaBase.CONTENT_URI, values);
        }
        Log.d(TAG, "addNewMediaBase  uri=" + uri);
        return uri;
    }

    public static Uri removeMediaDevice(ContentResolver cr, String uuid, String path) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaDevice.FIELD_DEVICE_UUID, uuid);
        values.put(MediaStore.MediaDevice.FIELD_DEVICE_PATH, path);
        values.put(MediaStore.MediaDevice.FIELD_LAST_MODIFY_TIME, Utils.getDeviceLaseTime(path));
        values.put(MediaStore.MediaDevice.FIELD_FS_SIZE, calculatSFSize(path));
        values.put(MediaStore.MediaDevice.FIELD_VALID, 1);
        Uri uri = cr.insert(MediaStore.MediaDevice.CONTENT_URI, values);
        Log.d(TAG, "addNewMediaDevice  uri=" + uri);
        return uri;
    }


    public static boolean checkDeviceValid(String path, String uuid) {
        String deviceUUID = Utils.readDeviceUUID(path);
        Log.d(TAG, "checkDeviceValid  deviceUUID=" + deviceUUID);
        return uuid.equals(deviceUUID);
    }


    public static int updateMediaValidByDevice(ContentResolver cr, long deviceId, boolean valid) {
        int count;
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaBase.FIELD_VALID, valid);
        String where = MediaStore.MediaBase.FIELD_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(deviceId)};
        count = cr.update(MediaStore.MediaBase.CONTENT_URI, values, where, selectionArgs);
        Log.d(TAG, "updateMediaValidByDevice  count=" + count);
        return count;
    }

    private static long calculatSFSize(String path) {
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long blockCount = statFs.getBlockCount();
        return blockSize * blockCount;
    }
}
