package com.vst.LocalPlayer.component.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shenh on 2015/3/26.
 */
public class MediaStoreDBHelper extends SQLiteOpenHelper {

    private static MediaStoreDBHelper mInstance;

    MediaStoreDBHelper(Context context) {
        super(context, MediaStore.NAME, null, MediaStore.VERSION);
    }

    static MediaStoreDBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MediaStoreDBHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DEVICE_TABLE_V1);
        db.execSQL(CREATE_MEDIA_BASE_TABLE_V1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    static final String CREATE_DEVICE_TABLE_V1 = "CREATE TABLE "
            + MediaStore.MediaDevice.TABLE_NAME
            + "("
            + MediaStore.MediaDevice._ID + " integer primary key autoincrement not null,"
            + MediaStore.MediaDevice.FIELD_DEVICE_UUID + " text not null ,"
            + MediaStore.MediaDevice.FIELD_DEVICE_PATH + " text not null,"
            + MediaStore.MediaDevice.FIELD_FS_SIZE + " long not null ,"
            + MediaStore.MediaDevice.FIELD_LAST_MODIFY_TIME + " long not null,"
            + MediaStore.MediaDevice.FIELD_VALID + " boolean not null"
            + ")";


    static final String CREATE_MEDIA_BASE_TABLE_V1 = "CREATE TABLE "
            + MediaStore.MediaBase.TABLE_NAME
            + "("
            + MediaStore.MediaBase._ID + " integer primary key autoincrement not null,"
            + MediaStore.MediaBase.FIELD_NAME + " text not null,"
            + MediaStore.MediaBase.FIELD_DISPLAY_NAME + " text,"
            + MediaStore.MediaBase.FIELD_FILE_SIZE + " long not null,"
            + MediaStore.MediaBase.FIELD_DEVICE_ID + " long not null,"
            + MediaStore.MediaBase.FIELD_TYPE + " text,"
            + MediaStore.MediaBase.FIELD_VALID + " boolean not null,"
            + MediaStore.MediaBase.FIELD_RELATIVE_PATH + " text not null,"
            + MediaStore.MediaBase.FIELD_ADD_DATE + " long not null"
            + ")";


    static final String CREATE_MEDIA_INFO_TABLE_V1 = "CREATE TABLE "
            + MediaStore.MediaInfo.TABLE_NAME
            + "("
            + MediaStore.MediaInfo._ID + " integer primary key autoincrement not null, "
            + MediaStore.MediaInfo.FIELD_RELEASE_NAME + " text ,"
            + MediaStore.MediaInfo.FIELD_RELEASE_DATE + " long ,"
            + MediaStore.MediaInfo.FIELD_ORIGINAL_TITLE + " text ,"
            + MediaStore.MediaInfo.FIELD_THEATRE_DATE + " long ,"
            + MediaStore.MediaInfo.FIELD_GENRE + " text ,"
            + MediaStore.MediaInfo.FIELD_SOURCE + " text ,"
            + MediaStore.MediaInfo.FIELD_FORMAT + " text ,"
            + MediaStore.MediaInfo.FIELD_VIDEO_BITRATE + " text ,"
            + MediaStore.MediaInfo.FIELD_FRAME_RATE + " text ,"
            + MediaStore.MediaInfo.FIELD_RESOLUTION + " text "
            + ")";
}
