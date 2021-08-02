package cn.shu.wechat.swing.utils;

import cn.shu.wechat.swing.app.Launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by 舒新胜 on 2017/6/11.
 */
public class FileCache {

    public String FILE_CACHE_ROOT_PATH;
    private DecimalFormat decimalFormat = new DecimalFormat("#.0");


    public FileCache() {
        try {
            //FILE_CACHE_ROOT_PATH = getClass().getResource("/cache").getPath() + "/file";
            FILE_CACHE_ROOT_PATH = Launcher.appFilesBasePath + "/cache/file";
            File file = new File(FILE_CACHE_ROOT_PATH);
            if (!file.exists()) {
                file.mkdirs();
                System.out.println("创建文件缓存目录：" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            FILE_CACHE_ROOT_PATH = "./";
        }
    }

    public String tryGetFileCache(String identify, String name) {
        File cacheFile = new File(FILE_CACHE_ROOT_PATH + "/" + identify + "_" + name);
        if (cacheFile.exists()) {
            return cacheFile.getAbsolutePath();
        }

        return null;
    }

    public String cacheFile(String identify, String name, byte[] data) {
        if (data == null || data.length < 1) {
            return null;
        }

        File cacheFile = new File(FILE_CACHE_ROOT_PATH + "/" + identify + "_" + name);
        try {
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            outputStream.write(data);
            outputStream.close();
            return cacheFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String fileSizeString(String path) {
        File file = new File(path);
        if (file == null) {
            return null;
        }

        long size = file.length();
        String retString = "";
        if (size < 1024) {
            retString = size + " 字节";
        } else if (size < 1024 * 1024) {
            retString = decimalFormat.format(size * 1.0F / 1024) + " KB";
        } else {
            retString = decimalFormat.format(size * 1.0F / 1024 / 1024) + " MB";
        }

        return retString;
    }


}