package com.vst.LocalPlayer.FileExplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.vst.LocalPlayer.R;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Stack;

public class LocalMenuView extends LinearLayout {


    public static final String THRD_SETTING = "3D设定";
    public static final String THRD_ITEM_2D = "2D 播放";
    public static final String THRD_ITEM_3DLF = "3D左右格式";
    public static final String THRD_ITEM_3DUD = "3D上下格式";
    public static final String THRD_ITEM_3D = "3D 播放";
    public static final String LOOPER_SETTING = "循环设定";
    public static final String LOOPER_ALL = "顺序播放";
    public static final String LOOPER_SINGLE = "单个循环";
    public static final String LOOPER_OFF = "单个播放";
    public static final String LOOPER_RANDOM = "随机播放";
    public static final String AUDIO_SETTING = "音频设定";

    public static final String SUBTRIP_SETTING = "字幕设定";

    public static final String MENU_SETTING = "菜单";
    private Stack<String> mPathStack = new Stack();

    public static final String TAG = "menu";
    private Context mContext;
    private TextView titleView;
    private ViewFlipper mFlipper;

    private HashMap<String, WeakReference<View>> mViews = new HashMap<String, WeakReference<View>>();

    public LocalMenuView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        initView();
    }

    private void initView() {
        titleView = new TextView(mContext);
        titleView.setText(MENU_SETTING);
        addView(titleView);
        mFlipper = new ViewFlipper(mContext);
        addView(mFlipper, new LayoutParams(460, 600));
    }


    private View getView(String tag) {
        View view = null;
        WeakReference<View> ref = mViews.get(tag);
        if (ref != null && (view = ref.get()) != null) {
            return view;
        }
        if (MENU_SETTING.equals(tag)) {
            view = makeMenuSettingView();
        } else if (THRD_SETTING.equals(tag)) {
            view = make3DSettingView();
        } else if (LOOPER_SETTING.equals(tag)) {
            view = makeLooperSettingView();
        } else if (AUDIO_SETTING.equals(tag)) {
            view = makeAudioSettingView();
        } else if (SUBTRIP_SETTING.equals(tag)) {

        }
        if (view != null) {
            mViews.put(tag, new WeakReference<View>(view));
        }
        return view;
    }


    private void toNextView(String nextViewTag, Object obj) {
        if (setupFlipper(nextViewTag)) {
            //updateViewData(tag, obj);
            mFlipper.setInAnimation(mContext, net.myvst.v2.extra.R.anim.translate_right_in);
            mFlipper.setOutAnimation(mContext, net.myvst.v2.extra.R.anim.translate_left_out);
            mFlipper.showNext();
            mFlipper.requestFocus();
            mPathStack.push(nextViewTag);
        }
    }

    private void toPreviousView() {
        if (mPathStack.size() > 1) {
            mPathStack.pop();
            String tag = mPathStack.peek();
            if (setupFlipper(tag)) {
                //updateViewData(tag, obj);
                mFlipper.setInAnimation(mContext, net.myvst.v2.extra.R.anim.translate_left_in);
                mFlipper.setOutAnimation(mContext, net.myvst.v2.extra.R.anim.translate_right_out);
                mFlipper.showPrevious();
                mFlipper.requestFocus();
            }
        }
    }

    private boolean setupFlipper(String tag) {
        View nextView = getView(tag);
        if (nextView != null) {
            if (nextView.getParent() != null) {
                mFlipper.removeAllViews();
            }
            if (mFlipper.getChildCount() > 1) {
                mFlipper.removeViewAt(0);
            }
            mFlipper.addView(nextView, mFlipper.getChildCount());
            return true;
        }
        return false;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        toNextView(MENU_SETTING, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFlipper.removeAllViews();
        mPathStack.clear();
    }

    private View makeMenuSettingView() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(makeSelectionItem(THRD_SETTING, getResources().getDrawable(R.drawable.icon_3d_setting)));
        layout.addView(makeSelectionItem(LOOPER_SETTING, getResources().getDrawable(R.drawable.icon_looper_setting)));
        layout.addView(makeSelectionItem(AUDIO_SETTING, getResources().getDrawable(R.drawable.icon_audio_setting)));
        layout.addView(makeSelectionItem(SUBTRIP_SETTING, getResources().getDrawable(R.drawable.icon_subtripe_setting)));
        return layout;
    }


    private View make3DSettingView() {
        RadioGroup layout = new RadioGroup(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(makeCheckableItemView(THRD_ITEM_2D, 1), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3DLF, 2), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3DUD, 3), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3D, 4), -1, -2);
        layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View child = group.findViewById(checkedId);
                int mod = (Integer) child.getTag();
                System.out.println(mod + "");
            }
        });
        return layout;
    }


    private View makeLooperSettingView() {
        RadioGroup layout = new RadioGroup(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(makeCheckableItemView(LOOPER_ALL, 1), -1, -2);
        layout.addView(makeCheckableItemView(LOOPER_SINGLE, 2), -1, -2);
        layout.addView(makeCheckableItemView(LOOPER_RANDOM, 3), -1, -2);
        layout.addView(makeCheckableItemView(LOOPER_OFF, 4), -1, -2);
        layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View child = group.findViewById(checkedId);
                int mod = (Integer) child.getTag();
                System.out.println(mod + "");
            }
        });
        return layout;
    }

    private View makeAudioSettingView() {
        RadioGroup layout = new RadioGroup(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(makeCheckableItemView(THRD_ITEM_2D, 1), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3DLF, 2), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3DUD, 3), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3D, 4), -1, -2);
        layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View child = group.findViewById(checkedId);
                int mod = (Integer) child.getTag();
                System.out.println(mod + "");
            }
        });
        return layout;
    }

    private View makeSelectionItem(final String content, Drawable left) {
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        layout.setBackgroundResource(R.drawable.icon_item_bg_l);
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toNextView(content, null);
                System.out.println(content);
            }
        });
        layout.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    child.setSelected(hasFocus);
                }
            }
        });
        layout.setPadding(60, 30, 60, 30);
        ImageView icon = new ImageView(mContext);
        icon.setImageDrawable(left);
        layout.addView(icon, new LinearLayout.LayoutParams(-2, -2));
        TextView contentView = new TextView(mContext);
        contentView.setText(content);
        contentView.setGravity(Gravity.CENTER_VERTICAL);
        contentView.setTextColor(Color.WHITE);
        contentView.setPadding(30, 0, 0, 0);
        contentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25);
        layout.addView(contentView, new LinearLayout.LayoutParams(0, -2, 1.0F));
        ImageView arrow = new ImageView(mContext);
        arrow.setImageResource(R.drawable.ic_arrow_focus);
        layout.addView(arrow, new LinearLayout.LayoutParams(-2, -2));
        layout.setFocusable(true);
        return layout;
    }


    private View makeCheckableItemView(final String content, Object obj) {
        final RadioButton rbt = new RadioButton(mContext);
        rbt.setBackgroundResource(R.drawable.icon_item_bg_l);
        rbt.setButtonRightDrawable(getResources().getDrawable(R.drawable.icon_checked_sel));
        rbt.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        rbt.setGravity(Gravity.CENTER_VERTICAL);
        rbt.setText(content);
        rbt.setPadding(0, 30, 60, 30);
        if (obj != null) {
            rbt.setTag(obj);
        }
        return rbt;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            if (mPathStack.size() > 1) {
                toPreviousView();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    class RadioButton extends android.widget.RadioButton {

        private Drawable mButtonDrawable;

        public RadioButton(Context context) {
            super(context);
        }

        @Override
        protected boolean verifyDrawable(Drawable who) {
            return super.verifyDrawable(who) || who == mButtonDrawable;
        }

        @Override
        public void jumpDrawablesToCurrentState() {
            super.jumpDrawablesToCurrentState();
            if (mButtonDrawable != null) mButtonDrawable.jumpToCurrentState();
        }

        public void setButtonRightDrawable(Drawable d) {
            if (d != null) {
                if (mButtonDrawable != null) {
                    mButtonDrawable.setCallback(null);
                    unscheduleDrawable(mButtonDrawable);
                }
                d.setCallback(this);
                d.setState(getDrawableState());
                d.setVisible(getVisibility() == VISIBLE, false);
                mButtonDrawable = d;
                mButtonDrawable.setState(null);
                setMinHeight(mButtonDrawable.getIntrinsicHeight());
            }
            refreshDrawableState();
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            if (mButtonDrawable != null) {
                int[] myDrawableState = getDrawableState();
                mButtonDrawable.setState(myDrawableState);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
                final int drawableHeight = buttonDrawable.getIntrinsicHeight();
                final int drawableWidth = buttonDrawable.getIntrinsicWidth();

                int top = 0;
                switch (verticalGravity) {
                    case Gravity.BOTTOM:
                        top = getHeight() - drawableHeight;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        top = (getHeight() - drawableHeight) / 2;
                        break;
                }
                int bottom = top + drawableHeight;
                int left = getWidth() - drawableWidth - getPaddingRight();
                int right = getWidth() - getPaddingRight();
                buttonDrawable.setBounds(left, top, right, bottom);
                buttonDrawable.draw(canvas);
            }
        }

        @Override
        public int getCompoundPaddingLeft() {
            int padding = super.getCompoundPaddingLeft();
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                padding += buttonDrawable.getIntrinsicWidth();
            }
            return padding;
        }

        @Override
        public int getCompoundPaddingRight() {
            int padding = super.getCompoundPaddingRight();
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                padding += buttonDrawable.getIntrinsicWidth();
            }
            return padding;
        }
    }


    class State {

        String tag;
        int focusdIndex;
        int foucusId;
        int checkedId;
        int checkedIndex;
    }

}
