package com.vst.dev.common.media;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.vst.dev.common.util.Utils;

public class SubTripe {

    private IPlayer mPlayer;
    private ArrayList<Subtitle> mSubtitles = new ArrayList<SubTripe.Subtitle>();
    private long mOffset = 0;
    private Subtitle mSubTitle = null;

    public SubTripe(IPlayer player, final Uri uri) {
        super();
        mPlayer = player;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Subtitle> list = parseSubtitles(uri);
                if (list != null && list.size() > 0) {
                    mSubtitles = list;
                    mHandler.sendEmptyMessage(0);
                }
            }
        }).start();
    }

    private ArrayList<Subtitle> parseSubtitles(Uri uri) {
        BufferedReader reader = null;
        InputStream in = null;
        HttpURLConnection conn = null;
        ArrayList<Subtitle> subtitleList = new ArrayList<SubTripe.Subtitle>();
        try {
            String scheme = uri.getScheme();
            if ("file".equalsIgnoreCase(scheme)) {
                String path = uri.getPath();
                in = new FileInputStream(path);
            } else if ("http".equalsIgnoreCase(scheme)) {
                conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                in = conn.getInputStream();
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
            bufferedInputStream.mark(2);
            int p = (bufferedInputStream.read() << 8) + bufferedInputStream.read();
            String charset = checkCharset(p);
            bufferedInputStream.reset();
            reader = new BufferedReader(new InputStreamReader(bufferedInputStream, charset));
            String line;
            Subtitle sub = null;
            // 正则表达式，用于匹配类似于“01:54:16,332 --> 01:54:18,163”的时间描述字符行
            final String regex = "\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d --> \\d\\d:\\d\\d:\\d\\d,\\d\\d\\d";
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) {
                    if (sub != null) {
                        subtitleList.add(sub);
                    }
                    sub = null;
                    continue;
                }
                if (Pattern.matches(regex, line)) { // 使用静态方法进行正则式的匹配。
                    String begin = line.substring(0, 12);
                    String end = line.substring(17, 29);
                    long beginTime = formatMils(begin);
                    long endTime = formatMils(end);
                    if (beginTime >= 0 && endTime >= 0 && endTime > beginTime) {
                        sub = new Subtitle(beginTime, endTime);
                    }
                    continue;
                }
                if (sub != null) {
                    line = line.replaceAll("<.*>", "").trim();
                    if (sub.srtBody != null) {
                        sub.srtBody = sub.srtBody + "\n" + line;
                    } else {
                        sub.srtBody = line;
                    }
                    continue;
                }
            }

        } catch (Exception e) {

        } finally {
            Utils.closeIO(reader);
            Utils.closeIO(in);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return subtitleList;
    }

    private static String checkCharset(int p) {
        String code = null;
        switch (p) {
        case 0xefbb:
            code = "UTF-8";
            break;
        case 0xfffe:
            code = "Unicode";
            break;
        case 0xfeff:
            code = "UTF-16BE";
            break;
        default:
            code = "GBK";
        }
        return code;
    }

    @SuppressWarnings("deprecation")
    private long formatMils(String timeText) {
        try {
            Date d = new SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault()).parse(timeText);
            int h = d.getHours();
            int m = d.getMinutes();
            int s = d.getSeconds();
            int ms = Integer.parseInt(timeText.substring(9, 12));
            long time = h * 60 * 60 * 1000 + m * 60 * 1000 + s * 1000 + ms;
            return time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private Subtitle getCurrentSubtitle(long time) {
        time = time + mOffset;
        for (int i = 0; i < mSubtitles.size(); i++) {
            Subtitle sub = mSubtitles.get(i);
            if (sub.beginTime <= time && sub.endTime >= time) {
                return sub;
            }
        }
        return null;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mPlayer != null && mSubtitles != null) {
                long position = mPlayer.getPosition();
                if (position > 0) {
                    Subtitle subtitle = getCurrentSubtitle(position);
                    if (subtitle != null) {
                        if (!subtitle.equals(mSubTitle)) {
                            ((VideoView) mPlayer).onTimedTextChanged(subtitle);
                        }
                    } else {
                        if (mSubTitle != null) {
                            ((VideoView) mPlayer).onTimedTextChanged(null);
                        }
                    }
                    mSubTitle = subtitle;
                }
            }
            sendEmptyMessageDelayed(0, 1000);
        }

    };

    public void setTimeOffset(long offset) {
        mOffset = offset;
    }

    public void relase() {
        mSubtitles.clear();
        mOffset = 0;
        mHandler.removeCallbacksAndMessages(null);
    }

    class Subtitle {
        public long beginTime;// 开始时间
        public long endTime;// 结束时间
        public String srtBody;// 字幕体

        @Override
        public String toString() {
            return "Subtitle [beginTime=" + beginTime + ", endTime=" + endTime + ", srtBody=" + srtBody + "]";
        }

        public Subtitle(long beginTime, long endTime) {
            super();
            this.beginTime = beginTime;
            this.endTime = endTime;
        }
    }
}
