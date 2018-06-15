package com.github.gongfuboy.company.sql.creater;


import com.github.gongfuboy.company.sql.creater.pojo.DatabaseInfoBean;
import com.github.gongfuboy.company.sql.creater.pojo.TableFieldBean;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class CodeCreateBySQL {

    /**静态关键字*/
    private static String AS = " as ";
    private static String LEFT_BRACKET = "(";
    private static String BLANK = " ";
    private static String RIGHT_BRACKET = ")";


    private static Properties properties;

    private static String dataBaseDriver;
    private static String url;
    private static String username;
    private static String password;
    private static String db;
    private static String sql;
    private static String scalaPackage;
    private static String scalaDomainName;

    private static String filePath;

    private static String[] databases;

    /**<database, connection>*/
    private static Map<String, Connection> connectionMap = new HashMap<String, Connection>();

    /**<database, <tableName
     *  , fieldName>>*/
    private static Map<String, List<TableFieldBean>> resultFieldMap = new HashMap<String, List<TableFieldBean>>();

    /**
     * 生成相应的代码
     * @param args
     */
    public static void create(String... args) {
        if(!checkArg(args))
            return;

        readConf(args[0]);
        JdbcUtils.setConnection();
        /**解析结果集*/
        parseResultFieldMap(sql);

        List<DatabaseInfoBean> resourceList = DbUtils.getDatabaseColumnInfo(connectionMap, resultFieldMap);
        String scalaClassFileSource = DbGeneratorUtil.toDbClassTemplate(scalaDomainName, scalaPackage, resourceList);

        String targetFilePath = filePath + scalaDomainName + ".scala";

        DbGeneratorUtil.generateEntityFile(scalaClassFileSource, targetFilePath, scalaDomainName);
        System.out.println("=================scala-domain生成结束====================");

    }

    private static void parseResultFieldMap(String sql) {
        int startIndex = StringUtils.indexOfIgnoreCase(sql, "select");
        int endIndex = StringUtils.indexOfIgnoreCase(sql, "from");
        String sourceResultSelect = StringUtils.substring(sql, startIndex + "select".length(), endIndex).trim();
        String[] results = StringUtils.splitByWholeSeparator(sourceResultSelect, ",");
        for (String temp : results) {
            if (isSimpleField(temp)) {
                parseResultSimpleField(temp, null);
            } else {
                parseResultComplexField(temp);
            }
        }
    }

    private static boolean isSimpleField(String sourceString) {
        return !StringUtils.containsIgnoreCase(sourceString, AS);
    }

    private static void parseResultSimpleField(String sourceString, String otherName) {
        String[] sourceStringParam = StringUtils.splitByWholeSeparator(sourceString, ".");
        TableFieldBean bean = new TableFieldBean();
        bean.otherName = otherName;
        bean.fieldName = sourceStringParam[2];
        bean.tableName = sourceStringParam[1];
        if (resultFieldMap.containsKey(sourceStringParam[0])) {
            resultFieldMap.get(sourceStringParam[0]).add(bean);
        } else {
            List<TableFieldBean> tempList = new ArrayList<TableFieldBean>();
            tempList.add(bean);
            resultFieldMap.put(sourceStringParam[0] , tempList);
        }
    }

    /**
     * 复杂result进行处理
     * @param sourceString
     */
    private static void parseResultComplexField(String sourceString) {
        int indexAs = StringUtils.indexOfIgnoreCase(sourceString, AS);
        String otherName = StringUtils.substring(sourceString, indexAs + AS.length());
        String remainResultString = StringUtils.remove(sourceString, StringUtils.substring(sourceString, indexAs));
        if (StringUtils.contains(remainResultString, LEFT_BRACKET)) {
            String dealedString = StringUtils.remove(StringUtils.remove(StringUtils.substringAfter(remainResultString, LEFT_BRACKET), LEFT_BRACKET), RIGHT_BRACKET);
            parseResultSimpleField(StringUtils.substring(dealedString, 0, StringUtils.indexOfIgnoreCase(dealedString, BLANK)), otherName);
        } else {
            parseResultSimpleField(remainResultString, otherName);
        }
    }


    private static boolean checkArg(String[] args) {
        return true;
    }

    /**
     * 读取配置
     * @param file
     */
    public static void readConf(String file){
        try {
            InputStream input = new FileInputStream(new File(file));
            properties = new Properties();
            properties.load(input);
            dataBaseDriver = properties.getProperty("dataBaseDriver");
            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            db = properties.getProperty("db");
            sql = properties.getProperty("sql");
            databases = db.split(",");
            scalaPackage = properties.getProperty("scala_package");
            scalaDomainName = properties.getProperty("scala_domain_name");
            filePath = properties.getProperty("filePath");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test(String file) throws Exception{
        readConf(file);
        Class.forName(dataBaseDriver);
        String databaseConnectUrl = url;
        databaseConnectUrl = databaseConnectUrl.replace("@module", db);
        Connection connection = DriverManager.getConnection(databaseConnectUrl, username, password);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
    }

    /**
     * JdbcUtils
     */
    private static class JdbcUtils {
        public static void setConnection() {
            try {
                Class.forName(dataBaseDriver);
                String databaseConnectUrl = url;
                connectionMap = new HashMap<String, Connection>();
                for (String database : databases) {
                    databaseConnectUrl = databaseConnectUrl.replace("@module", database);
                    Connection connection = DriverManager.getConnection(databaseConnectUrl, username, password);
                    connectionMap.put(database, connection);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}