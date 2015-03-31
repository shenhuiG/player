package com.vst.LocalPlayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class Utils {

    private static final String TAG = "Utils";

    /* This method may be  time-consuming.*/
    public static MediaMeta readMediaMeta(String mediaPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        MediaMeta mediaMeta = null;
        if (mediaPath.startsWith("file://")) {
            mediaPath = mediaPath.replace("file://", "");
        }
        try {
            mmr.setDataSource(mediaPath);
            mediaMeta = new MediaMeta();
            for (int i = 0; i < 25; i++) {
                String x = mmr.extractMetadata(i);
                //Log.d(TAG, i + ":" + x);
            }
            mediaMeta.duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            mediaMeta.bitrate = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            mediaMeta.width = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            mediaMeta.height = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            mediaMeta.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            mmr.release();
        }

        /*MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(mediaPath);
            *//*int numTracks = extractor.getTrackCount();
            Log.d(TAG, "numTracks:" + numTracks);
            for (int i = 0; i < numTracks; ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.d(TAG, "mime:" + mime);
                if (true) {
                    extractor.selectTrack(i);
                }
            }*//*
            ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
            while (extractor.readSampleData(inputBuffer, 1024) >= 0) {
                int trackIndex = extractor.getSampleTrackIndex();
                long presentationTimeUs = extractor.getSampleTime();
                Log.d(TAG, "trackIndex" + trackIndex + " ,presentationTimeUs : " + presentationTimeUs);
                extractor.advance();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            extractor.release();
            extractor = null;
        }*/

        /*MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(mediaPath);
            MediaPlayer.TrackInfo[] trackInfos = mp.getTrackInfo();
            Log.d(TAG, "trackInfos:" + trackInfos);
            if (trackInfos != null) {
                for (int i = 0; i < trackInfos.length; i++) {
                    MediaPlayer.TrackInfo trackInfo = trackInfos[i];
                    Log.d(TAG, "trackInfo:" + trackInfo.describeContents() + "," + trackInfo.getTrackType() + "," + trackInfo.getLanguage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mp.release();
        }*/


        return mediaMeta;
    }


    public static void pleyMediaFile(Context ctx, File mediaFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(mediaFile), "video/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static void pleyAudioFile(Context ctx, File mediaFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(mediaFile), "audio/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public enum FileCategory {
        Music, Video, Picture, Doc, Zip, Apk, Other, Dir
    }

    public static FileCategory getFileCategory(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                return FileCategory.Dir;
            }
            String fileName = file.getName();
            int i = fileName.lastIndexOf(".");
            String ext = fileName.substring(i + 1);
            if ("apk".equalsIgnoreCase(ext)) {
                return FileCategory.Apk;
            }
            if ("txt".equalsIgnoreCase(ext) || "doc".equalsIgnoreCase(ext) || "pdf".equalsIgnoreCase(ext) || "xls".equalsIgnoreCase(ext)) {
                return FileCategory.Doc;
            }
            if ("jpg".equalsIgnoreCase(ext) || "png".equalsIgnoreCase(ext) || "jepg".equalsIgnoreCase(ext)) {
                return FileCategory.Picture;
            }
            if ("rar".equals(ext) || "zip".equalsIgnoreCase(ext) || "iso".equalsIgnoreCase(ext)) {
                return FileCategory.Zip;
            }
            if ("mp3".equals(ext) || "wma".equalsIgnoreCase(ext) || "ogg".equalsIgnoreCase(ext)) {
                return FileCategory.Music;
            }
            if ("mp4".equalsIgnoreCase(ext) || "mkv".equalsIgnoreCase(ext) || "rmvb".equalsIgnoreCase(ext) || "avi".equalsIgnoreCase(ext)
                    || "flv".equalsIgnoreCase(ext) || "ts".equalsIgnoreCase(ext) || "rmvb".equalsIgnoreCase(ext)) {
                return FileCategory.Video;
            }
            return FileCategory.Other;
        }
        return null;
    }


    public static int getFileCategoryIcon(File file) {
        FileCategory category = getFileCategory(file);
        if (category != null) {
            switch (category) {
                case Music:
                    return R.drawable.ic_disk_music;
                case Video:
                    return R.drawable.ic_disk_video;
                case Picture:
                    return R.drawable.ic_disk_picture;
                case Doc:
                    return R.drawable.ic_disk_txt;
                case Zip:
                    return R.drawable.ic_disk_rar;
                case Apk:
                    return R.drawable.ic_disk_apk;
                case Dir:
                    return R.drawable.ic_disk_folder;
                default:
                    return R.drawable.ic_disk_unknown;
            }
        }
        return 0;
    }


    public static ArrayList<String> getAvailableDevicesPath(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("USBDeviceInfo", Context.MODE_MULTI_PROCESS);
        String possibleDevicesString = sp.getString("DevicesPathString", "");
        if (!"".equals(possibleDevicesString)) {
            String[] devicesPath = possibleDevicesString.split(";");
            if (devicesPath != null && devicesPath.length > 0) {
                ArrayList<String> devicesArray = new ArrayList<String>();
                for (int i = 0; i < devicesPath.length; i++) {
                    String filePath = devicesPath[i];
                    if (new File(filePath).exists()) {
                        devicesArray.add(filePath);
                    }
                }
                return devicesArray;
            }
        }
        return null;
    }

    public static boolean fileIsVideo(File file) {
        String filename = file.getName();
        if (file.isFile() && filename.endsWith(".ts") || filename.endsWith(".avi") || filename.endsWith(".mp4")
                || filename.endsWith(".rmvb") || filename.endsWith(".mkv") || filename.endsWith(".flv")) {
            return true;
        }
        return false;
    }


    public static String fileSizeFormat(long size) {
        float mbSize = size / 1000f / 1000f;
        if (mbSize < 1000f) {
            return String.format("%d MB ", Math.round(mbSize));
        } else {
            float gSize = mbSize / 1000f;
            if (gSize < 1000f) {
                return String.format("%d G ", Math.round(gSize));
            } else {
                float TSize = gSize / 1000f;
                return String.format("%d T ", Math.round(TSize));
            }

        }
    }


    public static String writeDeviceUUID(String devicePath) {
        File root = new File(devicePath);
        if (root.isDirectory()) {
            String uuid = UUID.randomUUID().toString();
            File uFile = new File(devicePath, ".uuid");
            FileWriter writer = null;
            try {
                writer = new FileWriter(uFile);
                writer.write(uuid);
                Log.d(TAG, "writeDeviceUUID uuid:" + uuid);
                return uuid;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }


    public static String readDeviceUUID(String devicePath) {
        String uuid = null;
        File root = new File(devicePath);
        if (root.isDirectory()) {
            File uFile = new File(devicePath, ".uuid");
            FileReader reader = null;
            try {
                char[] buffer = new char[36];
                reader = new FileReader(uFile);
                reader.read(buffer, 0, buffer.length);
                uuid = new String(buffer);
                Log.d(TAG, "readDeviceUUID uuid:" + uuid);
                return uuid;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return uuid;
    }


    public static long getDeviceLaseTime(String devicePath) {
        File root = new File(devicePath);
        if (root.isDirectory()) {
            //1
            long time1 = root.lastModified();
            Log.d(TAG, "getDeviceLaseTime time1:" + new Date(time1));
            return time1;
            /*long time2 = 0;
            File[] files = root.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return !filename.equals(".uuid");
                }
            });
            if (files != null && files.length > 0) {
                for (File f : files) {
                    time2 = Math.max(time2, f.lastModified());
                }
            }
            Log.d(TAG, "getDeviceLaseTime time2:" + new Date(time2));*/
        }
        return 0;
    }

}
