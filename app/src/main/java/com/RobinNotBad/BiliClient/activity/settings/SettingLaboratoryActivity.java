package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.SettingsAdapter;
import com.RobinNotBad.BiliClient.model.SettingSection;
import com.RobinNotBad.BiliClient.util.BackupUtil;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SettingLaboratoryActivity extends RefreshListActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("实验室");

        boolean debugBuild = BiliTerminal.isDebugBuild();

        final List<SettingSection> sectionList = new ArrayList<>() {
            {
                add(new SettingSection("title", "可用性", "", "", ""));
                add(new SettingSection("switch", "新版弹幕获取方式", "new_danmaku_api",
                        getString(R.string.desc_new_danmaku_api), "true"));
                add(new SettingSection("switch", "私信未读标记", SharedPreferencesUtil.PRIVATE_MSG_UNREAD_BADGE_ENABLE,
                        getString(R.string.desc_private_msg_unread_badge_enable), "false"));

                add(new SettingSection("title", "下载", "", "", ""));
                add(new SettingSection("switch", "使用旧版下载器", "dev_download_old",
                        getString(R.string.setting_lab_download_old), "false"));
                add(new SettingSection("input_string", "缓存路径", "save_path_video",
                        getString(R.string.setting_lab_path_video), FileUtil.getVideoDownloadPath().toString()));
                add(new SettingSection("input_string", "图片下载路径", "save_path_pictures",
                        getString(R.string.setting_lab_path_pictures), FileUtil.getPicturePath().toString()));

                add(new SettingSection("title", "UI", "", "", ""));
                add(new SettingSection("switch", "横屏模式", "ui_landscape", getString(R.string.setting_lab_ui_landscape),
                        "false"));
                add(new SettingSection("input_string", "开屏文字", "ui_splashtext",
                        getString(R.string.setting_lab_splashtext), "欢迎使用\n哔哩终端"));
                add(new SettingSection("switch", "文字跑马灯", "marquee_enable", getString(R.string.setting_lab_marquee),
                        "true"));

                add(new SettingSection("title", "播放器", "", "", ""));
                add(new SettingSection("switch", "播放器旋屏兼容方案", "dev_player_rotate_software",
                        "在极少数手表上（如小米手表），系统旋屏存在显示不全的问题。打开此开关，播放器将会使用软件旋屏方法。", "false"));
                add(new SettingSection("switch", "显示视频分段", "player_show_viewpoints",
                        "显示视频的章节看点信息，可快速跳转到指定章节", "false"));
                add(new SettingSection("switch", "系统媒体控件", SharedPreferencesUtil.PLAYER_MEDIA_SESSION_ENABLE,
                        getString(R.string.setting_lab_media_session), "false"));
                add(new SettingSection("switch", "互动视频调试", "player_interaction_debug",
                        "在互动视频播放时，在左侧倍速按钮上方显示调试按钮，可以查看和修改互动视频的变量", "false"));

                add(new SettingSection("title", "数据备份", "", "", ""));
                add(new SettingSection("switch", "备份/恢复", "backup_restore_enable",
                        "开启后，每次启动应用时会自动读取 /Documents/BiliClient 下的备份文件，恢复设置、教程进度与搜索历史。\n注意：此开关只负责“自动恢复”，不会自动备份；需要备份请点下面的「备份」按钮。恢复会覆盖当前设置，部分设置需重启应用后生效。", "false"));
                add(new SettingSection("button", "备份", "backup_do",
                        "将当前所有设置、教程进度备份到 setting.txt / guide.txt，搜索历史备份到 SearchRecords.txt", ""));
                add(new SettingSection("button", "加载", "backup_load",
                        "从 setting.txt / guide.txt / SearchRecords.txt 恢复设置、教程进度与搜索历史（部分设置重启应用后生效）", ""));
                add(new SettingSection("button", "备份登录信息", "login_read",
                        "将当前登录信息备份到 /Documents/BiliClient/login.txt，便于迁移登录状态", ""));
                add(new SettingSection("button", "读取登录信息", "login_load",
                        "从 /Documents/BiliClient/login.txt 恢复登录信息（与特殊登录数据格式一致）", ""));

                add(new SettingSection("title", "网络请求", "", "", ""));
                add(new SettingSection("input_float", "接口重试间隔（秒）", SharedPreferencesUtil.API_RETRY_INTERVAL_SECONDS,
                        "网络请求失败后的重试间隔（秒），填 0 表示立即重试", "0.1"));
                add(new SettingSection("input_int", "接口重试次数", SharedPreferencesUtil.API_RETRY_MAX_TIMES,
                        "网络请求失败后的最大重试次数，填 0 表示不重试（默认 5）", "5"));

                add(new SettingSection("title", "调试", "", "", ""));
                add(new SettingSection("switch", "允许Logu.v", "dev_logv", getString(R.string.setting_lab_logv),
                        String.valueOf(debugBuild)));
                add(new SettingSection("switch", "允许Logu.d", "dev_logd", "", String.valueOf(debugBuild)));
                add(new SettingSection("switch", "允许Logu.i", "dev_logi", "", String.valueOf(debugBuild)));
                add(new SettingSection("switch", "详细显示数据解析报错", "dev_jsonerr_detailed",
                        getString(R.string.setting_lab_jsonerr_detailed), String.valueOf(debugBuild)));
                add(new SettingSection("switch", "详细显示列表报错", "dev_recyclererr_detailed",
                        getString(R.string.setting_lab_recyclererr_detailed), String.valueOf(debugBuild)));
            }
        };

        // "备份" / "加载" / 登录读取 / 登录加载 按钮回调
        for (SettingSection section : sectionList) {
            if ("backup_do".equals(section.id)) {
                section.extra = (Runnable) () -> {
                    boolean ok = BackupUtil.backupOnly();
                    MsgUtil.showMsgLong(ok
                            ? "备份成功：设置→setting.txt，教程→guide.txt，搜索历史→SearchRecords.txt"
                            : "备份失败，请检查存储权限");
                };
            } else if ("backup_load".equals(section.id)) {
                section.extra = (Runnable) () -> {
                    boolean ok = BackupUtil.restoreOnly();
                    if (!ok) {
                        MsgUtil.showMsgLong("加载失败：文件不存在或格式错误");
                        return;
                    }
                    // 恢复只是把值写进 SharedPreferences，很多设置（Dpi、边距、播放器、界面等）
                    // 在本次进程启动时就已经读进内存了，必须重启应用才会生效。
                    // 以前这里只提示"加载成功"，用户会误以为功能失效，只好重装应用。
                    showRestartDialog();
                };
            } else if ("login_read".equals(section.id)) {
                section.extra = (Runnable) () -> {
                    // 备份当前登录信息并写入 login.txt（与特殊登录数据格式一致）
                    try {
                        JSONObject json = new JSONObject();
                        json.put("cookies", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
                        json.put("refresh_token", SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""));
                        boolean ok = FileUtil.saveLoginFile(json.toString());
                        MsgUtil.showMsgLong(ok ? "已备份当前登录信息到 login.txt" : "写入 login.txt 失败，请检查存储权限");
                    } catch (JSONException ignored) {
                        MsgUtil.showMsgLong("备份登录信息失败");
                    }
                };
            } else if ("login_load".equals(section.id)) {
                section.extra = (Runnable) () -> {
                    // 从 login.txt 读取并恢复登录信息（与特殊登录逻辑一致）
                    String content = FileUtil.loadLoginFile();
                    if (content == null) {
                        MsgUtil.showMsgLong("login.txt 不存在或为空，请先点击“备份登录信息”");
                        return;
                    }
                    try {
                        JSONObject json = parseLoginContent(content); // 兼容旧格式的解析
                        String cookies = json.getString("cookies");
                        if (cookies == null || cookies.isEmpty()) {
                            MsgUtil.showMsgLong("login.txt 中的登录信息无效");
                            return;
                        }
                        SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies, cookies);
                        SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf,
                                NetWorkUtil.getInfoFromCookie("bili_jct", cookies));
                        if (json.has("refresh_token"))
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token, json.getString("refresh_token"));

                        // DedeUserID 存在则视为已登录用户；缺失（如仅保存了匿名 cookie）则 mid 置 0，不强制报错
                        String dedeId = NetWorkUtil.getInfoFromCookie("DedeUserID", cookies);
                        if (dedeId != null && !dedeId.isEmpty()) {
                            try {
                                SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(dedeId));
                                SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.setup, true);
                                NetWorkUtil.refreshHeaders();
                                MsgUtil.showMsgLong("登录信息已从 login.txt 恢复，重启应用生效");
                            } catch (NumberFormatException e) {
                                MsgUtil.showMsgLong("login.txt 的 DedeUserID 无效");
                            }
                        } else {
                            // cookies 中无 DedeUserID：仅保留 cookies 状态（可能是未登录/匿名 cookie）
                            SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, 0L);
                            NetWorkUtil.refreshHeaders();
                            MsgUtil.showMsgLong("已加载 cookies，但未检测到登录用户（DedeUserID），如需登录请重新登录");
                        }
                    } catch (Exception ignored) {
                        MsgUtil.showMsgLong("login.txt 格式错误或登录信息无效");
                    }
                };
            }
        }

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);
    }

    /**
     * 恢复完成后提示重启应用。
     * 设置项大多只在应用启动时读取一次，不重启的话"加载"看起来就像没生效
     * （以前用户只能通过重装应用来解决，见 issue：安装器更新后数据备份加载失效）。
     */
    private void showRestartDialog() {
        if (isFinishing() || isDestroyed()) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("加载完成")
                .setMessage("已从 setting.txt / guide.txt / SearchRecords.txt 恢复设置、教程进度与搜索历史。\n\n"
                        + "部分设置需要重启应用才会生效，是否立即重启？")
                .setPositiveButton("立即重启", (dialog, which) -> restartApp())
                .setNegativeButton("稍后", (dialog, which) -> MsgUtil.showMsgLong("已加载，重启应用后生效"))
                .setCancelable(true)
                .show();
    }

    /** 重启应用进程，让恢复的设置立刻生效 */
    private void restartApp() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            MsgUtil.showMsgLong("重启失败，请手动退出并重新打开应用");
            return;
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 解析 login.txt 内容为登录 JSON。
     * 优先按当前版本格式（{"cookies":..., "refresh_token":...}）解析；
     * 若失败则兼容旧版本格式（可能是纯 cookies 字符串、或带 # 版本头的旧备份、或 "cookies=...;" 形式）。
     */
    private static JSONObject parseLoginContent(String content) throws JSONException {
        String trimmed = content.trim();

        // 1. 尝试当前版本 JSON 格式
        try {
            JSONObject json = new JSONObject(trimmed);
            if (json.has("cookies")) return json;
        } catch (JSONException ignored) {
            // 非 JSON 格式，继续尝试旧格式
        }

        // 2. 兼容旧格式：去掉可能的 # 注释头行（旧版本可能在文件开头写了版本/日期注释）
        StringBuilder sb = new StringBuilder();
        String[] lines = trimmed.split("\n");
        for (String line : lines) {
            if (!line.trim().startsWith("#")) sb.append(line).append('\n');
        }
        String clean = sb.toString().trim();
        if (!clean.isEmpty()) {
            try {
                JSONObject json = new JSONObject(clean);
                if (json.has("cookies")) return json;
            } catch (JSONException ignored) {
                // 去除注释后仍非 JSON，继续兜底
            }
        }

        // 3. 最后兜底：把整段内容当作纯 cookies 字符串（旧格式 login.txt 可能直接存 cookies）
        if (!clean.isEmpty() && (clean.contains("SESSDATA") || clean.contains("bili_jct") || clean.contains("="))) {
            JSONObject fallback = new JSONObject();
            fallback.put("cookies", clean);
            fallback.put("refresh_token", "");
            return fallback;
        }

        throw new JSONException("无法识别登录信息格式");
    }

}
