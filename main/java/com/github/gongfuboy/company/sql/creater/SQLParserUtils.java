package com.github.gongfuboy.company.sql.creater;

import com.github.gongfuboy.company.sql.creater.pojo.TableFieldBean;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import org.apache.commons.lang.StringUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ZhouLiMing on 2018/6/19.
 */
public class SQLParserUtils {

    private static String[] SQL_OPERATOR = {" = ", " != ", " >= ", " > ", " <= ", " < ", " is " , " in ", " not in "};

    private static String WHERE = " where ";
    private static String ORDER_BY = " order by ";
    private static String GROUP_BY = " group by ";
    private static String AND = " and ";
    private static String OR = " or ";


    private static String AS = " as ";
    private static String LEFT_BRACKET = "(";
    private static String BLANK = " ";
    private static String RIGHT_BRACKET = ")";

    /**
     * 解析sql中的结果集
     * @param sql 目标sql
     * @param resultFieldMap 解析后的结果集
     */
    public static void parseResultByUtils(String sql, Map<String, List<TableFieldBean>> resultFieldMap) {
        try {
            CCJSqlParserManager parserManager = new CCJSqlParserManager();
            Select parse = (Select) parserManager.parse(new StringReader(sql));
            PlainSelect selectBody = (PlainSelect) parse.getSelectBody();
            selectBody.getSelectItems().forEach(x -> {
                SelectExpressionItem selectExpressionItem = x instanceof SelectExpressionItem ? ((SelectExpressionItem) x) : new SelectExpressionItem();
                if (isSimpleField(selectExpressionItem.toString())) {
                    parseResultSimpleField(selectExpressionItem.toString(), null, resultFieldMap);
                } else {
                    parseResultComplexField(selectExpressionItem.toString(), resultFieldMap);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public static void main(String[] args) {
//        parseConditionSQL(new ArrayList<String>(), "1=1 and id = 1 or name != null and class_name = 'any_class_name'");
//        System.out.println(StringUtils.countMatches("database.table.name", "[a, z]+.[a, z]+.[a, z]+"));
//        String result = switch("1") {
//            case "1" : return "1";
//        }


    }

    /**
     * 解析sql中的条件集
     * @param sql
     * @param conditionFielfMap
     */
    public static void parseConditionFieldByUtils(String sql, Map<String, List<TableFieldBean>> conditionFielfMap) {
        try {
            CCJSqlParserManager parserManager = new CCJSqlParserManager();
            Select parse = (Select) parserManager.parse(new StringReader(sql));
            PlainSelect selectBody = (PlainSelect) parse.getSelectBody();
            Expression where = selectBody.getWhere();
            List<Expression> expressions = parseExpressionToSimple(where);
            parseExpressionsToMap(expressions, conditionFielfMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseExpressionsToMap(List<Expression> expressions, Map<String, List<TableFieldBean>> conditionFielfMap) {
        for (Expression expression : expressions) {
            String column;
            if (expression instanceof BinaryExpression) {
                column = ((BinaryExpression) expression).getLeftExpression().toString();
            } else if (expression instanceof InExpression) {
                column = ((InExpression) expression).getLeftExpression().toString();
            } else {
                throw new RuntimeException("暂不支持目前这种条件解析：" + expression.toString());
            }
            String[] strings = StringUtils.split(column, ".");
            TableFieldBean bean = new TableFieldBean();
            bean.tableName = strings[1];
            bean.fieldName = strings[2];
            if (conditionFielfMap.containsKey(strings[0])) {
                conditionFielfMap.get(strings[0]).add(bean);
            } else {
                List<TableFieldBean> tempList = new ArrayList<>();
                tempList.add(bean);
                conditionFielfMap.put(strings[0], tempList);
            }
        }
    }

    /**
     * 解析expression成为一个个简单expression
     * @param expression 复杂expression
     * @return 解析之后的简单expression集合
     */
    public static List<Expression> parseExpressionToSimple(Expression expression) {
        List<Expression> result = new ArrayList<>();
        if (expression instanceof BinaryExpression) {

            /**
             * 获取左表达式，进进行处理
             */
            Expression leftExpression = ((BinaryExpression) expression).getLeftExpression();
            if (leftExpression instanceof Column) {
                result.add(expression);
            } else if (leftExpression instanceof BinaryExpression) {
                result.addAll(parseExpressionToSimple(leftExpression));
            }

            /**
             * 获取右表达式
             */
            Expression rightExpression = ((BinaryExpression) expression).getRightExpression();
            if (rightExpression instanceof BinaryExpression) {
                Expression leftExpression1 = ((BinaryExpression) rightExpression).getLeftExpression();
                if (leftExpression1 instanceof Column) {
                    result.add(rightExpression);
                }
            } else if (rightExpression instanceof InExpression) {
                Expression leftExpression1 = ((InExpression) rightExpression).getLeftExpression();
                if (leftExpression1 instanceof Column) {
                    result.add(rightExpression);
                }
            }
        }
        return result;
    }

}
