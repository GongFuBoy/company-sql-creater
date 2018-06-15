package com.github.gongfuboy.company.sql.creater;

/**
 * Created by ZhouLiMing on 2018/6/6.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String path = Main.class.getClassLoader().getResource("reverse.conf").getPath();
        CodeCreateBySQL.create(path);
    }
}
