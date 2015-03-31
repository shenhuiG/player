package com.vst.LocalPlayer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


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
     * #    r	    #   No      #	json, xml	        #   json	    # The data type to return.  #
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


    public static String smartMediaName(String fileName) {
        String reg = "3D|720P|720p|1080P|1080p|X264|DTS|BluRay|Bluray|HSBS|x264|CHD|H-SBS|Wiki|WiKi|ML" +
                "|HD|RemuX|CnSCG|HDChina|Sample|sample|AVC|MA|5.1|AC3|AAC|rip|265|/[|/]|HDTV|DL";
        //reduce ext
        String result = fileName.substring(0, fileName.lastIndexOf("."));
        //reduce other word like 720P,DTS,X264..
        result = result.replaceAll(reg, "");
        //reduce last .
        while (result.endsWith(".") || result.endsWith(" ") || result.endsWith("-")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}
