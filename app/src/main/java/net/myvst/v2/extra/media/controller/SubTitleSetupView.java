package net.myvst.v2.extra.media.controller;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vst.LocalPlayer.R;
import com.vst.dev.common.util.Utils;

public class SubTitleSetupView extends FrameLayout implements View.OnKeyListener, View.OnFocusChangeListener,
        View.OnClickListener {

    public interface SetupCallBack {
        void setup(int type, Object value);
    }

    public static final int TEXT_SIZE = 0;
    public static final int TEXT_COLOR = 1;
    public static final int TEXT_LOCATION = 2;
    public static final int SUBTITLE_OFFSET = 3;

    private static final int[] COLORS = new int[] { Color.YELLOW, Color.BLUE, Color.WHITE, Color.RED,
            Color.GREEN };
    private static final WindowManager.LayoutParams[] LOCATIONS;
    static {
        LOCATIONS = new WindowManager.LayoutParams[8];
        int flag = LOCATIONS.length / 2;
        for (int i = 0; i < LOCATIONS.length; i++) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            if (i < flag) {
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                lp.y = 30 * (i + 1);
            } else {
                lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                lp.y = 30 * (flag - (i % flag));
            }
            LOCATIONS[i] = lp;
        }
    }
    private int mLocationIndex = 0;
    private int mColorIndex = 0;
    private int mTextSize = 30;
    private long mTimeOffset = 0;
    private ImageView mSetupUpOpration;
    private ImageView mSetupDownOpration;
    private LinearLayout mFocusView = null;
    private int mSetupItem = TEXT_SIZE;

    private Context mContext;
    private TextView mTextSizeSetup;
    private TextView mTextColorSetup;
    private TextView mTextLocationSetup;
    private TextView mSrtTimeOffsetSetup;
    private LinearLayout content;
    private SetupCallBack mCallback;

    public void setCallback(SetupCallBack callback) {
        mCallback = callback;
    }

    public SubTitleSetupView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    private void initView() {
        content = new LinearLayout(mContext);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setBackgroundResource(R.drawable.srt_setup_bg);
        mTextSizeSetup = makeAndAddTextView();
        mTextSizeSetup.setText("字幕大小");
        mTextColorSetup = makeAndAddTextView();
        mTextColorSetup.setText("字幕颜色");
        mTextLocationSetup = makeAndAddTextView();
        mTextLocationSetup.setText("字幕位置");
        mSrtTimeOffsetSetup = makeAndAddTextView();
        mSrtTimeOffsetSetup.setText("时间修正");
        addView(content, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
    }

    private TextView makeAndAddTextView() {
        TextView textView = new TextView(mContext);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25);
        textView.setGravity(Gravity.CENTER);
        textView.setFocusable(true);
        textView.setMinimumWidth(Utils.getFitSize(mContext, 180));
        textView.setOnFocusChangeListener(this);
        textView.setOnKeyListener(this);
        textView.setOnClickListener(this);
        content.addView(textView, new LinearLayout.LayoutParams(0, -1, 1.0f));
        return textView;
    }

    private void changTextSize(int delta) {
        mTextSize += delta;
        if (mTextSize < 28) {
            mTextSize = 28;
        }
        if (mTextSize > 58) {
            mTextSize = 58;
        }
        if (mCallback != null) {
            mCallback.setup(TEXT_SIZE, mTextSize);
        }
    }

    private void changeTextColor(int delta) {
        mColorIndex += delta;
        while (mColorIndex < 0) {
            mColorIndex += COLORS.length;
        }
        mColorIndex %= COLORS.length;
        int newColor = COLORS[mColorIndex];
        if (mCallback != null) {
            mCallback.setup(TEXT_COLOR, newColor);
        }
    }

    private void changeLocation(int delta) {
        mLocationIndex += delta;
        while (mLocationIndex < 0) {
            mLocationIndex += LOCATIONS.length;
        }
        mLocationIndex %= LOCATIONS.length;
        WindowManager.LayoutParams newLocation = LOCATIONS[mLocationIndex];
        if (mCallback != null) {
            mCallback.setup(TEXT_LOCATION, newLocation);
        }
    }

    private void changeTimeOffset(int delta) {
        mTimeOffset += delta;
        if (mCallback != null) {
            mCallback.setup(SUBTITLE_OFFSET, mTimeOffset);
        }
    }

    private Rect padding = new Rect();

    private void flyWhiteBorder(int toWidth, int toHeight, int toX, int toY) {
        if (mFocusView != null) {
            ViewPropertyAnimator animator = mFocusView.animate();
            animator.setDuration(250);
            animator.x(toX - padding.left);
            animator.start();
        } else {
            Drawable d = Utils.getLocalDrawable(mContext, R.drawable.srt_setup_focus);
            if (d instanceof NinePatchDrawable) {
                ((NinePatchDrawable) d).getPadding(padding);
            }
            int width = toWidth + padding.left + padding.right;
            int height = toHeight + padding.top + padding.bottom;
            mFocusView = new LinearLayout(mContext);
            mFocusView.setOrientation(LinearLayout.VERTICAL);
            mSetupUpOpration = new ImageView(mContext);
            mSetupUpOpration.setImageDrawable(Utils.getLocalDrawable(mContext, R.drawable.set_up_arrow));
            mSetupUpOpration.setOnClickListener(this);
            mFocusView.addView(mSetupUpOpration, -1, -2);
            ImageView center = new ImageView(mContext);
            center.setBackgroundDrawable(d);
            mFocusView.addView(center, width, height);
            mSetupDownOpration = new ImageView(mContext);
            mSetupDownOpration.setImageDrawable(Utils.getLocalDrawable(mContext, R.drawable.set_down_arrow));
            mSetupDownOpration.setOnClickListener(this);
            mFocusView.addView(mSetupDownOpration, -1, -2);
            MarginLayoutParams source = new MarginLayoutParams(width, -2);
            source.leftMargin = toX - padding.left;
            // source.topMargin = 10;
            // source.bottomMargin = 10;
            FrameLayout.LayoutParams lp = new LayoutParams(source);
            lp.gravity = Gravity.CENTER_VERTICAL;
            addView(mFocusView, -1, lp);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        boolean uniqueDown = event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0;
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            int i = keyCode == KeyEvent.KEYCODE_DPAD_UP ? 1 : -1;
            if (uniqueDown) {
                if (v == mTextSizeSetup) {
                    changTextSize(i);
                } else if (v == mTextColorSetup) {
                    changeTextColor(i);
                } else if (v == mTextLocationSetup) {
                    changeLocation(i);
                } else if (v == mSrtTimeOffsetSetup) {
                    changeTimeOffset(200 * i);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            int w = v.getWidth();
            int h = v.getHeight();
            int left = v.getLeft() + content.getLeft();
            int top = v.getTop() + content.getTop();
            flyWhiteBorder(w, h, left, top);
            if (v == mTextSizeSetup) {
                mSetupItem = TEXT_SIZE;
                if (mCallback != null) {
                    mCallback.setup(TEXT_SIZE, mTextSize);
                }
            } else if (v == mTextColorSetup) {
                mSetupItem = TEXT_COLOR;
                if (mCallback != null) {
                    mCallback.setup(TEXT_COLOR, COLORS[mColorIndex]);
                }
            } else if (v == mTextLocationSetup) {
                mSetupItem = TEXT_LOCATION;
                if (mCallback != null) {
                    mCallback.setup(TEXT_LOCATION, LOCATIONS[mLocationIndex]);
                }
            } else if (v == mSrtTimeOffsetSetup) {
                mSetupItem = SUBTITLE_OFFSET;
                if (mCallback != null) {
                    mCallback.setup(SUBTITLE_OFFSET, mTimeOffset);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSetupUpOpration || v == mSetupDownOpration) {
            int i = v == mSetupUpOpration ? 1 : -1;
            if (mSetupItem == TEXT_SIZE) {
                changTextSize(i);
            } else if (mSetupItem == TEXT_COLOR) {
                changeTextColor(i);
            } else if (mSetupItem == TEXT_LOCATION) {
                changeLocation(i);
            } else if (mSetupItem == SUBTITLE_OFFSET) {
                changeTimeOffset(200 * i);
            }
        } else {
            int w = v.getWidth();
            int h = v.getHeight();
            int left = v.getLeft() + content.getLeft();
            int top = v.getTop() + content.getTop();
            flyWhiteBorder(w, h, left, top);
            if (v == mTextSizeSetup) {
                mSetupItem = TEXT_SIZE;
                if (mCallback != null) {
                    mCallback.setup(TEXT_SIZE, mTextSize);
                }
            } else if (v == mTextColorSetup) {
                mSetupItem = TEXT_COLOR;
                if (mCallback != null) {
                    mCallback.setup(TEXT_COLOR, COLORS[mColorIndex]);
                }
            } else if (v == mTextLocationSetup) {
                mSetupItem = TEXT_LOCATION;
                if (mCallback != null) {
                    mCallback.setup(TEXT_LOCATION, LOCATIONS[mLocationIndex]);
                }
            } else if (v == mSrtTimeOffsetSetup) {
                mSetupItem = SUBTITLE_OFFSET;
                if (mCallback != null) {
                    mCallback.setup(SUBTITLE_OFFSET, mTimeOffset);
                }
            }
        }
    }
}
