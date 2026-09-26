package com.RobinNotBad.BiliClient.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.RobinNotBad.BiliClient.BiliTerminal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

//用于清除缓存，因为glide实际上会往本地存不少缩略图，时间一长就会爆炸
//清除调用我放在了每次刷新推荐页

public class FileUtil {
    public static void clearCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists() && Objects.requireNonNull(cacheDir.listFiles()).length != 0)
            deleteFolder(cacheDir);
        Log.e("debug", "清除了缓存");
    }

    public static void deleteFolder(File folder) {
        if (!folder.exists()) return;

        if (folder.isFile()) {
            folder.delete();
            return;
        }

        File[] templist = folder.listFiles();
        assert templist != null;
        for (File file : templist) {
            if (file.isFile()) {   //如果该项是文件，直接删除
                file.delete();
            } else {    //如果该项是目录
                if (Objects.requireNonNull(file.listFiles()).length != 0)
                    deleteFolder(file);    //如果子文件夹不是空的，继续扫描下去，实现套娃效果
            }
            Log.e("debug", file.toString());
        }
        folder.delete();
    }

    public static String readString(File file) {
        if (file == null || !file.exists() || !file.canRead() || !file.isFile()) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FileInputStream inputStream = new FileInputStream(file);
            FileChannel channel = inputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1 << 13);
            int i;
            while ((i = channel.read(buffer)) != -1) {
                buffer.flip();
                outputStream.write(buffer.array(), 0, i);
                buffer.clear();
            }
            return outputStream.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkStoragePermission() {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < 17) return true;
        Context context = BiliTerminal.context;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
    }

    public static File getVideoDownloadPath() {
        String saved = SharedPreferencesUtil.getString("save_path_video", "");
        // 未设置或为空时，默认使用 /storage/emulated/0/Download
        if (saved == null || saved.isEmpty()) {
            saved = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        }
        File path = new File(saved);

        try {
            File nomedia = new File(path, ".nomedia");
            if (SharedPreferencesUtil.getBoolean("save_ban_gallery", true) && !nomedia.exists())
                nomedia.createNewFile();
            else if (nomedia.exists()) nomedia.delete();
        } catch (Exception ignored) {
        }
        return path;
    }

    public static File getVideoDownloadPath(String title, String child) {
        File parentFolder = new File(getVideoDownloadPath(), stringToFile(title));
        if (child == null || child.isEmpty()) return parentFolder;
        return new File(parentFolder, stringToFile(child));
    }

    public static File getPicturePath() {
        String saved = SharedPreferencesUtil.getString("save_path_pictures", "");
        // 未设置或为空时，默认使用 /storage/emulated/0/Pictures
        if (saved == null || saved.isEmpty()) {
            saved = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        }
        return new File(saved);
    }

    public static File getDownloadPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static void requireTFCardPermission() {

    }

    public static String stringToFile(String str) {
        return str.substring(0, Math.min(85, str.length()))    //防止长度溢出
                .replace("|", "｜")
                .replace(":", "：")
                .replace("*", "﹡")
                .replace("?", "？")
                .replace("\"", "”")
                .replace("<", "＜")
                .replace(">", "＞")
                .replace("/", "／")
                .replace("\\", "＼");    //文件名里不能包含非法字符
    }

    public static String getFileNameFromLink(String link) {
        int length = link.length();
        for (int i = length - 1; i > 0; i--) {
            if (link.charAt(i) == '/') {
                return link.substring(i + 1);
            }
        }
        return "fail";
    }

    /**
     * 获取 /Documents/BiliClient 目录，不存在则创建
     */
    public static File getBiliClientDir() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "BiliClient");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /**
     * 获取测试版登录信息文件：/Documents/BiliClient/LoginData.txt
     * 仅测试版(betaVer>0)使用，用于在更新版本后保留登录状态。
     */
    public static File getLoginDataFile() {
        return new File(getBiliClientDir(), "LoginData.txt");
    }

    /** 获取设置备份文件：/Documents/BiliClient/setting.txt */
    public static File getSettingFile() {
        return new File(getBiliClientDir(), "setting.txt");
    }

    /** 获取教程进度备份文件：/Documents/BiliClient/guide.txt */
    public static File getGuideFile() {
        return new File(getBiliClientDir(), "guide.txt");
    }

    /** 获取搜索历史备份文件：/Documents/BiliClient/SearchRecords.txt */
    public static File getSearchRecordsFile() {
        return new File(getBiliClientDir(), "SearchRecords.txt");
    }

    /** 获取登录信息文件：/Documents/BiliClient/login.txt（即使未登录也会创建） */
    public static File getLoginFile() {
        File file = new File(getBiliClientDir(), "login.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception ignored) {
            }
        }
        return file;
    }

    /** 保存登录信息(JSON字符串)到 login.txt，返回是否成功 */
    public static boolean saveLoginFile(String json) {
        return writeString(getLoginFile(), json);
    }

    /** 从 login.txt 读取登录信息(JSON字符串)，失败/为空返回 null */
    public static String loadLoginFile() {
        String content = readString(getLoginFile());
        return (content == null || content.isEmpty()) ? null : content;
    }

    /** 通用写文件：把字符串写入指定文件，返回是否成功 */
    public static boolean writeString(File file, String content) {
        if (file == null) return false;
        try {
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(content);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 保存登录信息(JSON字符串)到 LoginData.txt，返回是否成功 */
    public static boolean saveLoginData(String json) {
        return writeString(getLoginDataFile(), json);
    }

    /** 从 LoginData.txt 读取登录信息(JSON字符串)，失败返回 null */
    public static String loadLoginData() {
        return readString(getLoginDataFile());
    }

    /**
     * 检测 /Documents/BiliClient 内是否存在可用的备份文件（setting.txt / guide.txt / SearchRecords.txt 非空）
     */
    public static boolean hasBackupFiles() {
        String setting = readString(getSettingFile());
        if (setting != null && !setting.isEmpty()) return true;
        String guide = readString(getGuideFile());
        if (guide != null && !guide.isEmpty()) return true;
        String search = readString(getSearchRecordsFile());
        return search != null && !search.isEmpty();
    }

    public static String getFileFirstName(String file) {
        for (int i = 0; i < file.length(); i++) {
            if (file.charAt(i) == '.') {
                return file.substring(0, i);
            }
        }
        return "fail";
    }
}
