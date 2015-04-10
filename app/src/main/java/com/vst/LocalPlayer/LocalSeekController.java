package com.vst.LocalPlayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vst.LocalPlayer.model.MediaBaseModel;
import com.vst.dev.common.util.Utils;

import net.myvst.v2.extra.media.controller.IInverseControl;
import net.myvst.v2.extra.media.controller.MediaControllerManager;
import net.myvst.v2.extra.media.controller.SeekView;
import net.myvst.v2.extra.media.controller.SeekView.OnSeekChangedListener;
import net.myvst.v2.extra.media.controller.TextDrawable;

public class LocalSeekController extends FrameLayout implements IInverseControl {

    public interface ControlCallback {

        public long getPosition();

        public long getDuration();

        public void seekTo(int pos);

        public void mediaPlay();

        public void mediaPause();

        public boolean isPlaying();
    }

    private MediaControllerManager mControllerManager;
    private LinearLayout mMetaView;
    private SeekView mSeekView;
    private TextView mTxtDuration;
    private FrameLayout mStateView;
    private String mControlId;
    private MediaBaseModel mMediaMeta;
    public static final String SEEK_CONTROLLER = "seek";
    private ControlCallback mControl;
    private Context mContext;
    private boolean mDragging = false;
    private boolean mAttach = false;
    private static final int SET_PROCESS = 1;
    private static final int PROGRESS_INCREMENT = 20000;
    private TextDrawable mPositionDrawable = null;
    private View pauseView = null;
    private ImageView seekBView = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SET_PROCESS) {
                setProgress();
            }
        }
    };

    public LocalSeekController(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        initView();
    }

    public void setMediaMeta(MediaBaseModel mediaMeta) {
        mMediaMeta = mediaMeta;
        if (mMetaView != null) {
            if (mMetaView != null) {
                updateMetaView(mediaMeta);
            } else {
                mMetaView.removeAllViews();
            }
        }
    }

    public void setControl(ControlCallback control) {
        mControl = control;
    }

    private void updateMetaView(MediaBaseModel mediaMeta) {
        if (mMetaView != null) {
            mMetaView.removeAllViews();
            TextView title = new TextView(mContext);
            title.setText("hhhhhh");
            mMetaView.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));
            ImageView cycleModImg = new ImageView(mContext);
            cycleModImg.setBackgroundResource(R.drawable.bg_format);
            cycleModImg.setImageResource(R.drawable.ic_loop_all);
            mMetaView.addView(cycleModImg, new LinearLayout.LayoutParams(-2, -2));

            ImageView cycleModImg1 = new ImageView(mContext);
            cycleModImg1.setBackgroundResource(R.drawable.bg_format);
            cycleModImg1.setImageResource(R.drawable.ic_loop_all);
            mMetaView.addView(cycleModImg1, new LinearLayout.LayoutParams(-2, -2));

            ImageView cycleModImg2 = new ImageView(mContext);
            cycleModImg2.setBackgroundResource(R.drawable.bg_format);
            cycleModImg2.setImageResource(R.drawable.ic_loop_all);
            mMetaView.addView(cycleModImg2, new LinearLayout.LayoutParams(-2, -2));

            ImageView cycleModImg3 = new ImageView(mContext);
            cycleModImg3.setBackgroundResource(R.drawable.bg_format);
            cycleModImg3.setImageResource(R.drawable.ic_loop_all);
            mMetaView.addView(cycleModImg3, new LinearLayout.LayoutParams(-2, -2));

        }
    }

    @SuppressLint("NewApi")
    private void initView() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.drawable.seek_controller_bg);
        mMetaView = new LinearLayout(mContext);
        mMetaView.setGravity(Gravity.CENTER_VERTICAL);
        mMetaView.setOrientation(LinearLayout.HORIZONTAL);
        mMetaView.setPadding(Utils.getFitSize(mContext, 60), Utils.getFitSize(mContext, 25),
                Utils.getFitSize(mContext, 60), 0);
        layout.addView(mMetaView, -1, -2);
        mSeekView = new SeekView(mContext);
        mSeekView.setProgressGravity(SeekView.PROGRESS_BOTTOM);
        mSeekView.setPadding(Utils.getFitSize(mContext, 20), 0, Utils.getFitSize(mContext, 20), 0);
        layout.addView(mSeekView, -1, -2);
        mTxtDuration = new TextView(mContext);
        Utils.applyFace(mTxtDuration);
        mTxtDuration.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 30));
        mTxtDuration.setGravity(Gravity.RIGHT);
        mTxtDuration.setPadding(0, Utils.getFitSize(mContext, 10), Utils.getFitSize(mContext, 60), 0);
        mTxtDuration.setTextColor(Color.WHITE);
        layout.addView(mTxtDuration, -1, -2);
        addView(layout, -1, -2);
        mStateView = new FrameLayout(mContext);
        addView(mStateView, new LayoutParams(-1, -1, Gravity.CENTER));
        initControllerView(this);
    }

    private Drawable makeTxtQualityBackGround() {
        PaintDrawable drawable = new PaintDrawable(0xA0FFFFFF);
        drawable.setPadding(Utils.getFitSize(mContext, 6), Utils.getFitSize(mContext, 2),
                Utils.getFitSize(mContext, 6), Utils.getFitSize(mContext, 2));
        drawable.setCornerRadius(Utils.getFitSize(mContext, 4));
        return drawable;
    }

    private void initControllerView(View v) {
        mSeekView.setKeyProgressIncrement(PROGRESS_INCREMENT);
        mSeekView.setOnSeekChangedListener(mOnSeekChangedListener);
        mPositionDrawable = new TextDrawable(getContext());
        mPositionDrawable.setText(Utils.stringForTime(0));
        mPositionDrawable.setBackDrawable(Utils.getLocalDrawable(getContext(), R.drawable.time_drawable_bg));
        mPositionDrawable.setTextSize(25);
        mSeekView.setThumb(mPositionDrawable);
    }

    private OnSeekChangedListener mOnSeekChangedListener = new SeekView.OnSeekChangedListener() {

        @Override
        public void onSeekChanged(SeekView bar, int progress, int startprogress, boolean increase) {

            if (mControl != null) {
                executeSeek(increase);
            }
            if (progress == startprogress) {
                mDragging = true;
            } else {
                if (mControl != null) {
                    if (progress >= mControl.getDuration()) {
                        progress = (int) (mControl.getDuration() - 1000);
                    }
                    mControl.seekTo(progress);
                    executePlay();
                }
                mDragging = false;
            }
        }

        @Override
        public void onProgressChanged(SeekView bar, int progress, boolean fromuser) {
            mPositionDrawable.setText(Utils.stringForTime(progress));
        }

        @Override
        public void onShowSeekBarView(boolean increase) {
            mHandler.removeCallbacksAndMessages(null);
            showSeekBarView(increase);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttach = false;
        mHandler.removeMessages(SET_PROCESS);
        if (mControl != null) {
            mControl.mediaPlay();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttach = true;
        updateView();

    }

    private GestureDetector mGestureDetector = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    public void updateView() {
        if (mControl != null && mAttach) {
            if (mTxtDuration != null) {
                mTxtDuration.setText(Utils.stringForTime(mControl.getDuration()));
            }
            if (mSeekView != null) {
                setProgress();
            }
        }
    }

    private void setProgress() {
        if (mControl != null && !mDragging) {
            long position = mControl.getPosition();
            long duration = mControl.getDuration();
            Log.d(LocalSeekController.class.getSimpleName(), "position=" + position + ",duration=" + duration);
            if (mSeekView != null) {
                mSeekView.setMax((int) duration);
                mSeekView.setProgress((int) position);
                mSeekView.setKeyProgressIncrement(PROGRESS_INCREMENT);
            }
        }
        mHandler.sendEmptyMessageDelayed(SET_PROCESS, 1000);
    }

    public void executePlay() {
        try {
            if (mStateView != null) {
                mStateView.removeAllViews();
            }
            mControllerManager.show(mControlId);
            if (mControl != null) {
                mControl.mediaPlay();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void executePause() {
        try {
            if (pauseView == null) {
                FrameLayout layout = new FrameLayout(mContext);
                TextView pauseIcon = new TextView(mContext);
                CharSequence c = Utils
                        .makeImageSpannable("*", getResources().getDrawable(R.drawable.ic_pause_1), 0,
                                Utils.getFitSize(mContext, 78), Utils.getFitSize(mContext, 78),
                                ImageSpan.ALIGN_BOTTOM);
                if (c != null) {
                    pauseIcon.setText(c);
                }
                pauseIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        executePlay();
                    }
                });
                LayoutParams lp2 = new LayoutParams(-2, -2);
                lp2.gravity = Gravity.LEFT | Gravity.BOTTOM;
                lp2.leftMargin = Utils.getFitSize(mContext, 65);
                lp2.bottomMargin = Utils.getFitSize(mContext, 50);
                layout.addView(pauseIcon, lp2);
                pauseView = layout;
            }
            if (pauseView.getParent() == null) {
                mStateView.removeAllViews();
                mStateView.addView(pauseView, new LayoutParams(-1, -1, Gravity.CENTER));
            }
            if (mControl != null) {
                mControl.mediaPause();
            }
            mControllerManager.show(mControlId, -1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void executeSeek(boolean increase) {
        showSeekBarView(increase);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(SET_PROCESS, 2000);
    }

    private void showSeekBarView(boolean increase) {
        if (seekBView == null) {
            seekBView = new ImageView(mContext);
            seekBView.setLayoutParams(new LayoutParams(Utils.getFitSize(mContext, 144),
                    Utils.getFitSize(mContext, 145), Gravity.CENTER));
        }
        if (increase) {
            seekBView.setImageResource(R.drawable.ic_seekforward);
        } else {
            seekBView.setImageResource(R.drawable.ic_seekbackward);
        }
        if (seekBView.getParent() == null) {
            mStateView.removeAllViews();
            mStateView.addView(seekBView);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) {
            return true;
        }
        try {
            boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
            int keyCode = event.getKeyCode();
            if (uniqueDown) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    if (mControl != null) {
                        if (mControl.isPlaying()) {
                            executePause();
                        } else {
                            executePlay();
                        }
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                    if (mControl != null) {
                        long p = (int) mControl.getPosition();
                        if (p > 15000) {
                            mControl.seekTo((int) p - 15000);
                        }
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                    if (mControl != null) {
                        long p = mControl.getPosition();
                        long duration = mControl.getDuration();
                        if (p > 0 && p < duration - 15000) {
                            mControl.seekTo((int) p + 15000);
                        }
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    /*if (adView != null && pauseView != null && pauseView.getParent() == mStateView) {
                        if (adView.getVisibility() == View.VISIBLE) {
                            adView.setVisibility(View.INVISIBLE);
                        } else {
                            adView.setVisibility(View.VISIBLE);
                        }
                    }*/
                    return true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void addControllerManager(MediaControllerManager controllerManager, String id) {
        mControllerManager = controllerManager;
        mControlId = id;
    }
}
