//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.widget.checkbox.SmoothCheckBox;
import com.monke.mprogressbar.MHorProgressBar;
import com.monke.mprogressbar.OnProgressListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadAdjustPop extends PopupWindow {
    @BindView(R.id.hpb_light)
    MHorProgressBar hpbLight;
    @BindView(R.id.scb_follow_sys)
    SmoothCheckBox scbFollowSys;
    @BindView(R.id.ll_follow_sys)
    LinearLayout llFollowSys;
    @BindView(R.id.ll_click)
    LinearLayout llClick;
    @BindView(R.id.hpb_click)
    MHorProgressBar hpbClick;
    @BindView(R.id.ll_tts_SpeechRate)
    LinearLayout llTtsSpeechRate;
    @BindView(R.id.hpb_tts_SpeechRate)
    MHorProgressBar hpbTtsSpeechRate;

    private Context mContext;
    private Boolean isFollowSys;
    private int light;
    private ReadBookControl readBookControl;
    private OnAdjustListener adjustListener;

    public interface OnAdjustListener {
        void changeSpeechRate(int speechRate);
    }

    public ReadAdjustPop(Context context, OnAdjustListener adjustListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.mContext = context;
        this.adjustListener = adjustListener;

        View view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_read_adjust, null);
        this.setContentView(view);
        ButterKnife.bind(this, view);
        initData();
        bindEvent();

        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_windowlight);
    }

    private void initData() {
        readBookControl = ReadBookControl.getInstance();
        isFollowSys = getIsFollowSys();
        light = getLight();

    }

    private void bindEvent() {
        llFollowSys.setOnClickListener(v -> {
            if (scbFollowSys.isChecked()) {
                scbFollowSys.setChecked(false, true);
            } else {
                scbFollowSys.setChecked(true, true);
            }
        });
        scbFollowSys.setOnCheckedChangeListener((checkBox, isChecked) -> {
            isFollowSys = isChecked;
            if (isChecked) {
                //跟随系统
                hpbLight.setCanTouch(false);
                setScreenBrightness();
            } else {
                //不跟随系统
                hpbLight.setCanTouch(true);
                hpbLight.setDurProgress(light);
            }
        });
        //亮度调节
        hpbLight.setProgressListener(new OnProgressListener() {
            @Override
            public void moveStartProgress(float dur) {

            }

            @Override
            public void durProgressChange(float dur) {
                if (!isFollowSys) {
                    light = (int) dur;
                    setScreenBrightness((int) dur);
                }
            }

            @Override
            public void moveStopProgress(float dur) {

            }

            @Override
            public void setDurProgress(float dur) {

            }
        });
        //点击灵敏度调节
        hpbClick.setMaxProgress(100);
        hpbClick.setDurProgress(readBookControl.getClickSensitivity());
        hpbClick.setProgressListener(new OnProgressListener() {
            @Override
            public void moveStartProgress(float dur) {

            }

            @Override
            public void durProgressChange(float dur) {
                readBookControl.setClickSensitivity((int) dur);
            }

            @Override
            public void moveStopProgress(float dur) {

            }

            @Override
            public void setDurProgress(float dur) {

            }
        });
        //朗读语速调节
        hpbTtsSpeechRate.setDurProgress(readBookControl.getSpeechRate()-5);
        hpbTtsSpeechRate.setProgressListener(new OnProgressListener() {
            @Override
            public void moveStartProgress(float dur) {

            }

            @Override
            public void durProgressChange(float dur) {

            }

            @Override
            public void moveStopProgress(float dur) {
                readBookControl.setSpeechRate((int)dur + 5);
                if (adjustListener != null) {
                    adjustListener.changeSpeechRate(readBookControl.getSpeechRate());
                }
            }

            @Override
            public void setDurProgress(float dur) {

            }
        });
    }

    public void setScreenBrightness(int value) {
        WindowManager.LayoutParams params = ((Activity) mContext).getWindow().getAttributes();
        params.screenBrightness = value * 1.0f / 255f;
        ((Activity) mContext).getWindow().setAttributes(params);
    }

    public void setScreenBrightness() {
        WindowManager.LayoutParams params = ((Activity) mContext).getWindow().getAttributes();
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        ((Activity) mContext).getWindow().setAttributes(params);
    }

    public int getScreenBrightness() {
        int value = 0;
        ContentResolver cr = mContext.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    private void saveLight() {
        SharedPreferences preference = mContext.getSharedPreferences("CONFIG", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt("light", light);
        editor.putBoolean("isfollowsys", isFollowSys);
        editor.commit();
    }

    private int getLight() {
        SharedPreferences preference = mContext.getSharedPreferences("CONFIG", Context.MODE_PRIVATE);
        return preference.getInt("light", getScreenBrightness());
    }

    private Boolean getIsFollowSys() {
        SharedPreferences preference = mContext.getSharedPreferences("CONFIG", Context.MODE_PRIVATE);
        return preference.getBoolean("isfollowsys", true);
    }

    @Override
    public void dismiss() {
        saveLight();
        super.dismiss();
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        initData();
        hpbLight.setDurProgress(light);
        scbFollowSys.setChecked(isFollowSys);
    }

    public void initLight() {
        if (!isFollowSys) {
            setScreenBrightness(light);
        }
    }
}
