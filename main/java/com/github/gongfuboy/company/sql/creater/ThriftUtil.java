package com.github.gongfuboy.company.sql.creater;

/**
 * Created by ZhouLiMing on 2018/6/15.
 */
public class ThriftUtil {

    public static final char UNDERLINE = '_';

    public static String toThriftType(String type) {
        if (type.equalsIgnoreCase("CHAR")
                || type.equalsIgnoreCase("VARCHAR")
                || type.equalsIgnoreCase("TINYBLOB")
                || type.equalsIgnoreCase("TINYTEXT")
                || type.equalsIgnoreCase("BLOB")
                || type.equalsIgnoreCase("TEXT")
                || type.equalsIgnoreCase("MEDIUMBLOB")
                || type.equalsIgnoreCase("MEDIUMTEXT")
                || type.equalsIgnoreCase("LOGNGBLOB")
                || type.equalsIgnoreCase("LONGTEXT")) {
            return "string";
        }
        if (type.equalsIgnoreCase("DOUBLE")) {
            return "double";
        }
        if (type.equalsIgnoreCase("DECIMAL")) {
            return "double";
        }
        if (type.equalsIgnoreCase("TINYINT")
                || type.equalsIgnoreCase("BIT")
                || type.equalsIgnoreCase("SMALLINT")
                || type.equalsIgnoreCase("MEDIUMINT")
                || type.equalsIgnoreCase("INT")
                || type.equalsIgnoreCase("BIGINT")) {
            return "i32";
        }

        if (type.equalsIgnoreCase("TIME") || type.equalsIgnoreCase("YEAR")) {
            return "string";
        }
        if (type.equalsIgnoreCase("DATE")) {
            return "i64";
        }
        if (type.equalsIgnoreCase("DATETIME")) {
            return "i64";
        }
        if (type.equalsIgnoreCase("Timestamp")) {
            return "i64";
        }
        if (type.equalsIgnoreCase("java.sql.Timestamp")) {
            return "i64";
        }
        if (type.equalsIgnoreCase("ENUM")) {
            return "string";
        }
        if (type.equalsIgnoreCase("LONGBLOB")) {
            return "binary";
        }
        return type;
    }

    public static String underlineToCamel(boolean transferFiled, String param, boolean firstLetterToUpper) {
        if (transferFiled && param.equals("type")) {
            return "`type`";
        }
        if ((param == null) || ("".equals(param.trim()))) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (i == 0) {
                if ((firstLetterToUpper) && (c >= 'a') && (c <= 'z')) {
                    sb.append((char) (c - 32));
                } else if ((!firstLetterToUpper) && (c >= 'A') && (c <= 'Z')) {
                    sb.append((char) (c + 32));
                } else {
                    sb.append(c);
                }
            } else if (c == UNDERLINE) {
                i++;
                if (i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}
