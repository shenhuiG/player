package com.vst.LocalPlayer.model;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shenh on 2015/3/31.
 * <link>http://www.omdbapi.com/</link>
 */
public class IMDBApi {

    static final String BASE_URL = "http://www.omdbapi.com/?";

    /**
     * By ID or Title
     * <p/>
     * ##############################################################################################
     * # Parameter	# Required	# Valid options	        # Default Value	# Description               #
     * ##############################################################################################
     * #    i	    # Optional* #	                    #   <empty>	    # A valid IMDb ID(tt1285016)#
     * #    t	    # Optional* #	                    #   <empty>	    # Movie title to search for.#
     * #   type     #	No	    # movie,series,episode  #	<empty>	    # Type of result to return. #
     * #    y	    #   No	    #	                    #   <empty>     #	Year of release.        #
     * #   plot	    #   No      #	short, full	        #   json	    # The data type to return.  #
     * #  tomatoes  #   No      #	true, false	        #   json	    # The data type to return.  #
     * #  callback  #	No		#                       #   <empty>     #  JSONP callback name.     #
     * #    v	    #   No	    #	                    #       1	    #       API version         #
     * ##############################################################################################
     */

    public static String imdbByTitle(String title, String year) {
        NameValuePair[] params = new NameValuePair[]{
                new BasicNameValuePair("t", title == null ? "" : title),
                new BasicNameValuePair("y", year == null ? "" : year)
        };
        byte[] result = com.vst.dev.common.util.Utils.doGet(BASE_URL, null, params);
        return new String(result);
    }

    public static String imdbById(String id, String year) {
        NameValuePair[] params = new NameValuePair[]{
                new BasicNameValuePair("i", id == null ? "" : id),
                new BasicNameValuePair("y", year == null ? "" : year)
        };
        byte[] result = com.vst.dev.common.util.Utils.doGet(BASE_URL, null, params);
        return new String(result);
    }


    /**
     * By Search
     * ##############################################################################################
     * # Parameter	# Required	# Valid options	        # Default Value	# Description               #
     * ##############################################################################################
     * #    s	    #   Yes	    #	                    #   <empty>	    # Movie title to search for.#
     * #    type    #	No	    # movie,series,episode  #	<empty>	    # Type of result to return. #
     * #    y	    #   No	    #	                    #   <empty>     #	Year of release.        #
     * #    r	    #   No      #	json, xml	        #   json	    # The data type to return.  #
     * #  callback  #	No		#                       #   <empty>     #  JSONP callback name.     #
     * #    v	    #   No	    #	                    #       1	    #       API version         #
     * ##############################################################################################
     */
    public static String imdbSearch(String keyWord, String year) {
        NameValuePair[] params = new NameValuePair[]{
                new BasicNameValuePair("s", keyWord == null ? "" : keyWord),
                new BasicNameValuePair("y", year == null ? "" : year)
        };
        byte[] result = com.vst.dev.common.util.Utils.doGet(BASE_URL, null, params);
        return new String(result);
    }


    public static String getImdbIdFromSearch(String keyWord, String year) {
        String content = imdbSearch(keyWord, year);
        try {
            JSONObject root = new JSONObject(content);
            JSONArray Search = root.optJSONArray("Search");
            if (Search != null && Search.length() > 0) {
                JSONObject result = Search.getJSONObject(0);
                return result.getString("imdbID");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 截取720P 标记之前，后面的全部丢掉
     *
     * @param fileName
     * @return
     */
    public static String smartMediaName(String fileName) {
        String reg = "WinG|ENG|aAf|3D|720P|720p|1080P|1080p|X264|DTS|BluRay|Bluray|HSBS|x264|CHD|H-SBS" +
                "|Wiki|WiKi|ML|RemuX|CnSCG|HDChina|Sample|sample|AVC|MA|5.1|AC3|AAC|rip|265|[|]" +
                "|HDTV|DL|DHD|HD|HEVC|DiCH|dich|dhd|hdtv|Pix|BAWLS|hv|NG";
        //reduce ext
        String result = fileName.substring(0, fileName.lastIndexOf("."));
        //reduce other word like 720P,DTS,X264..
        result = result.replaceAll(reg, "");
        //reduce last .
        String endReg = "[0-9]|[|]|.| |-";
        String endX = "0123456789.-[] ";
        Pattern pattern = Pattern.compile(endReg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(result);
        if (matcher.matches()) {
            return result;
        }
        if (result.length() > 2) {
            String endChar = result.substring(result.length() - 1, result.length());
            while (endX.contains(endChar)) {
                result = result.substring(0, result.length() - 1);
                if (result.length() > 2) {
                    endChar = result.substring(result.length() - 1, result.length());
                } else {
                    break;
                }
            }
        }
        return result;
    }

}
