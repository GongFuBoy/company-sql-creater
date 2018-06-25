package com.github.gongfuboy.company.sql.creater.select;

import com.github.gongfuboy.utils.DateUtils;

import java.util.Date;

/**
 * Created by ZhouLiMing on 2018/6/6.
 */
public class Main {

    public static void main(String[] args) throws Exception {
//        String path = Main.class.getClassLoader().getResource("reverse.conf").getPath();
//        CodeCreateBySQL.create(path);
        long time = DateUtils.getFirstDateOfYear().getTime();
        System.out.println(time);
        System.out.println(DateUtils.formatDate(DateUtils.format1, new Date(time)));
    }
}
