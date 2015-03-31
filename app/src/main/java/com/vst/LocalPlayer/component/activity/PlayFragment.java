package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.vst.LocalPlayer.FileExplorer.LocalMenuView;
import com.vst.LocalPlayer.LocalSeekController;
import com.vst.LocalPlayer.MediaMeta;
import com.vst.LocalPlayer.Utils;
import com.vst.dev.common.media.IPlayer;

import net.myvst.v2.extra.R;
import net.myvst.v2.extra.media.MediaControlFragment;
import net.myvst.v2.extra.media.controller.MediaControllerManager;

import java.util.HashMap;

public class PlayFragment extends MediaControlFragment implements IPlayer.OnCompletionListener,
        IPlayer.OnErrorListener, IPlayer.OnPreparedListener, MediaControllerManager.KeyEventHandler,
        IPlayer.OnInfoListener, LocalSeekController.ControlCallback {
    private static final int HANDLE_ERROR = 0x0002;
    private static final int FINAL_PLAY = 0x0001;
    private Context mContext = null;
    private int mSeekWhenPrepared = 0;
    private int mScaleSize = IPlayer.SURFACE_BEST_FIT;
    private int mDecodeType = IPlayer.HARD_DECODE;
    private String mMediaPath = null;
    private HashMap<String, String> mHeader = null;
    private Handler mHandler;
    private LocalSeekController mSeekController;
    private int mCycleMode = 0;

    public static PlayFragment newInstance(Bundle args) {
        PlayFragment fragment = new PlayFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    private boolean init(Bundle args) {
        String mediaUri;
        if (args != null && (mediaUri = args.getString("pushUrl")) != null) {
            if (!mediaUri.equals(mMediaPath)) {
                mPlayer.resetVideo();
                if (Uri.parse(mediaUri).getScheme().equalsIgnoreCase("file")) {
                    mHandler.sendMessage(mHandler.obtainMessage(FINAL_PLAY, mediaUri));
                }
            }
            return true;
        }
        return false;
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
                    MediaMeta meta = Utils.readMediaMeta(mMediaPath);
                    if (mSeekController != null) {
                        mSeekController.setMediaMeta(meta);
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
    }

    @Override
    public boolean onError(IPlayer mp, int what, int extra) {
        if (what == IPlayer.VLC_INIT_ERROR) {
        } else {
            handleError(getResources().getString(R.string.play_error_txt));
        }
        return true;
    }

    @Override
    public void onCompletion(IPlayer mp) {
        if (getActivity() != null) {
            getActivity().finish();
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
    }

    @Override
    public void changArguments(Bundle args) {
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
}
