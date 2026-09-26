package com.RobinNotBad.BiliClient.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;

public class InstanceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BiliTerminal.setInstance(this);
        super.onCreate(savedInstanceState);
        //在此处（而不是Application启动时）检查「夜深了」，保证提醒窗口出现在主界面之上，
        //同时内部做了「同一晚只提醒一次」的判断，从第三方播放器返回时不会重复弹出
        try {
            AppInfoApi.checkNightReminder();
        } catch (Exception ignored) {
        }
    }

    public void setMenuClick() {
        findViewById(R.id.top).setOnClickListener(view -> menuClick.run());
    }

    public Runnable menuClick = (() -> {
        Intent intent = new Intent();
        intent.setClass(this, MenuActivity.class);
        if (getIntent().hasExtra("from"))
            intent.putExtra("from", getIntent().getStringExtra("from"));
        startActivity(intent);
        overridePendingTransition(R.anim.anim_activity_in_down, 0);
    });

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) menuClick.run();
        return super.onKeyDown(keyCode, event);
    }
}
