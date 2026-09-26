package com.RobinNotBad.BiliClient;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.util.BackupUtil;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.Logu;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class BiliTerminal extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static boolean DPI_FORCE_CHANGE = false;

    /** 本进程的启动时刻，用于判断当前是否处于"应用刚启动、主界面还没出来"的阶段 */
    private static final long PROCESS_START_TIME = System.currentTimeMillis();

    public static long getProcessStartTime() {
        return PROCESS_START_TIME;
    }

    private static WeakReference<InstanceActivity> instance = new WeakReference<>(null);

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null) {
            SharedPreferencesUtil.sharedPreferences = getSharedPreferences("default", MODE_PRIVATE);
            context = getFitDisplayContext(this);
            ErrorCatch errorCatch = ErrorCatch.getInstance();
            errorCatch.init(context);

            boolean debugBuild = isDebugBuild();
            Logu.LOGV_ENABLED = SharedPreferencesUtil.getBoolean("dev_logv", debugBuild);
            Logu.LOGD_ENABLED = SharedPreferencesUtil.getBoolean("dev_logd", debugBuild);
            Logu.LOGI_ENABLED = SharedPreferencesUtil.getBoolean("dev_logi", debugBuild);

            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.DYNAMIC_UPDATE_CHECK_ENABLE, true) && SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) != 0) {
                CenterThreadPool.run(() -> {
                    try {
                        long updateBaseline = SharedPreferencesUtil.getLong("dynamic_update_baseline", 0);
                        int updateNum = DynamicApi.checkDynamicUpdate("all", updateBaseline);
                        SharedPreferencesUtil.putInt(SharedPreferencesUtil.DYNAMIC_UPDATE_NUM, updateNum);
                    } catch (IOException | JSONException e) {
                        SharedPreferencesUtil.putInt(SharedPreferencesUtil.DYNAMIC_UPDATE_NUM, 0);
                    }
                });
            }

            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.MESSAGE_UPDATE_CHECK_ENABLE, true) && SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) != 0) {
                CenterThreadPool.run(() -> {
                    try {
                        int messageUnread = MessageApi.checkMessageUnread();
                        int privateMsgUnread = MessageApi.checkPrivateMsgUnread();
                        int totalUnread = messageUnread + privateMsgUnread;
                        SharedPreferencesUtil.putInt(SharedPreferencesUtil.MESSAGE_UPDATE_NUM, totalUnread);
                    } catch (IOException | JSONException e) {
                        SharedPreferencesUtil.putInt(SharedPreferencesUtil.MESSAGE_UPDATE_NUM, 0);
                    }
                });
            }

            // 启动时检查应用更新（根据auto_check_update_enable设置）
            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.AUTO_CHECK_UPDATE_ENABLE, true)) {
                CenterThreadPool.run(() -> {
                    try {
                        AppInfoApi.check(context);
                    } catch (Exception e) {
                        Logu.e("AppInfoApi.check: 检查更新失败 - " + e.getMessage());
                    }
                });
            }

            // 若实验室"备份/恢复"开关已开启，启动时从本地文件恢复设置、教程进度与搜索历史（一次性，非循环）
            // 采用"只补齐缺失项"的方式，避免把用户后来修改过的设置还原成备份文件里的旧值
            if (SharedPreferencesUtil.getBoolean("backup_restore_enable", false)) {
                CenterThreadPool.run(() -> BackupUtil.autoRestore());
            }

            // 确保 /Documents/BiliClient/login.txt 存在（即使未登录也创建），供登录信息读取/加载
            try {
                FileUtil.getLoginFile();
            } catch (Exception ignored) {
            }
        }
    }

    public static void setInstance(InstanceActivity instanceActivity) {
        instance = new WeakReference<>(instanceActivity);
    }

    @Nullable
    public static InstanceActivity getInstanceActivityOnTop() {
        return instance.get();
    }

    /**
     * 重写attachBaseContext方法，用于调整应用内dpi
     * 尝试下这种风格代码是否会导致低版本设备异常
     *
     * @param old The origin context.
     */
    public static Context getFitDisplayContext(Context old) {
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0F);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return old;
        if (!DPI_FORCE_CHANGE && dpiTimes == 1.0F) return old;
        try {
            DisplayMetrics displayMetrics = old.getResources().getDisplayMetrics();
            Configuration configuration = old.getResources().getConfiguration();
            configuration.densityDpi = (int) (displayMetrics.densityDpi * dpiTimes);
            return old.createConfigurationContext(configuration);
        } catch (Exception e) {
            //MsgUtil.err(e,old);
            return old;
        }
    }

    public static int getVersion() throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    }

    public static boolean isDebugBuild() {
        return "debug".equals(BuildConfig.BUILD_TYPE);
    }

    public static void jumpToVideo(Context context, long aid) {
        TerminalContext.getInstance().enterVideoDetailPage(context, aid);
    }

    public static void jumpToVideo(Context context, String bvid) {
        TerminalContext.getInstance().enterVideoDetailPage(context, bvid);
    }

    public static void jumpToArticle(Context context, long cvid) {
        TerminalContext.getInstance().enterArticleDetailPage(context, cvid);
    }

    public static void jumpToUser(Context context, long mid) {
        Intent intent = new Intent();
        intent.setClass(context, UserInfoActivity.class);
        intent.putExtra("mid", mid);
        context.startActivity(intent);
    }

}
