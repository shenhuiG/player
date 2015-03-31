package com.vst.LocalPlayer.component.provider;


import android.net.Uri;
import android.provider.BaseColumns;

public final class MediaStore {
    public static final String NAME = "MediaStore";
    public static final int VERSION = 1;

    public static final String AUTHORITY = "com.vst.localplayer.mediaStore";

    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";


    public static final Uri getContentUri(String tableName) {
        return Uri.parse(CONTENT_AUTHORITY_SLASH + tableName);
    }

    public static final Uri getContentUri(String tableName, long rowId) {
        return Uri.parse(CONTENT_AUTHORITY_SLASH + tableName + "/" + rowId);
    }

    public interface MediaDevice extends BaseColumns {
        public static final String TABLE_NAME = "MediaDevice";
        public static final String FIELD_DEVICE_UUID = "UUID";
        public static final String FIELD_DEVICE_PATH = "point";
        public static final String FIELD_LAST_MODIFY_TIME = "lastTime";
        public static final String FIELD_FS_SIZE = "fsSize";
        public static final String FIELD_VALID = "isValid";
        public static final Uri CONTENT_URI = getContentUri(TABLE_NAME);

    }

    public interface MediaBase extends BaseColumns {
        public static final String TABLE_NAME = "MediaBase";
        public static final String FIELD_NAME = "name";
        public static final String FIELD_DISPLAY_NAME = "displayName";
        public static final String FIELD_RELATIVE_PATH = "relativePath";
        public static final String FIELD_TYPE = "type";
        public static final String FIELD_FILE_SIZE = "fileSize";
        public static final String FIELD_DEVICE_ID = "deviceId";
        public static final String FIELD_VALID = "valid";
        public static final String FIELD_ADD_DATE = "addDate";
        public static final String FIELD_MEDIA_INFO_ID = "mediaInfoId";
        public static final Uri CONTENT_URI = getContentUri(TABLE_NAME);
    }


    public interface MediaInfo extends BaseColumns {
        public static final String TABLE_NAME = "MediaInfo";
        public static final String FIELD_RELEASE_NAME = "releaseName";          //发行名称
        public static final String FIELD_ORIGINAL_TITLE = "originalTitle";
        public static final String FIELD_RELEASE_DATE = "ReleaseDATE";       //影片文件制作并上传的日期
        public static final String FIELD_THEATRE_DATE = "theatreDATE";      //影片公映日期
        public static final String FIELD_GENRE = "genre";                   //影片类型：冒险，家庭，奇幻
        public static final String FIELD_SOURCE = "source";                 // 发布源
        public static final String FIELD_FORMAT = "format";                 //文件格式 mkv ,mp4
        public static final String FIELD_VIDEO_BITRATE = "videoBitrate";    //视频码率
        public static final String FIELD_FRAME_RATE = "frameRate";          //帧率
        public static final String FIELD_RESOLUTION = "resolution";         //分辨率
        public static final Uri CONTENT_URI = getContentUri(TABLE_NAME);
    }


    public interface MediaExtraInfo extends BaseColumns {
        public static final String TABLE_NAME = "MediaExtraInfo";
    }


    public interface MediaRecord extends BaseColumns {
        public static final String TABLE_NAME = "mediaExtra";
    }


}
