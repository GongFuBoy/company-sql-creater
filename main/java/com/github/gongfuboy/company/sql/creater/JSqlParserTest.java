package com.github.gongfuboy.company.sql.creater;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.StringReader;
import java.util.List;

/**
 * Created by ZhouLiMing on 2018/6/21.
 */
public class JSqlParserTest {

    public static void main(String[] args) throws JSQLParserException {
//        insertParseExamples();
//        selectTableParseExamples();
        whereConditionParseExamples();
    }


    private static void insertParseExamples() throws JSQLParserException {
        Insert insert = (Insert) CCJSqlParserUtil.parse("insert into mytable (col1) values (1)");
        System.out.println(insert.toString());

//adding a column
        insert.getColumns().add(new Column("col2"));

//adding a value using a visitor
        insert.getItemsList().accept(new ItemsListVisitor() {

            public void visit(SubSelect subSelect) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void visit(ExpressionList expressionList) {
                expressionList.getExpressions().add(new LongValue(5));
            }

            public void visit(MultiExpressionList multiExprList) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        System.out.println(insert.toString());

//adding another column
        insert.getColumns().add(new Column("col3"));

//adding another value (the easy way)
        ((ExpressionList) insert.getItemsList()).getExpressions().add(new LongValue(10));

        System.out.println(insert.toString());
    }

    private static void selectTableParseExamples() throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM tab1");
        Select select = (Select) stmt;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);

        System.out.println(tableList);
    }

    private static void whereConditionParseExamples() throws JSQLParserException {
        String sql = "select DISTINCT my_personal_database.class.id from my_personal_database.class c left join company c1 on c.id = c1.company_id inner join (select * from customer where age > 10) as t" +
                " on c1.customer_id = t.id " +
                "where 1=1 and name not in (select name from company where create_time > 5) and id = 1 and id = 2 and id = 3 and id = 4 ";
//        String sql = "select sum(id + count) from class";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        Expression where_expression = plain.getWhere();

        List<Expression> expressions = SQLParserUtils.parseExpressionToSimple(where_expression);
        System.out.println(expressions);


//        FromItem fromItem = plain.getFromItem();
//        List<Join> joins = plain.getJoins();
//        joins.get(1).getRightItem().accept(new FromItemVisitorAdapter());
//        BinaryExpression be = (BinaryExpression) where_expression;
//        System.out.println(be.getStringExpression());
//        System.out.println(where_expression.getClass());
//        String str = where_expression.toString();
//        System.out.println(str);
    }

}
