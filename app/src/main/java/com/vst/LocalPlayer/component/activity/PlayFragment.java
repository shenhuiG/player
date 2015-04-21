package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;

import android.widget.PopupWindow;
import android.widget.TextView;
import com.vst.LocalPlayer.LocalMenuView;
import com.vst.LocalPlayer.LocalSeekController;
import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.model.MediaBaseModel;
import com.vst.dev.common.media.SubTrack;
import com.vst.LocalPlayer.model.SubtripApi;
import com.vst.dev.common.media.IPlayer;

import net.myvst.v2.extra.media.MediaControlFragment;
import net.myvst.v2.extra.media.controller.MediaControllerManager;

public class PlayFragment extends MediaControlFragment implements IPlayer.OnCompletionListener,
        IPlayer.OnErrorListener, IPlayer.OnPreparedListener, MediaControllerManager.KeyEventHandler,
        IPlayer.OnInfoListener, LocalSeekController.ControlCallback, IPlayer.OnTimedTextChangedListener {
    private static final int HANDLE_ERROR = 0x0002;
    private static final int FINAL_PLAY = 0x0001;
    private Context mContext = null;
    private int mSeekWhenPrepared = 0;
    private int mScaleSize = IPlayer.SURFACE_BEST_FIT;
    private int mDecodeType = IPlayer.HARD_DECODE;
    private String mMediaPath = null;
    private long mediaId = -1;
    private Handler mHandler;
    private LocalSeekController mSeekController;
    private int mCycleMode = IPlayer.NO_CYCLE;

    public void setCycleMode(int i) {
        mCycleMode = i;
    }

    public static PlayFragment newInstance(Bundle args) {
        PlayFragment fragment = new PlayFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    private boolean init(Bundle args) {
        String mediaUri;
        if (args != null && (mediaUri = args.getString("uri")) != null) {
            if (!mediaUri.equals(mMediaPath)) {
                mediaId = args.getLong("_id", -1);
                mSeekWhenPrepared = getPositionFromRecord(mediaId);
                mPlayer.resetVideo();
                if (Uri.parse(mediaUri).getScheme().equalsIgnoreCase("file")) {
                    mHandler.sendMessage(mHandler.obtainMessage(FINAL_PLAY, mediaUri));
                }
            }
            return true;
        }
        return false;
    }


    private int getPositionFromRecord(long mediaID) {
        if (mediaID >= 0) {
            System.out.println(mediaID);
            Cursor c = mContext.getContentResolver().query(MediaStore.MediaRecord.CONTENT_URI, null,
                    MediaStore.MediaRecord.FIELD_MEDIA_ID + "=?", new String[]{mediaID + ""}, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                return c.getInt(c.getColumnIndex(MediaStore.MediaRecord.FIELD_POSITION));
            }
            c.close();
        }
        return 0;
    }


    private MediaBaseModel queryMediaBase(long mediaId, String mediaPath) {
        MediaBaseModel model = new MediaBaseModel();
        if (mediaId < 0) {
            model.name = Utils.smartMediaName(mediaPath);
            if (model.name == null) {
                model.name = mediaPath;
            }
        } else {
            Cursor c = mContext.getContentResolver().query(MediaStore.getContentUri(
                    MediaStore.MediaBase.TABLE_NAME, mediaId), null, null, null, null);
            if (c.moveToFirst()) {
                model.name = c.getString(c.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                model.metaTitle = c.getString(c.getColumnIndex(MediaStore.MediaBase.FIELD_META_TITLE));
            }
            c.close();
        }
        return model;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        mHandler = HANDLER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (init(getArguments())) {
            initController();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onPause() {
        insertRecord(mediaId, mPlayer.getPosition(), mPlayer.getDuration());
        super.onPause();
    }


    private void insertRecord(long mediaId, long position, long duration) {
        if (mediaId >= 0) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaRecord.FIELD_POSITION, position);
            values.put(MediaStore.MediaRecord.FIELD_DURATION, duration);
            values.put(MediaStore.MediaRecord.FIELD_DATE, System.currentTimeMillis());
            //first update
            int count = mContext.getContentResolver().update(MediaStore.MediaRecord.CONTENT_URI, values,
                    MediaStore.MediaRecord.FIELD_MEDIA_ID + "=?", new String[]{mediaId + ""});
            if (count <= 0) {
                //update failure
                values.put(MediaStore.MediaRecord.FIELD_MEDIA_ID, mediaId);
                values.put(MediaStore.MediaRecord.FIELD_DATE, System.currentTimeMillis());
                Uri uri = mContext.getContentResolver().insert(MediaStore.MediaRecord.CONTENT_URI, values);
                System.out.println("insert success uri=" + uri);
            } else {
                System.out.println("update success");
            }
        }
    }

    private void initController() {
        if (mControllerManager != null) {
            mControllerManager.reset();
            mSeekController = new LocalSeekController(mContext);
            mSeekController.setControl(this);
            mControllerManager.addController(LocalSeekController.SEEK_CONTROLLER, mSeekController, null, null);
            LocalMenuView menuView = new LocalMenuView(mContext);
            mControllerManager.addController(LocalMenuView.TAG, menuView, null, null);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if (mControllerManager != null) {
            mControllerManager.reset();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mHandler = null;
    }

    private void handleError(String msg) {
        mHandler.sendMessage(mHandler.obtainMessage(HANDLE_ERROR, msg));
    }


    private final Handler HANDLER = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FINAL_PLAY:
                    mMediaPath = (String) msg.obj;
                    System.out.println(mMediaPath);
                    if (mSeekController != null) {
                        mSeekController.setMediaMeta(queryMediaBase(mediaId, (String) msg.obj));
                    }
                    if (mPlayer != null && mMediaPath != null) {
                        mPlayer.setDecodeType(mDecodeType);
                        mPlayer.setVideoPath(mMediaPath, null);
                        mPlayer.start();
                        if (mSeekWhenPrepared > 0) {
                            mPlayer.seekTo(mSeekWhenPrepared);
                            mSeekWhenPrepared = 0;
                        }
                    }
                    break;
                case HANDLE_ERROR:
                    break;
                default:
            }
        }
    };

    @Override
    public long getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return -1;
    }

    public long getPosition() {
        if (mPlayer != null) {
            return mPlayer.getPosition();
        }
        return -1;
    }

    public void seekTo(int pos) {
        if (mPlayer != null) {
            mPlayer.seekTo(pos);
        }
    }

    @Override
    public void mediaPlay() {

    }

    @Override
    public void mediaPause() {

    }

    @Override
    public void onPrepared(IPlayer mp) {
        mPlayer.changeScale(mScaleSize);
        mPlayer.start();
        SubTrack[] internalSubs = mPlayer.getInternalSubTitle();
        Log.e("", "internalSubs  >" + internalSubs);
        SubTrack[] localSubs = SubtripApi.getLocalSubTitle(mMediaPath);
        Log.e("", "localSubs  >" + localSubs);
        //Arrays.asList(internalSubs);
        if (localSubs != null) {
            mPlayer.setSubTrack(localSubs[0], 0);
        }
    }

    @Override
    public boolean onError(IPlayer mp, int what, int extra) {
        if (what == IPlayer.VLC_INIT_ERROR) {

        } else {
            handleError("error");
        }
        return true;
    }

    @Override
    public void onCompletion(IPlayer mp) {
        switch (mCycleMode) {
            case IPlayer.NO_CYCLE:
                if (getActivity() != null) {
                    getActivity().finish();
                }
                break;
            case IPlayer.SINGLE_CYCLE:
                break;
            case IPlayer.ALL_CYCLE:
                break;
            case IPlayer.QUEUE_CYCLE:
                break;
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public boolean onHandlerKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean uniqueDown = event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0;
        if (uniqueDown) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                System.out.println("mControllerManager" + mControllerManager);
                mControllerManager.show(LocalSeekController.SEEK_CONTROLLER);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (isPlaying()) {
//                    if (mSeekController != null) {
//                        mSeekController.executePause();
//                    }
                } else {
//                    if (mSeekController != null) {
//                        mSeekController.executePlay();
//                    }
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                mControllerManager.show(LocalMenuView.TAG);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handlerKeyEvent(KeyEvent event) {
        boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                mControllerManager.hide();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onHandlerTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void setPlayer(IPlayer player) {
        super.setPlayer(player);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnTimedTextChangedListener(this);
    }

    @Override
    public void changArguments(Bundle args) {
        if (init(getArguments())) {
            initController();
        } else {
            getActivity().finish();
        }
    }


    @Override
    public boolean onInfo(IPlayer mp, int what, int extra, Bundle b) {
        if (what == IPlayer.MEDIA_INFO_TIMEOUT) {
            String uri = b.getString("uri");
            int seek = b.getInt("seek");
            int count = b.getInt("count");
            if (count <= 1) {
                if (mPlayer != null) {
                    mPlayer.setDecodeType(mDecodeType);
                    mPlayer.setVideoPath(uri, null);
                    mPlayer.start();
                    if (seek > 0) {
                        mPlayer.seekTo(seek);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private PopupWindow mSubTitleWindow = null;
    private WindowManager.LayoutParams mSubTitlesLocation = null;

    private PopupWindow getAndMakeSubTitleWindow() {
        if (mSubTitleWindow == null) {
            TextView tv = new TextView(mContext);
            tv.setTextColor(Color.YELLOW);
            tv.getPaint().setFakeBoldText(true);
            tv.setTextSize(com.vst.dev.common.util.Utils.getFitSize(mContext, 35));
            tv.setShadowLayer(10, 0, 0, Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            mSubTitleWindow = new PopupWindow(tv);
            mSubTitleWindow.setFocusable(false);
            mSubTitleWindow.setWindowLayoutMode(-1, -2);
            mSubTitleWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return mSubTitleWindow;
    }

    @Override
    public void onTimedTextChanger(String text, long stat, long end) {
        mSubTitleWindow = getAndMakeSubTitleWindow();
        if (text != null) {
            TextView tv = (TextView) mSubTitleWindow.getContentView();
            tv.setText(text);
            if (!mSubTitleWindow.isShowing()) {
                if (mSubTitlesLocation == null) {
                    mSubTitlesLocation = new WindowManager.LayoutParams();
                    mSubTitlesLocation.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    mSubTitlesLocation.y = com.vst.dev.common.util.Utils.getFitSize(mContext, 30);
                }
                mSubTitleWindow.showAtLocation((View) mPlayer, mSubTitlesLocation.gravity, 0,
                        mSubTitlesLocation.y);
            }
        } else {
            if (mSubTitleWindow != null && mSubTitleWindow.isShowing()) {
                mSubTitleWindow.dismiss();
            }
        }
    }
}
