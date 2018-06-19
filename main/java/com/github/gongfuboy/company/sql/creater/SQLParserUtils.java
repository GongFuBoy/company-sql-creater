package com.github.gongfuboy.company.sql.creater;

import com.github.gongfuboy.company.sql.creater.pojo.TableFieldBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ZhouLiMing on 2018/6/19.
 */
public class SQLParserUtils {

    private static String AS = " as ";
    private static String LEFT_BRACKET = "(";
    private static String BLANK = " ";
    private static String RIGHT_BRACKET = ")";


    public static void parseResultFieldMap(String sql, Map<String, List<TableFieldBean>> resultFieldMap) {
        int startIndex = StringUtils.indexOfIgnoreCase(sql, "select");
        int endIndex = StringUtils.indexOfIgnoreCase(sql, "from");
        String sourceResultSelect = StringUtils.substring(sql, startIndex + "select".length(), endIndex).trim();
        String[] results = StringUtils.splitByWholeSeparator(sourceResultSelect, ",");
        for (String temp : results) {
            if (isSimpleField(temp)) {
                parseResultSimpleField(temp, null, resultFieldMap);
            } else {
                parseResultComplexField(temp, resultFieldMap);
            }
        }
    }

    private static boolean isSimpleField(String sourceString) {
        return !StringUtils.containsIgnoreCase(sourceString, AS);
    }

    private static void parseResultSimpleField(String sourceString, String otherName, Map<String, List<TableFieldBean>> resultFieldMap) {
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
    private static void parseResultComplexField(String sourceString, Map<String, List<TableFieldBean>> resultFieldMap) {
        int indexAs = StringUtils.indexOfIgnoreCase(sourceString, AS);
        String otherName = StringUtils.substring(sourceString, indexAs + AS.length());
        String remainResultString = StringUtils.remove(sourceString, StringUtils.substring(sourceString, indexAs));
        if (StringUtils.contains(remainResultString, LEFT_BRACKET)) {
            String dealedString = StringUtils.remove(StringUtils.remove(StringUtils.substringAfter(remainResultString, LEFT_BRACKET), LEFT_BRACKET), RIGHT_BRACKET);
            parseResultSimpleField(StringUtils.substring(dealedString, 0, StringUtils.indexOfIgnoreCase(dealedString, BLANK)), otherName, resultFieldMap);
        } else {
            parseResultSimpleField(remainResultString, otherName, resultFieldMap);
        }
    }

}
