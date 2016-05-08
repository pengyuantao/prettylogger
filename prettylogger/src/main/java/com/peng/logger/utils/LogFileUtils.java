package com.peng.logger.utils;

import java.io.File;

/**
 * 用来处理Log文件的工具类
 * Created by administrator on 16/5/1.
 */
public class LogFileUtils {

    /**
     * 根据路径创建文件
     * @param filePath
     * @return
     */
    public static File creatFileByPath(String filePath) {
        //创建一个文件对象
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

}
