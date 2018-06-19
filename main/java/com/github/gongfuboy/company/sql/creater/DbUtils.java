package com.github.gongfuboy.company.sql.creater;

import com.github.gongfuboy.company.sql.creater.pojo.DatabaseInfoBean;
import com.github.gongfuboy.company.sql.creater.pojo.TableFieldBean;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ZhouLiMing on 2018/6/13.
 */
public class DbUtils {

    public static List<DatabaseInfoBean> getDatabaseColumnInfo(Map<String, Connection> connectionMap, Map<String, List<TableFieldBean>> resultFieldMap) {
        List<DatabaseInfoBean> resultList = new ArrayList<DatabaseInfoBean>();
        for (String databaseName : resultFieldMap.keySet()) {
            Connection connection = connectionMap.get(databaseName);
            List<TableFieldBean> fieldBeans = resultFieldMap.get(databaseName);
            for (TableFieldBean temp : fieldBeans) {
                DatabaseInfoBean databaseInfoBean = getColumeInfo(temp.tableName, databaseName, connection, temp.fieldName, temp.otherName);
                resultList.add(databaseInfoBean);
            }
        }
        return resultList;
    }

    public static DatabaseInfoBean getColumeInfo(String tableName, String databaseName, Connection connection, String columnName, String otherName) {
        DatabaseInfoBean result = new DatabaseInfoBean();
        try {
            ResultSet resultSet = connection.getMetaData().getColumns("", databaseName, tableName, columnName);
            while (resultSet.next()) {
                if (resultSet.getString("COLUMN_NAME").equals(columnName)) {
                    result.columnName = otherName == null ? resultSet.getString("COLUMN_NAME") : otherName;
                    result.isNullable = resultSet.getString("IS_NULLABLE");
                    result.remarks = resultSet.getString("REMARKS");
                    result.typeName = resultSet.getString("TYPE_NAME");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


}
