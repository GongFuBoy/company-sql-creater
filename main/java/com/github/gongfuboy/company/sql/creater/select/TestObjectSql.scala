package com.github.gongfuboy.company.sql.creater.select

import java.io.StringReader
import java.lang.reflect.Field

import net.sf.jsqlparser.expression.{BinaryExpression, Expression}
import net.sf.jsqlparser.parser.CCJSqlParserManager
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.select.{PlainSelect, Select}

import scala.collection.mutable.ListBuffer

/**
  * Created by ZhouLiMing on 2018/6/22.
  */
object TestObjectSql {

  private lazy val baseSqlTemple: String = s"select distinct my_personal_database.class.id as temp_id, my_personal_database.class.name, max(my_personal_database.class.age) as maxAge from " +
    s"my_personal_database.class c left join my_personal_database.student s on c.id = s.class_id left join my_personal_database.student_info si on s.id = si.student_id" +
    s" where my_personal_database.class.class_count > ? group by temp_id, maxAge having temp_id > 10"

  private lazy val plantSelect = {
    val parserManager: CCJSqlParserManager = new CCJSqlParserManager
    val select: Select = parserManager.parse(new StringReader(baseSqlTemple)).asInstanceOf[Select]
    select.getSelectBody.asInstanceOf[PlainSelect]
  }

  def main(args: Array[String]): Unit = {
    println(getSelectSql())
    println(getFromSql())
    println(getJoinSql())
    println(getGroupBySql())
    println(getHavingSql())
  }

  /**
    * 获取basic-sql中的select-sql
    *
    * @return
    */
  def getSelectSql(): String = {
    val result = new StringBuilder
    result.append("SELECT ")
    result.append(plantSelect.getDistinct.->[String](if (plantSelect.getDistinct == null) "" else plantSelect.getDistinct.toString)._2).append(" ")
    plantSelect.getSelectItems.forEach(x => result.append(x.toString).append(", "))
    result.delete(result.lastIndexOf(","), result.lastIndexOf(",") + 1).toString()
  }

  /**
    * 获取basic-sql中的from-sql
    *
    * @return
    */
  def getFromSql(): String = {
    " FROM " + plantSelect.getFromItem.toString
  }

  /**
    * 获取basic-sql中的join-sql
    *
    * @return
    */
  def getJoinSql(): String = {
    val result = new StringBuilder
    plantSelect.getJoins.forEach(x => result.append(" ").append(x.toString).append(" "))
    result.toString()
  }

  /**
    * 根据条件中request生成where-sql
    *
    * @return
    */
  def getWhereSql(request: Any): String = {
    val result = new StringBuilder

    val clazz: Class[_] = request.getClass
    val fieldArray: Array[Field] = clazz.getDeclaredFields
    val fieldMap: Array[(String, AnyRef)] = fieldArray.map(x => {
      x.setAccessible(true)
      (x.getName, x.get(request))
    })
    val whereSql = " WHERE " + plantSelect.getWhere
    val expressions: List[Expression] = parseExpressionToSimple(plantSelect.getWhere)
    val expressionMap: List[String] = expressions.map(x => {
      if (x.isInstanceOf[BinaryExpression])
        (x.asInstanceOf[BinaryExpression].getLeftExpression).toString.split(".")(2) + x.getClass.getSimpleName
      else throw new RuntimeException(s"暂不支持: ${x.getClass} 类型解析");
    })

    fieldMap.foreach(x => {
      val expression: Option[String] = expressionMap.find(x1 => ThriftFileCreatorUtils.underlineToCamel(x1).equals(x._1))
      if (expression.isDefined) {
        expression.get.replace("?" , x._2.toString)
      }
    })

    result.toString
  }

  private def parseExpressionToSimple(expression: Expression): List[Expression] = {
    val result = new ListBuffer[Expression]
    if (expression.isInstanceOf[BinaryExpression]) {
      /**
        * 获取左表达式，进进行处理
        */
      val leftExpression: Expression = expression.asInstanceOf[BinaryExpression].getLeftExpression
      if (leftExpression.isInstanceOf[Column]) result.add(expression)
      else if (leftExpression.isInstanceOf[BinaryExpression]) result.addAll(parseExpressionToSimple(leftExpression))
      /**
        * 获取右表达式
        */
      val rightExpression: Expression = expression.asInstanceOf[BinaryExpression].getRightExpression
      if (rightExpression.isInstanceOf[BinaryExpression]) {
        val leftExpression1: Expression = rightExpression.asInstanceOf[BinaryExpression].getLeftExpression
        if (leftExpression1.isInstanceOf[Column]) result.add(rightExpression)
      }
    }
    result.toList
  }

  /**
    * 获取basic-sql中group-by-sql
    * @return
    */
  def getGroupBySql(): String = {
    val result = new StringBuilder
    if (plantSelect.getGroupByColumnReferences != null && !plantSelect.getGroupByColumnReferences.isEmpty)
      result.append(" GROUP BY ").append(plantSelect.getGroupByColumnReferences.get(0).toString)
    result.toString()
  }

  /**
    * 获取basic-sql中having-sql
    * @return
    */
  def getHavingSql(): String = {
    val result = new StringBuilder
    if (plantSelect.getHaving != null)
      result.append(" HAVING " + plantSelect.getHaving.toString)
    result.toString()
  }

  /**
    * 获取limit-sql语句
    * @param start
    * @param end
    * @return
    */
  def getLimitSql(start : Int, end : Int) : String = {
    s" limit ${start}, ${end}"
  }


}