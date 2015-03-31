package com.vst.LocalPlayer;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by shenh on 2015/3/31.
 */
public class NFOApi {

    public static void getNfoInfo(String mediaPath) {
        //find nfo file
        File mediaFile = new File(mediaPath);
        File parent = mediaFile.getParentFile();
        String name = mediaFile.getName();
        //same name
        String nfoName = name.substring(0, name.lastIndexOf(".")) + ".nfo";
        System.out.println("nfoName =" + nfoName);
        File nfoFile = new File(parent, nfoName);
        if (!nfoFile.exists()) {
            File[] files = parent.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".nfo");
                }
            });
            if (files != null && files.length > 0) {
                nfoFile = files[0];
            }
        }
        if (nfoFile.exists()) {
            try {
                parseNFOFile(nfoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void parseNFOFile(File nfoFile) throws IOException {
        System.out.println(nfoFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(nfoFile)));
        String buff;
        while ((buff = reader.readLine()) != null) {
            if (!TextUtils.isEmpty(buff)) {
                buff = buff.replaceAll("[^0-9a-zA-Z:@#%-|]", " ");
                if (!TextUtils.isEmpty(buff)) {
                    System.out.println(buff);


                }
            }
        }
    }

}
