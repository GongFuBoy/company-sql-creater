package com.github.gongfuboy.company.sql.creater.select;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by ZhouLiMing on 2018/6/15.
 */
public class FileUtils {

    /**
     * 将指定字符串写入文件
     * @param filePath
     * @param sourceString
     */
    public static void writeFile(String filePath, String sourceString) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            IOUtils.write(sourceString, new FileOutputStream(file), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
