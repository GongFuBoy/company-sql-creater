package com.github.gongfuboy.company.sql.creater;


import com.github.gongfuboy.company.sql.creater.pojo.DatabaseInfoBean;
import com.github.gongfuboy.company.sql.creater.pojo.TableFieldBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CodeCreateBySQL {

    private static Properties properties;

    private static String dataBaseDriver;
    private static String url;
    private static String username;
    private static String password;
    private static String db;
    private static String sql;
    private static String scalaPackage;
    private static String scalaDomainName;
    private static String thrift_package;
    private static String thrift_response_struct_name;
    private static String thrift_request_struct_name;


    private static String filePath;

    private static String[] databases;

    /**<database, connection>*/
    private static Map<String, Connection> connectionMap = new HashMap<String, Connection>();

    /**<database, <tableName
     *  , fieldName>>*/
    private static Map<String, List<TableFieldBean>> resultFieldMap = new HashMap<String, List<TableFieldBean>>();

    private static Map<String, List<TableFieldBean>> conditionFielfMap = new HashMap<String, List<TableFieldBean>>();

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
        SQLParserUtils.parseResultByUtils(sql, resultFieldMap);
        List<DatabaseInfoBean> responseDatabaseInfoList = DbUtils.getDatabaseColumnInfo(connectionMap, resultFieldMap);

        System.out.println("=================scala-domain生成开始====================");
        String scalaClassFileSource = DbGeneratorUtil.toDbClassTemplate(scalaDomainName, scalaPackage, responseDatabaseInfoList);
        String scalaTargetFilePath = filePath + scalaDomainName + ".scala";
        FileUtils.writeFile(scalaTargetFilePath,scalaClassFileSource);
        System.out.println("=================scala-domain生成结束====================");

        System.out.println("=================thrift-response生成开始====================");
        String responseThriftClassFileSource = ThriftFileCreatorUtils.createStructThriftFileSource(responseDatabaseInfoList, thrift_package, thrift_response_struct_name);
        String responseThriftTargetFilePath = filePath + thrift_response_struct_name + ".thrift";
        FileUtils.writeFile(responseThriftTargetFilePath, responseThriftClassFileSource);
        System.out.println("=================thrift-response生成结束====================");

        /**解析条件集*/
        SQLParserUtils.parseConditionFieldByUtils(sql, conditionFielfMap);
        List<DatabaseInfoBean> requestDatabaseInfoList = DbUtils.getDatabaseColumnInfo(connectionMap, conditionFielfMap);

        System.out.println("=================thrift-request生成开始====================");
        String requestThriftClassFileSource = ThriftFileCreatorUtils.createStructThriftFileSource(requestDatabaseInfoList, thrift_package, thrift_request_struct_name);
        String requestThriftTargetFilePath = filePath + thrift_request_struct_name + ".thrift";
        FileUtils.writeFile(requestThriftTargetFilePath, requestThriftClassFileSource);
        System.out.println("=================thrift-request生成结束====================");

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

            thrift_package = properties.getProperty("thrift_package");
            thrift_response_struct_name = properties.getProperty("thrift_response_struct_name");
            thrift_request_struct_name = properties.getProperty("thrift_request_struct_name");

        } catch (Exception e) {
            e.printStackTrace();
        }
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